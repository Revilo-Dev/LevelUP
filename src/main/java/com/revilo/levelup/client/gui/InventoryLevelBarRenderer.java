package com.revilo.levelup.client.gui;

import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

public final class InventoryLevelBarRenderer {
    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-background.png");
    private static final ResourceLocation BAR_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-progress.png");
    private static final int BAR_WIDTH = 184;
    private static final int BAR_HEIGHT = 11;

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
        int absoluteX = ((screenWidth - BAR_WIDTH) / 2);
        int absoluteY = (screenHeight / 2) + 95;
        int x = absoluteX - screen.getGuiLeft();
        int y = absoluteY - screen.getGuiTop();

        gui.blit(BAR_BACKGROUND, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        float progress = LevelUpApi.getProgressToNextLevel(minecraft.player);
        int progressWidth = Math.max(0, Math.min(BAR_WIDTH, Math.round(progress * BAR_WIDTH)));
        if (progressWidth > 0) {
            gui.blit(BAR_PROGRESS, x, y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        int level = LevelUpApi.getLevel(minecraft.player);
        Component label = Component.translatable("levelup.ui.level", level);
        int textWidth = minecraft.font.width(label);
        gui.drawString(minecraft.font, label, x + (BAR_WIDTH - textWidth) / 2, y - 10, 0x3B9DFF, false);
    }
}
