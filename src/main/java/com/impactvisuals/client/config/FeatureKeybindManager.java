package com.impactvisuals.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Lets any feature toggle from the settings menu be bound to a keyboard key
 * that flips it on/off directly in-game, without opening the menu.
 *
 * Toggles register themselves here (see ConfigScreen#addToggle) with their
 * label and the same getter/setter used by the settings UI. The actual key
 * assignment is stored in ModConfig#featureKeybinds and can be changed by
 * clicking the letter box next to a toggle in the settings screen.
 */
public final class FeatureKeybindManager {

    private FeatureKeybindManager() {}

    private static final class Target {
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;

        Target(BooleanSupplier getter, Consumer<Boolean> setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }

    private static final Map<String, Target> TARGETS = new HashMap<>();
    private static final Set<String> heldLastTick = new HashSet<>();

    /** Registers (or re-registers) which getter/setter a toggle's label controls. Safe to call repeatedly. */
    public static void register(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        TARGETS.put(label, new Target(getter, setter));
    }

    public static boolean hasKey(String label) {
        return ModConfig.get().featureKeybinds.containsKey(label);
    }

    public static void setKey(String label, int keyCode) {
        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        ModConfig cfg = ModConfig.get();
        cfg.featureKeybinds.put(label, key.getTranslationKey());
        cfg.save();
    }

    public static void clearKey(String label) {
        ModConfig cfg = ModConfig.get();
        cfg.featureKeybinds.remove(label);
        cfg.save();
    }

    /** Short label for the UI, e.g. "J", "F5", "-" if nothing is bound. */
    public static String displayName(String label) {
        String translationKey = ModConfig.get().featureKeybinds.get(label);
        if (translationKey == null) return "-";
        String text = InputUtil.fromTranslationKey(translationKey).getLocalizedText().getString();
        return text.length() > 4 ? text.substring(0, 4) : text;
    }

    /** Called every client tick; checks all bound keys and toggles their feature on a fresh key-down. */
    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.currentScreen != null) return; // don't fire while typing/chat/any menu is open

        long windowHandle = client.getWindow().getHandle();
        Map<String, String> binds = ModConfig.get().featureKeybinds;
        Set<String> stillHeld = new HashSet<>();

        for (Map.Entry<String, String> entry : binds.entrySet()) {
            String label = entry.getKey();
            Target target = TARGETS.get(label);
            if (target == null) continue;

            InputUtil.Key key = InputUtil.fromTranslationKey(entry.getValue());
            boolean pressed = InputUtil.isKeyPressed(windowHandle, key.getCode());

            if (pressed) {
                stillHeld.add(label);
                if (!heldLastTick.contains(label)) {
                    boolean newValue = !target.getter.getAsBoolean();
                    target.setter.accept(newValue);
                    notifyToggle(client, label, newValue);
                }
            }
        }

        heldLastTick.clear();
        heldLastTick.addAll(stillHeld);
    }

    /** Small local chat message so a keybind toggle is never silent/invisible. */
    private static void notifyToggle(MinecraftClient client, String label, boolean newValue) {
        if (client.player == null) return;
        String stateText = newValue ? "\u00A7aON" : "\u00A7cOFF";
        String displayLabel = com.impactvisuals.client.config.Lang.t(label);
        client.player.sendMessage(
                net.minecraft.text.Text.literal("\u00A7d[Impact Visuals] \u00A7f" + displayLabel + " \u00A77\u2192 " + stateText),
                false
        );
    }
}
