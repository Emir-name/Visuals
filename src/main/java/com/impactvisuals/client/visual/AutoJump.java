package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Makes the player jump every time they're on the ground, like holding
 * the jump key down permanently.
 */
public class AutoJump {

    public static void tick() {
        if (!ModConfig.get().autoJumpEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        if (player.isOnGround()) {
            player.jump();
        }
    }
}
