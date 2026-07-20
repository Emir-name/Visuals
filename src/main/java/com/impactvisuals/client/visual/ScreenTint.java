package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;

public class ScreenTint {

    private static int tickCounter = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.purpleSkyEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        tickCounter++;
        if (tickCounter < 3) return;
        tickCounter = 0;

        var random = player.getRandom();
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 40.0;
            double offsetZ = (random.nextDouble() - 0.5) * 40.0;
            double x = player.getX() + offsetX;
            double z = player.getZ() + offsetZ;
            double y = player.getY() + 20 + random.nextDouble() * 15;

            client.world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.02, 0.0);
        }
    }
}
