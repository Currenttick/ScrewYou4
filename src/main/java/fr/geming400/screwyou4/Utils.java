package fr.geming400.screwyou4;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtil;

public final class Utils {

    private Utils() {}

    public static void tellRaw(String message) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return;
        }
        try {
            // Heavy use of reflexion to prevent java from loading net.minecraft.client.* classes, that are inaccessible in EnvType.SERVER
            Object minecraft = Minecraft.class.getDeclaredMethod("getInstance").invoke(null);
            if (minecraft == null) {
                return;
            }
            if (((Boolean) minecraft.getClass().getDeclaredMethod("isLocalServer").invoke(minecraft)) == false) {
                return;
            };
            Object player = minecraft.getClass().getDeclaredField("player").get(minecraft);
            Object connection = player.getClass().getDeclaredField("connection").get(player);
            if (connection == null) {
                return;
            }
            StringBuilder messageBuilder = new StringBuilder();
            for (char c : message.toCharArray()) {
                if (StringUtil.isAllowedChatCharacter(c)) {
                    messageBuilder.append(c);
                }
            }
            connection.getClass().getDeclaredMethod("sendCommand", String.class).invoke(connection, "tellraw @a " + messageBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
