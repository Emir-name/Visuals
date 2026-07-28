package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Automatically triggers a jump when the player is moving and would
 * otherwise collide with a solid block in front of them.
 */
public class AutoJump {

    public static void tick() {
        if (!ModConfig.get().autoJumpEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        boolean isMoving = player.input.movementForward != 0 || player.input.movementSideways != 0;
        boolean blocked = player.horizontalCollision;

        if (isMoving && blocked && player.isOnGround() && !player.isSneaking()) {
            player.jump();
        }
    }
}
