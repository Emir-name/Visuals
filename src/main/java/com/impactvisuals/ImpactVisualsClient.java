package com.impactvisuals;

import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.config.ModKeybinds;
import com.impactvisuals.client.event.ClientEventHandler;
import com.impactvisuals.client.sound.ModSounds;
import net.fabricmc.api.ClientModInitializer;

public class ImpactVisualsClient implements ClientModInitializer {

    public static final String MOD_ID = "impactvisuals";

    @Override
    public void onInitializeClient() {
        ModConfig.get(); // load/create config on startup
        ModKeybinds.register();
        ModSounds.register();
        ClientEventHandler.register();
        System.out.println("[ImpactVisuals] Client visual effects initialized.");
    }
}
