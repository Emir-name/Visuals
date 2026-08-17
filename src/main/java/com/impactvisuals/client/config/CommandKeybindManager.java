package com.impactvisuals.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Companion to FeatureKeybindManager, but for the Bind tab's saved commands
 * instead of feature toggles: pressing the bound key sends the command once
 * (as a real "/" command or plain chat message) rather than flipping a
 * boolean. Key assignments live in ModConfig#commandKeybinds, keyed by the
 * command text itself.
 */
public final class CommandKeybindManager {

    private CommandKeybindManager() {}

    private static final Set<String> heldLastTick = new HashSet<>();

    public static boolean hasKey(String command) {
        return ModConfig.get().commandKeybinds.containsKey(command);
    }

    public static void setKey(String command, int keyCode) {
        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        ModConfig cfg = ModConfig.get();
        cfg.commandKeybinds.put(command, key.getTranslationKey());
        cfg.save();
    }

    public static void clearKey(String command) {
        ModConfig cfg = ModConfig.get();
        cfg.commandKeybinds.remove(command);
        cfg.save();
    }

    /** Short label for the UI, e.g. "J", "F5", "-" if nothing is bound. */
    public static String displayName(String command) {
        String translationKey = ModConfig.get().commandKeybinds.get(command);
        if (translationKey == null) return "-";
        String text = InputUtil.fromTranslationKey(translationKey).getLocalizedText().getString();
        return text.length() > 4 ? text.substring(0, 4) : text;
    }

    /** Called every client tick; checks all bound keys and sends their command on a fresh key-down. */
    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) return;
        if (client.currentScreen != null) return; // don't fire while typing/chat/any menu is open

        long windowHandle = client.getWindow().getHandle();
        Map<String, String> binds = ModConfig.get().commandKeybinds;
        Set<String> stillHeld = new HashSet<>();

        for (Map.Entry<String, String> entry : binds.entrySet()) {
            String command = entry.getKey();
            InputUtil.Key key = InputUtil.fromTranslationKey(entry.getValue());
            boolean pressed = InputUtil.isKeyPressed(windowHandle, key.getCode());

            if (pressed) {
                stillHeld.add(command);
                if (!heldLastTick.contains(command)) {
                    sendCommand(client, command);
                }
            }
        }

        heldLastTick.clear();
        heldLastTick.addAll(stillHeld);
    }

    private static void sendCommand(MinecraftClient client, String text) {
        if (text.startsWith("/")) {
            client.player.networkHandler.sendChatCommand(text.substring(1));
        } else {
            client.player.networkHandler.sendChatMessage(text);
        }
    }
}

