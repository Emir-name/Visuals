package com.impactvisuals.client.config;

import com.impactvisuals.client.visual.MenuButtonRenderer;
import com.impactvisuals.client.visual.StarfieldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Shown once, the very first time the player opens Impact Visuals - lets them
 * pick a starting HUD-scale preset for their device instead of guessing at
 * tiny/huge numbers themselves. Purely a starting point: everything it sets
 * can still be dragged/resized afterwards in the HUD editor.
 */
public class WelcomeScreen extends Screen {

    private final Screen parent;

    public WelcomeScreen(Screen parent) {
        super(Text.literal("Welcome"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int btnW = 180, btnH = 50;
        int gap = 24;
        int totalW = btnW * 2 + gap;
        int startX = this.width / 2 - totalW / 2;
        int y = this.height / 2 - btnH / 2 + 10;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\uD83D\uDCF1  " + Lang.t("Phone")), b -> apply(true))
                .dimensions(startX, y, btnW, btnH).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\uD83D\uDDA5  " + Lang.t("PC")), b -> apply(false))
                .dimensions(startX + btnW + gap, y, btnW, btnH).build());
    }

    private void apply(boolean phone) {
        // Phone screens are viewed from further away relative to their size and
        // hit with fingers, not a precise cursor, so HUD text/cards start
        // noticeably bigger. PC keeps the mod's original, more compact default.
        // Info HUD is already a dense multi-line block of text, so it gets a
        // smaller bump than the rest or it ends up oversized/overlapping.
        float scale = phone ? 1.15f : 1.0f;
        float infoHudScale = phone ? 1.0f : 1.0f;
        for (String id : HudLayoutManager.EDITABLE_HUDS.keySet()) {
            HudLayoutManager.setScale(id, id.equals("info_hud") ? infoHudScale : scale);
        }

        ModConfig cfg = ModConfig.get();
        cfg.setupComplete = true;
        cfg.save();

        MinecraftClient.getInstance().setScreen(new ConfigScreen(parent));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF000000);
        StarfieldRenderer.draw(context, this.width, this.height);

        String titleText = "IMPACT VISUALS";
        var matrices = context.getMatrices();
        float scale = 2.2f;
        int titleW = this.textRenderer.getWidth(titleText);
        matrices.push();
        matrices.translate(this.width / 2f, this.height / 2f - 80, 0);
        matrices.scale(scale, scale, 1f);
        matrices.translate(-titleW / 2f, 0, 0);
        context.drawText(this.textRenderer, titleText, 1, 1, 0x60000000, false);
        context.drawText(this.textRenderer, titleText, 0, 0, 0xFFFF8C1A, false);
        matrices.pop();

        String subtitle = Lang.t("Optimize the interface for:");
        int subW = this.textRenderer.getWidth(subtitle);
        context.drawText(this.textRenderer, Text.literal(subtitle).formatted(Formatting.GRAY),
                this.width / 2 - subW / 2, this.height / 2 - 30, 0xFFAAAAAA, false);

        for (var child : this.children()) {
            if (child instanceof ButtonWidget button) {
                MenuButtonRenderer.draw(context, this.textRenderer, button, mouseX, mouseY);
            }
        }

        String hint = Lang.t("You can change this anytime in the HUD editor");
        int hintW = this.textRenderer.getWidth(hint);
        context.drawText(this.textRenderer, hint, this.width / 2 - hintW / 2, this.height / 2 + 70, 0xFF777777, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
