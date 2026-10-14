package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.ScrewYou4;
import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;

public class ClassJudger implements ClassFileTransformer {

    public static final boolean VERBOSE = true;
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
    private final HashMap<Class<?>, RandomSource> toKill = new HashMap<>();

    public ClassJudger(Instrumentation inst) {
        this.inst = inst;
        inst.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (this.toKill.containsKey(classBeingRedefined)) {
            try {
//                ClassImage image = new ClassImage(classfileBuffer);
                ClassRepresentation image2 = new ClassRepresentation(classfileBuffer);
                ArrayList<Infos.MethodInfo> infos = image2.getMethodInfos();
                Infos.MethodInfo chosenMethod = infos.get(this.toKill.get(classBeingRedefined).nextInt(infos.size()));
                ScrewYou4.LOGGER.info("Killed method {} (CLIENT)", chosenMethod.getName());
            } catch (Exception e) {
                ScrewYou4.LOGGER.error("Failed to transform class : ", e);
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Failed to transform class : " + e));
            }
            this.toKill.remove(classBeingRedefined);
            return null;
        } else {
            return null;
        }
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return this.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    public void addToKill(Class<?> clazz, RandomSource random) {
        this.toKill.put(clazz, random);
    }

    public void cancel(Class<?> clazz) {
        this.toKill.remove(clazz);
    }

    public Instrumentation getInstrumentation() {
        return this.inst;
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
