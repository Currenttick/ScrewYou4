package fr.geming400.screwyou4.agent;

import fr.geming400.screwyou4.ScrewYou4;
import fr.geming400.screwyou4.killer.AgentLauncher;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.LoaderUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

public class Agent {

    private static Instrumentation INSTRUMENTATION = null;
    private volatile static Process runningAgent;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        INSTRUMENTATION = inst;
        ScrewYou4.LOGGER.info("Agent initialized!");
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            throw new RuntimeException(e);
        });
    }

    public static Instrumentation getInstrumentationOrThrow() {
        return Objects.requireNonNull(INSTRUMENTATION);
    }

    public static Instrumentation getInstrumentation() {
        return INSTRUMENTATION;
    }

    public static Instrumentation waitForInstrumentation() {
        while (runningAgent != null) {
            Thread.onSpinWait();
        }
        return INSTRUMENTATION;
    }

    public static void launch() {
        long pid = ProcessHandle.current().pid();
        Class<AgentLauncher> clazz = AgentLauncher.class;
        String rawClassName = clazz.getCanonicalName();
        String className = LoaderUtil.getClassFileName(rawClassName);
        InputStream stream = ClassExecutioner.class.getClassLoader().getResourceAsStream(className);
        String fileName = Arrays.stream(Objects.requireNonNull(FabricLoaderImpl.INSTANCE.getModsDirectory().list()))
                .filter(name -> name.contains("screw") && name.contains("you"))
                .findFirst()
                .orElse("../build/libs/screw-you-4-1.0.1.jar");
        new Thread(() -> {
            try {
                assert stream != null;
                File subFolder = new File(FabricLoaderImpl.INSTANCE.getGameDir().toFile(), "fr");
                FileUtils.deleteDirectory(subFolder);
                File folder = new File(FabricLoaderImpl.INSTANCE.getGameDir().toString() + "/" + className);
                if (!folder.getParentFile().mkdirs()) {
                    throw new RuntimeException("Could not create folder " + folder.getParentFile().getAbsolutePath());
                }
                Files.copy(
                        stream,
                        folder.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
                stream.close();
                runningAgent = new ProcessBuilder("java", "-cp", ".", rawClassName.replace('/', '.'), String.valueOf(pid), fileName)
                        .directory(FabricLoaderImpl.INSTANCE.getGameDir().toFile())
                        .inheritIO()
                        .start();
                runningAgent.waitFor();
                System.out.println("Finished attaching");
                runningAgent = null;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}
