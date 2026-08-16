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

    @Inject(method = "render", at = @At("HEAD"))
    private void impactvisuals$blackStarBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        context.fill(0, 0, w, h, 0xFF000000);

        long now = System.currentTimeMillis();
        for (float[] star : STARS) {
            float t = (now / 1000f) * star[3] + star[2];
            float pulse = (float) (Math.sin(t) * 0.5 + 0.5); // 0..1 "smoldering" brightness

            int size = pulse > 0.75f ? 2 : 1;
            int brightness = (int) (140 + pulse * 115); // dim ember to bright orange
            int alpha = (int) (140 + pulse * 115);
            int color = (alpha << 24) | (brightness << 16) | (Math.min(140, brightness / 2) << 8);

            int sx = (int) (star[0] * w);
            int sy = (int) (star[1] * h);
            context.fill(sx, sy, sx + size, sy + size, color);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void impactvisuals$addFriendsButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Друзья"), btn ->
                        this.client.setScreen(new FriendsScreen(this)))
                .dimensions(8, 8, 90, 20).build());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void impactvisuals$fireOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        int topColor = 0x882D0000;
        int bottomColor = 0xB3120000;
        context.fillGradient(0, 0, w, h / 3, topColor, 0x00000000);
        context.fillGradient(0, h - h / 3, w, h, 0x00000000, bottomColor);

        for (var child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                drawGlow(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }
        }

        int logoSize = 40;
        int logoX = w - logoSize - 8;
        int logoY = h - logoSize - 8;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                logoX, logoY, 0, 0, logoSize, logoSize, 256, 256, 256, 256);
    }

    private void drawGlow(DrawContext context, int x, int y, int w, int h) {
        int[] alphas = {170, 110, 60};
        for (int i = 0; i < alphas.length; i++) {
            int expand = i + 1;
            int color = (alphas[i] << 24) | 0xFF8C00;
            int gx = x - expand;
            int gy = y - expand;
            int gw = w + expand * 2;
            int gh = h + expand * 2;

            context.fill(gx, gy, gx + gw, gy + 1, color);
            context.fill(gx, gy + gh - 1, gx + gw, gy + gh, color);
            context.fill(gx, gy, gx + 1, gy + gh, color);
            context.fill(gx + gw - 1, gy, gx + gw, gy + gh, color);
        }
    }
}
