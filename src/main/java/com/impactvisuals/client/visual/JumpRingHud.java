package com.impactvisuals.client.visual;

import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.network.FirebaseJumpSync;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Spawns a flat ring of particles under a player's feet when they jump.
 * Local jumps always show the ring. If a Firebase relay is configured (see
 * FirebaseJumpSync), jumps are also broadcast, and rings are shown for other
 * players who are in the IV registry - see chat history for why this needs
 * an external relay and can't work through the Minecraft server alone.
 */
public class JumpRingHud {

    private static boolean wasOnGround = true;
    private static String listeningServerKey = null;

    // Jump events from other players, queued by the background network thread
    // and drained on the client thread so particles are only ever spawned there.
    private static final ConcurrentLinkedQueue<FirebaseJumpSync.JumpEvent> incoming = new ConcurrentLinkedQueue<>();

    public static void tick() {
        ModConfig cfg = ModConfig.get();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            wasOnGround = true;
            return;
        }

        if (!cfg.jumpRingEnabled) {
            wasOnGround = player.isOnGround();
            return;
        }

        ensureListening(client);
        drainIncomingRings(client);

        boolean onGround = player.isOnGround();

        // Trigger exactly on the tick the player leaves the ground while moving
        // upward (a real jump, not just walking off a ledge).
        if (wasOnGround && !onGround && player.getVelocity().y > 0.05) {
            spawnRing(client, player.getX(), player.getY() + 0.05, player.getZ());

            String serverKey = currentServerKey(client);
            if (serverKey != null) {
                FirebaseJumpSync.sendJump(serverKey, player.getGameProfile().getName(),
                        player.getX(), player.getY(), player.getZ());
            }
        }

        wasOnGround = onGround;
    }

    private static void ensureListening(MinecraftClient client) {
        String serverKey = currentServerKey(client);
        if (serverKey == null || serverKey.equals(listeningServerKey)) return;
        listeningServerKey = serverKey;

        FirebaseJumpSync.listen(serverKey, event -> {
            MinecraftClient c = MinecraftClient.getInstance();
            if (c.player == null) return;
            if (event.name.equalsIgnoreCase(c.player.getGameProfile().getName())) return; // ignore our own echo
            // No manual allow-list needed: only someone running this mod's code could
            // ever produce a valid event on this Firebase path in the first place.
            incoming.add(event);
        });
    }

    private static void drainIncomingRings(MinecraftClient client) {
        FirebaseJumpSync.JumpEvent event;
        while ((event = incoming.poll()) != null) {
            spawnRing(client, event.x, event.y + 0.05, event.z);
        }
    }

    private static String currentServerKey(MinecraftClient client) {
        var entry = client.getCurrentServerEntry();
        return entry != null ? entry.address : null;
    }

    private static void spawnRing(MinecraftClient client, double cx, double cy, double cz) {
        if (client.world == null) return;

        int color = ModConfig.get().jumpRingWhite ? CustomParticleManager.WHITE : CustomParticleManager.YELLOW;

        double radius = 0.6;
        int points = 24;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = cx + radius * Math.cos(angle);
            double z = cz + radius * Math.sin(angle);
            CustomParticleManager.spawn(x, cy, z, 0.0, 0.0, 0.0, color, 10, 0.1f, false);
        }
    }
}
