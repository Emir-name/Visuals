package com.impactvisuals.client.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ModKeybinds {

    public static KeyBinding openSettings;

    private ModKeybinds() {}

    public static void register() {
        openSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.impactvisuals.opensettings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.impactvisuals"
        ));
    }
}
