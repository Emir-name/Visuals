package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public class HitParticleRenderer {

    public static void spawn(double x, double y, double z, boolean critical) {
        if (!ModConfig.get().hitParticlesEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int color = (critical && ModConfig.get().criticalFlashEnabled)
                ? CustomParticleManager.YELLOW
                : CustomParticleManager.RED;

        for (int i = 0; i < 6; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.3;
            double oy = (client.world.random.nextDouble() - 0.5) * 0.3;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.3;
            double vx = (client.world.random.nextDouble() - 0.5) * 0.05;
            double vz = (client.world.random.nextDouble() - 0.5) * 0.05;
            CustomParticleManager.spawn(x + ox, y + oy, z + oz, vx, 0.03, vz, color, 12, 0.1f, true);
        }
    }
}
