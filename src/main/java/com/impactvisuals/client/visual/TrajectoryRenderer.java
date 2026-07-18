package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TrajectoryRenderer {

    private static final int STEPS = 40;

    public static void render(WorldRenderContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.trajectoryPredictionEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        var stack = player.getMainHandStack();
        boolean throwable = stack.isOf(Items.SNOWBALL) || stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.EGG);
        boolean bow = stack.isOf(Items.BOW) && player.isUsingItem();

        if (!throwable && !bow) return;

        Vec3d camPos = context.camera().getPos();
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d look = player.getRotationVec(1.0f);

        float speed = throwable ? 1.5f : 3.0f;
        float gravity = throwable ? 0.03f : 0.05f;
        float drag = 0.99f;

        Vec3d velocity = look.multiply(speed / 20.0);

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);

        var tessellator = Tessellator.getInstance();
        var buffer = tessellator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f pose = matrices.peek().getPositionMatrix();

        Vec3d pos = start;
        Vec3d vel = velocity;

        for (int i = 0; i <= STEPS; i++) {
            float alpha = 1.0f - ((float) i / STEPS) * 0.8f;
            buffer.vertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                    .color(1.0f, 0.85f, 0.2f, alpha);

            vel = new Vec3d(vel.x * drag, vel.y * drag - gravity / 20.0, vel.z * drag);
            pos = pos.add(vel);

            if (client.world != null && !client.world.isAir(net.minecraft.util.math.BlockPos.ofFloored(pos))) {
                break;
            }
        }

        matrices.pop();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
}
