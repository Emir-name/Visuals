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
    private static final int PANEL_BG = 0xE6161616;
    private static final int PANEL_BORDER = 0xFFFF8C00;
    private static final int TRACK_OFF = 0xFF3A3A3A;
    private static final int TEXT_MAIN = 0xFFEFEFEF;
    private static final int TEXT_DIM = 0xFFA0A0A0;

    private final Screen parent;
    private final ModConfig cfg;

    private int panelX, panelY, panelW, panelH;

    private final List<ToggleRow> toggles = new ArrayList<>();
    private SliderRow slider;

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

        panelW = 260;
        panelH = 360;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        toggles.clear();
        int rowY = panelY + 46;
        int rowH = 30;

        toggles.add(new ToggleRow("Hit Particles", rowY, () -> cfg.hitParticlesEnabled, v -> cfg.hitParticlesEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Target HUD", rowY, () -> cfg.targetHudEnabled, v -> cfg.targetHudEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Damage Numbers", rowY, () -> cfg.damageNumbersEnabled, v -> cfg.damageNumbersEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Critical Flash", rowY, () -> cfg.criticalFlashEnabled, v -> cfg.criticalFlashEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Trajectory Prediction", rowY, () -> cfg.trajectoryPredictionEnabled, v -> cfg.trajectoryPredictionEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Purple Sky", rowY, () -> cfg.purpleSkyEnabled, v -> cfg.purpleSkyEnabled = v));
        rowY += rowH;
        toggles.add(new ToggleRow("Info HUD (Name/FPS/Ping)", rowY, () -> cfg.infoHudEnabled, v -> cfg.infoHudEnabled = v));
        rowY += rowH + 10;

        slider = new SliderRow("Target HUD Range", rowY, 1, 15, cfg.targetHudRangeBlocks, v -> cfg.targetHudRangeBlocks = v);

        int buttonW = 100;
        int buttonH = 22;
        int buttonY = panelY + panelH - 36;
        resetX = panelX + 20;
        resetY = buttonY;
        resetW = buttonW;
        resetH = buttonH;

        doneX = panelX + panelW - 20 - buttonW;
        doneY = buttonY;
        doneW = buttonW;
        doneH = buttonH;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x99000000);

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, PANEL_BORDER);

        String title = "IMPACT VISUALS";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, panelX + (panelW - titleWidth) / 2, panelY + 14, ACCENT, false);

        for (ToggleRow row : toggles) {
            row.render(context, this, panelX, panelW, mouseX, mouseY);
        }

        if (slider != null) {
            slider.render(context, this, panelX, panelW, mouseX, mouseY);
        }

        drawButton(context, resetX, resetY, resetW, resetH, "RESET", mouseX, mouseY);
        drawButton(context, doneX, doneY, doneW, doneH, "DONE", mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawButton(DrawContext context, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int bg = hovered ? ACCENT_DIM : TRACK_OFF;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, ACCENT);
        int textWidth = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, label, x + (w - textWidth) / 2, y + (h - 8) / 2, TEXT_MAIN, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        for (ToggleRow row : toggles) {
            if (row.isInside(panelX, panelW, mouseX, mouseY)) {
                row.toggle();
                return true;
            }
        }

        if (slider != null && slider.isInsideTrack(panelX, panelW, mouseX, mouseY)) {
            draggingSlider = true;
            slider.updateFromMouse(panelX, panelW, mouseX);
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
            slider.updateFromMouse(panelX, panelW, mouseX);
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
        cfg.targetHudRangeBlocks = 6;
        this.init();
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

    private class ToggleRow {
        final String label;
        final int y;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;

        ToggleRow(String label, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.label = label;
            this.y = y;
            this.getter = getter;
            this.setter = setter;
        }

        void toggle() {
            setter.accept(!getter.getAsBoolean());
        }

        boolean isInside(int panelX, int panelW, double mouseX, double mouseY) {
            int trackW = 36;
            int trackH = 16;
            int trackX = panelX + panelW - 20 - trackW;
            int trackY = y;
            return mouseX >= trackX && mouseX < trackX + trackW && mouseY >= trackY && mouseY < trackY + trackH;
        }

        void render(DrawContext context, ConfigScreen screen, int panelX, int panelW, int mouseX, int mouseY) {
            context.drawText(screen.textRenderer, label, panelX + 20, y + 4, TEXT_MAIN, false);

            int trackW = 36;
            int trackH = 16;
            int trackX = panelX + panelW - 20 - trackW;
            int trackY = y;
            boolean on = getter.getAsBoolean();

            int trackColor = on ? ACCENT : TRACK_OFF;
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, trackColor);
            screen.drawBorder(context, trackX, trackY, trackW, trackH, on ? ACCENT : TEXT_DIM);

            int knobSize = 12;
            int knobX = on ? (trackX + trackW - knobSize - 2) : (trackX + 2);
            int knobY = trackY + (trackH - knobSize) / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }

    private class SliderRow {
        final String label;
        final int y;
        final int min;
        final int max;
        int value;
        final Consumer<Integer> setter;

        SliderRow(String label, int y, int min, int max, int initial, Consumer<Integer> setter) {
            this.label = label;
            this.y = y;
            this.min = min;
            this.max = max;
            this.value = initial;
            this.setter = setter;
        }

        boolean isInsideTrack(int panelX, int panelW, double mouseX, double mouseY) {
            int trackX = panelX + 20;
            int trackW = panelW - 40;
            int trackY = y + 14;
            int trackH = 8;
            return mouseX >= trackX && mouseX < trackX + trackW && mouseY >= trackY - 6 && mouseY < trackY + trackH + 6;
        }

        void updateFromMouse(int panelX, int panelW, double mouseX) {
            int trackX = panelX + 20;
            int trackW = panelW - 40;
            double pct = (mouseX - trackX) / (double) trackW;
            pct = Math.max(0, Math.min(1, pct));
            value = (int) Math.round(min + pct * (max - min));
            setter.accept(value);
        }

        void render(DrawContext context, ConfigScreen screen, int panelX, int panelW, int mouseX, int mouseY) {
            String text = label + ": " + value;
            context.drawText(screen.textRenderer, text, panelX + 20, y - 2, TEXT_MAIN, false);

            int trackX = panelX + 20;
            int trackW = panelW - 40;
            int trackY = y + 14;
            int trackH = 8;

            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, TRACK_OFF);
            double pct = (value - min) / (double) (max - min);
            int filledW = (int) (trackW * pct);
            context.fill(trackX, trackY, trackX + filledW, trackY + trackH, ACCENT);
            screen.drawBorder(context, trackX, trackY, trackW, trackH, ACCENT);

            int knobSize = 12;
            int knobX = trackX + filledW - knobSize / 2;
            int knobY = trackY + trackH / 2 - knobSize / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }
}
