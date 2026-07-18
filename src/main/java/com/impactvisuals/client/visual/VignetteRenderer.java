package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Simple low-health warning: red strips along the screen edges that get more
 * opaque the lower the player's health drops. Purely a readability aid.
 */
public class VignetteRenderer {

    private static final float LOW_HEALTH_THRESHOLD = 6.0f; // 3 hearts

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.lowHealthVignetteEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float health = client.player.getHealth();
        if (health <= 0f || health >= LOW_HEALTH_THRESHOLD) return;

        float severity = 1.0f - (health / LOW_HEALTH_THRESHOLD);
        int alpha = Math.round(severity * 140f);
        int color = (alpha << 24) | 0xAA0000;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int stripSize = Math.round(30 + severity * 40);

        context.fill(0, 0, width, stripSize, color);
        context.fill(0, height - stripSize, width, height, color);
        context.fill(0, 0, stripSize, height, color);
        context.fill(width - stripSize, 0, width, height, color);
    }
}
