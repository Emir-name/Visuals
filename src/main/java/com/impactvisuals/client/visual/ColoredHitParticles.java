package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public class ColoredHitParticles {

    // Kept in sync with the hex palette shown in the color-swatch picker; mapped
    // to the nearest custom-particle block color instead of a vanilla dust hue.
    private static final int[] PALETTE_COLOR = {
            CustomParticleManager.ORANGE, CustomParticleManager.PURPLE, CustomParticleManager.LIGHT_BLUE,
            CustomParticleManager.LIME, CustomParticleManager.RED, CustomParticleManager.CYAN
    };

    public static void spawn(double x, double y, double z) {
        ModConfig cfg = ModConfig.get();
        int index = cfg.hitParticleColorIndex;
        if (index <= 0 || index > PALETTE_COLOR.length) return; // 0 = default color, skip

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int color = PALETTE_COLOR[index - 1];

        for (int i = 0; i < 4; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oy = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.4;
            CustomParticleManager.spawn(x + ox, y + oy, z + oz, 0.0, 0.02, 0.0, color, 10, 0.11f, true);
        }
    }
}
