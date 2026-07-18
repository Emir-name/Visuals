package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

public class HitParticleRenderer {

    public static void spawn(double x, double y, double z, boolean critical) {
        if (!ModConfig.get().hitParticlesEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        var particleType = (critical && ModConfig.get().criticalFlashEnabled)
                ? ParticleTypes.CRIT
                : ParticleTypes.DAMAGE_INDICATOR;

        for (int i = 0; i < 6; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.3;
            double oy = (client.world.random.nextDouble() - 0.5) * 0.3;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.3;
            client.world.addParticle(particleType, x + ox, y + oy, z + oz, 0, 0, 0);
        }
    }
}
