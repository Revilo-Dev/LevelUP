package com.revilo.levelup.api;

import com.revilo.levelup.client.gui.LevelBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class LevelUpClientApi {
    private static final int TOP_HUD_LABEL_Y_OFFSET = 0;
    private static final boolean TOP_HUD_DRAW_BACKGROUND = true;
    private static final boolean TOP_HUD_DRAW_LABEL = true;
    private static final float TOP_HUD_ALPHA = 1.0F;

    private LevelUpClientApi() {}

    public static int getLevelBarWidth() {
        return LevelBarRenderer.BAR_WIDTH;
    }

    public static int getLevelBarHeight() {
        return LevelBarRenderer.BAR_HEIGHT;
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
            tooltip.add(Component.translatable("levelup.tooltip.requirement_met", currentLevel, requiredLevel)
                    .withStyle(ChatFormatting.GREEN));
            return;
        }

        long remainingXp = Math.max(0L, LevelUpApi.getTotalXpForLevel(requiredLevel) - LevelUpApi.getXp(player));
        tooltip.add(Component.translatable("levelup.tooltip.requires_level", requiredLevel)
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("levelup.tooltip.current_level", currentLevel)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("levelup.tooltip.xp_until_required", remainingXp)
                .withStyle(ChatFormatting.GRAY));
    }
}
