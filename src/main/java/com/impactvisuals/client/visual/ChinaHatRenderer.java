package com.impactvisuals.client.visual;

import com.impactvisuals.client.network.FirebasePresence;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * A conical "China hat" cosmetic worn above the head. It's not self-view-only
 * like the skin/cape/elytra presets - it's broadcast through Firebase
 * presence (see FirebasePresence) so every other Impact Visuals user nearby
 * actually sees it on you, the same way the Jump Ring works.
 */
public class ChinaHatRenderer {

    private static final int SEGMENTS = 14;
    private static final float RADIUS = 0.38f;
    private static final float HEIGHT = 0.42f;

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();
        boolean drewAny = false;

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            String name = player.getGameProfile().getName();
            if (!FirebasePresence.hasChinaHat(name)) continue;

            double baseX = player.getX();
            double baseZ = player.getZ();
            double baseY = player.getY() + player.getHeight() + 0.02;

            matrices.push();
            matrices.translate(baseX - camPos.x, baseY - camPos.y, baseZ - camPos.z);

            drawConeWireframe(matrices, consumers.getBuffer(RenderLayer.getLines()),
                    RADIUS, HEIGHT, SEGMENTS, 1.0f, 0.75f, 0.15f, 1.0f);

            matrices.pop();
            drewAny = true;
        }

        if (drewAny) consumers.draw();
    }

    /** Draws a cone's ribs + base circle as line segments - apex straight up from the origin. */
    private static void drawConeWireframe(MatrixStack matrices, VertexConsumer buffer,
                                           float radius, float height, int segments,
                                           float r, float g, float b, float a) {
        var entry = matrices.peek();

        float apexX = 0, apexY = height, apexZ = 0;

        float[][] basePoints = new float[segments][3];
        for (int i = 0; i < segments; i++) {
            double angle = (2 * Math.PI * i) / segments;
            basePoints[i][0] = (float) (Math.cos(angle) * radius);
            basePoints[i][1] = 0f;
            basePoints[i][2] = (float) (Math.sin(angle) * radius);
        }

        // Ribs from apex to base
        for (float[] base : basePoints) {
            drawLine(buffer, entry, apexX, apexY, apexZ, base[0], base[1], base[2], r, g, b, a);
        }

        // Base circle
        for (int i = 0; i < segments; i++) {
            float[] a1 = basePoints[i];
            float[] a2 = basePoints[(i + 1) % segments];
            drawLine(buffer, entry, a1[0], a1[1], a1[2], a2[0], a2[1], a2[2], r, g, b, a);
        }
    }

    private static void drawLine(VertexConsumer buffer, MatrixStack.Entry entry,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-6f) return;
        dx /= len; dy /= len; dz /= len;

        buffer.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r, g, b, a).normal(entry, dx, dy, dz);
        buffer.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r, g, b, a).normal(entry, dx, dy, dz);
    }
}

