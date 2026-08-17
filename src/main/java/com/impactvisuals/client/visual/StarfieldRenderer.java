package com.impactvisuals.client.visual;

import net.minecraft.client.gui.DrawContext;

/**
 * Draws the "smoldering ember" starfield used as the black background on
 * both the main menu and the world loading screen, so they share the same
 * look. Fixed, seeded star positions so they don't jump around between
 * frames - only their brightness/twinkle animates.
 */
public final class StarfieldRenderer {

    private StarfieldRenderer() {}

    private static final int STAR_COUNT = 140;
    private static final float[][] STARS = generateStars();

    private static float[][] generateStars() {
        java.util.Random random = new java.util.Random(20260816L);
        float[][] stars = new float[STAR_COUNT][4];
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i][0] = random.nextFloat();               // x (0-1 of screen width)
            stars[i][1] = random.nextFloat();               // y (0-1 of screen height)
            stars[i][2] = random.nextFloat() * 6.283f;       // phase offset for the "smoldering" pulse
            stars[i][3] = 0.6f + random.nextFloat() * 1.4f;  // pulse speed
        }
        return stars;
    }

    public static void draw(DrawContext context, int w, int h) {
        long now = System.currentTimeMillis();
        for (float[] star : STARS) {
            float t = (now / 1000f) * star[3] + star[2];
            float pulse = (float) (Math.sin(t) * 0.5 + 0.5); // 0..1 "smoldering" brightness

            int jitterX = (int) (Math.sin(t * 1.7) * 0.6);
            int jitterY = (int) (Math.cos(t * 1.3) * 0.6);

            int size = pulse > 0.75f ? 2 : 1;
            int brightness = (int) (140 + pulse * 115);
            int alpha = (int) (140 + pulse * 115);
            int color = (alpha << 24) | (brightness << 16) | (Math.min(140, brightness / 2) << 8);

            int sx = (int) (star[0] * w) + jitterX;
            int sy = (int) (star[1] * h) + jitterY;
            context.fill(sx, sy, sx + size, sy + size, color);

            if (pulse > 0.88f) {
                int flareAlpha = (int) ((pulse - 0.88f) / 0.12f * 90);
                int flareColor = (flareAlpha << 24) | 0xFFAA33;
                context.fill(sx - 3, sy, sx - 1, sy + 1, flareColor);
                context.fill(sx + size, sy, sx + size + 2, sy + 1, flareColor);
                context.fill(sx, sy - 3, sx + 1, sy - 1, flareColor);
                context.fill(sx, sy + size, sx + 1, sy + size + 2, flareColor);
            }
        }
    }
}
