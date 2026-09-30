package fr.geming400.screwyou4.killer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.RandomSource;
import org.spongepowered.tools.agent.MixinAgent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ClassExecutioner implements PreLaunchEntrypoint {

    private static Class<?> agentClass;
    private static ClassJudger classJudger;

    public static Object getAgents() {
        try {
            Field f = MixinAgent.class.getDeclaredField("classLoader");
            f.setAccessible(true);
            return f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String kill(RandomSource random) {
        checkClassDecoderPresent();
        Instrumentation inst = classJudger.getInstrumentation();
        Class<?> toKill;
        do {
            Class<?>[] classes = inst.getAllLoadedClasses();
            int classNumber = classes.length;
            toKill = classes[random.nextInt(0, classNumber - 1)];
        } while (!tryRetransform(random, toKill, inst));
        return toKill.getName();
    }

    /**
     * @return whether it was successful
     */
    private static boolean tryRetransform(RandomSource random, Class<?> toKill, Instrumentation inst) {
        try {
            classJudger.addToKill(toKill);
            inst.retransformClasses(toKill);
        } catch (UnmodifiableClassException e) {
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
