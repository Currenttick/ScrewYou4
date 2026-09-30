package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.ScrewYou4;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;

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
    private ArrayList<Class<?>> toKill = new ArrayList<>();

    public ClassJudger(Instrumentation inst) {
        this.inst = inst;
        inst.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (this.toKill.contains(classBeingRedefined)) {
            ClassImage image = new ClassImage(classfileBuffer);
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

    public void addToKill(Class<?> clazz) {
        this.toKill.add(clazz);
    }

    public void cancel(Class<?> clazz) {
        this.toKill.remove(clazz);
    }

    public Instrumentation getInstrumentation() {
        return this.inst;
    }

    public static void log(String message) {
        if (VERBOSE) {
            ScrewYou4.LOGGER.info(message);
            try {
                getLogFileStream().write((message + "\n").getBytes());
            } catch (IOException ignored) {}
        }
    }
}
