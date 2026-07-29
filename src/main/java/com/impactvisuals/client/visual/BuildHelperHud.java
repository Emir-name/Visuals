package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Pure QoL/visual helper for building - shows where a block would land if you
 * placed one right now, and a small readout of distance/coords/remaining
 * count. Doesn't place or automate anything; you still click yourself.
 */
public class BuildHelperHud {

    public static void renderWorld(WorldRenderContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.buildHelperEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        ItemStack held = client.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem)) return;

        BlockPos placePos = getPlacementPos((BlockHitResult) client.crosshairTarget, client.world);
        if (placePos == null) return;
        if (!client.world.getBlockState(placePos).isReplaceable()) return;

        Box box = new Box(placePos);
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        WorldRenderer.drawBox(matrices, consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines()),
                box, 0.71f, 0.4f, 1.0f, 0.85f);

        matrices.pop();
        consumers.draw();
    }

    public static void renderHud(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.buildHelperEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        ItemStack held = client.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem)) return;

        BlockPos placePos = getPlacementPos((BlockHitResult) client.crosshairTarget, client.world);
        if (placePos == null) return;

        double distance = client.player.getEyePos().distanceTo(Vec3d.ofCenter(placePos));

        String line1 = String.format("Place \u2022 %.1fm", distance);
        String line2 = placePos.getX() + ", " + placePos.getY() + ", " + placePos.getZ();
        String line3 = "x" + held.getCount();

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int cardW = 90;
        int cardH = 40;
        int x = screenW / 2 - cardW / 2;
        int y = screenH / 2 + 20;

        HudCard.draw(context, x, y, cardW, cardH);

        context.drawCenteredTextWithShadow(client.textRenderer, line1, x + cardW / 2, y + 5, 0xFFB266FF);
        context.drawCenteredTextWithShadow(client.textRenderer, line2, x + cardW / 2, y + 16, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, line3, x + cardW / 2, y + 27, 0xFFAAAAAA);
    }

    private static BlockPos getPlacementPos(BlockHitResult hit, net.minecraft.world.World world) {
        BlockPos hitPos = hit.getBlockPos();
        if (!world.getBlockState(hitPos).isReplaceable()) {
            return hitPos.offset(hit.getSide());
        }
        return hitPos;
    }
}

