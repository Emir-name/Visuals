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
     * just a plain black background with smoldering orange "stars", then the
     * normal buttons/widgets (via the base Screen implementation, which
     * knows nothing about TitleScreen's own panorama drawing), then the
     * existing fire-glow overlay and logo.
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

        // Solid accent panel BEHIND each button, drawn before the vanilla
        // button texture so it reads as a proper frame the button "sits in",
        // not a blurry glow - keeps it clearly readable against the black background.
        for (var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                drawButtonPanel(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }
        }

        // Base Screen behaviour only - draws every added widget (buttons, the
        // Friends button, etc.) and tooltips, without TitleScreen's own
        // panorama-drawing override running at all.
        super.render(context, mouseX, mouseY, delta);

        impactvisuals$drawTitleText(context, w);
        impactvisuals$fireOverlay(context, mouseX, mouseY, delta);

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

    private void impactvisuals$fireOverlay(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;

        int topColor = 0x882D0000;
        int bottomColor = 0xB3120000;
        context.fillGradient(0, 0, w, h / 3, topColor, 0x00000000);
        context.fillGradient(0, h - h / 3, w, h, 0x00000000, bottomColor);

        for (var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                drawButtonBorder(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }
        }

        int logoSize = 40;
        int logoX = w - logoSize - 8;
        int logoY = h - logoSize - 8;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                logoX, logoY, 0, 0, logoSize, logoSize, 256, 256, 256, 256);
    }

    /** A dark red-brown panel drawn just outside each button's bounds, before the vanilla texture draws on top - reads as a proper frame the button sits inside. */
    private void drawButtonPanel(DrawContext context, int x, int y, int w, int h) {
        int expand = 4;
        int px = x - expand;
        int py = y - expand;
        int pw = w + expand * 2;
        int ph = h + expand * 2;
        context.fill(px, py, px + pw, py + ph, 0xE82A0F05);
    }

    /** Crisp bright orange outline around the panel, drawn after the vanilla button so it's never covered. */
    private void drawButtonBorder(DrawContext context, int x, int y, int w, int h) {
        int expand = 4;
        int bx = x - expand;
        int by = y - expand;
        int bw = w + expand * 2;
        int bh = h + expand * 2;
        int color = 0xFFFF8C00;

        context.fill(bx, by, bx + bw, by + 2, color);
        context.fill(bx, by + bh - 2, bx + bw, by + bh, color);
        context.fill(bx, by, bx + 2, by + bh, color);
        context.fill(bx + bw - 2, by, bx + bw, by + bh, color);
    }
}
