package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * A single user-placed waypoint. Type in X/Y/Z in the Markers tab and a box
 * outline is drawn at that block, with a small distance readout. Doesn't
 * interact with any other feature (Target HUD, Focus Target, etc.) - fully
 * independent.
 */
public class MarkerHud {

    public static void renderWorld(WorldRenderContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.markerEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        BlockPos pos = new BlockPos(cfg.markerX, cfg.markerY, cfg.markerZ);
        Box box = new Box(pos);

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        drawBoxOutline(matrices, consumers.getBuffer(RenderLayer.getLines()),
                box, 1.0f, 0.85f, 0.2f, 0.9f);

        matrices.pop();
        consumers.draw();
    }

    public static void renderHud(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.markerEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Vec3d markerCenter = new Vec3d(cfg.markerX + 0.5, cfg.markerY + 0.5, cfg.markerZ + 0.5);
        double distance = client.player.getPos().distanceTo(markerCenter);

        String name = cfg.markerName == null || cfg.markerName.isBlank() ? "Marker" : cfg.markerName;
        String line1 = name;
        String line2 = String.format("%.1fm \u2022 %d, %d, %d", distance, cfg.markerX, cfg.markerY, cfg.markerZ);

        int screenW = context.getScaledWindowWidth();

        int cardW = 130;
        int cardH = 34;
        int x = screenW / 2 - cardW / 2;
        int y = 10;

        HudCard.draw(context, x, y, cardW, cardH);
        context.drawCenteredTextWithShadow(client.textRenderer, line1, x + cardW / 2, y + 6, 0xFFFFD633);
        context.drawCenteredTextWithShadow(client.textRenderer, line2, x + cardW / 2, y + 18, 0xFFAAAAAA);
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

