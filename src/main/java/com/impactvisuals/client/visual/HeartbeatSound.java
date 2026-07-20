package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class HeartbeatSound {

    private static final float LOW_HEALTH_THRESHOLD = 6.0f;
    private static long lastBeatMillis = 0;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        if (!cfg.heartbeatSoundEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        float health = player.getHealth();
        if (health <= 0f || health >= LOW_HEALTH_THRESHOLD) return;

        float severity = 1.0f - (health / LOW_HEALTH_THRESHOLD);
        long interval = (long) (900 - severity * 600);

        long now = System.currentTimeMillis();
        if (now - lastBeatMillis < interval) return;
        lastBeatMillis = now;

        client.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.8f)
        );
    }
}
