package fr.geming400.screwyou4;

import fr.geming400.screwyou4.killer.ClassExecutioner;
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

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ScrewYou4 implements ModInitializer {
	public static final String MOD_ID = "screw-you-4";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello from the mod that will Screw You (4) !");
		LOGGER.info(ClassExecutioner.getAgents().toString());

		// FIXME
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			LOGGER.error("Dedicated servers are not supported yet");
			throw new RuntimeException("Dedicated servers are not supported yet");
		}

		onLocalPlayerRespawn(player -> {
			String killed_method = ClassExecutioner.kill(player.getRandom());

			LOGGER.info("Killed method {} (CLIENT)", killed_method);
		});

		onDedicatedServerPlayerRespawn(player -> {
			String killed_method = ClassExecutioner.kill(player.getRandom());

			LOGGER.info("Killed method {} (DEDICATED SERVER)", killed_method);
		});
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		LOGGER.info("\n".repeat(6));
		LOGGER.info("Loadded boyyyyy");
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
						LOGGER.debug("Local player {} has respawned !", player);
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
						LOGGER.debug("Local player {} has respawned !", player);
						deadState.get().put(player.getId(), false);

						onRespawn.onRespawn(player);
					}
				}
			});
		}
	}

//	public static void killMethod(long method) {
//		KILLED_METHODS.add(method);
//		if (KILLED_METHODS.size() == METHODS.size())
//			LOGGER.info("Gg. You have destroyed everything. Wait, how are you even playing the game...");
//	}
//
//	public static boolean isMethodAlive(long method) {
//		return !KILLED_METHODS.contains(method);
//	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@FunctionalInterface
	interface RespawnCallback {
		void onRespawn(@NotNull Player player);
	}
}
