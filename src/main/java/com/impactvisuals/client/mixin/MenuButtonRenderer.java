package com.impactvisuals.client.visual;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

/**
 * Draws one button entirely by hand - solid dark-charcoal fill (brighter on
 * hover), a thin glowing orange edge, and the label - instead of letting the
 * vanilla grey button texture render. Used by the main menu and death screen
 * so both share the same look instead of duplicating this per mixin.
 */
public final class MenuButtonRenderer {

    private MenuButtonRenderer() {}

    public static void draw(DrawContext context, TextRenderer textRenderer, ButtonWidget button, int mouseX, int mouseY) {
        int x = button.getX();
        int y = button.getY();
        int w = button.getWidth();
        int h = button.getHeight();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean active = button.active;

        int fillColor = !active ? 0xFF141210 : hovered ? 0xFF2A211A : 0xFF1B1613;
        int borderColor = !active ? 0xFF4A4A4A : hovered ? 0xFFFFC966 : 0xFFCC6A1A;
        int textColor = !active ? 0xFF808080 : 0xFFFFFFFF;

        context.fill(x, y, x + w, y + h, fillColor);

        if (active) {
            int glow = hovered ? 0x55FF8C1A : 0x33FF8C1A;
            context.fill(x - 1, y - 1, x + w + 1, y, glow);
            context.fill(x - 1, y + h, x + w + 1, y + h + 1, glow);
            context.fill(x - 1, y, x, y + h, glow);
            context.fill(x + w, y, x + w + 1, y + h, glow);
        }

        context.fill(x, y, x + w, y + 1, borderColor);
        context.fill(x, y + h - 1, x + w, y + h, borderColor);
        context.fill(x, y, x + 1, y + h, borderColor);
        context.fill(x + w - 1, y, x + w, y + h, borderColor);

        String label = textRenderer.trimToWidth(button.getMessage().getString(), Math.max(0, w - 6));
        int textW = textRenderer.getWidth(label);
        context.drawText(textRenderer, label, x + (w - textW) / 2, y + (h - 8) / 2, textColor, true);
    }
}
