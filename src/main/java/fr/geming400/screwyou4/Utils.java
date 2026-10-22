package fr.geming400.screwyou4;

import fr.geming400.screwyou4.entrypoints.ScrewYou4;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class Utils {

    private Utils() {}

    public static void sendMessageToAllPlayers(Component message) {
        if (ScrewYou4.level instanceof ServerLevel serverLevel) {
            List<ServerPlayer> players = serverLevel.getServer()
                    .getPlayerList()
                    .getPlayers();

            players.forEach(player -> player.sendSystemMessage(message));
        } else if (ScrewYou4.level instanceof ClientLevel) {
            ClientUtils.sendMessageToAllPlayers(message);
        } else {
            ClientUtils.sendMessageToSelf(message);
        }
    }
}
