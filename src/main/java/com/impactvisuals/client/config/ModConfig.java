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
    public boolean markerEnabled = false;
    public int hatIndex = 0;
    public int hatColorIndex = 4;
    public boolean radarEnabled = false;
    public boolean hideCampfireSmoke = false;
    public boolean setupComplete = false;
    public int themePresetIndex = 0;
    public java.util.Map<String, Integer> hudOffsetX = new java.util.HashMap<>();
    public java.util.Map<String, Integer> hudOffsetY = new java.util.HashMap<>();
    public java.util.Map<String, Float> hudScale = new java.util.HashMap<>();
    public int markerX = 0;
    public int markerY = 64;
    public int markerZ = 0;
    public String markerName = "";
    public boolean buildHelperEnabled = false;
    public boolean jumpRingEnabled = false;
    public boolean jumpRingWhite = false;
    public boolean focusTargetEnabled = false;
    public String focusTargetName = "";
    public boolean targetHudDebugEnabled = false;
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
    public boolean autoJumpEnabled = false;
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

    public boolean damageFlashEnabled = true;
    public boolean hitImpactPunchEnabled = true;
    public boolean killStreakEnabled = true;
    public boolean bigKillBurstEnabled = true;
    public boolean killLaserEnabled = true;
    public boolean pulsingVignetteEnabled = false;
    public boolean sweepTrailEnabled = false;

    public boolean rainbowThemeEnabled = false;
    public boolean sprintTrailEnabled = false;
    public boolean footstepDustEnabled = false;
    public int crosshairStyleIndex = 0;
    public int hitmarkerStyleIndex = 0;
    public int hitParticleColorIndex = 0;

    public boolean killSoundEnabled = true;
    public boolean heartbeatSoundEnabled = false;
    public boolean streakSoundEnabled = true;
    public boolean menuSoundEnabled = true;
    public boolean footstepSoundEnabled = false;

    public boolean healFlashEnabled = true;

    public boolean coloredTrailsEnabled = false;
    public boolean handGlowEnabled = false;

    public int selectedSkinIndex = 0;
    public int selectedCapeIndex = 0;
    public int selectedElytraIndex = 0;
    public int armModelIndex = 0;

    public boolean friendsFeatureEnabled = false;
    public boolean activeEffectsHudEnabled = true;
    public boolean russianLanguage = false;
    public boolean betterNearEnabled = true;
    public String firebaseUrl = "";
    public java.util.List<String> friendsList = new java.util.ArrayList<>();
    public java.util.List<String> boundCommands = new java.util.ArrayList<>();
    /** Maps a feature's label (e.g. "Auto Jump") to a bound key's translation key (e.g. "key.keyboard.j"). */
    public java.util.Map<String, String> featureKeybinds = new java.util.HashMap<>();
    public java.util.Map<String, String> commandKeybinds = new java.util.HashMap<>();

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

    public java.util.Map<String, String> profiles = new java.util.HashMap<>();

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

    /** Serializes this config to a compact Base64 "code" string, for sharing with someone else. */
    public String exportCode() {
        String json = GSON.toJson(this);
        return java.util.Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /** Parses a code produced by exportCode() and copies every field over onto this live config, then saves. Returns false if the code was invalid. */
    public boolean importCode(String code) {
        try {
            String json = new String(java.util.Base64.getDecoder().decode(code.trim()), StandardCharsets.UTF_8);
            ModConfig parsed = GSON.fromJson(json, ModConfig.class);
            if (parsed == null) return false;
            copyFrom(parsed);
            save();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Saves the current settings as a named profile (overwrites if the name already exists). */
    public void saveProfile(String name) {
        profiles.put(name, exportCode());
        save();
    }

    /** Loads a previously saved named profile onto the live config, then saves. Returns false if the name isn't found or the stored code is invalid. */
    public boolean loadProfile(String name) {
        String code = profiles.get(name);
        if (code == null) return false;
        return importCode(code);
    }

    public void deleteProfile(String name) {
        profiles.remove(name);
        save();
    }

    /** Copies every field from another ModConfig instance onto this one via reflection, so importCode/loadProfile can apply a parsed snapshot without replacing the singleton instance itself. */
    private void copyFrom(ModConfig other) {
        for (java.lang.reflect.Field field : ModConfig.class.getFields()) {
            if (field.getName().equals("profiles")) continue;
            try {
                field.set(this, field.get(other));
            } catch (IllegalAccessException ignored) {
            }
        }
    }
}
