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

    private static final int[] PALETTE = {
            0xFFFF8C00, // Orange
            0xFFB266FF, // Purple
            0xFF3399FF, // Blue
            0xFF55DD55, // Green
            0xFFFF5555, // Red
            0xFF33DDDD  // Cyan
    };

    private static final int PANEL_BG = 0xE6141414;
    private static final int SIDEBAR_BG = 0xF01A1A1A;
    private static final int TRACK_OFF = 0xFF3A3A3A;
    private static final int TEXT_MAIN = 0xFFEFEFEF;
    private static final int TEXT_DIM = 0xFFA0A0A0;

    private static final net.minecraft.util.Identifier LOGO_TEXTURE =
            net.minecraft.util.Identifier.of("impactvisuals", "textures/gui/logo.png");

    private static final String[] CATEGORY_NAMES = {"COMBAT", "HUD", "EXTRA", "MISC", "QOL", "THEME", "MORE", "FX", "STYLE", "SOUND"};

    private final Screen parent;
    private final ModConfig cfg;

    private int accentColor;
    private int accentDimColor;

    private int panelX, panelY, panelW, panelH;
    private int sidebarW;
    private int sidebarItemY, sidebarItemH;
    private int skinPanelW, skinPanelH, skinPanelX, skinPanelY;

    private int currentCategory = 0;

    private final List<ToggleRow> toggles = new ArrayList<>();
    private final List<SliderRow> sliders = new ArrayList<>();
    private final List<SwatchButton> swatches = new ArrayList<>();
    private final List<CycleRow> cycles = new ArrayList<>();

    private int doneX, doneY, doneW, doneH;
    private int resetX, resetY, resetW, resetH;

    private SliderRow draggingSlider = null;
    private boolean draggingSkin = false;
    private float skinMouseX;
    private float skinMouseY;
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
        panelH = 320;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        sidebarW = 100;
        sidebarItemY = panelY + 46;
        sidebarItemH = 22;

        skinPanelW = 100;
        skinPanelH = 160;
        skinPanelX = panelX + panelW - skinPanelW - 10;
        skinPanelY = panelY + 34;

        skinMouseX = skinPanelX + skinPanelW / 2f;
        skinMouseY = skinPanelY + skinPanelH / 3f;

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
        sliders.clear();
        swatches.clear();
        cycles.clear();

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

            sliders.add(new SliderRow("Target HUD Range", contentX, gridStartY + 3 * rowH + 14, panelW - sidebarW - 60, 1, 15,
                    cfg.targetHudRangeBlocks, v -> cfg.targetHudRangeBlocks = v));
        } else if (currentCategory == 2) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Purple Sky", () -> cfg.purpleSkyEnabled, v -> cfg.purpleSkyEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Low HP Vignette", () -> cfg.lowHealthVignetteEnabled, v -> cfg.lowHealthVignetteEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Durability %", () -> cfg.durabilityHudEnabled, v -> cfg.durabilityHudEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Cooldown Bar", () -> cfg.cooldownIndicatorEnabled, v -> cfg.cooldownIndicatorEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Crosshair Dot", () -> cfg.crosshairDotEnabled, v -> cfg.crosshairDotEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Kill Feed", () -> cfg.killFeedEnabled, v -> cfg.killFeedEnabled = v);
        } else if (currentCategory == 3) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Sprint Indicator", () -> cfg.sprintIndicatorEnabled, v -> cfg.sprintIndicatorEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Health %", () -> cfg.healthPercentEnabled, v -> cfg.healthPercentEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Hunger %", () -> cfg.hungerPercentEnabled, v -> cfg.hungerPercentEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "XP %", () -> cfg.xpPercentEnabled, v -> cfg.xpPercentEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Armor HUD", () -> cfg.armorHudEnabled, v -> cfg.armorHudEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Biome", () -> cfg.biomeHudEnabled, v -> cfg.biomeHudEnabled = v);
        } else if (currentCategory == 4) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Light Level", () -> cfg.lightLevelHudEnabled, v -> cfg.lightLevelHudEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Held Item Name", () -> cfg.heldItemNameEnabled, v -> cfg.heldItemNameEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Offhand Item Name", () -> cfg.offhandItemNameEnabled, v -> cfg.offhandItemNameEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Total Playtime", () -> cfg.totalPlaytimeEnabled, v -> cfg.totalPlaytimeEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Zoom (hold C)", () -> cfg.zoomEnabled, v -> cfg.zoomEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Real Clock", () -> cfg.realClockEnabled, v -> cfg.realClockEnabled = v);
        } else if (currentCategory == 5) {
            int swatchSize = 40;
            int swatchGap = 14;
            int cols = 3;
            for (int i = 0; i < PALETTE.length; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = contentX + col * (swatchSize + swatchGap);
                int y = gridStartY + row * (swatchSize + swatchGap);
                swatches.add(new SwatchButton(i, x, y, swatchSize));
            }
        } else if (currentCategory == 6) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Crit Sound", () -> cfg.critSoundEnabled, v -> cfg.critSoundEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Small Fire", () -> cfg.smallFireEnabled, v -> cfg.smallFireEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Custom Handle", () -> cfg.customHandleEnabled, v -> cfg.customHandleEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Sprint Trail", () -> cfg.sprintTrailEnabled, v -> cfg.sprintTrailEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Footstep Dust", () -> cfg.footstepDustEnabled, v -> cfg.footstepDustEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Rainbow Theme", () -> cfg.rainbowThemeEnabled, v -> cfg.rainbowThemeEnabled = v);

            if (cfg.customHandleEnabled) {
                int sliderY = gridStartY + 2 * rowH + 18;
                int sliderW = panelW - sidebarW - 60;
                sliders.add(new SliderRow("Scale %", contentX, sliderY, sliderW, 30, 200,
                        cfg.customHandleScalePercent, v -> cfg.customHandleScalePercent = v));
                sliders.add(new SliderRow("Rotate X", contentX, sliderY + 34, sliderW, 0, 360,
                        cfg.customHandleRotX, v -> cfg.customHandleRotX = v));
                sliders.add(new SliderRow("Rotate Y", contentX, sliderY + 68, sliderW, 0, 360,
                        cfg.customHandleRotY, v -> cfg.customHandleRotY = v));
                sliders.add(new SliderRow("Rotate Z", contentX, sliderY + 102, sliderW, 0, 360,
                        cfg.customHandleRotZ, v -> cfg.customHandleRotZ = v));
            }
        } else if (currentCategory == 7) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Damage Flash", () -> cfg.damageFlashEnabled, v -> cfg.damageFlashEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Impact Punch", () -> cfg.hitImpactPunchEnabled, v -> cfg.hitImpactPunchEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Kill Streak", () -> cfg.killStreakEnabled, v -> cfg.killStreakEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Big Kill Burst", () -> cfg.bigKillBurstEnabled, v -> cfg.bigKillBurstEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Pulsing Vignette", () -> cfg.pulsingVignetteEnabled, v -> cfg.pulsingVignetteEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Sweep Trail", () -> cfg.sweepTrailEnabled, v -> cfg.sweepTrailEnabled = v);
        } else if (currentCategory == 8) {
            String[] crosshairNames = {"Off", "Dot", "Cross", "Ring"};
            String[] colorNames = {"Vanilla", "Orange", "Purple", "Blue", "Green", "Red", "Cyan"};
            cycles.add(new CycleRow("Crosshair Style", contentX, gridStartY, crosshairNames,
                    () -> cfg.crosshairStyleIndex, v -> cfg.crosshairStyleIndex = v));
            cycles.add(new CycleRow("Hit Particle Color", contentX, gridStartY + rowH + 10, colorNames,
                    () -> cfg.hitParticleColorIndex, v -> cfg.hitParticleColorIndex = v));

            int belowY = gridStartY + 2 * (rowH + 10) + 10;
            toggles.add(new ToggleRow("Colored Trails", contentX, belowY, () -> cfg.coloredTrailsEnabled, v -> cfg.coloredTrailsEnabled = v));
            toggles.add(new ToggleRow("Hand Glow", contentX, belowY + rowH, () -> cfg.handGlowEnabled, v -> cfg.handGlowEnabled = v));
        } else if (currentCategory == 9) {
            addToggle(0, contentX, colGap, gridStartY, rowH, "Kill Sound", () -> cfg.killSoundEnabled, v -> cfg.killSoundEnabled = v);
            addToggle(1, contentX, colGap, gridStartY, rowH, "Streak Sound", () -> cfg.streakSoundEnabled, v -> cfg.streakSoundEnabled = v);
            addToggle(2, contentX, colGap, gridStartY, rowH, "Heartbeat Sound", () -> cfg.heartbeatSoundEnabled, v -> cfg.heartbeatSoundEnabled = v);
            addToggle(3, contentX, colGap, gridStartY, rowH, "Menu Sound", () -> cfg.menuSoundEnabled, v -> cfg.menuSoundEnabled = v);
            addToggle(4, contentX, colGap, gridStartY, rowH, "Footstep Sound", () -> cfg.footstepSoundEnabled, v -> cfg.footstepSoundEnabled = v);
            addToggle(5, contentX, colGap, gridStartY, rowH, "Heal Flash", () -> cfg.healFlashEnabled, v -> cfg.healFlashEnabled = v);
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

    private void updateThemeColors() {
        if (cfg.rainbowThemeEnabled) {
            float hue = (System.currentTimeMillis() % 6000) / 6000f;
            accentColor = 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.65f, 1.0f) & 0xFFFFFF);
        } else {
            accentColor = PALETTE[Math.max(0, Math.min(PALETTE.length - 1, cfg.accentColorIndex))];
        }
        int r = (int) ((accentColor >> 16 & 0xFF) * 0.45);
        int g = (int) ((accentColor >> 8 & 0xFF) * 0.45);
        int b = (int) ((accentColor & 0xFF) * 0.45);
        accentDimColor = 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateThemeColors();

        context.fill(0, 0, this.width, this.height, 0x99000000);

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, accentColor);

        context.fill(panelX, panelY, panelX + sidebarW, panelY + panelH, SIDEBAR_BG);
        context.fill(panelX + sidebarW, panelY, panelX + sidebarW + 1, panelY + panelH, accentColor);

        int logoSize = 36;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                panelX + 8, panelY + 4, 0, 0, logoSize, logoSize, 256, 256, 256, 256);

        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            int itemY = sidebarItemY + i * sidebarItemH;
            boolean active = i == currentCategory;
            boolean hovered = inside(panelX, itemY, sidebarW, sidebarItemH, mouseX, mouseY);

            if (active) {
                context.fill(panelX, itemY, panelX + sidebarW, itemY + sidebarItemH, accentDimColor);
                context.fill(panelX, itemY, panelX + 2, itemY + sidebarItemH, accentColor);
            } else if (hovered) {
                context.fill(panelX, itemY, panelX + sidebarW, itemY + sidebarItemH, 0x30FFFFFF);
            }

            int color = active ? accentColor : TEXT_DIM;
            context.drawText(this.textRenderer, CATEGORY_NAMES[i], panelX + 14, itemY + 8, color, false);
        }

        for (ToggleRow row : toggles) {
            row.render(context, this, mouseX, mouseY);
        }

        for (SliderRow row : sliders) {
            row.render(context, this, mouseX, mouseY);
        }

        for (SwatchButton swatch : swatches) {
            swatch.render(context, this, mouseX, mouseY);
        }

        for (CycleRow row : cycles) {
            row.render(context, this, mouseX, mouseY);
        }

        drawButton(context, resetX, resetY, resetW, resetH, "RESET", mouseX, mouseY);
        drawButton(context, doneX, doneY, doneW, doneH, "DONE", mouseX, mouseY);

        renderSkinPanel(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSkinPanel(DrawContext context) {
        context.fill(skinPanelX, skinPanelY, skinPanelX + skinPanelW, skinPanelY + skinPanelH, 0xFF1E1E1E);
        drawBorder(context, skinPanelX, skinPanelY, skinPanelW, skinPanelH, accentColor);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int entitySize = (int) (skinPanelW * 0.6);
        net.minecraft.client.gui.screen.ingame.InventoryScreen.drawEntity(
                context,
                skinPanelX, skinPanelY,
                skinPanelX + skinPanelW, skinPanelY + skinPanelH,
                entitySize, 0.0625f,
                skinMouseX, skinMouseY,
                client.player
        );

        String name = client.getSession().getUsername();
        int nameWidth = this.textRenderer.getWidth(name);
        int nameX = skinPanelX + (skinPanelW - nameWidth) / 2;
        context.drawText(this.textRenderer, name, nameX, skinPanelY + skinPanelH + 6, TEXT_MAIN, false);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawButton(DrawContext context, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = inside(x, y, w, h, mouseX, mouseY);
        int bg = hovered ? accentDimColor : TRACK_OFF;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, accentColor);
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

        if (inside(skinPanelX, skinPanelY, skinPanelW, skinPanelH, mouseX, mouseY)) {
            draggingSkin = true;
            return true;
        }

        for (ToggleRow row : toggles) {
            if (row.isInside(mouseX, mouseY)) {
                row.toggle();
                return true;
            }
        }

        for (SwatchButton swatch : swatches) {
            if (swatch.isInside(mouseX, mouseY)) {
                cfg.accentColorIndex = swatch.paletteIndex;
                return true;
            }
        }

        for (CycleRow row : cycles) {
            if (row.isInside(mouseX, mouseY)) {
                row.advance();
                return true;
            }
        }

        for (SliderRow row : sliders) {
            if (row.isInsideTrack(mouseX, mouseY)) {
                draggingSlider = row;
                row.updateFromMouse(mouseX);
                return true;
            }
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
        if (draggingSkin) {
            skinMouseX += (float) deltaX;
            skinMouseY += (float) deltaY;
            return true;
        }
        if (draggingSlider != null) {
            draggingSlider.updateFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        draggingSkin = false;
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
        cfg.sprintIndicatorEnabled = false;
        cfg.healthPercentEnabled = false;
        cfg.hungerPercentEnabled = false;
        cfg.xpPercentEnabled = false;
        cfg.armorHudEnabled = false;
        cfg.biomeHudEnabled = false;
        cfg.crosshairDotEnabled = false;
        cfg.killFeedEnabled = true;
        cfg.lightLevelHudEnabled = false;
        cfg.heldItemNameEnabled = false;
        cfg.offhandItemNameEnabled = false;
        cfg.totalPlaytimeEnabled = false;
        cfg.zoomEnabled = true;
        cfg.realClockEnabled = false;
        cfg.accentColorIndex = 0;
        cfg.critSoundEnabled = true;
        cfg.smallFireEnabled = false;
        cfg.customHandleEnabled = false;
        cfg.customHandleScalePercent = 100;
        cfg.customHandleRotX = 0;
        cfg.customHandleRotY = 0;
        cfg.customHandleRotZ = 0;
        cfg.damageFlashEnabled = true;
        cfg.hitImpactPunchEnabled = true;
        cfg.killStreakEnabled = true;
        cfg.bigKillBurstEnabled = true;
        cfg.pulsingVignetteEnabled = false;
        cfg.sweepTrailEnabled = false;
        cfg.rainbowThemeEnabled = false;
        cfg.sprintTrailEnabled = false;
        cfg.footstepDustEnabled = false;
        cfg.crosshairStyleIndex = 0;
        cfg.hitmarkerStyleIndex = 0;
        cfg.hitParticleColorIndex = 0;
        cfg.killSoundEnabled = true;
        cfg.heartbeatSoundEnabled = false;
        cfg.streakSoundEnabled = true;
        cfg.menuSoundEnabled = true;
        cfg.footstepSoundEnabled = false;
        cfg.healFlashEnabled = true;
        cfg.coloredTrailsEnabled = false;
        cfg.handGlowEnabled = false;
        cfg.targetHudRangeBlocks = 6;
        buildCategoryContent();
    }

    @Override
    public void close() {
        cfg.save();
        com.impactvisuals.client.visual.UiSoundPlayer.play();
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
                context.fill(bx, y, bx + size, y + size, screen.accentColor);
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
            context.fill(x, trackY, x + filledW, trackY + trackH, screen.accentColor);
            screen.drawBorder(context, x, trackY, width, trackH, screen.accentColor);

            int knobSize = 12;
            int knobX = x + filledW - knobSize / 2;
            int knobY = trackY + trackH / 2 - knobSize / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }

    private class SwatchButton {
        final int paletteIndex;
        final int x, y, size;

        SwatchButton(int paletteIndex, int x, int y, int size) {
            this.paletteIndex = paletteIndex;
            this.x = x;
            this.y = y;
            this.size = size;
        }

        boolean isInside(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        }

        void render(DrawContext context, ConfigScreen screen, int mouseX, int mouseY) {
            int color = PALETTE[paletteIndex];
            context.fill(x, y, x + size, y + size, color);
            boolean selected = screen.cfg.accentColorIndex == paletteIndex;
            screen.drawBorder(context, x, y, size, size, selected ? 0xFFFFFFFF : 0xFF000000);
        }
    }

    private class CycleRow {
        final String label;
        final int x, y;
        final String[] options;
        final java.util.function.IntSupplier getter;
        final java.util.function.IntConsumer setter;

        CycleRow(String label, int x, int y, String[] options, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.options = options;
            this.getter = getter;
            this.setter = setter;
        }

        void advance() {
            int next = (getter.getAsInt() + 1) % options.length;
            setter.accept(next);
        }

        boolean isInside(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + 260 && mouseY >= y && mouseY < y + 18;
        }

        void render(DrawContext context, ConfigScreen screen, int mouseX, int mouseY) {
            int current = Math.max(0, Math.min(options.length - 1, getter.getAsInt()));
            String text = label + ": " + options[current] + "  (tap to change)";
            context.drawText(screen.textRenderer, text, x, y + 4, TEXT_MAIN, false);
        }
    }
                                          }
