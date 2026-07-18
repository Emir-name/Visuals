package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Small bar just below the crosshair showing attack cooldown progress
 * (net.minecraft.entity.player.PlayerEntity#getAttackCooldownProgress).
 * Draws additively alongside the vanilla crosshair rather than replacing it.
 */
public class CooldownIndicator {

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.cooldownIndicatorEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float progress = client.player.getAttackCooldownProgress(0.0f);
        if (progress >= 1.0f) return; // fully charged, nothing to show

        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;

        int width = 20;
        int height = 3;
        int x = cx - width / 2;
        int y = cy + 12;

        context.fill(x, y, x + width, y + height, 0x99000000);

        int filledW = Math.round(width * progress);
        int color = interpolateColor(progress);
        context.fill(x, y, x + filledW, y + height, color);
    }

    private static int interpolateColor(float progress) {
        int r = Math.round(255 * (1 - progress));
        int g = Math.round(255 * progress);
        return 0xFF000000 | (r << 16) | (g << 8);
    }
}
