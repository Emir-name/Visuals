package com.impactvisuals.client.friends;

import com.impactvisuals.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FriendsScreen extends Screen {

    private final Screen parent;
    private final ModConfig cfg;

    private TextFieldWidget addFriendField;

    private static final int ACCENT = 0xFFFF8C00;
    private static final int PANEL_W = 360;

    private int left;
    private int centerX;

    public FriendsScreen(Screen parent) {
        super(Text.literal("Friends"));
        this.parent = parent;
        this.cfg = ModConfig.get();
    }

    @Override
    protected void init() {
        this.clearChildren();

        centerX = this.width / 2;
        left = centerX - PANEL_W / 2;

        addFriendField = new TextFieldWidget(this.textRenderer, left, 30, 200, 20, Text.literal("Add friend"));
        addFriendField.setMaxLength(32);
        addFriendField.setPlaceholder(Text.literal("nickname"));
        this.addDrawableChild(addFriendField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String name = addFriendField.getText().trim();
            if (!name.isEmpty() && !cfg.friendsList.contains(name)) {
                cfg.friendsList.add(name);
                cfg.save();
                addFriendField.setText("");
                refreshAll();
                this.init();
            }
        }).dimensions(left + 210, 30, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), btn -> refreshAll())
                .dimensions(left + 290, 30, 70, 20).build());

        int rowY = 64;
        List<String> friends = new ArrayList<>(cfg.friendsList);
        for (String friend : friends) {
            int y = rowY;

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Copy IP"), btn -> {
                FriendsNetwork.Status status = FriendsNetwork.getCached(friend);
                if (status != null && !status.server.isBlank() && !status.server.equals("menu")) {
                    MinecraftClient.getInstance().keyboard.setClipboard(status.server);
                }
            }).dimensions(left + PANEL_W - 150, y, 70, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Remove"), btn -> {
                cfg.friendsList.remove(friend);
                cfg.save();
                this.init();
            }).dimensions(left + PANEL_W - 75, y, 75, 20).build());

            rowY += 26;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), btn -> {
            cfg.save();
            this.client.setScreen(parent);
        }).dimensions(centerX - 50, this.height - 30, 100, 20).build());

        refreshAll();
    }

    private void refreshAll() {
        for (String friend : cfg.friendsList) {
            FriendsNetwork.fetchStatus(friend);
            FriendsNetwork.fetchHead(friend);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xEE101010);

        context.drawText(this.textRenderer, "Friends (self-hosted via Firebase)", left, 12, ACCENT, false);
        context.drawText(this.textRenderer, "Nickname:", left, 54, 0xFFAAAAAA, false);

        int rowY = 64;
        long now = System.currentTimeMillis();
        for (String friend : cfg.friendsList) {
            FriendsNetwork.Status status = FriendsNetwork.getCached(friend);
            boolean online = status != null && (now - status.lastSeen) < 120_000;

            net.minecraft.util.Identifier head = FriendsNetwork.getHeadTexture(friend);
            if (head != null) {
                context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, head,
                        left, rowY, 0, 0, 16, 16, 32, 32, 32, 32);
            }

            int dotColor = online ? 0xFF55DD55 : 0xFF888888;
            context.fill(left + 20, rowY + 6, left + 28, rowY + 14, dotColor);

            context.drawText(this.textRenderer, friend, left + 34, rowY + 5, 0xFFFFFFFF, false);

            String info;
            if (status == null) {
                info = "no data";
            } else if (online) {
                info = "online - " + status.server;
            } else {
                long minutesAgo = (now - status.lastSeen) / 60_000;
                info = "last seen " + minutesAgo + "m ago";
            }
            context.drawText(this.textRenderer, info, left + 150, rowY + 5, 0xFFAAAAAA, false);

            rowY += 26;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
