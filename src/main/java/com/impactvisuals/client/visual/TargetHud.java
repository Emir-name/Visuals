package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetHud {

    // Matches "123/456", "123 / 456", "❤123/456", "[123/456]" etc. that RPG-style
    // servers embed in a player's nametag to show HP beyond the vanilla 20 hearts.
    private static final Pattern HP_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)");

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.targetHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        LivingEntity target = findLookedAtLivingEntity(client, cfg.targetHudRangeBlocks);
        if (target == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int cardW = 130;
        int cardH = 46;
        int cardX = screenWidth / 2 - cardW / 2;
        int cardY = screenHeight / 2 - 70;

        HudCard.draw(context, cardX, cardY, cardW, cardH);

        int iconSize = 28;
        int iconX = cardX + 6;
        int iconY = cardY + (cardH - iconSize) / 2;

        if (target instanceof AbstractClientPlayerEntity player) {
            net.minecraft.client.gui.PlayerSkinDrawer.draw(context, player.getSkinTextures(), iconX, iconY, iconSize);
        } else {
            context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xFF3A3A3A);
        }

        int textX = iconX + iconSize + 8;

        Text name = target.getDisplayName() != null ? target.getDisplayName() : Text.literal(target.getName().getString());
        String nameStr = name.getString();
        context.drawText(client.textRenderer, nameStr, textX, cardY + 8, 0xFFFFFFFF, false);

        // Prefer the "real" HP embedded in the nametag (used by servers whose actual
        // max health goes above vanilla's 20 hearts); fall back to the vanilla
        // entity attributes if no such pattern is present.
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float[] displayedHp = parseDisplayedHealth(target);
        if (displayedHp != null) {
            health = displayedHp[0];
            maxHealth = displayedHp[1];
        }

        String hpText = "HP \u2022 " + formatNumber(health) + "/" + formatNumber(maxHealth);
        context.drawText(client.textRenderer, hpText, textX, cardY + 19, 0xFFAAAAAA, false);

        int barX = cardX + 6;
        int barY = cardY + cardH - 8;
        int barWidth = cardW - 12;
        int barHeight = 4;
        float pct = maxHealth > 0 ? Math.max(0f, Math.min(1f, health / maxHealth)) : 0f;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x66000000);
        context.fill(barX, barY, barX + Math.round(barWidth * pct), barY + barHeight, 0xFFB266FF);
    }

    /**
     * Looks for a "current/max" style HP readout in the entity's nametag text.
     * Returns {current, max} if found, or null if the nametag doesn't contain one
     * (in which case the caller should keep using the vanilla health attributes).
     */
    private static float[] parseDisplayedHealth(LivingEntity target) {
        Text customName = target.getCustomName();
        if (customName == null) return null;

        String raw = customName.getString();
        Matcher matcher = HP_PATTERN.matcher(raw);
        if (!matcher.find()) return null;

        try {
            float current = Float.parseFloat(matcher.group(1));
            float max = Float.parseFloat(matcher.group(2));
            if (max <= 0) return null;
            return new float[]{current, max};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatNumber(float value) {
        return value == Math.round(value) ? String.valueOf(Math.round(value)) : String.format("%.1f", value);
    }

    private static LivingEntity findLookedAtLivingEntity(MinecraftClient client, double range) {
        Entity cameraEntity = client.cameraEntity;
        if (cameraEntity == null) return null;

        Vec3d start = cameraEntity.getCameraPosVec(1.0f);
        Vec3d look = cameraEntity.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(range));

        Box searchBox = cameraEntity.getBoundingBox().stretch(look.multiply(range)).expand(1.0);

        EntityHitResult result = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                cameraEntity,
                start,
                end,
                searchBox,
                e -> e instanceof LivingEntity && !e.isSpectator() && e.canHit() && e != client.player,
                range * range
        );

        if (result == null) return null;
        Entity hit = result.getEntity();
        return hit instanceof LivingEntity living ? living : null;
    }
}
