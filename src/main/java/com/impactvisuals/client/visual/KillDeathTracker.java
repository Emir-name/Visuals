package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KillDeathTracker {

    private static final long HIT_WINDOW_MILLIS = 4000;
    private static final long STREAK_WINDOW_MILLIS = 6000;

    private static class HitRecord {
        final long time;
        final String name;
        final double x, y, z;

        HitRecord(long time, String name, double x, double y, double z) {
            this.time = time;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final Map<Integer, HitRecord> recentlyHit = new HashMap<>();
    private static int kills = 0;
    private static int deaths = 0;
    private static float lastHealth = -1f;

    private static int streakCount = 0;
    private static long lastKillMillis = 0;

    public static void onHit(Entity target) {
        recentlyHit.put(target.getId(), new HitRecord(System.currentTimeMillis(), target.getName().getString(),
                target.getX(), target.getY() + target.getHeight() / 2, target.getZ()));
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        ModConfig cfg = ModConfig.get();

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, HitRecord>> it = recentlyHit.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HitRecord> entry = it.next();
            HitRecord record = entry.getValue();
            if (now - record.time > HIT_WINDOW_MILLIS) {
                it.remove();
                continue;
            }
            Entity entity = client.world.getEntityById(entry.getKey());
            boolean gone = entity == null || entity.isRemoved()
                    || (entity instanceof LivingEntity living && living.getHealth() <= 0f);
            if (gone) {
                kills++;
                ExtraHud.announceKill(record.name);
                CombatSounds.playKill();

                if (cfg.bigKillBurstEnabled) {
                    for (int i = 0; i < 10; i++) {
                        double ox = (client.world.random.nextDouble() - 0.5) * 0.6;
                        double oy = (client.world.random.nextDouble() - 0.5) * 0.6;
                        double oz = (client.world.random.nextDouble() - 0.5) * 0.6;
                        client.world.addParticle(ParticleTypes.CRIT, record.x + ox, record.y + oy, record.z + oz,
                                ox * 0.5, oy * 0.5, oz * 0.5);
                    }
                }

                if (cfg.killStreakEnabled) {
                    if (now - lastKillMillis <= STREAK_WINDOW_MILLIS) {
                        streakCount++;
                    } else {
                        streakCount = 1;
                    }
                    lastKillMillis = now;
                    if (streakCount >= 2) {
                        ExtraHud.announceKill(streakLabel(streakCount) + "!");
                        CombatSounds.playStreak();
                    }
                }

                it.remove();
            }
        }

        float health = client.player.getHealth();
        if (lastHealth > 0f && health <= 0f) {
            deaths++;
        }
        lastHealth = health;
    }

    private static String streakLabel(int count) {
        return switch (count) {
            case 2 -> "DOUBLE KILL";
            case 3 -> "TRIPLE KILL";
            case 4 -> "QUAD KILL";
            default -> count + "x KILL STREAK";
        };
    }

    public static void render(DrawContext context) {
        if (!ModConfig.get().killDeathCounterEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        String text = "K: " + kills + "  D: " + deaths;
        context.drawText(client.textRenderer, text, 6, 40, 0xFFFFFFFF, true);
    }
}
