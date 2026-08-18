package com.revilo.levelup.api;

import com.revilo.levelup.client.gui.LevelBarRenderer;
import com.revilo.levelup.client.gui.LevelBarRepositionController;
import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class LevelUpClientApi {
    // top hud render defaults
    private static final int TOP_HUD_LABEL_Y_OFFSET = 0;
    private static final boolean TOP_HUD_DRAW_BACKGROUND = true;
    private static final boolean TOP_HUD_DRAW_LABEL = true;
    private static final float TOP_HUD_ALPHA = 1.0F;
    // local data listeners
    private static final CopyOnWriteArrayList<Consumer<LevelProgressDisplay>> DATA_UPDATE_LISTENERS = new CopyOnWriteArrayList<>();

    private LevelUpClientApi() {}

    public static void openHudLevelBarRepositionGui() {
        LevelBarRepositionController.requestOpenHudRepositionGui();
    }

    public static void openInventoryLevelBarRepositionGui() {
        LevelBarRepositionController.requestOpenInventoryRepositionGui();
    }

    public static void setHudLevelBarOffset(int offsetX, int offsetY) {
        LevelUpClientConfig.CLIENT.hudLevelBarOffsetX.set(offsetX);
        LevelUpClientConfig.CLIENT.hudLevelBarOffsetY.set(offsetY);
    }

    public static void setInventoryLevelBarOffset(int offsetX, int offsetY) {
        LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetX.set(offsetX);
        LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetY.set(offsetY);
    }

    public static int getHudLevelBarOffsetX() {
        return LevelUpClientConfig.CLIENT.hudLevelBarOffsetX.get();
    }

    public static int getHudLevelBarOffsetY() {
        return LevelUpClientConfig.CLIENT.hudLevelBarOffsetY.get();
    }

    public static int getInventoryLevelBarOffsetX() {
        return LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetX.get();
    }

    public static int getInventoryLevelBarOffsetY() {
        return LevelUpClientConfig.CLIENT.inventoryLevelBarOffsetY.get();
    }

    public static int getLevelBarWidth() {
        return LevelBarRenderer.BAR_WIDTH;
    }

    public static int getLevelBarHeight() {
        return LevelBarRenderer.BAR_HEIGHT;
    }

    public static void onDataUpdated(Consumer<LevelProgressDisplay> listener) {
        DATA_UPDATE_LISTENERS.add(listener);
    }

    public static void removeDataUpdatedListener(Consumer<LevelProgressDisplay> listener) {
        DATA_UPDATE_LISTENERS.remove(listener);
    }

    public static LevelProgressDisplay getLocalLevelProgressDisplay() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            // empty client state
            return new LevelProgressDisplay(0, 0L, 0L, 0.0F);
        }

        return new LevelProgressDisplay(
                LevelUpApi.getLevel(minecraft.player),
                LevelUpApi.getXpIntoCurrentLevel(minecraft.player),
                LevelUpApi.getXpForNextLevel(LevelUpApi.getLevel(minecraft.player)),
                LevelUpApi.getProgressToNextLevel(minecraft.player)
        );
    }

    public static void notifyDataUpdated() {
        LevelProgressDisplay display = getLocalLevelProgressDisplay();
        // listener snapshot
        for (Consumer<LevelProgressDisplay> listener : DATA_UPDATE_LISTENERS) {
            listener.accept(display);
        }
    }

    public static void renderLevelBar(GuiGraphics guiGraphics, int x, int y, float progress, Component label) {
        renderTopHudStyleLevelBar(guiGraphics, x, y, progress, label);
    }

    public static void renderTopHudStyleLevelBar(GuiGraphics guiGraphics, int x, int y, float progress, Component label) {
        LevelBarRenderer.render(
                guiGraphics,
                x,
                y,
                progress,
                label,
                TOP_HUD_DRAW_BACKGROUND,
                TOP_HUD_LABEL_Y_OFFSET,
                TOP_HUD_DRAW_LABEL,
                TOP_HUD_ALPHA
        );
    }

    public static void renderLevelBar(
            GuiGraphics guiGraphics,
            int x,
            int y,
            float progress,
            Component label,
            boolean drawBackground,
            int labelYOffset,
            boolean drawLabel,
            float alpha
    ) {
        LevelBarRenderer.render(guiGraphics, x, y, progress, label, drawBackground, labelYOffset, drawLabel, alpha);
    }

    public static boolean renderPlayerLevelBar(GuiGraphics guiGraphics, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        float progress = LevelUpApi.getProgressToNextLevel(minecraft.player);
        int level = LevelUpApi.getLevel(minecraft.player);
        Component label = Component.translatable("levelup.ui.level", level);
        renderTopHudStyleLevelBar(guiGraphics, x, y, progress, label);
        return true;
    }

    public static boolean renderTopHudStylePlayerLevelBar(GuiGraphics guiGraphics, int x, int y) {
        return renderPlayerLevelBar(guiGraphics, x, y);
    }

    public static float getProgressToRequiredLevel(Player player, int requiredLevel) {
        int safeRequiredLevel = Math.max(1, requiredLevel);
        long requiredXp = LevelUpApi.getTotalXpForLevel(safeRequiredLevel);
        if (requiredXp <= 0L) {
            return 1.0F;
        }

        // requirement progress
        double progress = (double) LevelUpApi.getXp(player) / (double) requiredXp;
        return (float) Math.max(0.0D, Math.min(1.0D, progress));
    }

    public static Component getLevelRequirementLabel(Player player, int requiredLevel) {
        int currentLevel = LevelUpApi.getLevel(player);
        if (currentLevel >= requiredLevel) {
            return Component.translatable("levelup.ui.requirement_met", currentLevel, requiredLevel);
        }

        return Component.translatable("levelup.ui.requirement", currentLevel, requiredLevel);
    }

    public static boolean renderRequiredLevelBar(GuiGraphics guiGraphics, int x, int y, int requiredLevel) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        renderRequiredLevelBar(guiGraphics, x, y, minecraft.player, requiredLevel);
        return true;
    }

    public static void renderRequiredLevelBar(GuiGraphics guiGraphics, int x, int y, Player player, int requiredLevel) {
        renderTopHudStyleLevelBar(
                guiGraphics,
                x,
                y,
                getProgressToRequiredLevel(player, requiredLevel),
                getLevelRequirementLabel(player, requiredLevel)
        );
    }

    public static float getPreviewProgressAfterXp(Player player, long xpReward) {
        // reward preview xp
        long previewXp = Math.max(0L, LevelUpApi.getXp(player) + Math.max(0L, xpReward));
        int previewLevel = LevelUpApi.levelForTotalXp(previewXp);
        long currentFloor = LevelUpApi.getTotalXpForLevel(previewLevel);
        long nextCost = Math.max(1L, LevelUpApi.getXpForNextLevel(previewLevel));
        double progress = (double) (previewXp - currentFloor) / (double) nextCost;
        return (float) Math.max(0.0D, Math.min(1.0D, progress));
    }

    public static Component getRewardPreviewLabel(Player player, long xpReward) {
        long previewXp = Math.max(0L, LevelUpApi.getXp(player) + Math.max(0L, xpReward));
        int previewLevel = LevelUpApi.levelForTotalXp(previewXp);
        return Component.translatable("levelup.ui.reward_preview", previewLevel, xpReward);
    }

    public static boolean renderRewardPreviewBar(GuiGraphics guiGraphics, int x, int y, long xpReward) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        renderRewardPreviewBar(guiGraphics, x, y, minecraft.player, xpReward);
        return true;
    }

    public static void renderRewardPreviewBar(GuiGraphics guiGraphics, int x, int y, Player player, long xpReward) {
        renderTopHudStyleLevelBar(
                guiGraphics,
                x,
                y,
                getPreviewProgressAfterXp(player, xpReward),
                getRewardPreviewLabel(player, xpReward)
        );
    }

    public static void appendLevelRequirementTooltip(List<Component> tooltip, Player player, int requiredLevel) {
        int currentLevel = LevelUpApi.getLevel(player);
        if (currentLevel >= requiredLevel) {
            // met requirement
            tooltip.add(Component.translatable("levelup.tooltip.requirement_met", currentLevel, requiredLevel)
                    .withStyle(ChatFormatting.GREEN));
            return;
        }

        // missing requirement
        long remainingXp = Math.max(0L, LevelUpApi.getTotalXpForLevel(requiredLevel) - LevelUpApi.getXp(player));
        tooltip.add(Component.translatable("levelup.tooltip.requires_level", requiredLevel)
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("levelup.tooltip.current_level", currentLevel)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("levelup.tooltip.xp_until_required", remainingXp)
                .withStyle(ChatFormatting.GRAY));
    }
}
