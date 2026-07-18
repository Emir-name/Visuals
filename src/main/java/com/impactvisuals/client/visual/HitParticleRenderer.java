package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HitParticleRenderer {

    private static final List<HitParticle> PARTICLES = new ArrayList<>();
    private static final float SIZE = 0.18f;

    public static void spawn(double x, double y, double z, float r, float g, float b) {
        if (!ModConfig.get().hitParticlesEnabled) return;
        PARTICLES.add(new HitParticle(x, y, z, r, g, b, ModConfig.get().hitParticleLifetimeSeconds));
    }

    public static void tick() {
        if (PARTICLES.isEmpty()) return;
        PARTICLES.removeIf(p -> !p.tick(0.05f));
    }

    public static void render(WorldRenderContext context) {
        if (!ModConfig.get().hitParticlesEnabled || PARTICLES.isEmpty()) return;

        Vec3d camPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        Vector3f right = new Vector3f(1, 0, 0).rotate(context.camera().getRotation());
        Vector3f up = new Vector3f(0, 1, 0).rotate(context.camera().getRotation());

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        var tessellator = Tessellator.getInstance();
        var buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f pose = matrices.peek().getPositionMatrix();

        for (HitParticle p : PARTICLES) {
            float alpha = p.alpha();
            if (alpha <= 0f) continue;
            drawQuad(buffer, pose, p, right, up, alpha);
        }

        matrices.pop();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawQuad(BufferBuilder buffer, Matrix4f pose, HitParticle p,
                                  Vector3f right, Vector3f up, float alpha) {
        float x = (float) p.x;
        float y = (float) p.renderY();
        float z = (float) p.z;

        Vector3f rOff = new Vector3f(right).mul(SIZE);
        Vector3f uOff = new Vector3f(up).mul(SIZE);

        Vector3f v1 = new Vector3f(x, y, z).sub(rOff).sub(uOff);
        Vector3f v2 = new Vector3f(x, y, z).add(rOff).sub(uOff);
        Vector3f v3 = new Vector3f(x, y, z).add(rOff).add(uOff);
        Vector3f v4 = new Vector3f(x, y, z).sub(rOff).add(uOff);

        buffer.vertex(pose, v1.x, v1.y, v1.z).color(p.r, p.g, p.b, alpha);
        buffer.vertex(pose, v2.x, v2.y, v2.z).color(p.r, p.g, p.b, alpha);
        buffer.vertex(pose, v3.x, v3.y, v3.z).color(p.r, p.g, p.b, alpha);
        buffer.vertex(pose, v4.x, v4.y, v4.z).color(p.r, p.g, p.b, alpha);
    }
}
