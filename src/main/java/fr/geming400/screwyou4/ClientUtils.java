package fr.geming400.screwyou4;

import fr.geming400.screwyou4.entrypoints.ScrewYou4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

final class ClientUtils {

    private ClientUtils() {}

    @Environment(EnvType.CLIENT)
    public static void sendMessageToAllPlayers(Component message) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            players.forEach(player -> player.sendSystemMessage(message));
        } else {
            ScrewYou4.LOGGER.warn("Tried sending message to players in a singleplayer world but failed: Couldn't find a single player server");
        }
    }

    @Environment(EnvType.CLIENT)
    public static void sendMessageToSelf(Component message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(message);
        }
    }
}
