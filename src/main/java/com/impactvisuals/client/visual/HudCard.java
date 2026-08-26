package com.impactvisuals.client.visual;

import net.minecraft.client.gui.DrawContext;

/** Shared background panel style for the mod's in-game HUD cards (Target HUD, Marker, etc), with rounded corners. */
public class HudCard {

    public static final int BG = 0xCC101010;
    private static final int RADIUS = 5;

    public static void draw(DrawContext context, int x, int y, int w, int h) {
        int radius = Math.max(0, Math.min(RADIUS, Math.min(w, h) / 2));
        if (radius == 0) {
            context.fill(x, y, x + w, y + h, BG);
            return;
        }

        context.fill(x, y + radius, x + w, y + h - radius, BG);

        for (int row = 0; row < radius; row++) {
            double dy = radius - row - 0.5;
            double dx = Math.sqrt(Math.max(0, radius * radius - dy * dy));
            int inset = radius - (int) Math.round(dx);

            context.fill(x + inset, y + row, x + w - inset, y + row + 1, BG);
            context.fill(x + inset, y + h - 1 - row, x + w - inset, y + h - row, BG);
        }
    }
}
