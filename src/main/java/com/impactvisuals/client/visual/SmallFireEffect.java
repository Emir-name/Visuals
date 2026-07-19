package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;

public class SmallFireEffect {

    private static int tickCounter = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.smallFireEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        tickCounter++;
        if (tickCounter < 4) return;
        tickCounter = 0;

        double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.6;
        double y = player.getY() + 0.05;
        double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.6;

        client.world.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.02, 0.0);
    }
}
