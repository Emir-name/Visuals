package com.impactvisuals.client.visual;

import com.impactvisuals.client.network.FirebasePresence;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Draws whichever hat cosmetic each player has selected (Skins tab: None /
 * China Hat / Ushanka / Cap, plus a colour). Each hat is built out of small
 * solid coloured blocks (the same technique BuildHelperHud uses for its
 * hologram) so it's a real filled shape, not a wireframe outline. Not
 * self-view-only like skin/cape/elytra - the chosen hat + colour are
 * broadcast through Firebase presence so every other Impact Visuals user
 * nearby actually sees it on you, the same way the Jump Ring works.
 */
public class HatRenderer {

    public static final int NONE = 0;
    public static final int CHINA_HAT = 1;
    public static final int USHANKA = 2;
    public static final int CAP = 3;

    private static final Block[] CONCRETE_BY_COLOR = {
            Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
            Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
            Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE
    };

    private static final java.util.Set<String> notifiedHats = new java.util.HashSet<>();

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d camPos = context.camera().getPos();
        int fullBrightLight = LightmapTextureManager.pack(15, 15);

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            String name = player.getGameProfile().getName();
            int hat = FirebasePresence.getHatIndex(name);

            if (hat == NONE) {
                notifiedHats.remove(name.toLowerCase());
                continue;
            }

            // Your own hat only gets hidden in first person, where it would
            // sit right against the camera and get in the way. In third
            // person (including when testing solo on yourself) it still shows.
            boolean isSelf = player == client.player;
            if (isSelf && client.options.getPerspective().isFirstPerson()) continue;

            if (notifiedHats.add(name.toLowerCase())) {
                client.player.sendMessage(net.minecraft.text.Text.literal(
                        "\u00A7d[Impact Visuals] \u00A7f" + hatName(hat) + " visible on \u00A7e" + name), false);
            }

            int colorIndex = FirebasePresence.getHatColorIndex(name);
            BlockState blockState = CONCRETE_BY_COLOR[Math.max(0, Math.min(CONCRETE_BY_COLOR.length - 1, colorIndex))].getDefaultState();

            // Use the same interpolated position the player model itself renders at,
            // not the raw tick-quantized position - otherwise the hat lags/desyncs
            // from the head during fast movement (jumping, falling, etc).
            float tickDelta = context.tickCounter().getTickDelta(true);
            Vec3d lerped = player.getLerpedPos(tickDelta);

            double baseX = lerped.x;
            double baseZ = lerped.z;
            double baseY = lerped.y + player.getHeight() - 0.12;

            matrices.push();
            matrices.translate(baseX - camPos.x, baseY - camPos.y, baseZ - camPos.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.getYaw(tickDelta)));

            switch (hat) {
                case CHINA_HAT -> renderChinaHat(client, matrices, consumers, blockState, fullBrightLight);
                case USHANKA -> renderUshanka(client, matrices, consumers, blockState, fullBrightLight);
                case CAP -> renderCap(client, matrices, consumers, blockState, fullBrightLight);
                default -> { }
            }

            matrices.pop();
        }

        consumers.draw();
    }

    private static String hatName(int hat) {
        return switch (hat) {
            case CHINA_HAT -> "China Hat";
            case USHANKA -> "Ushanka";
            case CAP -> "Cap";
            default -> "Hat";
        };
    }

    /** Three shrinking stacked blocks approximating a cone silhouette. */
    private static void renderChinaHat(MinecraftClient client, MatrixStack matrices,
                                        VertexConsumerProvider.Immediate consumers,
                                        BlockState state, int light) {
        drawBlock(client, matrices, consumers, state, light, -0.24f, 0.00f, -0.24f, 0.48f, 0.14f, 0.48f);
        drawBlock(client, matrices, consumers, state, light, -0.15f, 0.12f, -0.15f, 0.30f, 0.14f, 0.30f);
        drawBlock(client, matrices, consumers, state, light, -0.06f, 0.24f, -0.06f, 0.12f, 0.14f, 0.12f);
    }

    /** Rounded fur box plus two hanging ear flaps. */
    private static void renderUshanka(MinecraftClient client, MatrixStack matrices,
                                       VertexConsumerProvider.Immediate consumers,
                                       BlockState state, int light) {
        drawBlock(client, matrices, consumers, state, light, -0.30f, 0.00f, -0.28f, 0.60f, 0.22f, 0.56f);
        drawBlock(client, matrices, consumers, state, light, -0.36f, -0.16f, -0.08f, 0.06f, 0.18f, 0.16f);
        drawBlock(client, matrices, consumers, state, light, 0.30f, -0.16f, -0.08f, 0.06f, 0.18f, 0.16f);
    }

    /** Low crown plus a brim projecting forward (the direction the player faces). */
    private static void renderCap(MinecraftClient client, MatrixStack matrices,
                                   VertexConsumerProvider.Immediate consumers,
                                   BlockState state, int light) {
        drawBlock(client, matrices, consumers, state, light, -0.28f, 0.00f, -0.28f, 0.56f, 0.16f, 0.56f);
        drawBlock(client, matrices, consumers, state, light, -0.26f, 0.00f, -0.48f, 0.52f, 0.03f, 0.22f);
    }

    /** Draws one solid coloured block, sized/positioned in local hat-space (blocks are normally 1x1x1 so we scale). */
    private static void drawBlock(MinecraftClient client, MatrixStack matrices,
                                   VertexConsumerProvider.Immediate consumers,
                                   BlockState state, int light,
                                   float x, float y, float z, float w, float h, float d) {
        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(w, h, d);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        client.getBlockRenderManager().renderBlockAsEntity(state, matrices, consumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
