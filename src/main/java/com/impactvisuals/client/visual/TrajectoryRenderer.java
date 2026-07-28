package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TrajectoryRenderer {

    private static final int STEPS = 20;
    // Only recompute/spawn the arc a few times a second instead of every tick,
    // so particles don't pile up on top of each other in front of the camera.
    private static final int SPAWN_EVERY_TICKS = 4;

    private static int cooldownTicks = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.trajectoryPredictionEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        var stack = player.getMainHandStack();
        boolean throwable = stack.isOf(Items.SNOWBALL) || stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.EGG);
        boolean bow = stack.isOf(Items.BOW) && player.isUsingItem();

        if (!throwable && !bow) {
            cooldownTicks = 0;
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }
        cooldownTicks = SPAWN_EVERY_TICKS;

        Vec3d pos = player.getCameraPosVec(1.0f);
        Vec3d look = player.getRotationVec(1.0f);

        // These speeds/gravity are already expressed per-tick (matching vanilla
        // thrown-entity physics), so they must NOT be divided by 20 again - doing
        // that was squashing the whole arc into a tiny space right at the camera.
        float speed = throwable ? 1.5f : 3.0f;
        float gravity = throwable ? 0.03f : 0.05f;
        float drag = 0.99f;

        Vec3d vel = look.multiply(speed);

        // Skip the first couple of steps closest to the eyes so the trail starts a
        // short distance in front of the player instead of right against the camera.
        int skipSteps = 2;

        for (int i = 0; i < STEPS; i++) {
            vel = new Vec3d(vel.x * drag, vel.y * drag - gravity, vel.z * drag);
            pos = pos.add(vel);

            if (!client.world.isAir(BlockPos.ofFloored(pos))) {
                break;
            }

            if (i >= skipSteps) {
                client.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }
}
