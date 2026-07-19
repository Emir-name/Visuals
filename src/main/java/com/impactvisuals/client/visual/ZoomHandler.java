package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.config.ModKeybinds;
import net.minecraft.client.MinecraftClient;

/**
 * While the zoom key is held, temporarily reduces the FOV option to simulate
 * a scope/zoom effect, then restores the original value on release.
 */
public class ZoomHandler {

    private static int previousFov = -1;

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean enabled = ModConfig.get().zoomEnabled;
        boolean held = enabled && ModKeybinds.zoomKey.isPressed();

        var fovOption = client.options.getFov();

        if (held) {
            if (previousFov == -1) {
                previousFov = fovOption.getValue();
            }
            int zoomed = Math.max(10, previousFov / 4);
            if (fovOption.getValue() != zoomed) {
                fovOption.setValue(zoomed);
            }
        } else if (previousFov != -1) {
            fovOption.setValue(previousFov);
            previousFov = -1;
        }
    }
}
