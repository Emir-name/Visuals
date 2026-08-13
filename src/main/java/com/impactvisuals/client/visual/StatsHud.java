package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/**
 * Small collection of independent info readouts:
 * - Coordinates (top-left)
 * - Facing direction / compass (top-left)
 * - Session timer, how long this game session has been running (top-left)
 * - Held item durability percentage (above hotbar)
 * Each is toggled independently in the config.
 */
public class StatsHud {

    private static final long SESSION_START_MILLIS = System.currentTimeMillis();

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        ModConfig cfg = ModConfig.get();

        int x = 6 + com.impactvisuals.client.config.HudLayoutManager.getOffsetX("stats_hud");
        int y = 6 + com.impactvisuals.client.config.HudLayoutManager.getOffsetY("stats_hud");
        int lineHeight = 10;

        boolean anyStatsShown = cfg.coordinatesHudEnabled || cfg.compassHudEnabled || cfg.sessionTimerEnabled;
        if (anyStatsShown) {
            com.impactvisuals.client.config.HudLayoutManager.pushTransform(context, "stats_hud", x, y);
        }

        if (cfg.coordinatesHudEnabled) {
            var pos = client.player.getPos();
            String text = String.format("XYZ: %.1f / %.1f / %.1f", pos.x, pos.y, pos.z);
            context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, true);
            y += lineHeight;
        }

        if (cfg.compassHudEnabled) {
            float yaw = client.player.getYaw() % 360f;
            if (yaw < 0) yaw += 360f;
            String direction = cardinalDirection(yaw);
            String text = String.format("Facing: %s (%.0f°)", direction, yaw);
            context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, true);
            y += lineHeight;
        }

        if (cfg.sessionTimerEnabled) {
            long elapsedSeconds = (System.currentTimeMillis() - SESSION_START_MILLIS) / 1000L;
            long minutes = elapsedSeconds / 60;
            long seconds = elapsedSeconds % 60;
            String text = String.format("Session: %02d:%02d", minutes, seconds);
            context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, true);
        }

        if (anyStatsShown) {
            com.impactvisuals.client.config.HudLayoutManager.popTransform(context);
        }

        if (cfg.durabilityHudEnabled) {
            renderDurability(context, client);
        }
    }

    private static void renderDurability(DrawContext context, MinecraftClient client) {
        ItemStack stack = client.player.getMainHandStack();
        if (stack.isEmpty() || !stack.isDamageable()) return;

        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int remaining = max - damage;
        float pct = max > 0 ? (remaining / (float) max) * 100f : 100f;

        String text = String.format("%.0f%%", pct);
        int color = pct > 50f ? 0xFF55FF55 : (pct > 20f ? 0xFFFFFF55 : 0xFFFF5555);

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int textWidth = client.textRenderer.getWidth(text);

        int dx = (screenWidth - textWidth) / 2 + com.impactvisuals.client.config.HudLayoutManager.getOffsetX("durability_hud");
        int dy = screenHeight - 58 + com.impactvisuals.client.config.HudLayoutManager.getOffsetY("durability_hud");

        com.impactvisuals.client.config.HudLayoutManager.pushTransform(context, "durability_hud", dx, dy);
        context.drawText(client.textRenderer, text, dx, dy, color, true);
        com.impactvisuals.client.config.HudLayoutManager.popTransform(context);
    }

    private static String cardinalDirection(float yaw) {
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S"};
        int index = Math.round(yaw / 45f);
        return directions[index];
    }
}
