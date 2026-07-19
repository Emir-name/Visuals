package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Informal session-only kill/death counter. Kills are detected heuristically:
 * if an entity we recently hit disappears from the world shortly after, we
 * count it as a kill. Deaths are detected by watching our own health drop to zero.
 * This is a cosmetic stat display, not a gameplay system — accuracy is "close enough"
 * for a scoreboard-style readout, not authoritative.
 */
public class KillDeathTracker {

    private static final long HIT_WINDOW_MILLIS = 4000;

    private static final Map<Integer, Long> recentlyHit = new HashMap<>();
    private static int kills = 0;
    private static int deaths = 0;
    private static float lastHealth = -1f;

    public static void onHit(Entity target) {
        recentlyHit.put(target.getId(), System.currentTimeMillis());
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> it = recentlyHit.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            if (now - entry.getValue() > HIT_WINDOW_MILLIS) {
                it.remove();
                continue;
            }
            Entity entity = client.world.getEntityById(entry.getKey());
            boolean gone = entity == null || entity.isRemoved()
                    || (entity instanceof LivingEntity living && living.getHealth() <= 0f);
            if (gone) {
                kills++;
                it.remove();
            }
        }

        float health = client.player.getHealth();
        if (lastHealth > 0f && health <= 0f) {
            deaths++;
        }
        lastHealth = health;
    }

    public static void render(DrawContext context) {
        if (!ModConfig.get().killDeathCounterEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        String text = "K: " + kills + "  D: " + deaths;
        context.drawText(client.textRenderer, text, 6, 40, 0xFFFFFFFF, true);
    }
}
