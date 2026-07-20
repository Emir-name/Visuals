package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

public class CosmeticTrails {

    private static final int[] PALETTE = {
            0xFFFF8C00, 0xFFB266FF, 0xFF3399FF, 0xFF55DD55, 0xFFFF5555, 0xFF33DDDD
    };

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

                if (cfg.coloredTrailsEnabled && cfg.hitParticleColorIndex > 0 && cfg.hitParticleColorIndex <= PALETTE.length) {
                    int color = PALETTE[cfg.hitParticleColorIndex - 1] & 0xFFFFFF;
                    DustParticleEffect effect = new DustParticleEffect(color, 1.0f);
                    client.world.addParticle(effect, x, y, z, 0.0, 0.01, 0.0);
                } else {
                    client.world.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.01, 0.0);
                }
            }
        }

        boolean walking = player.isOnGround()
                && (Math.abs(player.getVelocity().x) > 0.02 || Math.abs(player.getVelocity().z) > 0.02)
                && !player.isSprinting();

        if (walking && (cfg.footstepDustEnabled || cfg.footstepSoundEnabled)) {
            footstepCounter++;
            if (footstepCounter >= 6) {
                footstepCounter = 0;
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.3;
                double y = player.getY() + 0.05;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.3;

                if (cfg.footstepDustEnabled) {
                    client.world.addParticle(ParticleTypes.POOF, x, y, z, 0.0, 0.0, 0.0);
                }
                if (cfg.footstepSoundEnabled) {
                    client.getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.BLOCK_WOOL_STEP, 1.2f)
                    );
                }
            }
        }
    }
}
