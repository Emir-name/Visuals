package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void impactvisuals$applyCustomHandle(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                                   Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                                   MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                   int light, CallbackInfo ci) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.customHandleEnabled) return;

        float scale = cfg.customHandleScalePercent / 100f;

        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cfg.customHandleRotX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cfg.customHandleRotY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(cfg.customHandleRotZ));
        matrices.translate(-0.5, -0.5, -0.5);
    }
}
