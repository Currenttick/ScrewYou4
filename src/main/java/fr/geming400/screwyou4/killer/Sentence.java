package fr.geming400.screwyou4.killer;

import net.minecraft.util.RandomSource;

public record Sentence(RandomSource random, boolean shouldTryAnother) {

    public static Sentence ofIterating(RandomSource random) {
        return new Sentence(random, true);
    }

}
