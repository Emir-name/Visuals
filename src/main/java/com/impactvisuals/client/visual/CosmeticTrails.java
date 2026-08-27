package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class CosmeticTrails {

    // Kept in sync with the hex palette shown in the color-swatch picker; mapped
    // to the nearest custom-particle block color instead of a vanilla dust hue.
    private static final int[] PALETTE_COLOR = {
            CustomParticleManager.ORANGE, CustomParticleManager.PURPLE, CustomParticleManager.LIGHT_BLUE,
            CustomParticleManager.LIME, CustomParticleManager.RED, CustomParticleManager.CYAN
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

                int color = (cfg.coloredTrailsEnabled && cfg.hitParticleColorIndex > 0 && cfg.hitParticleColorIndex <= PALETTE_COLOR.length)
                        ? PALETTE_COLOR[cfg.hitParticleColorIndex - 1]
                        : CustomParticleManager.WHITE;
                CustomParticleManager.spawn(x, y, z, 0.0, 0.01, 0.0, color, 10, 0.1f, false);
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
                    CustomParticleManager.spawn(x, y, z, 0.0, 0.0, 0.0, CustomParticleManager.LIGHT_GRAY, 8, 0.09f, false);
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
