package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TargetHud {

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.targetHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        LivingEntity target = findLookedAtLivingEntity(client, cfg.targetHudRangeBlocks);
        if (target == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int y = screenHeight / 2 - 40;

        Text name = target.getDisplayName() != null ? target.getDisplayName() : Text.literal(target.getName().getString());
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();

        String nameStr = name.getString();
        int nameWidth = client.textRenderer.getWidth(nameStr);
        context.drawText(client.textRenderer, nameStr, centerX - nameWidth / 2, y, 0xFFFFFFFF, true);

        int barWidth = 60;
        int barHeight = 4;
        int barX = centerX - barWidth / 2;
        int barY = y + 11;
        float pct = maxHealth > 0 ? Math.max(0f, Math.min(1f, health / maxHealth)) : 0f;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x99000000);
        int fillColor = pct > 0.5f ? 0xFF55FF55 : (pct > 0.2f ? 0xFFFFFF55 : 0xFFFF5555);
        context.fill(barX, barY, barX + Math.round(barWidth * pct), barY + barHeight, fillColor);

        String hpText = String.format("%.1f / %.1f", health, maxHealth);
        int hpWidth = client.textRenderer.getWidth(hpText);
        context.drawText(client.textRenderer, hpText, centerX - hpWidth / 2, barY + 6, 0xFFCCCCCC, true);
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
