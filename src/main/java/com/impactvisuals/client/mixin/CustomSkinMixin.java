package com.impactvisuals.client.mixin;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class CustomSkinMixin {

    private static final String[] PRESET_PATHS = {
            "textures/entity/skins/preset1.png",
            "textures/entity/skins/preset2.png",
            "textures/entity/skins/preset3.png",
            "textures/entity/skins/preset4.png",
            "textures/entity/skins/preset5.png",
            "textures/entity/skins/preset6.png",
            "textures/entity/skins/preset7.png",
            "textures/entity/skins/preset8.png"
    };

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void impactvisuals$overrideSkin(CallbackInfoReturnable<SkinTextures> cir) {
        ModConfig cfg = ModConfig.get();
        int index = cfg.selectedSkinIndex;
        if (index <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if ((Object) this != client.player) return;

        Identifier customTexture;
        if (index == 9) {
            customTexture = Identifier.of("impactvisuals", "textures/entity/skins/custom.png");
        } else if (index >= 1 && index <= PRESET_PATHS.length) {
            customTexture = Identifier.of("impactvisuals", PRESET_PATHS[index - 1]);
        } else {
            return;
        }

        SkinTextures original = cir.getReturnValue();
        SkinTextures replaced = new SkinTextures(customTexture, null, original.capeTexture(),
                original.elytraTexture(), original.model(), original.secure());
        cir.setReturnValue(replaced);
    }
}
