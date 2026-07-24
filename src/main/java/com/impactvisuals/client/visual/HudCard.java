package com.impactvisuals.client.visual;

import net.minecraft.client.gui.DrawContext;

public class HudCard {

    public static final int BG = 0xCC101010;

    public static void draw(DrawContext context, int x, int y, int w, int h) {
        context.fill(x + 3, y, x + w - 3, y + h, BG);
        context.fill(x, y + 3, x + w, y + h - 3, BG);
    }
}
