package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class PlaytimeTracker {

    private static long lastTickMillis = -1;
    private static int ticksSinceSave = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        long now = System.currentTimeMillis();
        if (lastTickMillis > 0) {
            cfg.totalPlaytimeMillis += (now - lastTickMillis);
        }
        lastTickMillis = now;

        ticksSinceSave++;
        if (ticksSinceSave >= 200) { // roughly every 10 seconds
            ticksSinceSave = 0;
            cfg.save();
        }
    }

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.totalPlaytimeEnabled) return;

        long totalSeconds = cfg.totalPlaytimeMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        MinecraftClient client = MinecraftClient.getInstance();
        String text = String.format("Total Playtime: %dh %dm", hours, minutes);
        context.drawText(client.textRenderer, text, 6, 30, 0xFFFFFFFF, true);
    }
}
