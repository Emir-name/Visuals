package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes the player jump periodically while on the ground, with a small
 * randomized delay between jumps instead of jumping on every possible tick.
 */
public class AutoJump {

    // Ticks are ~50ms. These bounds give a delay roughly between 0.3s and 0.9s.
    private static final int MIN_DELAY_TICKS = 6;
    private static final int MAX_DELAY_TICKS = 18;

    private static int cooldownTicks = 0;

    public static void tick() {
        if (!ModConfig.get().autoJumpEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        if (!player.isOnGround()) {
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        player.jump();
        cooldownTicks = ThreadLocalRandom.current().nextInt(MIN_DELAY_TICKS, MAX_DELAY_TICKS + 1);
    }
}
