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
import java.util.jar.JarFile;

public class Agent {
    private static final String MOD_VERSION = "1.0.3";
    /// Only used in a dev environment
    private static final String MOD_PATH = "../build/libs/%s-%s.jar".formatted(ScrewYou4.MOD_ID, MOD_VERSION);

    private static Instrumentation INSTRUMENTATION = null;
    private volatile static Process runningAgent;

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        INSTRUMENTATION = inst;
        ScrewYou4.LOGGER.info("Adding {} to classpath.", agentArgs);
        inst.appendToSystemClassLoaderSearch(new JarFile(new File(agentArgs)));
        ScrewYou4.LOGGER.info("loader : {}", Agent.class.getClassLoader().getName());
        ScrewYou4.LOGGER.info("Agent initialized!");
    }

    @SuppressWarnings("unused")
    public static Instrumentation getInstrumentationOrThrow() {
        return Objects.requireNonNull(INSTRUMENTATION);
    }

    @SuppressWarnings("unused")
    public static Instrumentation getInstrumentation() {
        return INSTRUMENTATION;
    }

    @SuppressWarnings("unused")
    public static Instrumentation waitForInstrumentation() {
        while (runningAgent != null) {
            Thread.onSpinWait();
        }
        return INSTRUMENTATION;
    }

    @SuppressWarnings("unused")
    public static void launch() {
        long pid = ProcessHandle.current().pid();
        Class<AgentLauncher> clazz = AgentLauncher.class;
        String rawClassName = clazz.getCanonicalName();
        String className = LoaderUtil.getClassFileName(rawClassName);
        InputStream stream = ClassExecutioner.class.getClassLoader().getResourceAsStream(className);
        String fileName = Arrays.stream(Objects.requireNonNull(FabricLoaderImpl.INSTANCE.getModsDirectory().list()))
                .filter(name -> name.contains("screw") && name.contains("you"))
                .findFirst()
                .map(name -> "mods/" + name)
                .orElse(MOD_PATH);
        ScrewYou4.LOGGER.info("found mod's jar : {}", fileName);
        Thread t = new Thread(() -> {
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
                ScrewYou4.LOGGER.info("Finished attaching");
                runningAgent = null;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t.start();
        //noinspection LoopConditionNotUpdatedInsideLoop
        while (t.isAlive()) {
            Thread.onSpinWait();
        }
    }
}
