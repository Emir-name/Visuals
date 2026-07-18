package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;

/**
 * Low-opacity full-screen purple overlay ("purple sky" PvP aesthetic).
 * This is a screen-space filter rather than a true sky recolor — much simpler
 * and safer than hooking into the world sky rendering pipeline, and gives the
 * same visual effect players are after.
 */
public class ScreenTint {

    private static final int PURPLE = 0x33660099;

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.purpleSkyEnabled) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, PURPLE);
    }
}
