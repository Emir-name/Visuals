package com.impactvisuals.client.event;

import com.impactvisuals.client.config.ConfigScreen;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.config.ModKeybinds;
import com.impactvisuals.client.util.RenderUtils;
import com.impactvisuals.client.friends.FriendsNetwork;
import com.impactvisuals.client.visual.ColoredHitParticles;
import com.impactvisuals.client.visual.CooldownIndicator;
import com.impactvisuals.client.visual.CosmeticTrails;
import com.impactvisuals.client.visual.CritSoundPlayer;
import com.impactvisuals.client.visual.DamageFlash;
import com.impactvisuals.client.visual.DamageNumberRenderer;
import com.impactvisuals.client.visual.ExtraHud;
import com.impactvisuals.client.visual.HandGlow;
import com.impactvisuals.client.visual.HeartbeatSound;
import com.impactvisuals.client.visual.HitParticleRenderer;
import com.impactvisuals.client.visual.HitSoundPlayer;
import com.impactvisuals.client.visual.HitmarkerRenderer;
import com.impactvisuals.client.visual.ImpactPunch;
import com.impactvisuals.client.visual.InfoHud;
import com.impactvisuals.client.visual.KillDeathTracker;
import com.impactvisuals.client.visual.PlaytimeTracker;
import com.impactvisuals.client.visual.ScreenTint;
import com.impactvisuals.client.visual.SmallFireEffect;
import com.impactvisuals.client.visual.StatsHud;
import com.impactvisuals.client.visual.TargetHud;
import com.impactvisuals.client.visual.TrajectoryRenderer;
import com.impactvisuals.client.visual.UiSoundPlayer;
import com.impactvisuals.client.visual.VignetteRenderer;
import com.impactvisuals.client.visual.ZoomHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public final class ClientEventHandler {

    private ClientEventHandler() {}

    public static void register() {
        AttackEntityCallback.EVENT.register(ClientEventHandler::onAttack);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DamageNumberRenderer.tick();
            TrajectoryRenderer.tick();
            HitmarkerRenderer.tick();
            KillDeathTracker.tick();
            ZoomHandler.tick();
            PlaytimeTracker.tick();
            SmallFireEffect.tick();
            DamageFlash.tick();
            CosmeticTrails.tick();
            ScreenTint.tick();
            HeartbeatSound.tick();
            HandGlow.tick();
            FriendsNetwork.ensureStarted();

            while (ModKeybinds.openSettings.wasPressed()) {
                if (client.currentScreen == null) {
                    UiSoundPlayer.play();
                    client.setScreen(new ConfigScreen(null));
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            DamageNumberRenderer.render(context);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            VignetteRenderer.render(drawContext);
            TargetHud.render(drawContext);
            InfoHud.render(drawContext);
            StatsHud.render(drawContext);
            KillDeathTracker.render(drawContext);
            PlaytimeTracker.render(drawContext);
            HitmarkerRenderer.render(drawContext);
            CooldownIndicator.render(drawContext);
            ExtraHud.render(drawContext);
            DamageFlash.render(drawContext);
            ImpactPunch.render(drawContext);
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
        HitmarkerRenderer.spawn();
        HitSoundPlayer.play();
        KillDeathTracker.onHit(target);
        ColoredHitParticles.spawn(origin.x, origin.y, origin.z);
        ImpactPunch.trigger();

        if (cfg.sweepTrailEnabled) {
            world.addParticle(ParticleTypes.SWEEP_ATTACK, origin.x, origin.y, origin.z, 0.0, 0.0, 0.0);
        }

        if (critical) {
            CritSoundPlayer.play();
        }

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
