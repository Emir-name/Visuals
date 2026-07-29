package com.impactvisuals.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Realtime sync for the shared "Jump Ring" effect using a Firebase Realtime
 * Database as the relay - no custom server needed. Fill in DATABASE_URL with
 * your own project's Realtime Database URL before building.
 *
 * Data model: one node per server address holds the most recent jump:
 *   /servers/{serverKey}/lastJump = {"name":..., "x":.., "y":.., "z":.., "ts": millis}
 * Clients PUT that node when they jump, and keep an open SSE stream on it to
 * hear about everyone else's jumps in near real time.
 */
public final class FirebaseJumpSync {

    private FirebaseJumpSync() {}

    // Realtime Database URL for the impact-visual-724a7 Firebase project.
    private static final String DATABASE_URL = "https://impact-visual-724a7-default-rtdb.firebaseio.com";

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static volatile boolean listening = false;
    private static volatile String currentServerKey = null;

    public static class JumpEvent {
        public String name;
        public double x, y, z;
        public long ts;
    }

    /** Fire-and-forget: tell the relay you just jumped. */
    public static void sendJump(String serverKey, String playerName, double x, double y, double z) {
        if (DATABASE_URL.contains("YOUR-PROJECT")) return; // not configured yet

        JsonObject body = new JsonObject();
        body.addProperty("name", playerName);
        body.addProperty("x", x);
        body.addProperty("y", y);
        body.addProperty("z", z);
        body.addProperty("ts", System.currentTimeMillis());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(nodeUrl(serverKey)))
                .timeout(Duration.ofSeconds(5))
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    /**
     * Opens a persistent stream for the given server and calls onJump for every
     * jump event received (including your own echoed back - filter that out by
     * name on the caller's side). Safe to call repeatedly; only (re)connects when
     * the server actually changes.
     */
    public static synchronized void listen(String serverKey, Consumer<JumpEvent> onJump) {
        if (DATABASE_URL.contains("YOUR-PROJECT")) return; // not configured yet
        if (listening && serverKey.equals(currentServerKey)) return;

        currentServerKey = serverKey;
        listening = true;

        Thread thread = new Thread(() -> runStream(serverKey, onJump), "impactvisuals-firebase-jump-sync");
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized void stop() {
        listening = false;
        currentServerKey = null;
    }

    private static void runStream(String serverKey, Consumer<JumpEvent> onJump) {
        while (listening && serverKey.equals(currentServerKey)) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nodeUrl(serverKey)))
                        .header("Accept", "text/event-stream")
                        .timeout(Duration.ofHours(1))
                        .GET()
                        .build();

                HttpResponse<java.util.stream.Stream<String>> response =
                        HTTP.send(request, HttpResponse.BodyHandlers.ofLines());

                StringBuilder dataBuffer = new StringBuilder();
                var iterator = response.body().iterator();
                while (iterator.hasNext()) {
                    if (!listening || !serverKey.equals(currentServerKey)) break;
                    String line = iterator.next();

                    if (line.startsWith("data: ")) {
                        dataBuffer.append(line.substring(6));
                    } else if (line.isEmpty() && !dataBuffer.isEmpty()) {
                        handleEvent(dataBuffer.toString(), onJump);
                        dataBuffer.setLength(0);
                    }
                }
            } catch (Exception e) {
                // Network hiccup or stream closed - back off briefly and reconnect.
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }
    }

    private static void handleEvent(String json, Consumer<JumpEvent> onJump) {
        try {
            JsonObject wrapper = GSON.fromJson(json, JsonObject.class);
            if (wrapper == null || !wrapper.has("data")) return;
            var dataElem = wrapper.get("data");
            if (dataElem == null || dataElem.isJsonNull()) return;

            JumpEvent event = GSON.fromJson(dataElem, JumpEvent.class);
            if (event != null && event.name != null) {
                onJump.accept(event);
            }
        } catch (Exception ignored) {
            // malformed/partial event, skip it
        }
    }

    private static String nodeUrl(String serverKey) {
        return DATABASE_URL + "/servers/" + sanitize(serverKey) + "/lastJump.json";
    }

    private static String sanitize(String key) {
        return key.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}

