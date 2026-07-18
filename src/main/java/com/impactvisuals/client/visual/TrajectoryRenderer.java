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

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.trajectoryPredictionEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        var stack = player.getMainHandStack();
        boolean throwable = stack.isOf(Items.SNOWBALL) || stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.EGG);
        boolean bow = stack.isOf(Items.BOW) && player.isUsingItem();

        if (!throwable && !bow) return;

        Vec3d pos = player.getCameraPosVec(1.0f);
        Vec3d look = player.getRotationVec(1.0f);

        float speed = throwable ? 1.5f : 3.0f;
        float gravity = throwable ? 0.03f : 0.05f;
        float drag = 0.99f;

        Vec3d vel = look.multiply(speed / 20.0);

        for (int i = 0; i < STEPS; i++) {
            vel = new Vec3d(vel.x * drag, vel.y * drag - gravity / 20.0, vel.z * drag);
            pos = pos.add(vel);

            if (!client.world.isAir(BlockPos.ofFloored(pos))) {
                break;
            }

            if (i % 2 == 0) {
                client.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }
}
