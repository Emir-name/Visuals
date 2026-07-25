package com.impactvisuals.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Full-screen settings UI: sidebar of categories on the left, searchable
 * grid of feature cards on the right, live skin preview in the top-right
 * corner. Same features/categories as before - only the look changed.
 */
public class ConfigScreen extends Screen {

    private static final int[] PALETTE = {
            0xFFFF8C00, 0xFFB266FF, 0xFF3399FF, 0xFF55DD55, 0xFFFF5555, 0xFF33DDDD
    };

    private static final int BG = 0xFF101012;
    private static final int SIDEBAR_BG = 0xFF17171A;
    private static final int CARD_BG = 0xFF1D1D21;
    private static final int CARD_BG_HOVER = 0xFF232328;
    private static final int TRACK_OFF = 0xFF3A3A3E;
    private static final int TEXT_MAIN = 0xFFEFEFEF;
    private static final int TEXT_DIM = 0xFF9A9AA0;

    private static final net.minecraft.util.Identifier LOGO_TEXTURE =
            net.minecraft.util.Identifier.of("impactvisuals", "textures/gui/logo.png");

    private static final String[] CATEGORY_NAMES = {"COMBAT FX", "COMBAT+", "HUD INFO", "HUD STATS", "HUD EXTRA", "ENVIRONMENT", "COSMETIC", "STYLE", "SOUND", "THEME", "SKINS"};

    private final Screen parent;
    private final ModConfig cfg;

    private int accentColor;
    private int accentDimColor;

    // Layout
    private int sidebarW;
    private int headerH;
    private int contentX, contentTop, contentBottom, contentW;
    private int cardW, cardH, colGap, rowGap;
    private int skinPanelW, skinPanelH, skinPanelX, skinPanelY;
    private int closeX, closeY, closeW, closeH;
    private int resetX, resetY, resetW, resetH;
    private int langX, langY, langW, langH;

    private int currentCategory = 0;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private TextFieldWidget searchField;
    private String searchQuery = "";

    private final List<ToggleCard> toggles = new ArrayList<>();
    private final List<SliderRow> sliders = new ArrayList<>();
    private final List<SwatchButton> swatches = new ArrayList<>();
    private final List<CycleRow> cycles = new ArrayList<>();

    // computed each render() call, reused by mouseClicked()/mouseDragged()
    private final List<Placed<ToggleCard>> placedToggles = new ArrayList<>();
    private final List<Placed<SliderRow>> placedSliders = new ArrayList<>();
    private final List<Placed<CycleRow>> placedCycles = new ArrayList<>();
    private int swatchAreaY;

    private SliderRow draggingSlider = null;
    private boolean draggingSkin = false;
    private float skinMouseX;
    private float skinMouseY;
    private int previousBlurriness = 0;

    private long lastFrameNanos = System.nanoTime();
    private long categoryStartNanos = System.nanoTime();
    private static final long FADE_DURATION_NANOS = 220_000_000L;

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

        sidebarW = Math.max(120, Math.min(170, this.width / 5));
        headerH = 40;

        skinPanelW = 100;
        skinPanelH = 128;
        skinPanelX = this.width - skinPanelW - 16;
        skinPanelY = headerH + 12;
        skinMouseX = skinPanelX + skinPanelW / 2f;
        skinMouseY = skinPanelY + skinPanelH / 3f;

        contentX = sidebarW + 20;
        contentTop = headerH + 10;
        contentBottom = this.height - 10;
        contentW = Math.max(200, (skinPanelX - 16) - contentX);

        colGap = 14;
        cardW = (contentW - colGap) / 2;
        cardH = 40;
        rowGap = 10;

        int headerButtonH = 18;
        int headerY = (headerH - headerButtonH) / 2;

        langW = 34;
        langH = headerButtonH;
        langX = this.width - langW - 12;
        langY = headerY;

        resetW = 60;
        resetH = headerButtonH;
        resetX = langX - resetW - 8;
        resetY = headerY;

        closeW = 20;
        closeH = headerButtonH;
        closeX = resetX - closeW - 8;
        closeY = headerY;

        int searchW = Math.max(90, closeX - contentX - 16);
        searchField = new TextFieldWidget(this.textRenderer, contentX, headerY, searchW, headerButtonH, Text.literal(""));
        searchField.setMaxLength(40);
        searchField.setPlaceholder(Text.literal(Lang.t("Search")));
        searchField.setChangedListener(s -> {
            searchQuery = s.toLowerCase();
            scrollOffset = 0;
        });
        addDrawableChild(searchField);

        scrollOffset = 0;
        categoryStartNanos = System.nanoTime();
        buildCategoryContent();
    }

    private void buildCategoryContent() {
        toggles.clear();
        sliders.clear();
        swatches.clear();
        cycles.clear();

        if (currentCategory == 0) {
            addToggle("Hit Particles", () -> cfg.hitParticlesEnabled, v -> cfg.hitParticlesEnabled = v);
            addToggle("Damage Numbers", () -> cfg.damageNumbersEnabled, v -> cfg.damageNumbersEnabled = v);
            addToggle("Critical Flash", () -> cfg.criticalFlashEnabled, v -> cfg.criticalFlashEnabled = v);
            addToggle("Hitmarker Flash", () -> cfg.hitmarkerEnabled, v -> cfg.hitmarkerEnabled = v);
            addToggle("Damage Flash", () -> cfg.damageFlashEnabled, v -> cfg.damageFlashEnabled = v);
            addToggle("Impact Punch", () -> cfg.hitImpactPunchEnabled, v -> cfg.hitImpactPunchEnabled = v);
        } else if (currentCategory == 1) {
            addToggle("Trajectory Predict", () -> cfg.trajectoryPredictionEnabled, v -> cfg.trajectoryPredictionEnabled = v);
            addToggle("Kill Streak", () -> cfg.killStreakEnabled, v -> cfg.killStreakEnabled = v);
            addToggle("Big Kill Burst", () -> cfg.bigKillBurstEnabled, v -> cfg.bigKillBurstEnabled = v);
            addToggle("Pulsing Vignette", () -> cfg.pulsingVignetteEnabled, v -> cfg.pulsingVignetteEnabled = v);
            addToggle("Sweep Trail", () -> cfg.sweepTrailEnabled, v -> cfg.sweepTrailEnabled = v);
            addToggle("Heal Flash", () -> cfg.healFlashEnabled, v -> cfg.healFlashEnabled = v);
        } else if (currentCategory == 2) {
            addToggle("Target HUD", () -> cfg.targetHudEnabled, v -> cfg.targetHudEnabled = v);
            addToggle("Info HUD", () -> cfg.infoHudEnabled, v -> cfg.infoHudEnabled = v);
            addToggle("Coordinates", () -> cfg.coordinatesHudEnabled, v -> cfg.coordinatesHudEnabled = v);
            addToggle("Compass", () -> cfg.compassHudEnabled, v -> cfg.compassHudEnabled = v);
            addToggle("Session Timer", () -> cfg.sessionTimerEnabled, v -> cfg.sessionTimerEnabled = v);
            addToggle("K/D Counter", () -> cfg.killDeathCounterEnabled, v -> cfg.killDeathCounterEnabled = v);
            sliders.add(new SliderRow("Target HUD Range", 1, 15, cfg.targetHudRangeBlocks, v -> cfg.targetHudRangeBlocks = v));
        } else if (currentCategory == 3) {
            addToggle("Sprint Indicator", () -> cfg.sprintIndicatorEnabled, v -> cfg.sprintIndicatorEnabled = v);
            addToggle("Health %", () -> cfg.healthPercentEnabled, v -> cfg.healthPercentEnabled = v);
            addToggle("Hunger %", () -> cfg.hungerPercentEnabled, v -> cfg.hungerPercentEnabled = v);
            addToggle("XP %", () -> cfg.xpPercentEnabled, v -> cfg.xpPercentEnabled = v);
            addToggle("Armor HUD", () -> cfg.armorHudEnabled, v -> cfg.armorHudEnabled = v);
            addToggle("Biome", () -> cfg.biomeHudEnabled, v -> cfg.biomeHudEnabled = v);
            addToggle("Active Effects", () -> cfg.activeEffectsHudEnabled, v -> cfg.activeEffectsHudEnabled = v);
        } else if (currentCategory == 4) {
            addToggle("Light Level", () -> cfg.lightLevelHudEnabled, v -> cfg.lightLevelHudEnabled = v);
            addToggle("Held Item Name", () -> cfg.heldItemNameEnabled, v -> cfg.heldItemNameEnabled = v);
            addToggle("Offhand Item Name", () -> cfg.offhandItemNameEnabled, v -> cfg.offhandItemNameEnabled = v);
            addToggle("Total Playtime", () -> cfg.totalPlaytimeEnabled, v -> cfg.totalPlaytimeEnabled = v);
            addToggle("Zoom (hold C)", () -> cfg.zoomEnabled, v -> cfg.zoomEnabled = v);
            addToggle("Real Clock", () -> cfg.realClockEnabled, v -> cfg.realClockEnabled = v);
        } else if (currentCategory == 5) {
            addToggle("Purple Sky", () -> cfg.purpleSkyEnabled, v -> cfg.purpleSkyEnabled = v);
            addToggle("Low HP Vignette", () -> cfg.lowHealthVignetteEnabled, v -> cfg.lowHealthVignetteEnabled = v);
            addToggle("Durability %", () -> cfg.durabilityHudEnabled, v -> cfg.durabilityHudEnabled = v);
            addToggle("Cooldown Bar", () -> cfg.cooldownIndicatorEnabled, v -> cfg.cooldownIndicatorEnabled = v);
            addToggle("Kill Feed", () -> cfg.killFeedEnabled, v -> cfg.killFeedEnabled = v);
            addToggle("Small Fire", () -> cfg.smallFireEnabled, v -> cfg.smallFireEnabled = v);
        } else if (currentCategory == 6) {
            addToggle("Custom Handle", () -> cfg.customHandleEnabled, v -> cfg.customHandleEnabled = v);
            addToggle("Rainbow Theme", () -> cfg.rainbowThemeEnabled, v -> cfg.rainbowThemeEnabled = v);
            addToggle("Sprint Trail", () -> cfg.sprintTrailEnabled, v -> cfg.sprintTrailEnabled = v);
            addToggle("Footstep Dust", () -> cfg.footstepDustEnabled, v -> cfg.footstepDustEnabled = v);
            addToggle("Colored Trails", () -> cfg.coloredTrailsEnabled, v -> cfg.coloredTrailsEnabled = v);
            addToggle("Hand Glow", () -> cfg.handGlowEnabled, v -> cfg.handGlowEnabled = v);

            if (cfg.customHandleEnabled) {
                sliders.add(new SliderRow("Scale %", 30, 200, cfg.customHandleScalePercent, v -> cfg.customHandleScalePercent = v));
                sliders.add(new SliderRow("Rotate X", 0, 360, cfg.customHandleRotX, v -> cfg.customHandleRotX = v));
                sliders.add(new SliderRow("Rotate Y", 0, 360, cfg.customHandleRotY, v -> cfg.customHandleRotY = v));
                sliders.add(new SliderRow("Rotate Z", 0, 360, cfg.customHandleRotZ, v -> cfg.customHandleRotZ = v));
            }
        } else if (currentCategory == 7) {
            String[] crosshairNames = {"Off", "Dot", "Cross", "Ring"};
            String[] colorNames = {"Vanilla", "Orange", "Purple", "Blue", "Green", "Red", "Cyan"};
            cycles.add(new CycleRow("Crosshair Style", crosshairNames, () -> cfg.crosshairStyleIndex, v -> cfg.crosshairStyleIndex = v));
            cycles.add(new CycleRow("Hit Particle Color", colorNames, () -> cfg.hitParticleColorIndex, v -> cfg.hitParticleColorIndex = v));
        } else if (currentCategory == 8) {
            addToggle("Hit Sound", () -> cfg.hitSoundEnabled, v -> cfg.hitSoundEnabled = v);
            addToggle("Crit Sound", () -> cfg.critSoundEnabled, v -> cfg.critSoundEnabled = v);
            addToggle("Kill Sound", () -> cfg.killSoundEnabled, v -> cfg.killSoundEnabled = v);
            addToggle("Streak Sound", () -> cfg.streakSoundEnabled, v -> cfg.streakSoundEnabled = v);
            addToggle("Heartbeat Sound", () -> cfg.heartbeatSoundEnabled, v -> cfg.heartbeatSoundEnabled = v);
            addToggle("Menu Sound", () -> cfg.menuSoundEnabled, v -> cfg.menuSoundEnabled = v);
            addToggle("Footstep Sound", () -> cfg.footstepSoundEnabled, v -> cfg.footstepSoundEnabled = v);
        } else if (currentCategory == 9) {
            for (int i = 0; i < PALETTE.length; i++) {
                swatches.add(new SwatchButton(i));
            }
        } else if (currentCategory == 10) {
            String[] skinNames = {"Default", "Preset 1", "Preset 2", "Preset 3", "Preset 4",
                    "Preset 5", "Preset 6", "Preset 7", "Preset 8", "Custom"};
            cycles.add(new CycleRow("Skin (self-view only)", skinNames, () -> cfg.selectedSkinIndex, v -> cfg.selectedSkinIndex = v));
        }
    }

    private void addToggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        toggles.add(new ToggleCard(label, getter, setter));
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

    /** True if a toggle card's label or description matches the current search query. */
    private boolean matchesSearch(String label) {
        if (searchQuery.isEmpty()) return true;
        if (Lang.t(label).toLowerCase().contains(searchQuery)) return true;
        return Lang.desc(label).toLowerCase().contains(searchQuery);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateThemeColors();

        long nowNanos = System.nanoTime();
        float dt = (nowNanos - lastFrameNanos) / 1_000_000_000f;
        if (dt > 0.1f) dt = 0.1f;
        if (dt < 0f) dt = 0f;
        lastFrameNanos = nowNanos;

        context.fill(0, 0, this.width, this.height, BG);

        // sidebar
        context.fill(0, 0, sidebarW, this.height, SIDEBAR_BG);
        context.fill(sidebarW, 0, sidebarW + 1, this.height, 0x40000000 | (accentColor & 0xFFFFFF));

        int logoSize = 26;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE,
                12, 8, 0, 0, logoSize, logoSize, 256, 256, 256, 256);
        context.drawText(this.textRenderer, Text.literal("Impact Visuals").formatted(Formatting.BOLD),
                12 + logoSize + 8, 8 + (logoSize - 8) / 2, TEXT_MAIN, false);

        int navStartY = 8 + logoSize + 14;
        int navItemH = 24;
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            int itemY = navStartY + i * navItemH;
            boolean active = i == currentCategory;
            boolean hovered = inside(0, itemY, sidebarW, navItemH, mouseX, mouseY);

            if (active) {
                context.fill(6, itemY + 2, sidebarW - 6, itemY + navItemH - 2, accentDimColor);
            } else if (hovered) {
                context.fill(6, itemY + 2, sidebarW - 6, itemY + navItemH - 2, 0x20FFFFFF);
            }

            int dotSize = 6;
            int dotY = itemY + (navItemH - dotSize) / 2;
            context.fill(14, dotY, 14 + dotSize, dotY + dotSize, active ? accentColor : TEXT_DIM);

            int color = active ? TEXT_MAIN : TEXT_DIM;
            context.drawText(this.textRenderer, Lang.t(CATEGORY_NAMES[i]), 14 + dotSize + 8, itemY + (navItemH - 8) / 2, color, false);
        }

        // header
        String title = Lang.t(CATEGORY_NAMES[currentCategory]);
        context.getMatrices().push();
        context.getMatrices().translate(contentX, 12, 0);
        context.getMatrices().scale(1.4f, 1.4f, 1f);
        context.drawText(this.textRenderer, Text.literal(title).formatted(Formatting.BOLD), 0, 0, TEXT_MAIN, false);
        context.getMatrices().pop();

        drawHeaderButton(context, closeX, closeY, closeW, closeH, "x", mouseX, mouseY);
        drawHeaderButton(context, resetX, resetY, resetW, resetH, "RESET", mouseX, mouseY);
        drawHeaderButton(context, langX, langY, langW, langH, cfg.russianLanguage ? "RU" : "EN", mouseX, mouseY);

        // content (scissored + scrollable)
        context.enableScissor(contentX, contentTop, contentX + contentW + skinPanelW + 16, contentBottom);

        placedToggles.clear();
        placedSliders.clear();
        placedCycles.clear();

        int y = contentTop - scrollOffset;

        List<ToggleCard> visible = new ArrayList<>();
        for (ToggleCard t : toggles) {
            if (matchesSearch(t.label)) visible.add(t);
        }

        for (int i = 0; i < visible.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = contentX + col * (cardW + colGap);
            int cy = y + row * (cardH + rowGap);
            placedToggles.add(new Placed<>(visible.get(i), cx, cy, cardW, cardH));
        }
        int rows = (visible.size() + 1) / 2;
        y += rows * (cardH + rowGap);
        if (!visible.isEmpty()) y += 4;

        int sliderRowH = 38;
        for (SliderRow s : sliders) {
            placedSliders.add(new Placed<>(s, contentX, y, contentW, sliderRowH));
            y += sliderRowH;
        }
        if (!sliders.isEmpty()) y += 6;

        int cycleRowH = 34;
        for (CycleRow c : cycles) {
            placedCycles.add(new Placed<>(c, contentX, y, contentW, cycleRowH));
            y += cycleRowH + 8;
        }
        if (!cycles.isEmpty()) y += 4;

        swatchAreaY = y;
        int contentBottomY = y;
        if (!swatches.isEmpty()) {
            int swatchSize = 36;
            int swatchGap = 12;
            int cols = Math.max(1, (contentW + swatchGap) / (swatchSize + swatchGap));
            int rowsSw = (swatches.size() + cols - 1) / cols;
            contentBottomY = y + rowsSw * (swatchSize + swatchGap);
        }

        maxScroll = Math.max(0, (contentBottomY + scrollOffset - contentTop) - (contentBottom - contentTop));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (Placed<ToggleCard> p : placedToggles) {
            p.item.render(context, this, p.x, p.y, p.w, p.h, mouseX, mouseY, dt);
        }
        for (Placed<SliderRow> p : placedSliders) {
            p.item.render(context, this, p.x, p.y, p.w, mouseX, mouseY);
        }
        for (Placed<CycleRow> p : placedCycles) {
            p.item.render(context, this, p.x, p.y, p.w, p.h, mouseX, mouseY);
        }
        if (!swatches.isEmpty()) {
            int swatchSize = 36;
            int swatchGap = 12;
            int cols = Math.max(1, (contentW + swatchGap) / (swatchSize + swatchGap));
            for (int i = 0; i < swatches.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int sx = contentX + col * (swatchSize + swatchGap);
                int sy = swatchAreaY + row * (swatchSize + swatchGap);
                swatches.get(i).render(context, this, sx, sy, swatchSize, mouseX, mouseY);
            }
        }

        long fadeElapsed = nowNanos - categoryStartNanos;
        if (fadeElapsed < FADE_DURATION_NANOS) {
            float t = Math.max(0f, Math.min(1f, fadeElapsed / (float) FADE_DURATION_NANOS));
            int fadeAlpha = (int) ((1f - t) * 200);
            int overlay = (fadeAlpha << 24);
            context.fill(contentX - 6, contentTop - 6, contentX + contentW + skinPanelW + 20, contentBottom, overlay);
        }

        context.disableScissor();

        if (visible.isEmpty() && toggles.size() > 0) {
            String msg = Lang.t("No results");
            int w = this.textRenderer.getWidth(msg);
            context.drawText(this.textRenderer, msg, contentX + (contentW - w) / 2, contentTop + 20, TEXT_DIM, false);
        }

        renderSkinPanel(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHeaderButton(DrawContext context, int x, int y, int w, int h, String label, int mouseX, int mouseY) {
        boolean hovered = inside(x, y, w, h, mouseX, mouseY);
        int bg = hovered ? accentDimColor : CARD_BG;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, hovered ? accentColor : TRACK_OFF);
        String translated = Lang.t(label);
        int textWidth = this.textRenderer.getWidth(translated);
        context.drawText(this.textRenderer, translated, x + (w - textWidth) / 2, y + (h - 8) / 2, TEXT_MAIN, false);
    }

    private void renderSkinPanel(DrawContext context) {
        context.fill(skinPanelX, skinPanelY, skinPanelX + skinPanelW, skinPanelY + skinPanelH, CARD_BG);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int navStartY = 8 + 26 + 14;
        int navItemH = 24;
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            int itemY = navStartY + i * navItemH;
            if (inside(0, itemY, sidebarW, navItemH, mouseX, mouseY) && currentCategory != i) {
                currentCategory = i;
                scrollOffset = 0;
                categoryStartNanos = System.nanoTime();
                searchField.setText("");
                searchQuery = "";
                buildCategoryContent();
                return true;
            }
        }

        if (inside(skinPanelX, skinPanelY, skinPanelW, skinPanelH, mouseX, mouseY)) {
            draggingSkin = true;
            return true;
        }

        if (inside(closeX, closeY, closeW, closeH, mouseX, mouseY)) {
            close();
            return true;
        }
        if (inside(resetX, resetY, resetW, resetH, mouseX, mouseY)) {
            resetToDefaults();
            return true;
        }
        if (inside(langX, langY, langW, langH, mouseX, mouseY)) {
            cfg.russianLanguage = !cfg.russianLanguage;
            cfg.save();
            return true;
        }

        if (mouseY >= contentTop && mouseY <= contentBottom) {
            for (Placed<ToggleCard> p : placedToggles) {
                if (inside(p.x, p.y, p.w, p.h, mouseX, mouseY)) {
                    p.item.toggle();
                    return true;
                }
            }
            for (Placed<CycleRow> p : placedCycles) {
                if (inside(p.x, p.y, p.w, p.h, mouseX, mouseY)) {
                    p.item.advance();
                    return true;
                }
            }
            if (!swatches.isEmpty()) {
                int swatchSize = 36;
                int swatchGap = 12;
                int cols = Math.max(1, (contentW + swatchGap) / (swatchSize + swatchGap));
                for (int i = 0; i < swatches.size(); i++) {
                    int col = i % cols;
                    int row = i / cols;
                    int sx = contentX + col * (swatchSize + swatchGap);
                    int sy = swatchAreaY + row * (swatchSize + swatchGap);
                    if (inside(sx, sy, swatchSize, swatchSize, mouseX, mouseY)) {
                        cfg.accentColorIndex = i;
                        return true;
                    }
                }
            }
            for (Placed<SliderRow> p : placedSliders) {
                int trackY = p.y + 20;
                int trackH = 8;
                if (mouseX >= p.x && mouseX < p.x + p.w && mouseY >= trackY - 6 && mouseY < trackY + trackH + 6) {
                    draggingSlider = p.item;
                    p.item.updateFromMouse(mouseX, p.x, p.w);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= contentX && mouseY >= contentTop && mouseY <= contentBottom) {
            scrollOffset -= (int) (verticalAmount * 18);
            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSkin) {
            skinMouseX += (float) deltaX;
            skinMouseY += (float) deltaY;
            return true;
        }
        if (draggingSlider != null) {
            for (Placed<SliderRow> p : placedSliders) {
                if (p.item == draggingSlider) {
                    draggingSlider.updateFromMouse(mouseX, p.x, p.w);
                    break;
                }
            }
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
        cfg.selectedSkinIndex = 0;
        cfg.activeEffectsHudEnabled = true;
        cfg.russianLanguage = false;
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

    /** A UI element paired with its computed screen-space position for this frame. */
    private static class Placed<T> {
        final T item;
        final int x, y, w, h;

        Placed(T item, int x, int y, int w, int h) {
            this.item = item;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private class ToggleCard {
        final String label;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        float animT;

        ToggleCard(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.animT = getter.getAsBoolean() ? 1f : 0f;
        }

        void toggle() {
            setter.accept(!getter.getAsBoolean());
        }

        void render(DrawContext context, ConfigScreen screen, int x, int y, int w, int h, int mouseX, int mouseY, float dt) {
            boolean hovered = screen.inside(x, y, w, h, mouseX, mouseY);
            context.fill(x, y, x + w, y + h, hovered ? CARD_BG_HOVER : CARD_BG);
            screen.drawBorder(context, x, y, w, h, hovered ? screen.accentColor : 0xFF2A2A2E);

            context.drawText(screen.textRenderer, Text.literal(Lang.t(label)).formatted(Formatting.BOLD),
                    x + 8, y + 6, TEXT_MAIN, false);

            String desc = Lang.desc(label);
            if (!desc.isEmpty()) {
                int pillW = 30;
                int maxDescW = w - 16 - pillW;
                Text trimmed = Text.literal(screen.textRenderer.trimToWidth(desc, maxDescW));
                context.drawText(screen.textRenderer, trimmed, x + 8, y + 6 + 11, TEXT_DIM, false);
            }

            float target = getter.getAsBoolean() ? 1f : 0f;
            animT += (target - animT) * Math.min(1f, dt * 12f);
            if (Math.abs(target - animT) < 0.004f) animT = target;

            int pillW = 26;
            int pillH = 13;
            int px = x + w - pillW - 8;
            int py = y + (h - pillH) / 2;
            int trackColor = lerpColor(TRACK_OFF, screen.accentColor, animT);
            context.fill(px, py, px + pillW, py + pillH, trackColor);
            int knobSize = 9;
            int travel = pillW - knobSize - 4;
            int knobX = px + 2 + Math.round(travel * animT);
            int knobY = py + (pillH - knobSize) / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private class SliderRow {
        final String label;
        final int min;
        final int max;
        int value;
        final Consumer<Integer> setter;

        SliderRow(String label, int min, int max, int initial, Consumer<Integer> setter) {
            this.label = label;
            this.min = min;
            this.max = max;
            this.value = initial;
            this.setter = setter;
        }

        void updateFromMouse(double mouseX, int x, int width) {
            double pct = (mouseX - x) / (double) width;
            pct = Math.max(0, Math.min(1, pct));
            value = (int) Math.round(min + pct * (max - min));
            setter.accept(value);
        }

        void render(DrawContext context, ConfigScreen screen, int x, int y, int w, int mouseX, int mouseY) {
            String text = Lang.t(label) + ": " + value;
            context.drawText(screen.textRenderer, text, x, y, TEXT_MAIN, false);

            int trackY = y + 20;
            int trackH = 8;

            context.fill(x, trackY, x + w, trackY + trackH, TRACK_OFF);
            double pct = (value - min) / (double) (max - min);
            int filledW = (int) (w * pct);
            context.fill(x, trackY, x + filledW, trackY + trackH, screen.accentColor);
            screen.drawBorder(context, x, trackY, w, trackH, screen.accentColor);

            int knobSize = 12;
            int knobX = x + filledW - knobSize / 2;
            int knobY = trackY + trackH / 2 - knobSize / 2;
            context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
        }
    }

    private class SwatchButton {
        final int paletteIndex;

        SwatchButton(int paletteIndex) {
            this.paletteIndex = paletteIndex;
        }

        void render(DrawContext context, ConfigScreen screen, int x, int y, int size, int mouseX, int mouseY) {
            int color = PALETTE[paletteIndex];
            context.fill(x, y, x + size, y + size, color);
            boolean selected = screen.cfg.accentColorIndex == paletteIndex;
            screen.drawBorder(context, x, y, size, size, selected ? 0xFFFFFFFF : 0xFF000000);
        }
    }

    private class CycleRow {
        final String label;
        final String[] options;
        final IntSupplier getter;
        final IntConsumer setter;

        CycleRow(String label, String[] options, IntSupplier getter, IntConsumer setter) {
            this.label = label;
            this.options = options;
            this.getter = getter;
            this.setter = setter;
        }

        void advance() {
            int next = (getter.getAsInt() + 1) % options.length;
            setter.accept(next);
        }

        void render(DrawContext context, ConfigScreen screen, int x, int y, int w, int h, int mouseX, int mouseY) {
            boolean hovered = screen.inside(x, y, w, h, mouseX, mouseY);
            context.fill(x, y, x + w, y + h, hovered ? CARD_BG_HOVER : CARD_BG);
            screen.drawBorder(context, x, y, w, h, hovered ? screen.accentColor : 0xFF2A2A2E);

            int current = Math.max(0, Math.min(options.length - 1, getter.getAsInt()));
            context.drawText(screen.textRenderer, Text.literal(Lang.t(label)).formatted(Formatting.BOLD),
                    x + 8, y + (h - 8) / 2, TEXT_MAIN, false);

            String valueText = Lang.t(options[current]) + "  >";
            int valueW = screen.textRenderer.getWidth(valueText);
            context.drawText(screen.textRenderer, valueText, x + w - valueW - 10, y + (h - 8) / 2, screen.accentColor, false);
        }
    }
                     }
