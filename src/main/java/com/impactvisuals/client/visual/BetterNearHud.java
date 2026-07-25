package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BetterNearHud {

    private static final double RANGE = 50.0;
    private static final int MAX_ROWS = 5;

    public static void render(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.betterNearEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        List<AbstractClientPlayerEntity> nearby = client.world.getPlayers().stream()
                .filter(p -> p != client.player)
                .filter(p -> p.distanceTo(client.player) <= RANGE)
                .sorted(Comparator.comparingDouble(p -> p.distanceTo(client.player)))
                .limit(MAX_ROWS)
                .collect(Collectors.toList());

        if (nearby.isEmpty()) return;

        int lineH = 16;
        int cardW = 150;
        int cardH = 20 + nearby.size() * lineH;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        int x = screenW - cardW - 6;
        int y = screenH - cardH - 30;

        HudCard.draw(context, x, y, cardW, cardH);

        int textX = x + 8;
        int textY = y + 6;
        context.drawText(client.textRenderer, "Better Near", textX, textY, 0xFFB266FF, false);
        textY += 14;

        for (AbstractClientPlayerEntity p : nearby) {
            int rowY = textY;

            net.minecraft.client.gui.PlayerSkinDrawer.draw(context, p.getSkinTextures(), textX, rowY, 12);

            String name = p.getGameProfile().getName();
            context.drawText(client.textRenderer, name, textX + 16, rowY + 2, 0xFFFFFFFF, false);

            int distance = (int) p.distanceTo(client.player);
            String distText = distance + "m";
            int distWidth = client.textRenderer.getWidth(distText);
            context.drawText(client.textRenderer, distText, x + cardW - 24 - distWidth, rowY + 2, 0xFFAAAAAA, false);

            String arrow = getRelativeArrow(client.player, p);
            context.drawText(client.textRenderer, arrow, x + cardW - 16, rowY + 2, 0xFFB266FF, false);

            textY += lineH;
        }
    }

    private static String getRelativeArrow(PlayerEntity self, PlayerEntity other) {
        double dx = other.getX() - self.getX();
        double dz = other.getZ() - self.getZ();
        double angleToTarget = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double relative = MathHelper.wrapDegrees(angleToTarget - self.getYaw());

        if (relative > -45 && relative <= 45) return "\u2191";
        if (relative > 45 && relative <= 135) return "\u2192";
        if (relative > 135 || relative <= -135) return "\u2193";
        return "\u2190";
    }
}
