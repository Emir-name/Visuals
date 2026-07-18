package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

/**
 * Classic FPS-style hitmarker: a small white cross flashes over the crosshair
 * for a fraction of a second whenever the player lands a hit.
 */
public class HitmarkerRenderer {

    private static float age = 999f;
    private static final float MAX_AGE = 0.25f;

    public static void spawn() {
        if (!ModConfig.get().hitmarkerEnabled) return;
        age = 0f;
    }

    public static void tick() {
        if (age < MAX_AGE) {
            age += 0.05f;
        }
    }

    public static void render(DrawContext context) {
        if (!ModConfig.get().hitmarkerEnabled) return;
        if (age >= MAX_AGE) return;

        float alpha = 1.0f - (age / MAX_AGE);
        int a = Math.round(alpha * 255f) << 24;
        int color = 0x00FFFFFF | a;

        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        int size = 6;
        int thickness = 2;

        // Top-left to center, top-right to center, etc. (an "X" hitmarker)
        context.fill(cx - size, cy - size, cx - size + thickness, cy - size + thickness + 3, color);
        context.fill(cx + size - thickness, cy - size, cx + size, cy - size + thickness + 3, color);
        context.fill(cx - size, cy + size - thickness - 3, cx - size + thickness, cy + size, color);
        context.fill(cx + size - thickness, cy + size - thickness - 3, cx + size, cy + size, color);
    }
}
