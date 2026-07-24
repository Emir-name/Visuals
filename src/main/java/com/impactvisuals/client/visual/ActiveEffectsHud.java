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

        int x = 6;
        int y = 60;
        int lineH = 12;

        context.drawText(client.textRenderer, "Potions", x, y, 0xFFFF8C00, true);
        y += 12;

        for (StatusEffectInstance effect : effects) {
            String name = effect.getEffectType().value().getName().getString();
            int amplifier = effect.getAmplifier();
            if (amplifier > 0) {
                name += " " + toRoman(amplifier + 1);
            }

            int color = effect.getEffectType().value().getColor() | 0xFF000000;
            context.fill(x, y + 2, x + 6, y + 8, color);

            String time = formatDuration(effect.getDuration());
            String line = name + "  " + time;
            context.drawText(client.textRenderer, line, x + 10, y, 0xFFFFFFFF, true);

            y += lineH;
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
