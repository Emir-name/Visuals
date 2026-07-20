package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;

public class ScreenTint {

    private static int tickCounter = 0;
    private static net.minecraft.client.option.CloudRenderMode previousCloudMode = null;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();

        var cloudOption = client.options.getCloudRenderMode();
        if (cfg.purpleSkyEnabled) {
            if (previousCloudMode == null) {
                previousCloudMode = cloudOption.getValue();
                cloudOption.setValue(net.minecraft.client.option.CloudRenderMode.OFF);
            }
        } else if (previousCloudMode != null) {
            cloudOption.setValue(previousCloudMode);
            previousCloudMode = null;
        }

        if (!cfg.purpleSkyEnabled) return;

        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        tickCounter++;
        if (tickCounter < 3) return;
        tickCounter = 0;

        var random = player.getRandom();
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 40.0;
            double offsetZ = (random.nextDouble() - 0.5) * 40.0;
            double x = player.getX() + offsetX;
            double z = player.getZ() + offsetZ;
            double y = player.getY() + 20 + random.nextDouble() * 15;

            client.world.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.02, 0.0);
        }
    }
}
