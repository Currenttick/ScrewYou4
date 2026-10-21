package fr.geming400.screwyou4.entrypoints;

import fr.geming400.screwyou4.commands.ScrewOverCommand;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrewYou4 implements ModInitializer {
	public static final String MOD_ID = "screw-you-4";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Level level = null;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello from the mod that will Screw You (4) !");

		ScrewOverCommand.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@FunctionalInterface
	interface RespawnCallback {
		void onRespawn(@NotNull Player player);
	}
}
