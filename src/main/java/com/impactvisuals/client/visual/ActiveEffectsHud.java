package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class ActiveEffectsHud {

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.activeEffectsHudEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        var effects = client.player.getStatusEffects();
        if (effects.isEmpty()) return;

        int lineH = 12;
        int cardW = 160;
        int cardH = 20 + effects.size() * lineH;
        int x = 6 + com.impactvisuals.client.config.HudLayoutManager.getOffsetX("active_effects");
        int y = 60 + com.impactvisuals.client.config.HudLayoutManager.getOffsetY("active_effects");

        HudCard.draw(context, x, y, cardW, cardH);

        int textX = x + 8;
        int textY = y + 6;
        context.drawText(client.textRenderer, "Potions", textX, textY, 0xFFFF8C00, false);
        textY += 14;

        var spriteManager = client.getStatusEffectSpriteManager();

        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int amplifier = effect.getAmplifier();
            if (amplifier > 0) {
                name += " " + toRoman(amplifier + 1);
            }

            net.minecraft.client.texture.Sprite sprite = spriteManager.getSprite(effect.getEffectType());
            context.drawSpriteStretched(net.minecraft.client.render.RenderLayer::getGuiTextured, sprite, textX, textY - 2, 12, 12);

            String time = formatDuration(effect.getDuration());
            String line = name + "  " + time;
            context.drawText(client.textRenderer, line, textX + 16, textY, 0xFFFFFFFF, false);

            textY += lineH;
        }
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String toRoman(int number) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 0 && number < romans.length) return romans[number];
        return String.valueOf(number);
    }
}
