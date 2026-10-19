package fr.geming400.screwyou4;

import fr.geming400.screwyou4.commands.ScrewOverCommand;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ScrewYou4 implements ModInitializer {
	public static final String MOD_ID = "screw-you-4";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello from the mod that will Screw You (4) !");

		// It should work on dedicated servers now
//		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
//			LOGGER.error("Dedicated servers are not supported yet");
//			throw new RuntimeException("Dedicated servers are not supported yet");
//		}

		onLocalPlayerRespawn(player -> {
            ClassExecutioner.kill(Sentence.ofIterating(player.getRandom()));
        });

		onDedicatedServerPlayerRespawn(player -> {
            ClassExecutioner.kill(Sentence.ofIterating(player.getRandom()));
        });

		ScrewOverCommand.initialize();
	}

	private static void onLocalPlayerRespawn(RespawnCallback onRespawn) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
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

	private static void onDedicatedServerPlayerRespawn(RespawnCallback onRespawn) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
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

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@FunctionalInterface
	interface RespawnCallback {
		void onRespawn(@NotNull Player player);
	}
}
