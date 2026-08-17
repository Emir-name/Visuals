package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Campfires (and soul campfires, which also extend CampfireBlock) spawn
 * their smoke/spark particles from randomDisplayTick, the same client-only
 * ambient-effect hook fire/portals/redstone ore use. Cancelling it here
 * stops the smoke without touching the campfire's actual behaviour (light,
 * cooking, damage) at all.
 */
@Mixin(CampfireBlock.class)
public class CampfireSmokeMixin {

    @Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$cancelSmoke(CallbackInfo ci) {
        if (ModConfig.get().hideCampfireSmoke) {
            ci.cancel();
        }
    }
}
