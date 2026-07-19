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
    public boolean hitmarkerEnabled = true;
    public boolean coordinatesHudEnabled = false;
    public boolean compassHudEnabled = false;
    public boolean sessionTimerEnabled = false;
    public boolean lowHealthVignetteEnabled = true;
    public boolean durabilityHudEnabled = false;
    public boolean killDeathCounterEnabled = false;
    public boolean hitSoundEnabled = true;
    public boolean cooldownIndicatorEnabled = true;
    public boolean sprintIndicatorEnabled = false;
    public boolean healthPercentEnabled = false;
    public boolean hungerPercentEnabled = false;
    public boolean xpPercentEnabled = false;
    public boolean armorHudEnabled = false;
    public boolean biomeHudEnabled = false;
    public boolean crosshairDotEnabled = false;
    public boolean killFeedEnabled = true;
    public boolean lightLevelHudEnabled = false;
    public boolean heldItemNameEnabled = false;
    public boolean offhandItemNameEnabled = false;
    public boolean totalPlaytimeEnabled = false;
    public boolean zoomEnabled = true;
    public boolean realClockEnabled = false;
    public long totalPlaytimeMillis = 0L;

    public int accentColorIndex = 0;
    public boolean critSoundEnabled = true;
    public boolean smallFireEnabled = false;
    public boolean customHandleEnabled = false;
    public int customHandleScalePercent = 100;
    public int customHandleRotX = 0;
    public int customHandleRotY = 0;
    public int customHandleRotZ = 0;

    // Combat FX
    public boolean damageFlashEnabled = true;
    public boolean hitImpactPunchEnabled = true;
    public boolean killStreakEnabled = true;
    public boolean bigKillBurstEnabled = true;
    public boolean pulsingVignetteEnabled = false;
    public boolean sweepTrailEnabled = false;

    // Cosmetics
    public boolean rainbowThemeEnabled = false;
    public boolean sprintTrailEnabled = false;
    public boolean footstepDustEnabled = false;
    public int crosshairStyleIndex = 0; // 0=off, 1=dot, 2=cross, 3=ring
    public int hitmarkerStyleIndex = 0; // 0=vanilla X, 1=dot, 2=ring
    public int hitParticleColorIndex = 0; // 0=vanilla, 1-6=palette color

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
