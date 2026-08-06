package com.impactvisuals.client.visual;

import com.impactvisuals.client.api.HealthSnapshot;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.network.FirebasePresence;
import com.impactvisuals.client.util.CacheUtils;
import com.impactvisuals.client.visual.health.HealthResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TargetHud {

    private static final HealthResolver HEALTH_RESOLVER = new HealthResolver();
    private static final long[] CACHE_TIME = {0};
    private static final LivingEntity[] CACHE_TARGET = {null};

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.hud.targetHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        LivingEntity target = CacheUtils.getTimed(200, CACHE_TIME, CACHE_TARGET,
                () -> findLookedAtLivingEntity(client, cfg.hud.targetHudRangeBlocks));
        if (target == null) return;

        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        int cardW = 130, cardH = 70;
        int cardX = sw / 2 - cardW / 2;
        int cardY = sh / 2 - 70;

        HudCard.draw(context, cardX, cardY, cardW, cardH);

        int iconSize = 28, iconX = cardX + 6, iconY = cardY + 6;
        if (target instanceof AbstractClientPlayerEntity player) {
            net.minecraft.client.gui.PlayerSkinDrawer.draw(context, player.getSkinTextures(), iconX, iconY, iconSize);
        } else {
            context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xFF3A3A3A);
        }

        int textX = iconX + iconSize + 8;
        String nameStr = target.getDisplayName() != null
                ? target.getDisplayName().getString()
                : target.getName().getString();
        context.drawText(client.textRenderer, nameStr, textX, cardY + 8, 0xFFFFFFFF, false);

        if (FirebasePresence.isOnline(target.getName().getString())) {
            int nameW = client.textRenderer.getWidth(nameStr);
            context.drawText(client.textRenderer, "IV", textX + nameW + 4, cardY + 8, 0xFFB266FF, false);
        }

        HealthSnapshot hp = HEALTH_RESOLVER.resolve(target, client);
        float health = hp != null ? hp.current() : target.getHealth();
        float maxHealth = hp != null ? hp.max() : target.getMaxHealth();

        String hpText = "HP • " + formatNumber(health) + "/" + formatNumber(maxHealth);
        context.drawText(client.textRenderer, hpText, textX, cardY + 19, 0xFFAAAAAA, false);

        if (cfg.hud.targetHudDebugEnabled) {
            dumpNearbyEntities(client, target);
        }

        renderArmorRow(context, client, target, cardX, cardY + 38, cardW);

        int barX = cardX + 6, barY = cardY + cardH - 8;
        int barW = cardW - 12, barH = 4;
        float pct = maxHealth > 0 ? Math.max(0f, Math.min(1f, health / maxHealth)) : 0f;

        context.fill(barX, barY, barX + barW, barY + barH, 0x66000000);
        context.fill(barX, barY, barX + Math.round(barW * pct), barY + barH, 0xFFB266FF);
    }

    private static void renderArmorRow(DrawContext context, MinecraftClient client, LivingEntity target,
                                       int cardX, int rowY, int cardW) {
        var slots = new net.minecraft.entity.EquipmentSlot[]{
                net.minecraft.entity.EquipmentSlot.HEAD,
                net.minecraft.entity.EquipmentSlot.CHEST,
                net.minecraft.entity.EquipmentSlot.LEGS,
                net.minecraft.entity.EquipmentSlot.FEET
        };
        int slotSize = 18, gap = 4;
        int totalW = slots.length * slotSize + (slots.length - 1) * gap;
        int startX = cardX + (cardW - totalW) / 2;

        for (int i = 0; i < slots.length; i++) {
            var stack = target.getEquippedStack(slots[i]);
            int x = startX + i * (slotSize + gap);
            context.fill(x, rowY, x + slotSize, rowY + slotSize, 0x40000000);
            if (stack.isEmpty()) continue;
            context.drawItem(stack, x + 1, rowY + 1);
            if (stack.isDamageable()) {
                float pct = 1f - ((float) stack.getDamage() / stack.getMaxDamage());
                pct = Math.max(0f, Math.min(1f, pct));
                int barY = rowY + slotSize + 1, barW = slotSize, filled = Math.round(barW * pct);
                int color = pct > 0.5f ? 0xFF6FCF4A : pct > 0.2f ? 0xFFE0C13C : 0xFFE0483C;
                context.fill(x, barY, x + barW, barY + 2, 0x66000000);
                context.fill(x, barY, x + filled, barY + 2, color);
            }
        }
    }

    private static String formatNumber(float v) {
        return v == Math.round(v) ? String.valueOf(Math.round(v)) : String.format("%.1f", v);
    }

    private static LivingEntity findLookedAtLivingEntity(MinecraftClient client, double range) {
        Entity cam = client.cameraEntity;
        if (cam == null) return null;
        Vec3d start = cam.getCameraPosVec(1.0f);
        Vec3d look = cam.getRotationVec(1.0f);
        Box box = cam.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        EntityHitResult res = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                cam, start, start.add(look.multiply(range)), box,
                e -> e instanceof LivingEntity && !e.isSpectator() && e.canHit() && e != client.player,
                range * range);
        return res != null && res.getEntity() instanceof LivingEntity le ? le : null;
    }

    private static long lastDump = 0;
    private static void dumpNearbyEntities(MinecraftClient client, LivingEntity target) {
        long now = System.nanoTime();
        if (now - lastDump < 1_000_000_000L) return;
        lastDump = now;
        if (client.world == null || client.player == null) return;
        Box box = target.getBoundingBox().expand(4.0);
        var nearby = client.world.getOtherEntities(target, box, e -> e.getCustomName() != null && e != target);
        client.player.sendMessage(Text.literal("[TargetHUD] target=" + target.getName().getString()
                + " health=" + target.getHealth() + "/" + target.getMaxHealth()), false);
        for (Entity e : nearby) {
            String line = String.format("[TargetHUD] \"%s\" type=%s offset=(%.2f, %.2f, %.2f)",
                    e.getCustomName().getString(), e.getType().toString(),
                    e.getX() - target.getX(), e.getY() - target.getY(), e.getZ() - target.getZ());
            client.player.sendMessage(Text.literal(line), false);
        }
    }
}
