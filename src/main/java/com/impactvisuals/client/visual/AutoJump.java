package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Automatically jumps when the player is walking toward a solid, one-block
 * obstacle that they could step over, mirroring vanilla's own auto-jump
 * detection (look-ahead box check) instead of reacting to the collision flag
 * after the fact, which misses too many cases.
 */
public class AutoJump {

    public static void tick() {
        if (!ModConfig.get().autoJumpEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;
        if (!player.isOnGround() || player.isSneaking() || player.isSwimming()) return;

        float forward = player.input.movementForward;
        float strafe = player.input.movementSideways;
        if (forward == 0 && strafe == 0) return;

        Vec3d moveDir = relativeMoveDirection(forward, strafe, player.getYaw());
        if (moveDir.lengthSquared() < 1.0E-7) return;
        moveDir = moveDir.normalize();

        Box currentBox = player.getBoundingBox();
        // Small step ahead in the direction the player is actually moving.
        Box ahead = currentBox.offset(moveDir.x * 0.3, 0.0, moveDir.z * 0.3);

        boolean blockedAtFeet = !client.world.isSpaceEmpty(player, ahead);
        boolean clearIfJumped = client.world.isSpaceEmpty(player, ahead.offset(0.0, 1.0, 0.0));

        if (blockedAtFeet && clearIfJumped) {
            player.jump();
        }
    }

    /** Converts raw WASD input + yaw into a world-space direction vector, same transform vanilla uses. */
    private static Vec3d relativeMoveDirection(float forward, float strafe, float yaw) {
        double f = strafe;
        double f1 = forward;
        double len = MathHelper.sqrt((float) (f * f + f1 * f1));
        if (len >= 1.0E-4) {
            len = Math.max(len, 1.0);
            f /= len;
            f1 /= len;
        }
        float sinYaw = MathHelper.sin(yaw * ((float) Math.PI / 180F));
        float cosYaw = MathHelper.cos(yaw * ((float) Math.PI / 180F));
        double dx = f * cosYaw - f1 * sinYaw;
        double dz = f1 * cosYaw + f * sinYaw;
        return new Vec3d(dx, 0.0, dz);
    }
}
