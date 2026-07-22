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
    private static final ConcurrentHashMap<String, net.minecraft.util.Identifier> HEAD_TEXTURES = new ConcurrentHashMap<>();
    private static final java.util.Set<String> HEAD_FETCHING = ConcurrentHashMap.newKeySet();
    private static ScheduledExecutorService heartbeatExecutor;

    private static final String FIREBASE_URL = "https://impact-visual-724a7-default-rtdb.firebaseio.com";

    private static String baseUrl() {
        return FIREBASE_URL;
    }

    public static void startHeartbeat(String username) {
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

    public static net.minecraft.util.Identifier getHeadTexture(String username) {
        return HEAD_TEXTURES.get(username);
    }

    public static void fetchHead(String username) {
        String key = username.toLowerCase();
        if (HEAD_TEXTURES.containsKey(username) || !HEAD_FETCHING.add(key)) return;

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://minotar.net/avatar/" + username + "/32.png"))
                    .GET().build();
            HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenAccept(response -> {
                byte[] bytes = response.body();
                MinecraftClient.getInstance().execute(() -> {
                    try {
                        net.minecraft.client.texture.NativeImage image =
                                net.minecraft.client.texture.NativeImage.read(new java.io.ByteArrayInputStream(bytes));
                        net.minecraft.client.texture.NativeImageBackedTexture texture =
                                new net.minecraft.client.texture.NativeImageBackedTexture(image);
                        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of("impactvisuals", "friend_head_" + key);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                        HEAD_TEXTURES.put(username, id);
                    } catch (Exception ignored) {
                    } finally {
                        HEAD_FETCHING.remove(key);
                    }
                });
            }).exceptionally(ex -> {
                HEAD_FETCHING.remove(key);
                return null;
            });
        } catch (Exception ignored) {
            HEAD_FETCHING.remove(key);
        }
    }
}
