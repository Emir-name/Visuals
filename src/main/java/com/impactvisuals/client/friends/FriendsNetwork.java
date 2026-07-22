package com.impactvisuals.client.friends;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendsNetwork {

    public static class Status {
        public String server = "";
        public long lastSeen = 0;
    }

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static final ConcurrentHashMap<String, Status> CACHE = new ConcurrentHashMap<>();
    private static ScheduledExecutorService heartbeatExecutor;

    private static String baseUrl() {
        String url = ModConfig.get().firebaseUrl.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static void startHeartbeat(String username) {
        if (!ModConfig.get().friendsFeatureEnabled) return;
        if (ModConfig.get().firebaseUrl.isBlank()) return;

        stopHeartbeat();
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> sendHeartbeat(username), 0, 20, TimeUnit.SECONDS);
    }

    public static void stopHeartbeat() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
            heartbeatExecutor = null;
        }
    }

    private static void sendHeartbeat(String username) {
        if (baseUrl().isBlank()) return;
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("server", currentServerAddress());
            obj.addProperty("lastSeen", System.currentTimeMillis());

            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/players/" + username + ".json"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(obj)))
                    .build();
            HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    private static String currentServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        if (client.isInSingleplayer()) {
            return "singleplayer";
        }
        return "menu";
    }

    public static void fetchStatus(String username) {
        if (baseUrl().isBlank()) return;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/players/" + username + ".json"))
                    .GET().build();
            HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                try {
                    String body = response.body();
                    if (body == null || body.equals("null")) {
                        CACHE.remove(username);
                        return;
                    }
                    Status status = GSON.fromJson(body, Status.class);
                    if (status != null) {
                        CACHE.put(username, status);
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static Status getCached(String username) {
        return CACHE.get(username);
    }
}
