package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/**
 * Local-only feedback sound on hit (nothing sent to the server, nothing other
 * players hear) — purely a client-side audio cue, same idea as a hitmarker.
 */
public class HitSoundPlayer {

    public static void play() {
        if (!ModConfig.get().hitSoundEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.4f)
        );
    }
}
