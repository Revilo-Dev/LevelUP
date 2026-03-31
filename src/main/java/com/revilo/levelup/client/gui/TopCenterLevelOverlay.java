package com.revilo.levelup.client.gui;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.config.LevelUpClientConfig;
import com.revilo.levelup.data.LevelFormula;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class TopCenterLevelOverlay {
    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-background.png");
    private static final ResourceLocation BAR_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-progress.png");
    private static final ResourceLocation HUD_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, "top_center_level_overlay");
    private static final int BAR_WIDTH = 184;
    private static final int BAR_HEIGHT = 11;
    private static final int TOP_MARGIN = 18;
    private static final long FADE_IN_MILLIS = 150L;
    private static final long FADE_OUT_MILLIS = 400L;
    private static final long HOLD_MILLIS = 1200L;
    private static final long MIN_ANIMATION_MILLIS = 650L;
    private static final long MAX_ANIMATION_MILLIS = 1800L;

    private static boolean initialized;
    private static long startXp;
    private static long displayedXp;
    private static long targetXp;
    private static long animationStartedAt;
    private static long animationEndsAt;
    private static long visibleUntil;

    private TopCenterLevelOverlay() {}

    public static ResourceLocation layerId() {
        return HUD_LAYER_ID;
    }

    public static void onProgressionUpdated(long oldXp, long newXp) {
        long now = Util.getMillis();
        long safeNewXp = Math.max(0L, newXp);
        if (!initialized) {
            initialized = true;
            startXp = safeNewXp;
            displayedXp = safeNewXp;
            targetXp = safeNewXp;
            animationStartedAt = now;
            animationEndsAt = now;
            visibleUntil = 0L;
            return;
        }

        if (safeNewXp <= oldXp) {
            startXp = safeNewXp;
            displayedXp = safeNewXp;
            targetXp = safeNewXp;
            animationStartedAt = now;
            animationEndsAt = now;
            visibleUntil = 0L;
            return;
        }

        long currentDisplayedXp = getDisplayedXp(now);
        startXp = currentDisplayedXp;
        displayedXp = currentDisplayedXp;
        targetXp = safeNewXp;
        animationStartedAt = now;
        animationEndsAt = now + getAnimationDuration(currentDisplayedXp, safeNewXp);
        visibleUntil = animationEndsAt + HOLD_MILLIS;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        if (!LevelUpClientConfig.CLIENT.showTopCenterLevelOverlay.get()) {
            return;
        }

        long now = Util.getMillis();
        if (!initialized || now >= visibleUntil) {
            return;
        }

        long xp = getDisplayedXp(now);
        int maxLevel = Math.max(1, LevelFormula.getConfiguredMaxLevel());
        int level = LevelFormula.levelForXp(xp, maxLevel);
        long currentFloor = LevelFormula.totalXpForLevel(level);
        long nextCost = Math.max(1L, LevelFormula.xpForNextLevel(level));
        float progress = (float) Math.min(1.0D, Math.max(0.0D, (double) (xp - currentFloor) / (double) nextCost));
        int progressWidth = Math.max(0, Math.min(BAR_WIDTH, Math.round(progress * BAR_WIDTH)));

        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = TOP_MARGIN + getAnimatedYOffset(now);
        int alpha = getAlpha(now);
        int textColor = (alpha << 24) | 0x3B9DFF;

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
        guiGraphics.blit(BAR_BACKGROUND, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        if (progressWidth > 0) {
            guiGraphics.blit(BAR_PROGRESS, x, y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        Component label = Component.translatable("levelup.ui.level", level);
        int textWidth = minecraft.font.width(label);
        guiGraphics.drawString(minecraft.font, label, x + (BAR_WIDTH - textWidth) / 2, y - 10, textColor, false);
    }

    private static long getDisplayedXp(long now) {
        if (!initialized) {
            return 0L;
        }
        if (now >= animationEndsAt || animationEndsAt <= animationStartedAt) {
            displayedXp = targetXp;
            return displayedXp;
        }

        float rawProgress = (float) (now - animationStartedAt) / (float) (animationEndsAt - animationStartedAt);
        float easedProgress = rawProgress * rawProgress * (3.0F - (2.0F * rawProgress));
        displayedXp = startXp + Math.round((targetXp - startXp) * easedProgress);
        return displayedXp;
    }

    private static long getAnimationDuration(long fromXp, long toXp) {
        long delta = Math.max(1L, toXp - fromXp);
        long duration = MIN_ANIMATION_MILLIS + (delta * 8L);
        return Math.max(MIN_ANIMATION_MILLIS, Math.min(MAX_ANIMATION_MILLIS, duration));
    }

    private static int getAlpha(long now) {
        if (now < animationStartedAt + FADE_IN_MILLIS) {
            float progress = (float) (now - animationStartedAt) / (float) FADE_IN_MILLIS;
            return Math.max(0, Math.min(255, Math.round(255.0F * progress)));
        }
        if (now > visibleUntil - FADE_OUT_MILLIS) {
            float progress = (float) (visibleUntil - now) / (float) FADE_OUT_MILLIS;
            return Math.max(0, Math.min(255, Math.round(255.0F * progress)));
        }
        return 255;
    }

    private static int getAnimatedYOffset(long now) {
        if (now >= animationStartedAt + FADE_IN_MILLIS) {
            return 0;
        }
        float progress = (float) (now - animationStartedAt) / (float) FADE_IN_MILLIS;
        return Math.round((1.0F - Math.max(0.0F, Math.min(1.0F, progress))) * -8.0F);
    }
}
