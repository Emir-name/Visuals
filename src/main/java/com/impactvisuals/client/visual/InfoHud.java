package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/**
 * Always-on overlay in the top-right corner: nickname, FPS, ping.
 * No menu required to see this — it's separate from the settings panel.
 */
public class InfoHud {

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.infoHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String name = client.getSession().getUsername();
        int fps = client.getCurrentFps();

        int ping = -1;
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        int screenWidth = context.getScaledWindowWidth();
        int x = screenWidth - 6;
        int y = 6;
        int lineHeight = 10;

        drawRight(context, name, x, y, 0xFFFFFFFF);
        drawRight(context, "FPS: " + fps, x, y + lineHeight, 0xFFAAFFAA);
        drawRight(context, "Ping: " + (ping < 0 ? "--" : ping + "ms"), x, y + lineHeight * 2, pingColor(ping));
    }

    private static int pingColor(int ping) {
        if (ping < 0) return 0xFFAAAAAA;
        if (ping < 80) return 0xFF55FF55;
        if (ping < 150) return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    private static void drawRight(DrawContext context, String text, int rightX, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.textRenderer.getWidth(text);
        context.drawText(client.textRenderer, text, rightX - width, y, color, true);
    }
}
