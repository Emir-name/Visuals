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
 * for the local player, and reads back everyone else's heartbeats.
 *
 * Data model: /servers/{serverKey}/online/{sanitizedName} = {"name":..., "ts": millis}
 * An entry only counts as online if its timestamp is recent (see STALE_AFTER).
 */
public final class FirebasePresence {

    private FirebasePresence() {}

    // Same project as FirebaseJumpSync - keep both in sync if you change one.
    private static final String DATABASE_URL = "https://impact-visual-724a7-default-rtdb.firebaseio.com";

    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(8);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(6);
    private static final Duration STALE_AFTER = Duration.ofSeconds(30);

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Map<String, Long> onlineByName = new ConcurrentHashMap<>();
    private static final Map<String, Integer> hatByName = new ConcurrentHashMap<>();

    private static ScheduledExecutorService scheduler;
    private static String activeServerKey;
    private static String activePlayerName;

    /** Call once per server join (safe to call repeatedly - only restarts when the server changes). */
    public static synchronized void start(String serverKey, String playerName) {
        if (DATABASE_URL.contains("YOUR-PROJECT")) return; // not configured yet
        if (serverKey.equals(activeServerKey) && scheduler != null) return;

        stop();
        activeServerKey = serverKey;
        activePlayerName = playerName;
        onlineByName.clear();

        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "impactvisuals-firebase-presence");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(FirebasePresence::sendHeartbeat, 0,
                HEARTBEAT_INTERVAL.toSeconds(), TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(FirebasePresence::pollOnline, 1,
                POLL_INTERVAL.toSeconds(), TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
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

    private static void sendHeartbeat() {
        String serverKey = activeServerKey;
        String playerName = activePlayerName;
        if (serverKey == null || playerName == null) return;

        JsonObject body = new JsonObject();
        body.addProperty("name", playerName);
        body.addProperty("ts", System.currentTimeMillis());
        body.addProperty("hat", com.impactvisuals.client.config.ModConfig.get().hatIndex);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(nodeUrl(serverKey, playerName)))
                .timeout(Duration.ofSeconds(5))
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    private static void pollOnline() {
        String serverKey = activeServerKey;
        if (serverKey == null) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(onlineListUrl(serverKey)))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null || response.body().equals("null")) return;

            JsonObject root = GSON.fromJson(response.body(), JsonObject.class);
            if (root == null) return;

            Map<String, Long> fresh = new ConcurrentHashMap<>();
            Map<String, Integer> freshHats = new ConcurrentHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonElement value = entry.getValue();
                if (value == null || !value.isJsonObject()) continue;
                JsonObject obj = value.getAsJsonObject();
                if (!obj.has("name") || !obj.has("ts")) continue;

                String name = obj.get("name").getAsString();
                long ts = obj.get("ts").getAsLong();
                fresh.put(name.toLowerCase(), ts);
                freshHats.put(name.toLowerCase(), obj.has("hat") ? obj.get("hat").getAsInt() : 0);
            }

            onlineByName.clear();
            onlineByName.putAll(fresh);
            hatByName.clear();
            hatByName.putAll(freshHats);
        } catch (Exception ignored) {
            // network hiccup - keep the last known snapshot, try again next poll
        }
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
