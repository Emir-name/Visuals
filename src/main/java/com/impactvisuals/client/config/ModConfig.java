package com.impactvisuals.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("impactvisuals.json");

    public boolean hitParticlesEnabled = true;
    public boolean targetHudEnabled = true;
    public boolean damageNumbersEnabled = true;
    public boolean criticalFlashEnabled = true;
    public boolean trajectoryPredictionEnabled = true;
    public boolean purpleSkyEnabled = false;
    public boolean infoHudEnabled = true;

    // Tunables
    public float hitParticleLifetimeSeconds = 0.5f;
    public float damageNumberLifetimeSeconds = 0.8f;
    public int targetHudRangeBlocks = 6;

    private static ModConfig instance;

    public static ModConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                System.err.println("[ImpactVisuals] Failed to read config, using defaults: " + e.getMessage());
            }
        }
        ModConfig fresh = new ModConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[ImpactVisuals] Failed to save config: " + e.getMessage());
        }
    }
}
