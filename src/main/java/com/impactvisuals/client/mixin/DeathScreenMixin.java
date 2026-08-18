package com.impactvisuals.client.mixin;

import com.impactvisuals.client.visual.MenuButtonRenderer;
import com.impactvisuals.client.visual.StarfieldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla red vignette "You Died!" screen with the same black
 * background + smoldering stars as the main menu, and reskins the Respawn /
 * Title Screen buttons to match.
 */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected DeathScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    @Final
    private Text message;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$replaceDeathScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        context.fill(0, 0, w, h, 0xFF000000);
        StarfieldRenderer.draw(context, w, h);

        String titleText = "YOU DIED";
        var matrices = context.getMatrices();
        float scale = 2.5f;
        int titleW = this.textRenderer.getWidth(titleText);

        matrices.push();
        matrices.translate(w / 2f, h / 2f - 60, 0);
        matrices.scale(scale, scale, 1f);
        matrices.translate(-titleW / 2f, 0, 0);
        context.drawText(this.textRenderer, titleText, 1, 1, 0x60000000, false);
        context.drawText(this.textRenderer, titleText, 0, 0, 0xFFFF4433, false);
        matrices.pop();

        String cause = this.message != null ? this.message.getString() : "";
        if (!cause.isBlank()) {
            int causeW = this.textRenderer.getWidth(cause);
            context.drawText(this.textRenderer, cause, (w - causeW) / 2, h / 2 - 20, 0xFFCCCCCC, true);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String score = "Score: " + client.player.getScore();
            int scoreW = this.textRenderer.getWidth(score);
            context.drawText(this.textRenderer, score, (w - scoreW) / 2, h / 2 - 6, 0xFFFF8C00, true);
        }

        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getWidth() > 40) {
                MenuButtonRenderer.draw(context, this.textRenderer, button, mouseX, mouseY);
            } else if (child instanceof ClickableWidget widget) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }

        ci.cancel();
    }
}
