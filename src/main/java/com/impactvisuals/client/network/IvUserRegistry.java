package com.impactvisuals.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tags players who've added their name to a public list of Impact Visuals
 * users. This is NOT real-time detection through the Minecraft protocol -
 * that's not possible against a server you don't control. Instead it's an
 * independent HTTP lookup against a JSON file hosted on GitHub, the same
 * approach mods like Meteor Client use for their "capes"/known-user lists.
 *
 * To "register", a player's name needs to be added to that JSON file (e.g.
 * by editing it directly in the repo, since it's just a flat list).
 */
public final class IvUserRegistry {

    private IvUserRegistry() {}

    // Raw GitHub URL - point this at a JSON file in your own repo, e.g.
    // https://raw.githubusercontent.com/Emir-name/Visuals/main/iv_users.json
    // containing a simple JSON array of lowercase usernames: ["notch", "jeb_"]
    private static final String LIST_URL =
            "https://raw.githubusercontent.com/Emir-name/Visuals/main/iv_users.json";

    private static final Duration REFRESH_INTERVAL = Duration.ofMinutes(3);
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static volatile Set<String> registeredNames = Collections.emptySet();
    private static ScheduledExecutorService scheduler;

    /** Call once on mod init / first use. Safe to call more than once. */
    public static synchronized void startIfNeeded() {
        if (scheduler != null) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "impactvisuals-iv-registry");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(IvUserRegistry::refresh, 0, REFRESH_INTERVAL.toSeconds(), TimeUnit.SECONDS);
    }

    public static boolean isRegistered(String username) {
        if (username == null) return false;
        return registeredNames.contains(username.toLowerCase());
    }

    private static void refresh() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LIST_URL))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;

            String[] names = GSON.fromJson(response.body(), String[].class);
            if (names == null) return;

            Set<String> lowered = new CopyOnWriteArraySet<>();
            for (String name : names) {
                if (name != null && !name.isBlank()) {
                    lowered.add(name.toLowerCase());
                }
            }
            registeredNames = lowered;
        } catch (JsonSyntaxException | java.io.IOException | InterruptedException ignored) {
            // Keep the last known good list on any network/parse failure.
        }
    }
}
