package com.impactvisuals.client.visual;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces vanilla particle textures (crit, end rod, snowflake, etc.) across
 * the whole mod with our own tiny solid-colour cubes - same technique
 * HatRenderer/BuildHelperHud already use to draw blocks in world space, so
 * no new texture assets are needed, just a real custom look instead of
 * borrowing Minecraft's own particle sprites.
 */
public class CustomParticleManager {

    public static final int WHITE = 0, ORANGE = 1, MAGENTA = 2, LIGHT_BLUE = 3, YELLOW = 4, LIME = 5,
            PINK = 6, GRAY = 7, LIGHT_GRAY = 8, CYAN = 9, PURPLE = 10, BLUE = 11, BROWN = 12,
            GREEN = 13, RED = 14, BLACK = 15;

    private static final Block[] BLOCK_BY_COLOR = {
            Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
            Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
            Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE
    };

    private static class Particle {
        double x, y, z;
        double vx, vy, vz;
        int color;
        float size;
        int age;
        int maxAge;
        boolean gravity;
        float spinDeg;
    }

    private static final List<Particle> particles = new ArrayList<>();
    private static final int MAX_PARTICLES = 400;

    /**
     * Spawns one custom particle.
     * @param color one of the constants above (WHITE, ORANGE, RED, ...)
     * @param lifeTicks how long it lives, in client ticks (20/sec)
     * @param size roughly the block-fraction size (0.12 = small speck, 0.3 = chunky)
     * @param gravity whether it falls over time like a real particle
     */
    public static void spawn(double x, double y, double z, double vx, double vy, double vz,
                              int color, int lifeTicks, float size, boolean gravity) {
        if (particles.size() >= MAX_PARTICLES) return;
        Particle p = new Particle();
        p.x = x; p.y = y; p.z = z;
        p.vx = vx; p.vy = vy; p.vz = vz;
        p.color = Math.max(0, Math.min(BLOCK_BY_COLOR.length - 1, color));
        p.size = size;
        p.maxAge = Math.max(1, lifeTicks);
        p.gravity = gravity;
        p.spinDeg = (float) (Math.random() * 360);
        particles.add(p);
    }

    public static void tick() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.age++;
            if (p.age >= p.maxAge) {
                particles.remove(i);
                continue;
            }
            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;
            if (p.gravity) {
                p.vy -= 0.02;
            }
            p.vx *= 0.98;
            p.vz *= 0.98;
            p.spinDeg += 6f;
        }
    }

    public static void render(WorldRenderContext context) {
        if (particles.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Vec3d camPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        int fullBrightLight = 0xF000F0;
        float tickDelta = context.tickCounter().getTickDelta(true);

        for (Particle p : particles) {
            float lifeFrac = (p.age + tickDelta) / p.maxAge;
            float fade = 1f - Math.max(0f, Math.min(1f, lifeFrac));
            float scale = p.size * (0.6f + 0.4f * fade);
            if (scale <= 0.001f) continue;

            double px = p.x + p.vx * tickDelta;
            double py = p.y + p.vy * tickDelta;
            double pz = p.z + p.vz * tickDelta;

            BlockState state = BLOCK_BY_COLOR[p.color].getDefaultState();

            matrices.push();
            matrices.translate(px - camPos.x, py - camPos.y, pz - camPos.z);
            matrices.translate(-scale / 2, -scale / 2, -scale / 2);
            matrices.scale(scale, scale, scale);

            client.getBlockRenderManager().renderBlockAsEntity(state, matrices, consumers, fullBrightLight, OverlayTexture.DEFAULT_UV);

            matrices.pop();
        }

        consumers.draw();
    }

    public static void clear() {
        particles.clear();
    }
}

