package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;

public class CosmeticTrails {

    private static int sprintCounter = 0;
    private static int footstepCounter = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        if (cfg.sprintTrailEnabled && player.isSprinting()) {
            sprintCounter++;
            if (sprintCounter >= 3) {
                sprintCounter = 0;
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.3;
                double y = player.getY() + 0.1;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.3;
                client.world.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.01, 0.0);
            }
        }

        if (cfg.footstepDustEnabled && player.isOnGround()
                && (Math.abs(player.getVelocity().x) > 0.02 || Math.abs(player.getVelocity().z) > 0.02)
                && !player.isSprinting()) {
            footstepCounter++;
            if (footstepCounter >= 6) {
                footstepCounter = 0;
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.3;
                double y = player.getY() + 0.05;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.3;
                client.world.addParticle(ParticleTypes.POOF, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
