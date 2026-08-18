package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Pure QoL/visual helper for building - shows a translucent "hologram" of the
 * exact block state that would be placed right now (correct stair
 * half/facing, shulker box direction, slab top/bottom, etc.), using
 * Minecraft's own placement logic instead of guessing, plus a wireframe
 * outline and a small readout of distance/coords/remaining count. Doesn't
 * place or automate anything - you still click yourself.
 */
public class BuildHelperHud {

    public static void renderWorld(WorldRenderContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.buildHelperEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        ItemStack held = client.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem blockItem)) return;

        BlockHitResult hit = (BlockHitResult) client.crosshairTarget;

        ItemPlacementContext placementContext = buildPlacementContext(client, blockItem, held, hit);
        if (placementContext == null) return;

        BlockPos placePos = placementContext.getBlockPos();
        if (placePos == null || !client.world.getBlockState(placePos).isReplaceable()) return;

        BlockState previewState = resolvePlacementState(blockItem, placementContext);
        if (previewState == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();

        // 1) The actual block model, correctly oriented, drawn translucent so it
        // reads as a preview/hologram rather than a real placed block.
        matrices.push();
        matrices.translate(placePos.getX() - camPos.x, placePos.getY() - camPos.y, placePos.getZ() - camPos.z);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.55f);

        int fullBrightLight = LightmapTextureManager.pack(15, 15);
        client.getBlockRenderManager().renderBlockAsEntity(previewState, matrices, consumers, fullBrightLight, OverlayTexture.DEFAULT_UV);
        consumers.draw();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();

        matrices.pop();

        // 2) A thin accent outline around the same spot, for visibility against busy backgrounds.
        Box box = new Box(placePos);
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        drawBoxOutline(matrices, consumers.getBuffer(net.minecraft.client.render.RenderLayer.getLines()),
                box, 0.71f, 0.4f, 1.0f, 0.85f);

        matrices.pop();
        consumers.draw();
    }

    /**
     * Builds the exact same ItemPlacementContext vanilla uses when you actually
     * click to place - its own getBlockPos() correctly handles replaceable/partial
     * blocks (snow layers, farmland, etc.) instead of us re-deriving position by hand.
     */
    private static ItemPlacementContext buildPlacementContext(MinecraftClient client, BlockItem blockItem,
                                                                ItemStack stack, BlockHitResult hit) {
        try {
            ItemUsageContext useContext = new ItemUsageContext(client.world, client.player, Hand.MAIN_HAND, stack, hit);
            return new ItemPlacementContext(useContext);
        } catch (Exception e) {
            return null;
        }
    }

    /** Asks the block itself how it would orient given the real placement context (facing, half, waterlogged, etc.). */
    private static BlockState resolvePlacementState(BlockItem blockItem, ItemPlacementContext placementContext) {
        try {
            BlockState state = blockItem.getBlock().getPlacementState(placementContext);
            return state != null ? state : blockItem.getBlock().getDefaultState();
        } catch (Exception e) {
            return blockItem.getBlock().getDefaultState();
        }
    }

    /**
     * Manually draws the 12 edges of a box as line segments. Written by hand instead
     * of relying on WorldRenderer's built-in box helper, since that method's exact
     * signature shifts between Minecraft versions and isn't worth chasing here.
     */
    private static void drawBoxOutline(MatrixStack matrices, net.minecraft.client.render.VertexConsumer buffer,
                                        Box box, float r, float g, float b, float a) {
        var entry = matrices.peek();
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        float[][] edges = {
                {minX, minY, minZ, maxX, minY, minZ}, {maxX, minY, minZ, maxX, minY, maxZ},
                {maxX, minY, maxZ, minX, minY, maxZ}, {minX, minY, maxZ, minX, minY, minZ},
                {minX, maxY, minZ, maxX, maxY, minZ}, {maxX, maxY, minZ, maxX, maxY, maxZ},
                {maxX, maxY, maxZ, minX, maxY, maxZ}, {minX, maxY, maxZ, minX, maxY, minZ},
                {minX, minY, minZ, minX, maxY, minZ}, {maxX, minY, minZ, maxX, maxY, minZ},
                {maxX, minY, maxZ, maxX, maxY, maxZ}, {minX, minY, maxZ, minX, maxY, maxZ},
        };

        for (float[] e : edges) {
            float dx = e[3] - e[0], dy = e[4] - e[1], dz = e[5] - e[2];
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0e-6f) continue;
            dx /= len; dy /= len; dz /= len;

            buffer.vertex(entry.getPositionMatrix(), e[0], e[1], e[2])
                    .color(r, g, b, a)
                    .normal(entry, dx, dy, dz);
            buffer.vertex(entry.getPositionMatrix(), e[3], e[4], e[5])
                    .color(r, g, b, a)
                    .normal(entry, dx, dy, dz);
        }
    }

    public static void renderHud(DrawContext context) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.buildHelperEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        ItemStack held = client.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem blockItem)) return;

        BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
        ItemPlacementContext placementContext = buildPlacementContext(client, blockItem, held, hit);
        if (placementContext == null) return;

        BlockPos placePos = placementContext.getBlockPos();
        if (placePos == null) return;

        double distance = client.player.getEyePos().distanceTo(Vec3d.ofCenter(placePos));

        String line1 = String.format("Place \u2022 %.1fm", distance);
        String line2 = placePos.getX() + ", " + placePos.getY() + ", " + placePos.getZ();
        String line3 = "x" + held.getCount();

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int cardW = 90;
        int cardH = 40;
        int x = screenW / 2 - cardW / 2 + com.impactvisuals.client.config.HudLayoutManager.getOffsetX("build_helper_hud");
        int y = screenH / 2 + 20 + com.impactvisuals.client.config.HudLayoutManager.getOffsetY("build_helper_hud");
        com.impactvisuals.client.config.HudLayoutManager.pushTransform(context, "build_helper_hud", x, y);

        HudCard.draw(context, x, y, cardW, cardH);

        context.drawCenteredTextWithShadow(client.textRenderer, line1, x + cardW / 2, y + 5, 0xFFB266FF);
        context.drawCenteredTextWithShadow(client.textRenderer, line2, x + cardW / 2, y + 16, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, line3, x + cardW / 2, y + 27, 0xFFAAAAAA);
        com.impactvisuals.client.config.HudLayoutManager.popTransform(context);
    }
}
