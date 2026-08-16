package com.impactvisuals.client.mixin;

import com.impactvisuals.client.friends.FriendsScreen;
import com.impactvisuals.client.config.ModConfig;
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

    private static final int STAR_COUNT = 140;
    private static final float[][] STARS = generateStars();

    private static float[][] generateStars() {
        java.util.Random random = new java.util.Random(20260816L);
        float[][] stars = new float[STAR_COUNT][4];
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i][0] = random.nextFloat();               // x (0-1 of screen width)
            stars[i][1] = random.nextFloat();               // y (0-1 of screen height)
            stars[i][2] = random.nextFloat() * 6.283f;       // phase offset for the "smoldering" pulse
            stars[i][3] = 0.6f + random.nextFloat() * 1.4f;  // pulse speed
        }
        return stars;
    }

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

        long now = System.currentTimeMillis();
        for (float[] star : STARS) {
            float t = (now / 1000f) * star[3] + star[2];
            float pulse = (float) (Math.sin(t) * 0.5 + 0.5); // 0..1 "smoldering" brightness

            // Tiny drift so stars don't sit perfectly still - like heat shimmer off embers.
            int jitterX = (int) (Math.sin(t * 1.7) * 0.6);
            int jitterY = (int) (Math.cos(t * 1.3) * 0.6);

            int size = pulse > 0.75f ? 2 : 1;
            int brightness = (int) (140 + pulse * 115); // dim ember to bright orange
            int alpha = (int) (140 + pulse * 115);
            int color = (alpha << 24) | (brightness << 16) | (Math.min(140, brightness / 2) << 8);

            int sx = (int) (star[0] * w) + jitterX;
            int sy = (int) (star[1] * h) + jitterY;
            context.fill(sx, sy, sx + size, sy + size, color);

            // Brightest embers get a faint cross-shaped twinkle flare.
            if (pulse > 0.88f) {
                int flareAlpha = (int) ((pulse - 0.88f) / 0.12f * 90);
                int flareColor = (flareAlpha << 24) | 0xFFAA33;
                context.fill(sx - 3, sy, sx - 1, sy + 1, flareColor);
                context.fill(sx + size, sy, sx + size + 2, sy + 1, flareColor);
                context.fill(sx, sy - 3, sx + 1, sy - 1, flareColor);
                context.fill(sx, sy + size, sx + 1, sy + size + 2, flareColor);
            }
        }

        impactvisuals$drawTitleText(context, w);

        // Draw every button completely ourselves (fill + border + label) -
        // NOT the vanilla grey texture - so we skip calling super.render()
        // entirely and instead do our own pass over the widget list.
        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getWidth() > 40) {
                drawCustomButton(context, button, mouseX, mouseY);
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
    private void drawCustomButton(DrawContext context, ButtonWidget button, int mouseX, int mouseY) {
        int x = button.getX();
        int y = button.getY();
        int w = button.getWidth();
        int h = button.getHeight();
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean active = button.active;

        // Warm charcoal base (close to the background tone, not a separate red
        // block) with a thin glowing orange edge - reads as part of the same
        // dark scene while the bright border keeps it from disappearing.
        int fillColor = !active ? 0xFF141210 : hovered ? 0xFF2A211A : 0xFF1B1613;
        int borderColor = !active ? 0xFF4A4A4A : hovered ? 0xFFFFC966 : 0xFFCC6A1A;
        int textColor = !active ? 0xFF808080 : 0xFFFFFFFF;

        context.fill(x, y, x + w, y + h, fillColor);

        // Soft outer glow, one pixel wider than the crisp border, so the edge
        // feels like it's lit rather than just outlined.
        if (active) {
            int glow = hovered ? 0x55FF8C1A : 0x33FF8C1A;
            context.fill(x - 1, y - 1, x + w + 1, y, glow);
            context.fill(x - 1, y + h, x + w + 1, y + h + 1, glow);
            context.fill(x - 1, y, x, y + h, glow);
            context.fill(x + w, y, x + w + 1, y + h, glow);
        }

        context.fill(x, y, x + w, y + 1, borderColor);
        context.fill(x, y + h - 1, x + w, y + h, borderColor);
        context.fill(x, y, x + 1, y + h, borderColor);
        context.fill(x + w - 1, y, x + w, y + h, borderColor);

        String label = this.textRenderer.trimToWidth(button.getMessage().getString(), Math.max(0, w - 6));
        int textW = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, label, x + (w - textW) / 2, y + (h - 8) / 2, textColor, true);
    }
}
