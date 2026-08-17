package com.impactvisuals.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.WorldGenerationProgressTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the plain grey "Building terrain..." loading screen with the same
 * black+stars scene as the main menu, plus a text percentage readout instead
 * of the vanilla progress bar graphic.
 */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected LevelLoadingScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private WorldGenerationProgressTracker worldGenerationProgressTracker;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$replaceLoadingScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        context.fill(0, 0, w, h, 0xFF000000);
        StarfieldRenderer.draw(context, w, h);

        int progress = this.worldGenerationProgressTracker != null
                ? this.worldGenerationProgressTracker.getProgressPercentage()
                : 0;

        String title = "IMPACT VISUALS";
        int titleW = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, (w - titleW) / 2, h / 2 - 20, 0xFFFF8C00, true);

        String label = "Loading world... " + progress + "%";
        int labelW = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, label, (w - labelW) / 2, h / 2, 0xFFFFFFFF, true);

        int barW = 200;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 14;
        context.fill(barX, barY, barX + barW, barY + 4, 0x33FFFFFF);
        context.fill(barX, barY, barX + (barW * Math.max(0, Math.min(100, progress)) / 100), barY + 4, 0xFFFF8C00);

        ci.cancel();
    }
}
