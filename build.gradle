package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void impactvisuals$tintSky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.purpleSkyEnabled) return;

        int original = cir.getReturnValue();
        int alpha = original & 0xFF000000;
        int r = (original >> 16) & 0xFF;
        int g = (original >> 8) & 0xFF;
        int b = original & 0xFF;

        int targetR = 102, targetG = 0, targetB = 153;
        float blend = 0.55f;
        r = (int) (r * (1 - blend) + targetR * blend);
        g = (int) (g * (1 - blend) + targetG * blend);
        b = (int) (b * (1 - blend) + targetB * blend);

        int tinted = alpha | (r << 16) | (g << 8) | b;
        cir.setReturnValue(tinted);
    }
}
