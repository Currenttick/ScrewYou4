package fr.geming400.screwyou4.entrypoints;

import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public class ScrewYou4Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onLocalPlayerRespawn(player ->
                ClassExecutioner.kill(Sentence.ofIterating(player.getRandom()))
        );
    }

    @SuppressWarnings({"Convert2Lambda"})
    private static void onLocalPlayerRespawn(ScrewYou4.RespawnCallback onRespawn) {
        AtomicBoolean isDead = new AtomicBoolean();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Player player = client.player;
            if (player != null) {
                if (player.isDeadOrDying()) {
                    isDead.set(true);
                } else if (isDead.get()) {
                    isDead.set(false);
                    onRespawn.onRespawn(player);
                }
            }
        });
    }
}
