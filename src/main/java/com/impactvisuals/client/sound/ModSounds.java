package com.impactvisuals.client.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {

    public static SoundEvent CRIT_HIT;

    private ModSounds() {}

    public static void register() {
        Identifier id = Identifier.of("impactvisuals", "crit_hit");
        CRIT_HIT = Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
