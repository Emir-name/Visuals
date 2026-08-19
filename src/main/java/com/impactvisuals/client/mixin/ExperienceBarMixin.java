package com.impactvisuals.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Recolors the XP bar (the thin green bar above the hotbar) to the mod's
 * accent orange, the same idea as the boss-bar recolor - replaces the
 * vanilla textured bar with a solid fill instead.
 */
@Mixin(InGameHud.class)
public abstract class ExperienceBarMixin {

    private static boolean errorReported = false;

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$recolorExpBar(DrawContext context, int x, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        try {
            int screenW = context.getScaledWindowWidth();
            int screenH = context.getScaledWindowHeight();
            int barW = 182;
            int barH = 5;
            int barX = screenW / 2 - barW / 2;
            int barY = screenH - 29;

            context.fill(barX, barY, barX + barW, barY + barH, 0xFF201510);

            float progress = client.player.experienceProgress;
            int filledW = (int) (barW * Math.max(0f, Math.min(1f, progress)));
            context.fill(barX, barY, barX + filledW, barY + barH, 0xFFFF8C00);

            context.fill(barX, barY, barX + barW, barY + 1, 0xFFFFC966);
            context.fill(barX, barY + barH - 1, barX + barW, barY + barH, 0xFF3A2410);

            if (client.player.experienceLevel > 0) {
                String level = String.valueOf(client.player.experienceLevel);
                int levelW = client.textRenderer.getWidth(level);
                int lx = screenW / 2 - levelW / 2;
                int ly = barY - 9;

                context.drawText(client.textRenderer, level, lx + 1, ly, 0xFF000000, false);
                context.drawText(client.textRenderer, level, lx - 1, ly, 0xFF000000, false);
                context.drawText(client.textRenderer, level, lx, ly + 1, 0xFF000000, false);
                context.drawText(client.textRenderer, level, lx, ly - 1, 0xFF000000, false);
                context.drawText(client.textRenderer, level, lx, ly, 0xFF7CFC46, false);
            }
        } catch (Throwable t) {
            // Surface the real cause instead of failing silently every frame -
            // this is temporary diagnostic output, not a permanent feature.
            if (!errorReported) {
                errorReported = true;
                System.err.println("[ImpactVisuals] ExperienceBarMixin failed: " + t);
                t.printStackTrace();
                if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal(
                            "\u00A7c[Impact Visuals] XP bar error: " + t), false);
                }
            }
        }

        ci.cancel();
    }
}
