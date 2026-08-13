package com.impactvisuals.client.config;

import java.util.Map;

/**
 * Lets individual HUD cards (Target HUD, Marker HUD, etc.) be dragged to a
 * custom position in the HUD Editor screen. Each HUD element has a string id
 * and a default on-screen anchor computed by its own renderer; this class
 * only stores the extra (dx, dy) offset from that default anchor.
 */
public final class HudLayoutManager {

    private HudLayoutManager() {}

    public static int getOffsetX(String id) {
        return ModConfig.get().hudOffsetX.getOrDefault(id, 0);
    }

    public static int getOffsetY(String id) {
        return ModConfig.get().hudOffsetY.getOrDefault(id, 0);
    }

    public static void setOffset(String id, int x, int y) {
        ModConfig cfg = ModConfig.get();
        cfg.hudOffsetX.put(id, x);
        cfg.hudOffsetY.put(id, y);
    }

    public static float getScale(String id) {
        return ModConfig.get().hudScale.getOrDefault(id, 1.0f);
    }

    public static void setScale(String id, float scale) {
        ModConfig.get().hudScale.put(id, scale);
    }

    public static void resetAll() {
        ModConfig cfg = ModConfig.get();
        cfg.hudOffsetX.clear();
        cfg.hudOffsetY.clear();
        cfg.hudScale.clear();
    }

    /** Wraps a HUD's draw calls so its saved position offset AND scale apply, scaled around (anchorX, anchorY). Always pair with popTransform. */
    public static void pushTransform(net.minecraft.client.gui.DrawContext context, String id, int anchorX, int anchorY) {
        float scale = getScale(id);
        context.getMatrices().push();
        if (scale != 1.0f) {
            context.getMatrices().translate(anchorX, anchorY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            context.getMatrices().translate(-anchorX, -anchorY, 0);
        }
    }

    public static void popTransform(net.minecraft.client.gui.DrawContext context) {
        context.getMatrices().pop();
    }

    public static void resetOffset(String id) {
        ModConfig cfg = ModConfig.get();
        cfg.hudOffsetX.remove(id);
        cfg.hudOffsetY.remove(id);
    }

    public static void save() {
        ModConfig.get().save();
    }

    /** Registered HUD elements the editor screen knows how to show/drag, with a human label and default box size for the editor preview. */
    public static final Map<String, int[]> EDITABLE_HUDS = Map.of(
            "target_hud", new int[]{130, 70},
            "marker_hud", new int[]{130, 34},
            "active_effects", new int[]{160, 40},
            "info_hud", new int[]{110, 30},
            "stats_hud", new int[]{140, 32},
            "durability_hud", new int[]{60, 12},
            "extra_hud", new int[]{160, 24},
            "better_near", new int[]{150, 90},
            "cooldown_indicator", new int[]{60, 14},
            "build_helper_hud", new int[]{90, 40}
    );
}
