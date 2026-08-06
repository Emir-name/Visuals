package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class SkyColorMixin {

    @ModifyVariable(method = "getSkyColor", at = @At("RETURN"), ordinal = 0)
    private Vec3d impactvisuals$modifySkyColor(Vec3d original) {
        if (ModConfig.get().cosmetic.purpleSkyEnabled) {
            return new Vec3d(0.6, 0.1, 0.9);
        }
        return original;
    }
}
