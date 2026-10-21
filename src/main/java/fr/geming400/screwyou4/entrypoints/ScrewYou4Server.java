package fr.geming400.screwyou4.entrypoints;

import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ScrewYou4Server implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        onDedicatedServerPlayerRespawn(player ->
                ClassExecutioner.kill(Sentence.ofIterating(player.getRandom()))
        );
    }

    private static void onDedicatedServerPlayerRespawn(ScrewYou4.RespawnCallback onRespawn) {
        AtomicReference<Map<Integer, Boolean>> deadState = new AtomicReference<>(new HashMap<>());
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : PlayerLookup.all(server)) {
                if (player.isDeadOrDying()) {
                    deadState.get().put(player.getId(), true);
                } else if (deadState.get().getOrDefault(player.getId(), false)) {
                    deadState.get().put(player.getId(), false);
                    onRespawn.onRespawn(player);
                }
            }
        });
    }
}
