package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

public class ScreenTint {

    private static final int STAR_COUNT = 40;
    private static final long BASE_CYCLE_MILLIS = 9000;

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.purpleSkyEnabled) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int bandHeight = height / 3;

        int topColor = 0x66660099;
        int bottomColor = 0x00660099;
        context.fillGradient(0, 0, width, bandHeight, topColor, bottomColor);

        long now = System.currentTimeMillis();
        for (int i = 0; i < STAR_COUNT; i++) {
            long seed = i * 928371L;
            double xFrac = ((seed * 2654435761L) & 0xFFFF) / 65535.0;
            double speedFactor = 0.5 + (((seed * 40503L) & 0xFF) / 255.0);
            long starCycle = (long) (BASE_CYCLE_MILLIS / speedFactor);
            long phase = seed % starCycle;
            long t = (now + phase) % starCycle;
            double progress = t / (double) starCycle;

            int x = (int) (xFrac * width);
            int y = (int) (progress * bandHeight);
            int alpha = (int) (255 * (1.0 - progress * 0.6));
            if (alpha < 0) alpha = 0;

            int color = (alpha << 24) | 0xFFFFFF;
            context.fill(x, y, x + 1, y + 2, color);
        }
    }
}
