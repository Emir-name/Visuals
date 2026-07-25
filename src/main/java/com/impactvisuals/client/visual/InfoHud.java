package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Always-on badge in the top-right corner: checkmark, mod name, FPS, frame time.
 * e.g.  "✓  Impact Visuals  ·  137 FPS  ·  40 ms"
 * No menu required to see this — it's separate from the settings panel.
 */
public class InfoHud {

    private static final String MOD_NAME = "Impact Visuals";
    private static final String CHECK = "\u2713";
    private static final String SEPARATOR = "  \u00B7  ";

    private static final int CHECK_COLOR = 0xFF4ADE80;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int SEP_COLOR = 0xFF707070;
    private static final int MS_COLOR = 0xFFAAAAAA;

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.infoHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        var textRenderer = client.textRenderer;

        int fps = client.getCurrentFps();
        float frameMs = fps > 0 ? 1000f / fps : 0f;

        String fpsText = fps + " FPS";
        String msText = String.format("%.0f ms", frameMs);

        int checkW = textRenderer.getWidth(CHECK);
        int nameW = textRenderer.getWidth(MOD_NAME);
        int sepW = textRenderer.getWidth(SEPARATOR);
        int fpsW = textRenderer.getWidth(fpsText);
        int msW = textRenderer.getWidth(msText);

        int iconGap = 5;
        int contentW = checkW + iconGap + nameW + sepW + fpsW + sepW + msW;

        int padX = 8;
        int padY = 5;
        int boxW = contentW + padX * 2;
        int boxH = textRenderer.fontHeight + padY * 2;

        int screenWidth = context.getScaledWindowWidth();
        int x = screenWidth - boxW - 6;
        int y = 6;

        HudCard.draw(context, x, y, boxW, boxH);

        int textY = y + padY;
        int cursorX = x + padX;

        context.drawText(textRenderer, CHECK, cursorX, textY, CHECK_COLOR, true);
        cursorX += checkW + iconGap;

        context.drawText(textRenderer, MOD_NAME, cursorX, textY, NAME_COLOR, true);
        cursorX += nameW;

        context.drawText(textRenderer, SEPARATOR, cursorX, textY, SEP_COLOR, false);
        cursorX += sepW;

        context.drawText(textRenderer, fpsText, cursorX, textY, fpsColor(fps), true);
        cursorX += fpsW;

        context.drawText(textRenderer, SEPARATOR, cursorX, textY, SEP_COLOR, false);
        cursorX += sepW;

        context.drawText(textRenderer, msText, cursorX, textY, MS_COLOR, false);
    }

    private static int fpsColor(int fps) {
        if (fps >= 100) return 0xFF55FF55;
        if (fps >= 60) return 0xFFFFFF55;
        return 0xFFFF5555;
    }
}
