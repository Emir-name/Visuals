package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class DamageFlash {

    private static float lastHealth = -1f;
    private static long flashStartMillis = 0;
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
        if (cfg.damageFlashEnabled && lastHealth > 0f && health < lastHealth) {
            flashStartMillis = System.currentTimeMillis();
        }
        lastHealth = health;
    }

    public static void render(DrawContext context) {
        if (flashStartMillis == 0) return;
        long elapsed = System.currentTimeMillis() - flashStartMillis;
        if (elapsed > FLASH_DURATION) return;

        float progress = elapsed / (float) FLASH_DURATION;
        int alpha = (int) (120 * (1.0f - progress));
        int color = (alpha << 24) | 0xAA0000;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int edge = Math.max(20, height / 8);

        context.fillGradient(0, 0, width, edge, color, 0x00AA0000);
        context.fillGradient(0, height - edge, width, height, 0x00AA0000, color);
    }
}
