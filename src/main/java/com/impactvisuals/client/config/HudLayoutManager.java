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
            "marker_hud", new int[]{130, 34}
    );
}

