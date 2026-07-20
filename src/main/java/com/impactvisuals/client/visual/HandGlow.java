package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class HandGlow {

    private static int tickCounter = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.handGlowEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        tickCounter++;
        if (tickCounter < 6) return;
        tickCounter = 0;

        Vec3d look = player.getRotationVec(1.0f);
        Vec3d base = player.getEyePos().add(look.multiply(0.8)).subtract(0, 0.3, 0);

        double ox = (player.getRandom().nextDouble() - 0.5) * 0.15;
        double oy = (player.getRandom().nextDouble() - 0.5) * 0.15;
        double oz = (player.getRandom().nextDouble() - 0.5) * 0.15;

        client.world.addParticle(ParticleTypes.END_ROD, base.x + ox, base.y + oy, base.z + oz, 0.0, 0.01, 0.0);
    }
}
