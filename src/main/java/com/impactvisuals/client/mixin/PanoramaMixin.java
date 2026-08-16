package com.impactvisuals.client.mixin;

import net.minecraft.client.gui.screen.RotatingCubeMapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops the main menu's rotating panorama cube from drawing at all. Paired
 * with TitleScreenMixin, which paints our own black+stars background at the
 * very start of TitleScreen.render() - since the panorama is the thing that
 * would otherwise draw over it every frame, silencing it here is what makes
 * our background actually stick instead of being immediately overwritten.
 */
@Mixin(RotatingCubeMapRenderer.class)
public class PanoramaMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$cancelPanorama(CallbackInfo ci) {
        ci.cancel();
    }
}

