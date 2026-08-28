package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the sky angle (in radians) used to position the sun/moon and
 * drive the sky colour gradient and star visibility, letting the player
 * force day or night purely visually. This is a client-only cosmetic
 * override: it does NOT change the real world time, so mob spawning, crop
 * growth, sleeping, and anything else server-authoritative behave exactly
 * as they normally would - only what you personally see changes.
 *
 * Declared on the World base class (getSkyAngleRadians), not ClientWorld -
 * ClientWorld inherits it without overriding.
 */
@Mixin(World.class)
public class FakeTimeMixin {

    @Inject(method = "getSkyAngleRadians", at = @At("RETURN"), cancellable = true)
    private void impactvisuals$fakeTime(float tickDelta, CallbackInfoReturnable<Float> cir) {
        ModConfig cfg = ModConfig.get();
        // 0 = Auto (real time), 1 = Day, 2 = Night
        // A full day/night cycle is 2*PI radians; 0 = sun directly overhead (noon),
        // PI = sun directly below (midnight, moon up).
        if (cfg.fakeTimeMode == 1) {
            cir.setReturnValue(0.0f);
        } else if (cfg.fakeTimeMode == 2) {
            cir.setReturnValue((float) Math.PI);
        }
    }
}
