package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.minecraft.util.RandomSource;

import java.util.Optional;
import java.util.function.Consumer;

public record Sentence(RandomSource random, boolean shouldTryAnother, Optional<Infos.MethodInfo> methodInfo, Optional<ClassRepresentation> representation, Optional<Consumer<byte[]>> bufferEater, Optional<String> overrideTerminator) {

    public Sentence(RandomSource random, boolean shouldTryAnother) {
        this(random, shouldTryAnother, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static Sentence ofGetter(Consumer<byte[]> getter) {
        return new Sentence(
                null, false,
                Optional.empty(), Optional.empty(),
                Optional.of(getter), Optional.empty()
        );
    }

    public static Sentence ofIterating(RandomSource random) {
        return new Sentence(random, true);
    }

    public Sentence fork() {
        return new Sentence(this.random.fork(), this.shouldTryAnother);
    }

}
