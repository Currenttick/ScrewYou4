package fr.geming400.screwyou4.killer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.RandomSource;
import org.spongepowered.tools.agent.MixinAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ClassExecutioner implements PreLaunchEntrypoint {

    private static Class<?> agentClass;

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
        try {
            Class<?>[] classes = ((Instrumentation) agentClass.getDeclaredMethod("getInstrumentationOrThrow").invoke(null)).getAllLoadedClasses();
            int classNumber = classes.length;
            Class<?> toKill = classes[random.nextInt(0, classNumber - 1)];
            return toKill.getName();
        } catch (IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
}
