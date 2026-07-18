package com.impactvisuals.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final ModConfig cfg;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Impact Visuals — Settings"));
        this.parent = parent;
        this.cfg = ModConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 90;
        int rowHeight = 24;
        int checkboxWidth = 220;

        addDrawableChild(CheckboxWidget.builder(Text.literal("Hit Particles"), this.textRenderer)
                .checked(cfg.hitParticlesEnabled)
                .callback((cb, checked) -> cfg.hitParticlesEnabled = checked)
                .pos(centerX - checkboxWidth / 2, y)
                .build());
        y += rowHeight;

        addDrawableChild(CheckboxWidget.builder(Text.literal("Target HUD"), this.textRenderer)
                .checked(cfg.targetHudEnabled)
                .callback((cb, checked) -> cfg.targetHudEnabled = checked)
                .pos(centerX - checkboxWidth / 2, y)
                .build());
        y += rowHeight;

        addDrawableChild(CheckboxWidget.builder(Text.literal("Damage Numbers"), this.textRenderer)
                .checked(cfg.damageNumbersEnabled)
                .callback((cb, checked) -> cfg.damageNumbersEnabled = checked)
                .pos(centerX - checkboxWidth / 2, y)
                .build());
        y += rowHeight;

        addDrawableChild(CheckboxWidget.builder(Text.literal("Critical Hit Flash"), this.textRenderer)
                .checked(cfg.criticalFlashEnabled)
                .callback((cb, checked) -> cfg.criticalFlashEnabled = checked)
                .pos(centerX - checkboxWidth / 2, y)
                .build());
        y += rowHeight;

        addDrawableChild(CheckboxWidget.builder(Text.literal("Trajectory Prediction"), this.textRenderer)
                .checked(cfg.trajectoryPredictionEnabled)
                .callback((cb, checked) -> cfg.trajectoryPredictionEnabled = checked)
                .pos(centerX - checkboxWidth / 2, y)
                .build());
        y += rowHeight + 10;

        addDrawableChild(new RangeSlider(
                centerX - checkboxWidth / 2, y, checkboxWidth, 20,
                "Target HUD Range", 1, 15, cfg.targetHudRangeBlocks,
                value -> cfg.targetHudRangeBlocks = value
        ));
        y += rowHeight;

        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget
                .builder(Text.literal("Done"), button -> close())
                .dimensions(centerX - 75, this.height - 30, 150, 20)
                .build());
    }

    @Override
    public void close() {
        cfg.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class RangeSlider extends SliderWidget {
        private final String label;
        private final int min;
        private final int max;
        private final java.util.function.IntConsumer onChange;

        RangeSlider(int x, int y, int width, int height, String label, int min, int max, int initial,
                    java.util.function.IntConsumer onChange) {
            super(x, y, width, height, Text.literal(label + ": " + initial), (initial - min) / (double) (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
        }

        private int currentValue() {
            return (int) Math.round(min + value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + currentValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(currentValue());
        }
    }
}
