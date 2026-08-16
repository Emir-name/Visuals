package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.HudLayoutManager;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.network.FirebasePresence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Map;

/**
 * A small circular radar in the corner showing where other online Impact
 * Visuals users are relative to you, using the same position data broadcast
 * through Firebase presence that powers the "IV" badge. Rotates with your
 * own facing direction, like a compass-style minimap.
 */
public class RadarHud {

    private static final int RADIUS = 42;
    private static final double RANGE_BLOCKS = 150.0;

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.radarEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenW = context.getScaledWindowWidth();
        int cx = screenW - RADIUS - 12 + HudLayoutManager.getOffsetX("radar_hud");
        int cy = RADIUS + 60 + HudLayoutManager.getOffsetY("radar_hud");

        HudLayoutManager.pushTransform(context, "radar_hud", cx, cy);

        // Background disc approximated by a filled square plus a dotted border
        // ring (a true circular clip would need per-pixel distance checks,
        // too slow to be worth it at this size) - reads fine visually.
        context.fill(cx - RADIUS, cy - RADIUS, cx + RADIUS, cy + RADIUS, 0x99000000);
        drawRing(context, cx, cy, RADIUS, 0xFF6E6480);

        drawSelfMarker(context, cx, cy);

        double selfX = client.player.getX();
        double selfZ = client.player.getZ();
        float selfYaw = client.player.getYaw();

        Map<String, double[]> others = FirebasePresence.getOnlinePositions();
        String selfName = client.player.getGameProfile().getName().toLowerCase();

        for (Map.Entry<String, double[]> entry : others.entrySet()) {
            if (entry.getKey().equals(selfName)) continue;

            double dx = entry.getValue()[0] - selfX;
            double dz = entry.getValue()[1] - selfZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > RANGE_BLOCKS) continue;

            // Rotate the offset by -yaw so "up" on the radar is always the direction you're facing.
            double angle = Math.toRadians(-selfYaw);
            double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
            double rz = dx * Math.sin(angle) + dz * Math.cos(angle);

            double scale = (RADIUS - 6) / RANGE_BLOCKS;
            int dotX = cx + (int) Math.round(rx * scale);
            int dotY = cy + (int) Math.round(rz * scale);

            context.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, 0xFFB266FF);
        }

        HudLayoutManager.popTransform(context);
    }

    private static void drawSelfMarker(DrawContext context, int cx, int cy) {
        context.fill(cx - 1, cy - 3, cx + 1, cy + 3, 0xFFFFFFFF);
        context.fill(cx - 3, cy - 1, cx + 3, cy + 1, 0xFFFFFFFF);
    }

    private static void drawRing(DrawContext context, int cx, int cy, int radius, int color) {
        int segments = 40;
        for (int i = 0; i < segments; i++) {
            double angle = (2 * Math.PI * i) / segments;
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int y = cy + (int) Math.round(Math.sin(angle) * radius);
            context.fill(x - 1, y - 1, x + 1, y + 1, color);
        }
    }
}

