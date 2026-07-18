package com.impactvisuals.client.event;

import com.impactvisuals.client.config.ConfigScreen;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.config.ModKeybinds;
import com.impactvisuals.client.util.RenderUtils;
import com.impactvisuals.client.visual.DamageNumberRenderer;
import com.impactvisuals.client.visual.HitParticleRenderer;
import com.impactvisuals.client.visual.InfoHud;
import com.impactvisuals.client.visual.ScreenTint;
import com.impactvisuals.client.visual.TargetHud;
import com.impactvisuals.client.visual.TrajectoryRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public final class ClientEventHandler {

    private ClientEventHandler() {}

    public static void register() {
        AttackEntityCallback.EVENT.register(ClientEventHandler::onAttack);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DamageNumberRenderer.tick();
            TrajectoryRenderer.tick();

            while (ModKeybinds.openSettings.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ConfigScreen(null));
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            DamageNumberRenderer.render(context);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            ScreenTint.render(drawContext);
            TargetHud.render(drawContext);
            InfoHud.render(drawContext);
        });
    }

    private static ActionResult onAttack(PlayerEntity player, net.minecraft.world.World world,
                                          net.minecraft.util.Hand hand, net.minecraft.entity.Entity target,
                                          net.minecraft.util.hit.EntityHitResult hitResult) {
        ModConfig cfg = ModConfig.get();
        Vec3d origin = RenderUtils.chestPos(target);

        boolean critical = isCriticalHit(player);
        float estimatedDamage = estimateDamage(player, critical);

        HitParticleRenderer.spawn(origin.x, origin.y, origin.z, critical);

        if (cfg.damageNumbersEnabled) {
            DamageNumberRenderer.spawn(origin.x, origin.y, origin.z, estimatedDamage, critical);
        }

        return ActionResult.PASS;
    }

    private static boolean isCriticalHit(PlayerEntity player) {
        return player.fallDistance > 0.0f
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)
                && !player.hasVehicle();
    }

    private static float estimateDamage(PlayerEntity player, boolean critical) {
        double base = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        float value = (float) base;
        if (critical) {
            value *= 1.5f;
        }
        return value;
    }
}
