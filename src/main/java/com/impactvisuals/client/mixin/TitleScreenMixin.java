package com.impactvisuals.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final Identifier LOGO_TEXTURE = Identifier.of("impactvisuals", "textures/gui/logo.png");

    @Inject(method = "render", at = @At("TAIL"))
    private void impactvisuals$fireOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        int width = self.width;
        int height = self.height;

        int topColor = 0x662D0000;
        int bottomColor = 0x99120000;
        context.fillGradient(0, 0, width, height / 2, topColor, 0x00000000);
        context.fillGradient(0, height / 2, width, height, 0x00000000, bottomColor);

        int logoSize = 64;
        int logoX = (width - logoSize) / 2;
        int logoY = 6;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                logoX, logoY, 0, 0, logoSize, logoSize, 256, 256, 256, 256);
    }
}
