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

    // Accessory: cape presets, same self-view-only convention as the skin presets above.
    private static final String[] CAPE_PATHS = {
            "textures/entity/cape/red.png",
            "textures/entity/cape/blue.png",
            "textures/entity/cape/gold.png",
            "textures/entity/cape/rainbow.png"
    };

    // Accessory: elytra presets, same self-view-only convention as skin/cape above.
    private static final String[] ELYTRA_PATHS = {
            "textures/entity/elytra/violet.png",
            "textures/entity/elytra/crimson.png",
            "textures/entity/elytra/gold.png"
    };

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void impactvisuals$overrideSkin(CallbackInfoReturnable<SkinTextures> cir) {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if ((Object) this != client.player) return;

        SkinTextures original = cir.getReturnValue();

        Identifier skinTexture = original.texture();
        int skinIndex = cfg.selectedSkinIndex;
        if (skinIndex == 9) {
            skinTexture = Identifier.of("impactvisuals", "textures/entity/skins/custom.png");
        } else if (skinIndex >= 1 && skinIndex <= PRESET_PATHS.length) {
            skinTexture = Identifier.of("impactvisuals", PRESET_PATHS[skinIndex - 1]);
        }

        Identifier capeTexture = original.capeTexture();
        int capeIndex = cfg.selectedCapeIndex;
        if (capeIndex >= 1 && capeIndex <= CAPE_PATHS.length) {
            capeTexture = Identifier.of("impactvisuals", CAPE_PATHS[capeIndex - 1]);
        }

        Identifier elytraTexture = original.elytraTexture();
        int elytraIndex = cfg.selectedElytraIndex;
        if (elytraIndex >= 1 && elytraIndex <= ELYTRA_PATHS.length) {
            elytraTexture = Identifier.of("impactvisuals", ELYTRA_PATHS[elytraIndex - 1]);
        }

        SkinTextures.Model model = original.model();
        if (cfg.armModelIndex == 1) {
            model = SkinTextures.Model.SLIM;
        } else if (cfg.armModelIndex == 2) {
            model = SkinTextures.Model.WIDE;
        }

        if (skinTexture == original.texture() && capeTexture == original.capeTexture()
                && elytraTexture == original.elytraTexture() && model == original.model()) {
            return; // nothing overridden, leave vanilla result alone
        }

        SkinTextures replaced = new SkinTextures(skinTexture, null, capeTexture,
                elytraTexture, model, original.secure());
        cir.setReturnValue(replaced);
    }
}
