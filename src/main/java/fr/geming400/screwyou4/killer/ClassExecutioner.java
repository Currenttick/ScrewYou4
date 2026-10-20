package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.agent.Agent;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.lang.instrument.Instrumentation;
import java.util.Optional;

public class ClassExecutioner implements PreLaunchEntrypoint {

    private static Class<?> agentClass;
    private static ClassJudger classJudger;

    public static void kill(Sentence sentence) {
        Class<?>[] classes = getLoadedClasses();
        Class<?> toKill;
        do {
            toKill = classes[sentence.random().nextInt(classes.length)];
        } while (tryRetransform(sentence, toKill).isPresent());
    }

    public static Class<?>[] getLoadedClasses() {
        checkClassDecoderPresent();
        Instrumentation inst = classJudger.getInstrumentation();
        return (Class<?>[]) inst.getAllLoadedClasses();
    }

    /**
     * @return whether it was successful
     */
    public static Optional<Exception> tryRetransform(Sentence sentence, Class<?> toKill) {
        try {
            if (toKill.isInterface() || toKill.isAnnotation()) {
                return Optional.of(new UnsupportedOperationException("Interfaces and annotations not supported yet :(")); // Not supported for now...
            }
            classJudger.addToKill(toKill, sentence);
            classJudger.killClass(toKill);
        } catch (Exception e) {
            classJudger.cancel(toKill);
            return Optional.of(e);
        }
        return Optional.empty();
    }

    @Override
    public void onPreLaunch() {
        try {
            Agent.class.getDeclaredMethod("launch").invoke(null);
            agentClass = ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName());
        } catch (Exception e) {
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
