package com.impactvisuals.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {

    private static final int ACCENT = 0xFFFF8C00;
    private static final int ACCENT_DIM = 0xFF7A4300;
    private static final int PANEL_BG = 0xE6141414;
    private static final int SIDEBAR_BG = 0xF01A1A1A;
    private static final int PANEL_BORDER = 0xFFFF8C00;
    private static final int TRACK_OFF = 0xFF3A3A3A;
    private static final int TEXT_MAIN = 0xFFEFEFEF;
    private static final int TEXT_DIM = 0xFFA0A0A0;

    private static final String[] CATEGORY_NAMES = {"COMBAT", "HUD", "EXTRA"};

    private final Screen parent;
    private final ModConfig cfg;

    private int panelX, panelY, panelW, panelH;
    private int sidebarW;
    private int sidebarItemY, sidebarItemH;
    private int skinPanelW, skinPanelX, skinPanelY;

    private int currentCategory = 0;

    private final List<ToggleRow> toggles = new ArrayList<>();
    private SliderRow slider;
    private boolean showSlider = false;

    private int doneX, doneY, doneW, doneH;
    private int resetX, resetY, resetW, resetH;

    private boolean draggingSlider = false;
    private int previousBlurriness = 0;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Impact Visuals"));
        this.parent = parent;
        this.cfg = ModConfig.get();
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        previousBlurriness = client.options.getMenuBackgroundBlurriness().getValue();
        client.options.getMenuBackgroundBlurriness().setValue(0);

        panelW = 560;
        panelH = 300;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        sidebarW = 100;
        sidebarItemY = panelY + 46;
        sidebarItemH = 26;

        skinPanelW = 90;
        skinPanelX = panelX + panelW - skinPanelW - 10;
        skinPanelY = panelY + 36;

        int buttonW = 100;
        int buttonH = 22;
        int buttonY = panelY + panelH - 34;
        resetX = panelX + panelW - 20 - buttonW * 2 - 10;
        resetY = buttonY;
        resetW = buttonW;
        resetH = buttonH;

        doneX = panelX + panelW - 20 - buttonW;
        doneY = buttonY;
        doneW = buttonW;
        doneH = buttonH;

        buildCategoryContent();
    }

    private void buildCategoryContent() {
        toggles.clear();
        showSlider = false;
        slider = null;

        int contentX = panelX + sidebarW + 30;
        int gridStartY = panelY + 50;
        int rowH = 28;
        int colGap = 160;

        if (currentCategory == 0) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Hit Particles", () -> cfg.hitParticlesEnabled, v -> cfg.hitParticlesEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Damage Numbers", () -> cfg.damageNumbersEnabled, v -> cfg.damageNumbersEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Critical Flash", () -> cfg.criticalFlashEnabled, v -> cfg.criticalFlashEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Trajectory Predict", () -> cfg.trajectoryPredictionEnabled, v -> cfg.trajectoryPredictionEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Hitmarker Flash", () -> cfg.hitmarkerEnabled, v -> cfg.hitmarkerEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Hit Sound", () -> cfg.hitSoundEnabled, v -> cfg.hitSoundEnabled = v);
        } else if (currentCategory == 1) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Target HUD", () -> cfg.targetHudEnabled, v -> cfg.targetHudEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Info HUD", () -> cfg.infoHudEnabled, v -> cfg.infoHudEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Coordinates", () -> cfg.coordinatesHudEnabled, v -> cfg.coordinatesHudEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Compass", () -> cfg.compassHudEnabled, v -> cfg.compassHudEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Session Timer", () -> cfg.sessionTimerEnabled, v -> cfg.sessionTimerEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "K/D Counter", () -> cfg.killDeathCounterEnabled, v -> cfg.killDeathCounterEnabled = v);

            showSlider = true;
            slider = new SliderRow("Target HUD Range", contentX, gridStartY + 3 * rowH + 14, panelW - sidebarW - 60, 1, 15, cfg.targetHudRangeBlocks, v -> cfg.targetHudRangeBlocks = v);
        } else {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Purple Sky", () -> cfg.purpleSkyEnabled, v -> cfg.purpleSkyEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Low HP Vignette", () -> cfg.lowHealthVignetteEnabled, v -> cfg.lowHealthVignetteEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Durability %", () -> cfg.durabilityHudEnabled, v -> cfg.durabilityHudEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Cooldown Bar", () -> cfg.cooldownIndicatorEnabled, v -> cfg.cooldownIndicatorEnabled = v);
        }
    }

    private void addToggle(int index, int contentX, int colGap, int gridStartY, int rowH,
                            String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        int col = index % 2;
        int row = index / 2;
        int x = contentX + col * colGap;
        int y = gridStartY + row * rowH;
        toggles.add(new ToggleRow(label, x, y, getter, setter));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x99000000);

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, PANEL_BORDER);

        context.fill(panelX, panelY, panelX + sidebarW, panelY + panelH, SIDEBAR_BG);
        context.fill(panelX + sidebarW, panelY, panelX + sidebarW + 1, panelY + panelH, PANEL_BORDER);

        String title = "IMPACT";
        context.drawText(this.textRenderer, title, panelX + 12, panelY + 10, ACCENT, false);

        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            int itemY = sidebarItemY + i * sidebarItemH;
            boolean active = i == currentCategory;
            boolean hovered = inside(panelX, itemY, sidebarW, sidebarItemH, mouseX, mouseY);

            if (active) {
                context.fill(panelX, itemY, panelX + sidebarW, itemY + sidebarItemH, ACCENT_DIM);
                context.fill(panelX, itemY, panelX + 2, itemY + sidebarItemH, ACCENT);
            } else if (hovered) {
                context.fill(panelX, itemY, panelX + sidebarW, itemY + sidebarItemH, 0x30FFFFFF);
            }

            int color = active ? ACCENT : TEXT_DIM;
            context.drawText(this.textRenderer, CATEGORY_NAMES[i], panelX + 14, itemY + 9, color, false);
        }

        for (ToggleRow row : toggles) {
            row.render(context, this, mouseX, mouseY);
        }

        if (showSlider && slider != null) {
            slider.render(context, this, mouseX, mouseY);
        }

        drawButton(context, resetX, resetY, resetW, resetH, "RESET", mouseX, mouseY);
        drawButton(context, doneX, doneY, doneW, doneH, "DONE", mouseX, mouseY);

        renderSkinPanel(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSkinPanel(DrawContext context) {
        int skinPanelH = 90;
        context.fill(skinPanelX, skinPanelY, skinPanelX + skinPanelW, skinPanelY + skinPanelH, 0xFF1E1E1E);
        drawBorder(context, skinPanelX, skinPanelY, skinPanelW, skinPanelH, ACCENT);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        var texture = client.player.getSkinTextures().texture();
        int faceSize = 48;
        int faceX = skinPanelX + (skinPanelW - faceSize) / 2;
        int faceY = skinPanelY + 8;

        // Base face layer (UV 8,8 in a 64x64 skin) then the hat/overlay layer (UV 40,8) on top.
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, texture,
                faceX, faceY, faceSize, faceSize, 8, 8, 8, 8, 64, 64);
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, texture,
                faceX, faceY, faceSize, faceSize, 40, 8, 8, 8, 64, 64);

        String name = client.getSession().getUsername();
        int nameWidth = this.textRenderer.getWidth(name);
        int nameX = skinPanelX + (skinPanelW - nameWidth) / 2;
        context.drawText(this.textRenderer, name, nameX, faceY + faceSize + 8, TEXT_MAIN, false);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawButton(DrawContext context, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = inside(x, y, w, h, mouseX, mouseY);
        int bg = hovered ? ACCENT_DIM : TRACK_OFF;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, ACCENT);
        int textWidth = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, label, x + (w - textWidth) / 2, y + (h - 8) / 2, TEXT_MAIN, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            int itemY = sidebarItemY + i * sidebarItemH;
            if (inside(panelX, itemY, sidebarW, sidebarItemH, mouseX, mouseY) && currentCategory != i) {
                currentCategory = i;
                buildCategoryContent();
                return true;
            }
        }

        for (ToggleRow row : toggles) {
            if (row.isInside(mouseX, mouseY)) {
                row.toggle();
                return true;
            }
        }

        if (showSlider && slider != null && slider.isInsideTrack(mouseX, mouseY)) {
            draggingSlider = true;
            slider.updateFromMouse(mouseX);
            return true;
        }

        if (inside(resetX, resetY, resetW, resetH, mouseX, mouseY)) {
            resetToDefaults();
            return true;
        }

        if (inside(doneX, doneY, doneW, doneH, mouseX, mouseY)) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider && slider != null) {
            slider.updateFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean inside(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void resetToDefaults() {
        cfg.hitParticlesEnabled = true;
        cfg.targetHudEnabled = true;
        cfg.damageNumbersEnabled = true;
        cfg.criticalFlashEnabled = true;
        cfg.trajectoryPredictionEnabled = true;
        cfg.purpleSkyEnabled = false;
        cfg.infoHudEnabled = true;
        cfg.hitmarkerEnabled = true;
        cfg.coordinatesHudEnabled = false;
        cfg.compassHudEnabled = false;
        cfg.sessionTimerEnabled = false;
        cfg.lowHealthVignetteEnabled = true;
        cfg.durabilityHudEnabled = false;
        cfg.killDeathCounterEnabled = false;
        cfg.hitSoundEnabled = true;
        cfg.cooldownIndicatorEnabled = true;
        cfg.targetHudRangeBlocks = 6;
        buildCategoryContent();
    }

    @Override
    public void close() {
        cfg.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void removed() {
        MinecraftClient.getInstance().options.getMenuBackgroundBlurriness().setValue(previousBlurriness);
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Small square "bullet" toggle, similar in spirit to the circular indicators in Meteor/Retro-style clients. */
    private class ToggleRow {
        final String label;
        final int x, y;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;

        ToggleRow(String label, int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.getter = getter;
            this.setter = setter;
        }

        void toggle() {
            setter.accept(!getter.getAsBoolean());
        }

        private int bulletX() {
            return x + 130;
        }

        boolean isInside(double mouseX, double mouseY) {
            int size = 10;
            int bx = bulletX();
            return mouseX >= bx && mouseX < bx + size && mouseY >= y && mouseY < y + size;
        }

        void render(DrawContext context, ConfigScreen screen, int mouseX, int mouseY) {
            context.drawText(screen.textRenderer, label, x, y + 1, TEXT_MAIN, false);

            int size = 10;
            int bx = bulletX();
            boolean on = getter.getAsBoolean();

            if (on) {
                context.fill(bx, y, bx + size, y + size, ACCENT);
            } else {
                screen.drawBorder(context, bx, y, size, size, TEXT_DIM);
            }
        }
    }

    private class SliderRow {
        final String label;
        final int x, y, width;
        final int min;
        final int max;
        int value;
        final Consumer<Integer> setter;

        SliderRow(String label, int x, int y, int width, int min, int max, int initial, Consumer<Integer> setter) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.min = min;
            this.max = max;
            this.value = initial;
            this.setter = setter;
        }

        boolean isInsideTrack(double mouseX, double mouseY) {
            int trackY = y + 14;
            int trackH = 8;
            return mouseX >= x && mouseX < x + width && mouseY >= trackY - 6 && mouseY < trackY + trackH + 6;
        }

        void updateFromMouse(double mouseX) {
            double pct = (mouseX - x) / (double) width;
            pct = Math.max(0, Math.min(1, pct));
            value = (int) Math.round(min + pct * (max - min));
            setter.accept(value);
        }

        void render(DrawContext context, ConfigScreen screen, int mouseX, int mouseY) {
            String text = label + ": " + value;
            context.drawText(screen.textRenderer, text, x, y - 10, TEXT_MAIN, false);

            int trackY = y + 4;
            int trackH = 8;

            context.fill(x, trackY, x + width, trackY + trackH, TRACK_OFF);
            double pct = (value - min) / (double) (max - min);
            int filledW = (int) (width * pct);
            context.fill(x, trackY, x + filledW, trackY + trackH, ACCENT);
            screen.drawBorder(context, x, trackY, width, trackH, ACCENT);

            int knobSize = 12;
            int knobX = x + filledW - knobSize / 2;
            int knobY = trackY + trackH / 2 - knobSize / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }
}
