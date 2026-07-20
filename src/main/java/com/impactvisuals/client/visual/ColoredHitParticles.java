package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;

public class ColoredHitParticles {

    private static final int[] PALETTE = {
            0xFFFF8C00, // Orange
            0xFFB266FF, // Purple
            0xFF3399FF, // Blue
            0xFF55DD55, // Green
            0xFFFF5555, // Red
            0xFF33DDDD  // Cyan
    };

    public static void spawn(double x, double y, double z) {
        ModConfig cfg = ModConfig.get();
        int index = cfg.hitParticleColorIndex;
        if (index <= 0 || index > PALETTE.length) return; // 0 = vanilla colors, skip

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int color = PALETTE[index - 1] & 0xFFFFFF;
        DustParticleEffect effect = new DustParticleEffect(color, 1.2f);

        for (int i = 0; i < 4; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oy = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.4;
            client.world.addParticle(effect, x + ox, y + oy, z + oz, 0.0, 0.02, 0.0);
        }
    }
}
