package fr.geming400.screwyou4.killer;

import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record AssignedSentence(Class<?> clazz,
                               boolean all,
                               Optional<Infos.MethodInfo> methodInfo,
                               ClassRepresentation representation,
                               Optional<String> overrideTerminator) {

    public @NonNull Sentence getSentence(RandomSource random) {
        return new Sentence(
                random, false,
                this.methodInfo,
                Optional.of(this.representation),
                Optional.empty(),
                this.overrideTerminator
        );
    }
}
