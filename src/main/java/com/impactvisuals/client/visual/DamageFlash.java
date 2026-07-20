package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class DamageFlash {

    private static float lastHealth = -1f;
    private static long damageFlashStart = 0;
    private static long healFlashStart = 0;
    private static final long FLASH_DURATION = 350;

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            lastHealth = -1f;
            return;
        }

        float health = player.getHealth();
        if (lastHealth > 0f) {
            if (cfg.damageFlashEnabled && health < lastHealth) {
                damageFlashStart = System.currentTimeMillis();
            } else if (cfg.healFlashEnabled && health > lastHealth) {
                healFlashStart = System.currentTimeMillis();
            }
        }
        lastHealth = health;
    }

    public static void render(DrawContext context) {
        renderFlash(context, damageFlashStart, 0xAA0000);
        renderFlash(context, healFlashStart, 0x33CC33);
    }

    private static void renderFlash(DrawContext context, long startMillis, int rgb) {
        if (startMillis == 0) return;
        long elapsed = System.currentTimeMillis() - startMillis;
        if (elapsed > FLASH_DURATION) return;

        float progress = elapsed / (float) FLASH_DURATION;
        int alpha = (int) (120 * (1.0f - progress));
        int color = (alpha << 24) | rgb;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int edge = Math.max(20, height / 8);

        int transparent = rgb & 0x00FFFFFF;
        context.fillGradient(0, 0, width, edge, color, transparent);
        context.fillGradient(0, height - edge, width, height, transparent, color);
    }
}
