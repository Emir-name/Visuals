package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.DustParticleEffect;
import org.joml.Vector3f;

public class ColoredHitParticles {

    private static final int[] PALETTE = {
            0xFFFF8C00, 0xFFB266FF, 0xFF3399FF, 0xFF55DD55, 0xFFFF5555, 0xFF33DDDD
    };

    public static void spawn(double x, double y, double z) {
        ModConfig cfg = ModConfig.get();
        int index = cfg.hitParticleColorIndex;
        if (index <= 0 || index > PALETTE.length) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int color = PALETTE[index - 1];
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        DustParticleEffect effect = new DustParticleEffect(new Vector3f(r, g, b), 1.2f);

        for (int i = 0; i < 4; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oy = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.4;
            client.world.addParticle(effect, x + ox, y + oy, z + oz, 0.0, 0.02, 0.0);
        }
    }
}
