package com.impactvisuals.client.visual;

import com.impactvisuals.client.network.FirebasePresence;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Draws whichever hat cosmetic each player has selected (Skins tab: None /
 * China Hat / Ushanka / Cap). Not self-view-only like skin/cape/elytra - the
 * chosen hat index is broadcast through Firebase presence (see
 * FirebasePresence) so every other Impact Visuals user nearby actually sees
 * it on you, the same way the Jump Ring works.
 */
public class HatRenderer {

    public static final int NONE = 0;
    public static final int CHINA_HAT = 1;
    public static final int USHANKA = 2;
    public static final int CAP = 3;

    private static final java.util.Set<String> notifiedHats = new java.util.HashSet<>();

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
            int hat = FirebasePresence.getHatIndex(name);

            if (hat == NONE) {
                notifiedHats.remove(name.toLowerCase());
                continue;
            }

            if (notifiedHats.add(name.toLowerCase())) {
                client.player.sendMessage(net.minecraft.text.Text.literal(
                        "\u00A7d[Impact Visuals] \u00A7f" + hatName(hat) + " visible on \u00A7e" + name), false);
            }

            double baseX = player.getX();
            double baseZ = player.getZ();
            double baseY = player.getY() + player.getHeight() + 0.02;

            matrices.push();
            matrices.translate(baseX - camPos.x, baseY - camPos.y, baseZ - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw()));

            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
            switch (hat) {
                case CHINA_HAT -> drawConeWireframe(matrices, lines, 0.38f, 0.42f, 14, 1.0f, 0.75f, 0.15f, 1.0f);
                case USHANKA -> drawUshankaWireframe(matrices, lines);
                case CAP -> drawCapWireframe(matrices, lines);
                default -> { }
            }

            matrices.pop();
            drewAny = true;
        }

        if (drewAny) consumers.draw();
    }

    private static String hatName(int hat) {
        return switch (hat) {
            case CHINA_HAT -> "China Hat";
            case USHANKA -> "Ushanka";
            case CAP -> "Cap";
            default -> "Hat";
        };
    }

    /** Cone: apex straight up, ribs + base circle. */
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

        for (float[] base : basePoints) {
            drawLine(buffer, entry, apexX, apexY, apexZ, base[0], base[1], base[2], r, g, b, a);
        }
        for (int i = 0; i < segments; i++) {
            float[] a1 = basePoints[i];
            float[] a2 = basePoints[(i + 1) % segments];
            drawLine(buffer, entry, a1[0], a1[1], a1[2], a2[0], a2[1], a2[2], r, g, b, a);
        }
    }

    /** Rounded fur box with two hanging ear flaps on the sides. */
    private static void drawUshankaWireframe(MatrixStack matrices, VertexConsumer buffer) {
        var entry = matrices.peek();
        float r = 0.85f, g = 0.82f, b = 0.75f, a = 1.0f; // off-white fur
        float w = 0.34f, d = 0.32f, h = 0.24f;

        drawBoxWireframe(buffer, entry, -w, 0, -d, w, h, d, r, g, b, a);

        // Ear flaps: small hanging rectangles on the left/right sides.
        float flapW = 0.06f, flapH = 0.22f, flapY0 = -0.02f;
        drawBoxWireframe(buffer, entry, -w - flapW, flapY0 - flapH, -0.08f, -w, flapY0, 0.08f, r, g, b, a);
        drawBoxWireframe(buffer, entry, w, flapY0 - flapH, -0.08f, w + flapW, flapY0, 0.08f, r, g, b, a);
    }

    /** Low flat crown plus a brim sticking out forward (toward -Z, the way the player faces). */
    private static void drawCapWireframe(MatrixStack matrices, VertexConsumer buffer) {
        var entry = matrices.peek();
        float r = 0.25f, g = 0.45f, b = 0.85f, a = 1.0f; // blue cap

        float w = 0.32f, d = 0.32f, h = 0.16f;
        drawBoxWireframe(buffer, entry, -w, 0, -d, w, h, d, r, g, b, a);

        // Brim: thin flat rectangle projecting forward from the front-bottom edge.
        float brimW = 0.30f, brimLen = 0.22f, brimY = 0.01f;
        float frontZ = -d;
        drawBoxWireframe(buffer, entry, -brimW, brimY, frontZ - brimLen, brimW, brimY + 0.02f, frontZ, r, g, b, a);
    }

    private static void drawBoxWireframe(VertexConsumer buffer, MatrixStack.Entry entry,
                                          float minX, float minY, float minZ,
                                          float maxX, float maxY, float maxZ,
                                          float r, float g, float b, float a) {
        float[][] edges = {
                {minX, minY, minZ, maxX, minY, minZ}, {maxX, minY, minZ, maxX, minY, maxZ},
                {maxX, minY, maxZ, minX, minY, maxZ}, {minX, minY, maxZ, minX, minY, minZ},
                {minX, maxY, minZ, maxX, maxY, minZ}, {maxX, maxY, minZ, maxX, maxY, maxZ},
                {maxX, maxY, maxZ, minX, maxY, maxZ}, {minX, maxY, maxZ, minX, maxY, minZ},
                {minX, minY, minZ, minX, maxY, minZ}, {maxX, minY, minZ, maxX, maxY, minZ},
                {maxX, minY, maxZ, maxX, maxY, maxZ}, {minX, minY, maxZ, minX, maxY, maxZ},
        };
        for (float[] e : edges) {
            drawLine(buffer, entry, e[0], e[1], e[2], e[3], e[4], e[5], r, g, b, a);
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
