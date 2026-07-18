package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class DamageNumberRenderer {

    private static final List<DamageNumber> NUMBERS = new ArrayList<>();
    private static final float TEXT_SCALE = 0.025f;

    public static void spawn(double x, double y, double z, float amount, boolean critical) {
        if (!ModConfig.get().damageNumbersEnabled) return;
        NUMBERS.add(new DamageNumber(x, y, z, amount, critical, ModConfig.get().damageNumberLifetimeSeconds));
    }

    public static void tick() {
        if (NUMBERS.isEmpty()) return;
        NUMBERS.removeIf(n -> !n.tick(0.05f));
    }

    public static void render(WorldRenderContext context) {
        if (!ModConfig.get().damageNumbersEnabled || NUMBERS.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();
        Quaternionf camRotation = context.camera().getRotation();

        for (DamageNumber n : NUMBERS) {
            float alpha = n.alpha();
            if (alpha <= 0f) continue;

            matrices.push();
            matrices.translate(n.x - camPos.x, n.renderY() - camPos.y, n.z - camPos.z);
            matrices.multiply(camRotation);
            matrices.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

            int color = n.critical ? 0xFFFF4444 : 0xFFFFFFFF;
            int alphaInt = Math.round(alpha * 255f) << 24;
            int argb = (color & 0x00FFFFFF) | alphaInt;

            String text = n.text();
            float width = textRenderer.getWidth(text);

            textRenderer.draw(
                    text,
                    -width / 2f,
                    0f,
                    argb,
                    false,
                    matrices.peek().getPositionMatrix(),
                    consumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    0xF000000
            );

            matrices.pop();
        }

        consumers.draw();
    }
}
