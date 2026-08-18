package com.impactvisuals.client.mixin;

import com.impactvisuals.client.friends.FriendsScreen;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.visual.MenuButtonRenderer;
import com.impactvisuals.client.visual.StarfieldRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends net.minecraft.client.gui.screen.Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    private static final Identifier LOGO_TEXTURE = Identifier.of("impactvisuals", "textures/gui/logo.png");

    @Inject(method = "init", at = @At("TAIL"))
    private void impactvisuals$addFriendsButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Друзья"), btn ->
                        this.client.setScreen(new FriendsScreen(this)))
                .dimensions(8, 8, 90, 20).build());
    }

    /**
     * Replaces the vanilla main menu render entirely: no rotating panorama,
     * a plain black background with smoldering orange "stars", then every
     * button drawn with our own fire-themed fill/border/text (not the
     * vanilla grey button texture at all), then the title and glow overlay.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$replaceBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        context.fill(0, 0, w, h, 0xFF000000);
        StarfieldRenderer.draw(context, w, h);

        impactvisuals$drawTitleText(context, w);

        // Draw every button completely ourselves (fill + border + label) -
        // NOT the vanilla grey texture - so we skip calling super.render()
        // entirely and instead do our own pass over the widget list.
        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getWidth() > 40) {
                MenuButtonRenderer.draw(context, this.textRenderer, button, mouseX, mouseY);
            } else if (child instanceof ClickableWidget widget) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }

        impactvisuals$fireOverlay(context, w, h);

        ci.cancel();
    }

    /** The vanilla pixel-art MINECRAFT logo texture is drawn inside the part of
     * render() we skip - replace it with a plain scaled-up text title instead
     * of guessing the vanilla texture identifiers again. */
    private void impactvisuals$drawTitleText(DrawContext context, int w) {
        var matrices = context.getMatrices();
        String title = "MINECRAFT";
        float scale = 3.5f;
        int textW = this.textRenderer.getWidth(title);

        matrices.push();
        matrices.translate(w / 2f, 38, 0);
        matrices.scale(scale, scale, 1f);
        matrices.translate(-textW / 2f, 0, 0);
        context.drawText(this.textRenderer, title, 1, 1, 0x60000000, false);
        context.drawText(this.textRenderer, title, 0, 0, 0xFFFFFFFF, false);
        matrices.pop();

        String subtitle = "IMPACT VISUALS";
        int subW = this.textRenderer.getWidth(subtitle);
        context.drawText(this.textRenderer, subtitle, (w - subW) / 2, 38 + (int) (10 * scale) + 6, 0xFFFF8C00, true);
    }

    private void impactvisuals$fireOverlay(DrawContext context, int w, int h) {
        int topColor = 0x882D0000;
        int bottomColor = 0xB3120000;
        context.fillGradient(0, 0, w, h / 3, topColor, 0x00000000);
        context.fillGradient(0, h - h / 3, w, h, 0x00000000, bottomColor);

        int logoSize = 40;
        int logoX = w - logoSize - 8;
        int logoY = h - logoSize - 8;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                logoX, logoY, 0, 0, logoSize, logoSize, 256, 256, 256, 256);
    }

    /** Draws one button entirely ourselves - solid dark-red/black fill (brighter on hover), a
     * bright orange border, and the label - instead of letting the vanilla grey button texture
     * render, so buttons never blend into the black starfield background. */
}
