package com.revilo.levelup.client.gui;

import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LevelBarRepositionScreen extends Screen {
    private final Screen parent;
    private final RepositionTarget target;
    private int workingOffsetX;
    private int workingOffsetY;
    private int initialOffsetX;
    private int initialOffsetY;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public LevelBarRepositionScreen(Screen parent, RepositionTarget target) {
        super(Component.translatable(target == RepositionTarget.HUD
                ? "levelup.config.reposition_hud_title"
                : "levelup.config.reposition_inventory_title"));
        this.parent = parent;
        this.target = target;
    }

    @Override
    protected void init() {
        initialOffsetX = getConfigOffsetX();
        initialOffsetY = getConfigOffsetY();
        workingOffsetX = initialOffsetX;
        workingOffsetY = initialOffsetY;

        int buttonY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            saveWorkingOffsets();
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 160, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("controls.reset"), btn -> {
            workingOffsetX = 0;
            workingOffsetY = 0;
            saveWorkingOffsets();
        }).bounds(this.width / 2 - 50, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> {
            setConfigOffsets(initialOffsetX, initialOffsetY);
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 + 60, buttonY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int[] previewPos = getPreviewBarPosition();
        Component label = Component.translatable("levelup.ui.level", 42);
        LevelBarRenderer.render(guiGraphics, previewPos[0], previewPos[1], 0.65F, label);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("levelup.config.reposition_hint"),
                this.width / 2, 32, 0xB0B0B0);
        guiGraphics.drawCenteredString(this.font,
                Component.literal("X: " + workingOffsetX + "  Y: " + workingOffsetY),
                this.width / 2, 46, 0xB0B0B0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int[] previewPos = getPreviewBarPosition();
        int left = previewPos[0];
        int top = previewPos[1];
        int right = left + LevelBarRenderer.BAR_WIDTH;
        int bottom = top + LevelBarRenderer.BAR_HEIGHT;
        if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
            dragging = true;
            dragOffsetX = (int) Math.round(mouseX) - left;
            dragOffsetY = (int) Math.round(mouseY) - top;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            int[] anchor = getAnchorPosition();
            int newX = (int) Math.round(mouseX) - dragOffsetX;
            int newY = (int) Math.round(mouseY) - dragOffsetY;
            workingOffsetX = newX - anchor[0];
            workingOffsetY = newY - anchor[1];
            saveWorkingOffsets();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int[] getPreviewBarPosition() {
        int[] anchor = getAnchorPosition();
        return new int[]{anchor[0] + workingOffsetX, anchor[1] + workingOffsetY};
    }

    private int[] getAnchorPosition() {
        int baseX = (this.width - LevelBarRenderer.BAR_WIDTH) / 2;
        int baseY;
        if (target == RepositionTarget.HUD) {
            baseY = 18;
        } else {
            baseY = (this.height / 2) + 95;
        }
        return new int[]{baseX, baseY};
    }

    private int getConfigOffsetX() {
        return target == RepositionTarget.HUD
                ? LevelUpClientConfig.CLIENT.hudLevelBarOffsetX.get()
                : LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetX.get();
    }

    private int getConfigOffsetY() {
        return target == RepositionTarget.HUD
                ? LevelUpClientConfig.CLIENT.hudLevelBarOffsetY.get()
                : LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetY.get();
    }

    private void setConfigOffsets(int x, int y) {
        if (target == RepositionTarget.HUD) {
            LevelUpClientConfig.CLIENT.hudLevelBarOffsetX.set(x);
            LevelUpClientConfig.CLIENT.hudLevelBarOffsetY.set(y);
            return;
        }
        LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetX.set(x);
        LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetY.set(y);
    }

    private void saveWorkingOffsets() {
        setConfigOffsets(workingOffsetX, workingOffsetY);
    }
}
