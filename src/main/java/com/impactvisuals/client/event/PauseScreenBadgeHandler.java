package com.impactvisuals.client.event;

import com.impactvisuals.client.config.ConfigScreen;
import com.impactvisuals.client.config.ModConfig;
import com.impactvisuals.client.visual.InfoHud;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;

/**
 * The Info HUD badge can't be clicked during normal gameplay because the
 * mouse cursor is captured for camera control and has no meaningful screen
 * position. The pause menu is the one place the cursor is actually free,
 * so we redraw the badge there too and let a click on it jump straight
 * into ConfigScreen - a quick shortcut on top of the existing keybind.
 */
public final class PauseScreenBadgeHandler {

    private PauseScreenBadgeHandler() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof GameMenuScreen)) return;

            ScreenEvents.afterRender(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
                if (ModConfig.get().infoHudEnabled) {
                    InfoHud.render(drawContext);
                }
            });

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
                if (button == 0 && ModConfig.get().infoHudEnabled && InfoHud.isInsideBadge(mouseX, mouseY)) {
                    if (ModConfig.get().setupComplete) {
                        MinecraftClient.getInstance().setScreen(new ConfigScreen(screen));
                    } else {
                        MinecraftClient.getInstance().setScreen(new com.impactvisuals.client.config.WelcomeScreen(screen));
                    }
                    return false;
                }
                return true;
            });
        });
    }
}
