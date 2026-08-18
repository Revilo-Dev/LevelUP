package com.revilo.levelup.client.gui;

import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class LevelBarRepositionController {
    private static RepositionTarget pendingTarget;

    private LevelBarRepositionController() {}

    public static void requestOpenHudRepositionGui() {
        pendingTarget = RepositionTarget.HUD;
    }

    public static void requestOpenInventoryRepositionGui() {
        pendingTarget = RepositionTarget.INVENTORY;
    }

    public static void onClientTick() {
        if (!LevelUpClientConfig.SPEC.isLoaded()) {
            return;
        }

        if (pendingTarget == null) {
            if (LevelUpClientConfig.CLIENT.openHudLevelBarRepositionGui.get()) {
                // config trigger
                LevelUpClientConfig.CLIENT.openHudLevelBarRepositionGui.set(false);
                requestOpenHudRepositionGui();
            } else if (LevelUpClientConfig.CLIENT.openInventoryLevelBarRepositionGui.get()) {
                // config trigger
                LevelUpClientConfig.CLIENT.openInventoryLevelBarRepositionGui.set(false);
                requestOpenInventoryRepositionGui();
            }
        }

        if (pendingTarget == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof LevelBarRepositionScreen) {
            return;
        }

        Screen parent = minecraft.screen;
        // open requested editor
        minecraft.setScreen(new LevelBarRepositionScreen(parent, pendingTarget));
        pendingTarget = null;
    }
}
