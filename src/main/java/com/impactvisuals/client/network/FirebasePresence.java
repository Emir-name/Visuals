package com.impactvisuals.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automatically tracks who else on the current server is also running Impact
 * Visuals, using the same Firebase Realtime Database as the jump ring relay.
 * No manual name list needed: the mod itself periodically writes "I'm here"
 * for the local player, and keeps an open SSE stream on the whole online-list
 * node so updates from everyone else arrive in near real time instead of on
 * a polling interval (the same streaming technique FirebaseJumpSync uses).
 *
 * Data model: /servers/{serverKey}/online/{sanitizedName} = {"name":..., "ts": millis, "hat":.., "hatColor":.., "x":.., "z":..}
 * An entry only counts as online if its timestamp is recent (see STALE_AFTER).
 */
public final class FirebasePresence {

    private FirebasePresence() {}

    // Same project as FirebaseJumpSync - keep both in sync if you change one.
    private static final String DATABASE_URL = "https://impact-visual-724a7-default-rtdb.firebaseio.com";

    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(8);
    private static final Duration STALE_AFTER = Duration.ofSeconds(30);

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Map<String, Long> onlineByName = new ConcurrentHashMap<>();
    private static final Map<String, Integer> hatByName = new ConcurrentHashMap<>();
    private static final Map<String, Integer> hatColorByName = new ConcurrentHashMap<>();
    private static final Map<String, Double> posXByName = new ConcurrentHashMap<>();
    private static final Map<String, Double> posZByName = new ConcurrentHashMap<>();

    private static ScheduledExecutorService heartbeatScheduler;
    private static volatile boolean streaming = false;
    private static String activeServerKey;
    private static String activePlayerName;

    /** Call once per server join (safe to call repeatedly - only restarts when the server changes). */
    public static synchronized void start(String serverKey, String playerName) {
        if (DATABASE_URL.contains("YOUR-PROJECT")) return; // not configured yet
        if (serverKey.equals(activeServerKey) && heartbeatScheduler != null) return;

        stop();
        activeServerKey = serverKey;
        activePlayerName = playerName;
        onlineByName.clear();
        hatByName.clear();
        hatColorByName.clear();
        posXByName.clear();
        posZByName.clear();

        heartbeatScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "impactvisuals-firebase-presence-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatScheduler.scheduleWithFixedDelay(FirebasePresence::sendHeartbeat, 0,
                HEARTBEAT_INTERVAL.toSeconds(), TimeUnit.SECONDS);

        streaming = true;
        Thread streamThread = new Thread(() -> runStream(serverKey), "impactvisuals-firebase-presence-stream");
        streamThread.setDaemon(true);
        streamThread.start();
    }

    public static synchronized void stop() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdownNow();
            heartbeatScheduler = null;
        }
        streaming = false;
        activeServerKey = null;
        activePlayerName = null;
    }

    /** Sends a heartbeat right now instead of waiting for the next scheduled tick - used when a broadcast cosmetic like the China Hat gets toggled, so it shows up faster. */
    public static void forceHeartbeat() {
        if (activeServerKey != null && activePlayerName != null) {
            sendHeartbeat();
        }
    }

    public static boolean isOnline(String name) {
        if (name == null) return false;
        Long ts = onlineByName.get(name.toLowerCase());
        if (ts == null) return false;
        return System.currentTimeMillis() - ts < STALE_AFTER.toMillis();
    }

    /** Which hat (0=none, 1=China Hat, 2=Ushanka, 3=Cap) the given player has broadcast, or 0 if offline/none. */
    public static int getHatIndex(String name) {
        if (!isOnline(name)) return 0;
        Integer hat = hatByName.get(name.toLowerCase());
        return hat != null ? hat : 0;
    }

    /** Which wool colour (0-15) the given player's hat is broadcasting, or 0 (white) if unknown. */
    public static int getHatColorIndex(String name) {
        if (!isOnline(name)) return 0;
        Integer color = hatColorByName.get(name.toLowerCase());
        return color != null ? color : 0;
    }

    /** All online players' names (lowercase) mapped to their last-broadcast X/Z, for the radar HUD. Excludes stale entries. */
    public static Map<String, double[]> getOnlinePositions() {
        Map<String, double[]> result = new java.util.HashMap<>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : onlineByName.entrySet()) {
            if (now - entry.getValue() >= STALE_AFTER.toMillis()) continue;
            Double x = posXByName.get(entry.getKey());
            Double z = posZByName.get(entry.getKey());
            if (x == null || z == null) continue;
            result.put(entry.getKey(), new double[]{x, z});
        }
        return result;
    }

    private static void sendHeartbeat() {
        String serverKey = activeServerKey;
        String playerName = activePlayerName;
        if (serverKey == null || playerName == null) return;

        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        double x = 0, z = 0;
        if (client.player != null) {
            x = client.player.getX();
            z = client.player.getZ();
        }

        JsonObject body = new JsonObject();
        body.addProperty("name", playerName);
        body.addProperty("ts", System.currentTimeMillis());
        body.addProperty("hat", com.impactvisuals.client.config.ModConfig.get().hatIndex);
        body.addProperty("hatColor", com.impactvisuals.client.config.ModConfig.get().hatColorIndex);
        body.addProperty("x", x);
        body.addProperty("z", z);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(nodeUrl(serverKey, playerName)))
                .timeout(Duration.ofSeconds(5))
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    /** Keeps an open SSE connection on the whole online-list node, applying each push update as it arrives instead of polling. Reconnects automatically on drop. */
    private static void runStream(String serverKey) {
        while (streaming && serverKey.equals(activeServerKey)) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(onlineListUrl(serverKey)))
                        .header("Accept", "text/event-stream")
                        .timeout(Duration.ofHours(1))
                        .GET()
                        .build();

                HttpResponse<java.util.stream.Stream<String>> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofLines());

                StringBuilder dataBuffer = new StringBuilder();
                var iterator = response.body().iterator();
                while (iterator.hasNext()) {
                    if (!streaming || !serverKey.equals(activeServerKey)) break;
                    String line = iterator.next();

                    if (line.startsWith("data: ")) {
                        dataBuffer.append(line.substring(6));
                    } else if (line.isEmpty() && !dataBuffer.isEmpty()) {
                        handleStreamEvent(dataBuffer.toString());
                        dataBuffer.setLength(0);
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }
    }

    /** A Firebase SSE event either replaces the whole online list (path "/", on first connect) or one child (path "/name", on every change after that). */
    private static void handleStreamEvent(String json) {
        try {
            JsonObject wrapper = GSON.fromJson(json, JsonObject.class);
            if (wrapper == null || !wrapper.has("path") || !wrapper.has("data")) return;

            String path = wrapper.get("path").getAsString();
            JsonElement data = wrapper.get("data");

            if ("/".equals(path)) {
                onlineByName.clear();
                hatByName.clear();
                hatColorByName.clear();
                posXByName.clear();
                posZByName.clear();
                if (data != null && data.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject().entrySet()) {
                        applyEntry(entry.getKey(), entry.getValue());
                    }
                }
                return;
            }

            String name = path.startsWith("/") ? path.substring(1) : path;
            if (name.contains("/")) return; // deeper than one level - shouldn't happen since we PUT whole objects
            applyEntry(name, data);
        } catch (Exception ignored) {
            // malformed/partial event, skip it
        }
    }

    private static void applyEntry(String key, JsonElement value) {
        String lower = key.toLowerCase();
        if (value == null || value.isJsonNull() || !value.isJsonObject()) {
            onlineByName.remove(lower);
            hatByName.remove(lower);
            hatColorByName.remove(lower);
            posXByName.remove(lower);
            posZByName.remove(lower);
            return;
        }

        JsonObject obj = value.getAsJsonObject();
        if (!obj.has("ts")) return;

        onlineByName.put(lower, obj.get("ts").getAsLong());
        hatByName.put(lower, obj.has("hat") ? obj.get("hat").getAsInt() : 0);
        hatColorByName.put(lower, obj.has("hatColor") ? obj.get("hatColor").getAsInt() : 0);
        if (obj.has("x")) posXByName.put(lower, obj.get("x").getAsDouble());
        if (obj.has("z")) posZByName.put(lower, obj.get("z").getAsDouble());
    }

    private static String onlineListUrl(String serverKey) {
        return DATABASE_URL + "/servers/" + sanitize(serverKey) + "/online.json";
    }

    private static String nodeUrl(String serverKey, String playerName) {
        return DATABASE_URL + "/servers/" + sanitize(serverKey) + "/online/" + sanitize(playerName) + ".json";
    }

    private static String sanitize(String key) {
        return key.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
