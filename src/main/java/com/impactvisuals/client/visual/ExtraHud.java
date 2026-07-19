package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ExtraHud {

    private static String killFeedText = null;
    private static long killFeedExpireAt = 0;

    public static void announceKill(String targetName) {
        killFeedText = "Eliminated " + targetName;
        killFeedExpireAt = System.currentTimeMillis() + 2000;
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        ModConfig cfg = ModConfig.get();

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        int line = 10;

        int y = screenH - 30;
        int x = 6;

        if (cfg.sprintIndicatorEnabled) {
            String text = client.player.isSprinting() ? "Sprinting" : "Walking";
            context.drawText(client.textRenderer, text, x, y, 0xFFAAAAAA, true);
            y -= line;
        }
        if (cfg.healthPercentEnabled) {
            float pct = client.player.getMaxHealth() > 0 ? (client.player.getHealth() / client.player.getMaxHealth()) * 100f : 0f;
            context.drawText(client.textRenderer, String.format("HP: %.0f%%", pct), x, y, 0xFFFF5555, true);
            y -= line;
        }
        if (cfg.hungerPercentEnabled) {
            int hunger = client.player.getHungerManager().getFoodLevel();
            float pct = (hunger / 20f) * 100f;
            context.drawText(client.textRenderer, String.format("Hunger: %.0f%%", pct), x, y, 0xFFDDAA55, true);
            y -= line;
        }
        if (cfg.xpPercentEnabled) {
            float pct = client.player.experienceProgress * 100f;
            context.drawText(client.textRenderer, String.format("XP: %.0f%% (Lvl %d)", pct, client.player.experienceLevel), x, y, 0xFF55FF55, true);
            y -= line;
        }
        if (cfg.armorHudEnabled) {
            int armor = client.player.getArmor();
            context.drawText(client.textRenderer, "Armor: " + armor, x, y, 0xFFAAAAFF, true);
            y -= line;
        }
        if (cfg.biomeHudEnabled && client.world != null) {
            var biomeEntry = client.world.getBiome(client.player.getBlockPos());
            String biomeName = biomeEntry.getKey().map(k -> k.getValue().getPath()).orElse("unknown");
            context.drawText(client.textRenderer, "Biome: " + biomeName, x, y, 0xFFFFFFFF, true);
            y -= line;
        }

        int yr = screenH - 30;
        int xr = screenW - 6;

        if (cfg.lightLevelHudEnabled && client.world != null) {
            int light = client.world.getLightLevel(client.player.getBlockPos());
            drawRight(context, "Light: " + light, xr, yr);
            yr -= line;
        }
        if (cfg.heldItemNameEnabled) {
            ItemStack stack = client.player.getMainHandStack();
            if (!stack.isEmpty()) {
                drawRight(context, stack.getName().getString(), xr, yr);
                yr -= line;
            }
        }
        if (cfg.offhandItemNameEnabled) {
            ItemStack stack = client.player.getOffHandStack();
            if (!stack.isEmpty()) {
                drawRight(context, "Off: " + stack.getName().getString(), xr, yr);
                yr -= line;
            }
        }
        if (cfg.realClockEnabled) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            drawRight(context, time, xr, yr);
            yr -= line;
        }

        if (cfg.crosshairStyleIndex > 0) {
            int cx = screenW / 2;
            int cy = screenH / 2;
            renderCrosshairStyle(context, cx, cy, cfg.crosshairStyleIndex);
        }

        if (cfg.killFeedEnabled && killFeedText != null) {
            if (System.currentTimeMillis() < killFeedExpireAt) {
                int tw = client.textRenderer.getWidth(killFeedText);
                context.drawText(client.textRenderer, killFeedText, (screenW - tw) / 2, 40, 0xFFFF5555, true);
            } else {
                killFeedText = null;
            }
        }
    }

    private static void renderCrosshairStyle(DrawContext context, int cx, int cy, int style) {
        int color = 0xFFFFFFFF;
        switch (style) {
            case 1 -> context.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
            case 2 -> {
                context.fill(cx - 4, cy, cx + 4, cy + 1, color);
                context.fill(cx, cy - 4, cx + 1, cy + 4, color);
            }
            case 3 -> {
                int r = 4;
                context.fill(cx - r, cy - 1, cx - r + 2, cy + 1, color);
                context.fill(cx + r - 2, cy - 1, cx + r, cy + 1, color);
                context.fill(cx - 1, cy - r, cx + 1, cy - r + 2, color);
                context.fill(cx - 1, cy + r - 2, cx + 1, cy + r, color);
            }
            default -> {}
        }
    }

    private static void drawRight(DrawContext context, String text, int rightX, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        int w = client.textRenderer.getWidth(text);
        context.drawText(client.textRenderer, text, rightX - w, y, 0xFFFFFFFF, true);
    }
}
