package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;

public class CritSoundPlayer {

    public static void play() {
        if (!ModConfig.get().critSoundEnabled) return;
        if (ModSounds.CRIT_HIT == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(
                PositionedSoundInstance.master(ModSounds.CRIT_HIT, 1.0f)
        );
    }
}
