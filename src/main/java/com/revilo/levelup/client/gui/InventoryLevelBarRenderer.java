package com.revilo.levelup.client.gui;

import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

public final class InventoryLevelBarRenderer {
    private InventoryLevelBarRenderer() {}

    public static void render(ContainerScreenEvent.Render.Foreground event) {
        if (!(event.getContainerScreen() instanceof InventoryScreen screen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!LevelUpClientConfig.CLIENT.showInventoryLevelBar.get()) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int absoluteX = (screenWidth - LevelBarRenderer.BAR_WIDTH) / 2;
        int absoluteY = (screenHeight / 2) + 95;
        int x = absoluteX - screen.getGuiLeft();
        int y = absoluteY - screen.getGuiTop();

        float progress = LevelUpApi.getProgressToNextLevel(minecraft.player);
        int level = LevelUpApi.getLevel(minecraft.player);
        Component label = Component.translatable("levelup.ui.level", level);
        LevelBarRenderer.render(gui, x, y, progress, label);
    }
}
