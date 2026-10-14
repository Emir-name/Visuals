package com.impactvisuals.client.visual;

import net.minecraft.client.gui.DrawContext;

/**
 * Shared background panel style for the mod's in-game HUD cards (Target HUD,
 * Marker, Better Near, ...).
 *
 * Look: a dark rounded panel with a soft 1px accent border and a faint top
 * highlight, so cards read as "glass" instead of flat black rectangles.
 * Everything is pure {@code fill} calls (no textures, no shaders) so it stays
 * cheap enough to draw every frame.
 */
public class HudCard {

    public static final int BG = 0xCC101010;
    /** Soft violet outline around the panel. */
    public static final int BORDER = 0x59B266FF;
    /** One-pixel inner highlight along the top edge. */
    public static final int TOP_LIGHT = 0x1FFFFFFF;
    private static final int RADIUS = 6;

    public static void draw(DrawContext context, int x, int y, int w, int h) {
        draw(context, x, y, w, h, true);
    }

    public static void draw(DrawContext context, int x, int y, int w, int h, boolean border) {
        int radius = Math.max(0, Math.min(RADIUS, Math.min(w, h) / 2));

        // Body: middle band + stepped corners.
        context.fill(x, y + radius, x + w, y + h - radius, BG);
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            context.fill(x + inset, y + row, x + w - inset, y + row + 1, BG);
            context.fill(x + inset, y + h - 1 - row, x + w - inset, y + h - row, BG);
        }

        if (!border) return;

        // Faint top highlight (1px, inset by the corner steps).
        context.fill(x + radius, y, x + w - radius, y + 1, TOP_LIGHT);
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            if (inset < radius) {
                context.fill(x + inset, y + row, x + inset + 1, y + row + 1, TOP_LIGHT);
                context.fill(x + w - inset - 1, y + row, x + w - inset, y + row + 1, TOP_LIGHT);
            }
        }

        // 1px accent border, same stepped corners as the fill.
        context.fill(x, y + radius, x + 1, y + h - radius, BORDER);
        context.fill(x + w - 1, y + radius, x + w, y + h - radius, BORDER);
        context.fill(x + radius, y + h - 1, x + w - radius, y + h, BORDER);
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            if (inset < radius) {
                context.fill(x + inset, y + row, x + inset + 1, y + row + 1, BORDER);
                context.fill(x + w - inset - 1, y + row, x + w - inset, y + row + 1, BORDER);
                context.fill(x + inset, y + h - 1 - row, x + inset + 1, y + h - row, BORDER);
                context.fill(x + w - inset - 1, y + h - 1 - row, x + w - inset, y + h - row, BORDER);
            }
        }
    }

    /** Horizontal inset (in px) for the given row of a quarter-circle corner. */
    private static int cornerInset(int radius, int row) {
        double dy = radius - row - 0.5;
        double dx = Math.sqrt(Math.max(0, radius * radius - dy * dy));
        return radius - (int) Math.round(dx);
    }
}
