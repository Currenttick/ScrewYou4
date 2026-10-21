package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.entrypoints.ScrewYou4;
import fr.geming400.screwyou4.Utils;
import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassJudger implements ClassFileTransformer {
    private static final boolean POST_MORTEM_PROPERTY = Boolean.parseBoolean(System.getProperty("SCREWYOU4_POST_MORTEM", "false"));
    private static final boolean VERBOSE_PROPERTY = Boolean.parseBoolean(System.getProperty("SCREWYOU4_VERBOSE", "false"));

    public static final boolean VERBOSE = FabricLoader.getInstance().isDevelopmentEnvironment() || VERBOSE_PROPERTY;
    public static final boolean POST_MORTEM = FabricLoader.getInstance().isDevelopmentEnvironment() || POST_MORTEM_PROPERTY;
    public static final boolean NOTIFY_PLAYERS = Boolean.parseBoolean(System.getProperty("SCREWYOU4_NOTIFY_PLAYERS", "true"));

    public static FileOutputStream logFileStream = null;

    public static FileOutputStream getLogFileStream() {
        if (logFileStream == null) {
            File folder = FabricLoaderImpl.INSTANCE.getGameDir().toFile();
            File logFile = new File(folder, "screw-you-4-log.txt");
            if (!logFile.exists()) {
                try {
                    boolean ignored = logFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                logFileStream = new FileOutputStream(logFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return logFileStream;
    }

    private final Instrumentation inst;
    private final HashMap<Class<?>, Sentence> toKill = new HashMap<>();

    public ClassJudger(Instrumentation inst) {
        this.inst = inst;
        inst.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (this.toKill.containsKey(classBeingRedefined)) {
            final Sentence sentence = this.toKill.remove(classBeingRedefined);
            if (sentence.bufferEater().isPresent()) {
                sentence.bufferEater().get().accept(classfileBuffer);
                return null;
            }

            try {
                ClassRepresentation image2 = sentence.representation().isPresent()
                        ? sentence.representation().get()
                        : new ClassRepresentation(classfileBuffer);

                final Infos.MethodInfo chosenMethod;
                if (sentence.methodInfo().isPresent()) {
                    chosenMethod = sentence.methodInfo().get();
                } else {
                    ArrayList<Infos.MethodInfo> infos = image2.getMethodInfos();
                    chosenMethod = infos.get(sentence.random().nextInt(infos.size()));
                }

                MethodTerminator terminator = new MethodTerminator(chosenMethod, sentence.random(), sentence.overrideTerminator().orElse(null));
                terminator.terminate();
                byte[] buffer = image2.toByteArray();
                if (POST_MORTEM) {
                    Files.write(
                            new File(FabricLoaderImpl.INSTANCE.getGameDir().toFile(), "original.class").toPath(),
                            classfileBuffer,
                            StandardOpenOption.CREATE
                    );

                    Files.write(
                            new File(FabricLoaderImpl.INSTANCE.getGameDir().toFile(), "produced.class").toPath(),
                            buffer,
                            StandardOpenOption.CREATE
                    );
                }

                notifyKilled(classBeingRedefined, chosenMethod);
                return buffer;
            } catch (Exception e) {
                // for some strange reason, you cannot re-transform classes from this thread
                if (sentence.shouldTryAnother()) {
                    Thread.ofVirtual().start(() -> ClassExecutioner.kill(sentence.fork()));
                    if (
                            !(
                                    (e instanceof UnsupportedOperationException && e.getMessage().contains("Method terminator"))
                                            || (e instanceof IllegalArgumentException && e.getMessage().contains("Code attribute"))
                            )
                    ) {
                        ScrewYou4.LOGGER.error("Failed to transform class : {}", e.getMessage());
                        Utils.sendMessageToAllPlayers(Component.literal("\"Failed to transform class : " + e + "\""));
                        ScrewYou4.LOGGER.warn("Failed to transform class" + className, e);
                    }
                } else {
                    Utils.sendMessageToAllPlayers(Component.literal("\"Failed to transform class : " + e + "\""));
                    ScrewYou4.LOGGER.warn("Failed to transform class" + className, e);
                }
            }
        }
        return null;
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return this.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    public void addToKill(Class<?> clazz, Sentence sentence) {
        this.toKill.put(clazz, sentence);
    }

    public void cancel(Class<?> clazz) {
        this.toKill.remove(clazz);
    }

    public void killClass(Class<?> clazz) throws UnmodifiableClassException {
        try {
            this.inst.retransformClasses(clazz);
        } catch (Exception e) {
            if (e instanceof UnmodifiableClassException ee) {
                throw ee;
            }
            ScrewYou4.LOGGER.error("", e);
        }
    }

    public Instrumentation getInstrumentation() {
        return this.inst;
    }

    public static void notifyKilled(Class<?> clazz, Infos.MethodInfo method) {
        String envName = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                ? "CLIENT"
                : "SERVER";

        ScrewYou4.LOGGER.info("Killed method {} in {} ({})", method.getName(), clazz, envName);
        if (NOTIFY_PLAYERS) {
            String name = clazz.getSimpleName();
            if (name.isEmpty()) {
                name = clazz.getName();
            }

            String displayedMethodName = "%s.%s(...)".formatted(name, method.getShortName());

            Utils.sendMessageToAllPlayers(
                    Component.literal("Killed ")
                            .append(Component.literal(displayedMethodName).withColor(TextColor.RED))
            );
        }
    }

    public static void log(String message) {
        if (VERBOSE) {
            ScrewYou4.LOGGER.warn(message);
            try {
                getLogFileStream().write((message + "\n").getBytes());
            } catch (IOException ignored) {}
        }
    }
}
