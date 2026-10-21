package fr.geming400.screwyou4.entrypoints;

import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScrewYou4Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onLocalPlayerRespawn(player ->
                ClassExecutioner.kill(Sentence.ofIterating(player.getRandom()))
        );

        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) ->
            ScrewYou4.level = null
        );

        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
            if (client.hasSingleplayerServer()) {
                ClientLevel level = Objects.requireNonNull(client.level);
                ResourceKey<Level> dimension = level.dimension();

                if (ScrewYou4.level == null && dimension == Level.OVERWORLD)
                    ScrewYou4.level = level;
            }
        });
    }

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
