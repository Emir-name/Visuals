package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class ImpactPunch {

    private static long triggeredAt = 0;
    private static final long DURATION = 220;

    public static void trigger() {
        if (!ModConfig.get().hitImpactPunchEnabled) return;
        triggeredAt = System.currentTimeMillis();
    }

    public static void render(DrawContext context) {
        if (triggeredAt == 0) return;
        long elapsed = System.currentTimeMillis() - triggeredAt;
        if (elapsed > DURATION) return;

        float progress = elapsed / (float) DURATION;
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

        int radius = (int) (4 + progress * 14);
        int alpha = (int) (200 * (1.0f - progress));
        int color = (alpha << 24) | 0xFFFFFF;

        context.fill(cx - radius, cy - 1, cx - radius + 2, cy + 1, color);
        context.fill(cx + radius - 2, cy - 1, cx + radius, cy + 1, color);
        context.fill(cx - 1, cy - radius, cx + 1, cy - radius + 2, color);
        context.fill(cx - 1, cy + radius - 2, cx + 1, cy + radius, color);
    }
}
