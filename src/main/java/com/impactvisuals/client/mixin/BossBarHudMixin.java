package com.impactvisuals.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

/**
 * Replaces the vanilla boss-bar texture (which draws a different colour
 * depending on the boss's own BossBar.Color, e.g. pink for the Wither,
 * purple/pink for the Ender Dragon) with a single solid theme-accent bar,
 * so every boss fight matches Impact Visuals' orange/ember look instead.
 */
@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Shadow
    private Map<UUID, ClientBossBar> bossBars;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void impactvisuals$replaceBossBar(DrawContext context, CallbackInfo ci) {
        if (bossBars == null || bossBars.isEmpty()) return;

        int screenW = context.getScaledWindowWidth();
        int y = 12;

        for (ClientBossBar bossBar : bossBars.values()) {
            int barW = 182;
            int barH = 5;
            int x = screenW / 2 - barW / 2;

            context.fill(x, y, x + barW, y + barH, 0xFF201510);

            float pct = Math.max(0f, Math.min(1f, bossBar.getPercent()));
            int filledW = (int) (barW * pct);
            context.fill(x, y, x + filledW, y + barH, 0xFFFF8C00);

            context.fill(x, y, x + barW, y + 1, 0xFFFFC966);
            context.fill(x, y + barH - 1, x + barW, y + barH, 0xFF3A2410);
            context.fill(x, y, x + 1, y + barH, 0xFFFFC966);
            context.fill(x + barW - 1, y, x + barW, y + barH, 0xFF3A2410);

            Text name = bossBar.getName();
            var client = net.minecraft.client.MinecraftClient.getInstance();
            int nameW = client.textRenderer.getWidth(name);
            context.drawText(client.textRenderer, name, screenW / 2 - nameW / 2, y - 10, 0xFFFFFFFF, true);

            y += barH + 18;
        }

        ci.cancel();
    }
}
