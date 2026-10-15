package fr.geming400.screwyou4.killer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.RandomSource;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

public class ClassExecutioner implements PreLaunchEntrypoint {

    private static Class<?> agentClass;
    private static ClassJudger classJudger;

    public static String kill(RandomSource random) {
        checkClassDecoderPresent();
        Instrumentation inst = classJudger.getInstrumentation();
        Class<?>[] classes = inst.getAllLoadedClasses();
        Class<?> toKill;
        do {
            toKill = classes[random.nextInt(classes.length)];
        } while (!tryRetransform(random, toKill, inst));
        return toKill.getName();
    }

    /**
     * @return whether it was successful
     */
    private static boolean tryRetransform(RandomSource random, Class<?> toKill, Instrumentation inst) {
        try {
            if (toKill.isInterface() || toKill.isAnnotation()) {
                return false; // Not supported for now...
            }
            classJudger.addToKill(toKill, random);
            classJudger.killClasses();
        } catch (Exception e) {
            classJudger.cancel(toKill);
            return false;
        }
        return true;
    }

    @Override
    public void onPreLaunch() {
        try {
            agentClass = ClassLoader.getSystemClassLoader().loadClass("fr.geming400.screwyou4.agent.Agent");
            agentClass.getDeclaredMethod("launch").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkClassDecoderPresent() {
        if (classJudger == null) {
            try {
                classJudger = new ClassJudger(((Instrumentation) agentClass.getDeclaredMethod("getInstrumentationOrThrow").invoke(null)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
