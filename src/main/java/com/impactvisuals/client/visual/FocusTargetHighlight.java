package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a colored box outline around a specific named player, typed into the
 * "Focus Target" field, so they're easy to spot in a crowd during a fight.
 * Only draws while you can actually see them (raycast check) - this is not
 * an ESP/wallhack, it doesn't reveal position through blocks.
 */
public class FocusTargetHighlight {

    public static void render(WorldRenderContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.focusTargetEnabled) return;

        String targetName = cfg.focusTargetName;
        if (targetName == null || targetName.isBlank()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        AbstractClientPlayerEntity target = null;
        for (var entry : client.world.getPlayers()) {
            if (entry.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                target = entry;
                break;
            }
        }
        if (target == null || target == client.player) return;
        if (!isVisible(client, target)) return;

        Box box = target.getBoundingBox().expand(0.05);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        drawBoxOutline(matrices, consumers.getBuffer(RenderLayer.getLines()), box, 1.0f, 0.25f, 0.25f, 0.9f);

        matrices.pop();
        consumers.draw();
    }

    /** Simple raycast from the camera to the target's eyes - blocked by any solid block. */
    private static boolean isVisible(MinecraftClient client, Entity target) {
        Entity camera = client.cameraEntity;
        if (camera == null) return false;

        Vec3d from = camera.getCameraPosVec(1.0f);
        Vec3d to = target.getEyePos();

        HitResult hit = client.world.raycast(new net.minecraft.world.RaycastContext(
                from, to,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                camera
        ));

        return hit == null || hit.getType() == HitResult.Type.MISS;
    }

    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer buffer,
                                        Box box, float r, float g, float b, float a) {
        var entry = matrices.peek();
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        float[][] edges = {
                {minX, minY, minZ, maxX, minY, minZ}, {maxX, minY, minZ, maxX, minY, maxZ},
                {maxX, minY, maxZ, minX, minY, maxZ}, {minX, minY, maxZ, minX, minY, minZ},
                {minX, maxY, minZ, maxX, maxY, minZ}, {maxX, maxY, minZ, maxX, maxY, maxZ},
                {maxX, maxY, maxZ, minX, maxY, maxZ}, {minX, maxY, maxZ, minX, maxY, minZ},
                {minX, minY, minZ, minX, maxY, minZ}, {maxX, minY, minZ, maxX, maxY, minZ},
                {maxX, minY, maxZ, maxX, maxY, maxZ}, {minX, minY, maxZ, minX, maxY, maxZ},
        };

        for (float[] e : edges) {
            float dx = e[3] - e[0], dy = e[4] - e[1], dz = e[5] - e[2];
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0e-6f) continue;
            dx /= len; dy /= len; dz /= len;

            buffer.vertex(entry.getPositionMatrix(), e[0], e[1], e[2]).color(r, g, b, a).normal(entry, dx, dy, dz);
            buffer.vertex(entry.getPositionMatrix(), e[3], e[4], e[5]).color(r, g, b, a).normal(entry, dx, dy, dz);
        }
    }
}

