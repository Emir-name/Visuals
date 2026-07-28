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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetHud {

    // "current/max" style HP embedded directly in a nametag, e.g. "123/456".
    private static final Pattern HP_RATIO_PATTERN =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*/\\s*(\\d+(?:[.,]\\d+)?)");

    // A bare number (optionally with a heart symbol / decoration around it), used by
    // servers that float a separate live HP readout above a player's head instead of
    // putting it in the player's own nametag or vanilla health attribute.
    private static final Pattern HP_NUMBER_PATTERN =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)");

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

        // Vanilla numbers as the baseline.
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();

        // 1) A "current/max" pattern directly in the target's own nametag.
        float[] ratio = parseRatioFromText(target.getCustomName() != null ? target.getCustomName().getString() : null);
        if (ratio != null) {
            health = ratio[0];
            maxHealth = ratio[1];
        } else {
            // 2) A separate floating marker entity above the target's head showing a
            // live current-HP number (common on servers where max HP goes past 20).
            Float liveCurrent = findFloatingHealthNumber(client, target);
            if (liveCurrent != null) {
                health = liveCurrent;
                // maxHealth stays whatever the vanilla attribute reported; on servers
                // that do this, the max attribute is usually kept accurate even though
                // current health isn't ticked live.
            }
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

    private static float[] parseRatioFromText(String raw) {
        if (raw == null) return null;
        Matcher matcher = HP_RATIO_PATTERN.matcher(raw);
        if (!matcher.find()) return null;
        try {
            float current = Float.parseFloat(matcher.group(1).replace(',', '.'));
            float max = Float.parseFloat(matcher.group(2).replace(',', '.'));
            if (max <= 0) return null;
            return new float[]{current, max};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Looks for a small marker entity (armor stand, text display, etc.) floating just
     * above the target's head whose custom name is basically just a number - the kind
     * of live "HP readout" some servers spawn instead of updating the real player
     * entity's health attribute. Returns the parsed number, or null if none found.
     */
    private static Float findFloatingHealthNumber(MinecraftClient client, LivingEntity target) {
        if (client.world == null) return null;

        Box searchBox = new Box(
                target.getX() - 1.0, target.getEyeY(), target.getZ() - 1.0,
                target.getX() + 1.0, target.getEyeY() + 3.0, target.getZ() + 1.0
        );

        List<Entity> nearby = client.world.getOtherEntities(target, searchBox,
                e -> e.getCustomName() != null && e != target);

        for (Entity marker : nearby) {
            String text = marker.getCustomName().getString();
            Matcher matcher = HP_NUMBER_PATTERN.matcher(text);
            if (!matcher.find()) continue;
            // Skip if it doesn't look like a standalone number (avoids grabbing part of
            // an unrelated custom name that just happens to contain digits).
            String stripped = text.replaceAll("[^0-9.,]", "");
            if (stripped.isEmpty() || stripped.length() > 8) continue;
            try {
                return Float.parseFloat(matcher.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) {
                // try next entity
            }
        }
        return null;
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
