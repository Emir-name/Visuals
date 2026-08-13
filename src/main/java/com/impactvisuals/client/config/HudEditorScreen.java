package com.impactvisuals.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * "Screenshot Tool" in the menu actually opens this: a screen where every
 * registered HUD card can be dragged to a custom spot, and resized by
 * scrolling on it. Changes only take effect for real when you hit Save;
 * Exit throws them away. Reset clears both position and size back to default.
 */
public class HudEditorScreen extends Screen {

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.0f;

    private final Screen parent;

    /** Working copies, edited live here and only committed to ModConfig on Save. */
    private final Map<String, int[]> workingOffsets = new LinkedHashMap<>();
    private final Map<String, Float> workingScales = new LinkedHashMap<>();
    private String draggingId = null;
    private int dragStartMouseX, dragStartMouseY;
    private int dragStartOffsetX, dragStartOffsetY;
    private int previousBlurriness = 0;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        previousBlurriness = client.options.getMenuBackgroundBlurriness().getValue();
        client.options.getMenuBackgroundBlurriness().setValue(0);

        loadWorkingCopy();

        int btnW = 90, btnH = 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> onReset())
                .dimensions(this.width / 2 - btnW * 3 / 2 - 12, this.height - 32, btnW, btnH).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Exit"), b -> onExit())
                .dimensions(this.width / 2 - btnW / 2, this.height - 32, btnW, btnH).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> onSave())
                .dimensions(this.width / 2 + btnW / 2 + 12, this.height - 32, btnW, btnH).build());
    }

    private void loadWorkingCopy() {
        workingOffsets.clear();
        workingScales.clear();
        for (String id : HudLayoutManager.EDITABLE_HUDS.keySet()) {
            workingOffsets.put(id, new int[]{HudLayoutManager.getOffsetX(id), HudLayoutManager.getOffsetY(id)});
            workingScales.put(id, HudLayoutManager.getScale(id));
        }
    }

    private void restoreBlur() {
        MinecraftClient.getInstance().options.getMenuBackgroundBlurriness().setValue(previousBlurriness);
    }

    private void onExit() {
        restoreBlur();
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void onSave() {
        for (Map.Entry<String, int[]> e : workingOffsets.entrySet()) {
            HudLayoutManager.setOffset(e.getKey(), e.getValue()[0], e.getValue()[1]);
        }
        for (Map.Entry<String, Float> e : workingScales.entrySet()) {
            HudLayoutManager.setScale(e.getKey(), e.getValue());
        }
        HudLayoutManager.save();
        restoreBlur();
        MinecraftClient.getInstance().setScreen(parent);
    }

    /** Resets every HUD back to default position/size - applied immediately and saved, since "as before" implies persisting it. */
    private void onReset() {
        HudLayoutManager.resetAll();
        HudLayoutManager.save();
        loadWorkingCopy();
    }

    private int[] baseAnchor(String id, int boxW, int boxH) {
        // Mirrors each HUD's own default (offset-free) anchor math, so the
        // draggable preview box lines up with where the real card would sit.
        return switch (id) {
            case "target_hud" -> new int[]{this.width / 2 - boxW / 2, this.height / 2 - 70};
            case "marker_hud" -> new int[]{this.width / 2 - boxW / 2, 10};
            case "active_effects" -> new int[]{6, 60};
            case "info_hud" -> new int[]{this.width - boxW - 6, 6};
            case "stats_hud" -> new int[]{6, 6};
            case "durability_hud" -> new int[]{this.width / 2 - boxW / 2, this.height - 58};
            case "extra_hud" -> new int[]{6, this.height - 30};
            case "better_near" -> new int[]{this.width - boxW - 6, this.height - boxH - 30};
            case "cooldown_indicator" -> new int[]{this.width / 2 - boxW / 2, this.height / 2 + 12};
            case "build_helper_hud" -> new int[]{this.width / 2 - boxW / 2, this.height / 2 + 20};
            default -> new int[]{this.width / 2 - boxW / 2, this.height / 2 - boxH / 2};
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x70000000);

        context.drawCenteredTextWithShadow(this.textRenderer, "Тащи карточки, крути колёсико чтобы изменить размер", this.width / 2, 14, 0xFFFFFFFF);

        for (Map.Entry<String, int[]> entry : HudLayoutManager.EDITABLE_HUDS.entrySet()) {
            String id = entry.getKey();
            int boxW = entry.getValue()[0];
            int boxH = entry.getValue()[1];
            int[] offset = workingOffsets.get(id);
            float scale = workingScales.get(id);
            int[] anchor = baseAnchor(id, boxW, boxH);

            int scaledW = Math.round(boxW * scale);
            int scaledH = Math.round(boxH * scale);
            int cx = anchor[0] + boxW / 2 + offset[0];
            int cy = anchor[1] + boxH / 2 + offset[1];
            int x = cx - scaledW / 2;
            int y = cy - scaledH / 2;

            boolean hovered = mouseX >= x && mouseX <= x + scaledW && mouseY >= y && mouseY <= y + scaledH;
            boolean active = id.equals(draggingId);

            int fill = active ? 0x60B266FF : hovered ? 0x40B266FF : 0x40202020;
            int border = active || hovered ? 0xFFB266FF : 0xFF6E6480;

            context.fill(x, y, x + scaledW, y + scaledH, fill);
            context.fill(x, y, x + scaledW, y + 1, border);
            context.fill(x, y + scaledH - 1, x + scaledW, y + scaledH, border);
            context.fill(x, y, x + 1, y + scaledH, border);
            context.fill(x + scaledW - 1, y, x + scaledW, y + scaledH, border);

            String label = Lang.t(labelFor(id));
            String sizeText = Math.round(scale * 100) + "%";
            context.drawCenteredTextWithShadow(this.textRenderer, label, x + scaledW / 2, y + scaledH / 2 - 9, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, sizeText, x + scaledW / 2, y + scaledH / 2 + 2, 0xFFB266FF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String labelFor(String id) {
        return switch (id) {
            case "target_hud" -> "Target HUD";
            case "marker_hud" -> "Marker HUD";
            case "active_effects" -> "Active Effects";
            case "info_hud" -> "Info HUD";
            case "stats_hud" -> "Coords/Compass/Timer";
            case "durability_hud" -> "Durability %";
            case "extra_hud" -> "Extra HUD";
            case "better_near" -> "Better Near";
            case "cooldown_indicator" -> "Cooldown";
            case "build_helper_hud" -> "Build Helper";
            default -> id;
        };
    }

    /** Returns the id of the box under the given point, accounting for its current scaled size, or null. */
    private String hitTest(double mouseX, double mouseY) {
        for (Map.Entry<String, int[]> entry : HudLayoutManager.EDITABLE_HUDS.entrySet()) {
            String id = entry.getKey();
            int boxW = entry.getValue()[0];
            int boxH = entry.getValue()[1];
            int[] offset = workingOffsets.get(id);
            float scale = workingScales.get(id);
            int[] anchor = baseAnchor(id, boxW, boxH);

            int scaledW = Math.round(boxW * scale);
            int scaledH = Math.round(boxH * scale);
            int cx = anchor[0] + boxW / 2 + offset[0];
            int cy = anchor[1] + boxH / 2 + offset[1];
            int x = cx - scaledW / 2;
            int y = cy - scaledH / 2;

            if (mouseX >= x && mouseX <= x + scaledW && mouseY >= y && mouseY <= y + scaledH) {
                return id;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String id = hitTest(mouseX, mouseY);
            if (id != null) {
                draggingId = id;
                dragStartMouseX = (int) mouseX;
                dragStartMouseY = (int) mouseY;
                int[] offset = workingOffsets.get(id);
                dragStartOffsetX = offset[0];
                dragStartOffsetY = offset[1];
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingId != null) {
            int[] offset = workingOffsets.get(draggingId);
            offset[0] = dragStartOffsetX + (int) (mouseX - dragStartMouseX);
            offset[1] = dragStartOffsetY + (int) (mouseY - dragStartMouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingId = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        String id = hitTest(mouseX, mouseY);
        if (id != null) {
            float current = workingScales.get(id);
            float next = current + (float) (verticalAmount * 0.05);
            next = Math.max(MIN_SCALE, Math.min(MAX_SCALE, next));
            workingScales.put(id, next);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
