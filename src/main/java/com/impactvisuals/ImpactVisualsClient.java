package com.impactvisuals;

import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.event.ClientEventHandler;
import net.fabricmc.api.ClientModInitializer;

public class ImpactVisualsClient implements ClientModInitializer {

    public static final String MOD_ID = "impactvisuals";

    @Override
    public void onInitializeClient() {
        ModConfig.get(); // load/create config on startup
        ClientEventHandler.register();
        System.out.println("[ImpactVisuals] Client visual effects initialized.");
    }
}
