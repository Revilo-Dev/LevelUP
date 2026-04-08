package com.revilo.levelup.client.gui;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.config.LevelUpClientConfig;
import com.revilo.levelup.data.LevelFormula;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class TopCenterLevelOverlay {
    private static final ResourceLocation HUD_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, "top_center_level_overlay");
    private static final int TOP_MARGIN = 18;
    private static final int BOTTOM_MARGIN = 29;
    private static final int SLIDE_OFFSET = LevelBarRenderer.BAR_HEIGHT + 18;
    private static final long SLIDE_IN_MILLIS = 150L;
    private static final long SLIDE_OUT_MILLIS = 400L;
    private static final long HOLD_MILLIS = 1200L;
    private static final long MIN_ANIMATION_MILLIS = 650L;
    private static final long MAX_ANIMATION_MILLIS = 1800L;

    private static boolean initialized;
    private static long startXp;
    private static long displayedXp;
    private static long targetXp;
    private static long progressAnimationStartedAt;
    private static long progressAnimationEndsAt;
    private static long visibleStartedAt;
    private static long visibleUntil;
    private static long customVisibleUntil;
    private static float customProgress;
    private static Component customLabel;

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
            progressAnimationStartedAt = now;
            progressAnimationEndsAt = now;
            visibleStartedAt = now;
            visibleUntil = 0L;
            return;
        }

        if (safeNewXp <= oldXp) {
            startXp = safeNewXp;
            displayedXp = safeNewXp;
            targetXp = safeNewXp;
            progressAnimationStartedAt = now;
            progressAnimationEndsAt = now;
            visibleStartedAt = now;
            visibleUntil = 0L;
            return;
        }

        boolean wasHidden = now >= visibleUntil;
        long currentDisplayedXp = usesBottomHud() ? safeNewXp : getDisplayedXp(now);
        startXp = currentDisplayedXp;
        displayedXp = currentDisplayedXp;
        targetXp = safeNewXp;
        progressAnimationStartedAt = now;
        progressAnimationEndsAt = usesBottomHud() ? now : now + getAnimationDuration(currentDisplayedXp, safeNewXp);
        if (wasHidden) {
            visibleStartedAt = now;
        }
        visibleUntil = progressAnimationEndsAt + HOLD_MILLIS;
    }

    public static void showCustom(Component label, float progress, long durationMillis) {
        long now = Util.getMillis();
        customLabel = label;
        customProgress = Math.max(0.0F, Math.min(1.0F, progress));
        customVisibleUntil = Math.max(customVisibleUntil, now + Math.max(0L, durationMillis));
        if (now >= visibleUntil) {
            visibleStartedAt = now;
        }
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
        if (!shouldRender(now, minecraft.player)) {
            return;
        }

        HudSnapshot snapshot = resolveSnapshot(now, minecraft.player);
        int x = (minecraft.getWindow().getGuiScaledWidth() - LevelBarRenderer.BAR_WIDTH) / 2;
        boolean bottomHud = usesBottomHud();
        int y = bottomHud
                ? minecraft.getWindow().getGuiScaledHeight() - BOTTOM_MARGIN
                : TOP_MARGIN + getAnimatedYOffset(now);
        LevelBarRenderer.render(guiGraphics, x, y, snapshot.progress(), snapshot.label(), !bottomHud);
    }

    private static long getDisplayedXp(long now) {
        if (!initialized) {
            return 0L;
        }
        if (now >= progressAnimationEndsAt || progressAnimationEndsAt <= progressAnimationStartedAt) {
            displayedXp = targetXp;
            return displayedXp;
        }

        float rawProgress = (float) (now - progressAnimationStartedAt) / (float) (progressAnimationEndsAt - progressAnimationStartedAt);
        float clampedProgress = Math.max(0.0F, Math.min(1.0F, rawProgress));
        displayedXp = startXp + Math.round((targetXp - startXp) * clampedProgress);
        return displayedXp;
    }

    private static long getAnimationDuration(long fromXp, long toXp) {
        long delta = Math.max(1L, toXp - fromXp);
        long duration = MIN_ANIMATION_MILLIS + (delta * 8L);
        return Math.max(MIN_ANIMATION_MILLIS, Math.min(MAX_ANIMATION_MILLIS, duration));
    }

    private static int getAnimatedYOffset(long now) {
        if (usesBottomHud() || LevelUpClientConfig.CLIENT.levelHudStayOnScreen.get()) {
            return 0;
        }
        if (now < visibleStartedAt + SLIDE_IN_MILLIS) {
            float progress = (float) (now - visibleStartedAt) / (float) SLIDE_IN_MILLIS;
            float clampedProgress = Math.max(0.0F, Math.min(1.0F, progress));
            return Math.round((1.0F - clampedProgress) * -SLIDE_OFFSET);
        }
        if (now > visibleUntil - SLIDE_OUT_MILLIS) {
            float progress = (float) (visibleUntil - now) / (float) SLIDE_OUT_MILLIS;
            float clampedProgress = Math.max(0.0F, Math.min(1.0F, progress));
            return Math.round((1.0F - clampedProgress) * -SLIDE_OFFSET);
        }
        if (now >= visibleUntil) {
            return -SLIDE_OFFSET;
        }
        if (now >= visibleStartedAt + SLIDE_IN_MILLIS) {
            return 0;
        }
        return -SLIDE_OFFSET;
    }

    private static boolean shouldRender(long now, Player player) {
        if (LevelUpClientConfig.CLIENT.levelHudStayOnScreen.get()) {
            return true;
        }
        return (initialized && now < visibleUntil) || now < customVisibleUntil;
    }

    private static HudSnapshot resolveSnapshot(long now, Player player) {
        if (now < customVisibleUntil && customLabel != null) {
            return new HudSnapshot(customProgress, customLabel);
        }

        if (LevelUpClientConfig.CLIENT.levelHudStayOnScreen.get() || usesBottomHud()) {
            return new HudSnapshot(
                    LevelUpApi.getProgressToNextLevel(player),
                    usesBottomHud()
                            ? Component.literal(Integer.toString(LevelUpApi.getLevel(player)))
                            : Component.translatable("levelup.ui.level", LevelUpApi.getLevel(player))
            );
        }

        long xp = getDisplayedXp(now);
        int maxLevel = Math.max(1, LevelFormula.getConfiguredMaxLevel());
        int level = LevelFormula.levelForXp(xp, maxLevel);
        long currentFloor = LevelFormula.totalXpForLevel(level);
        long nextCost = Math.max(1L, LevelFormula.xpForNextLevel(level));
        float progress = (float) Math.min(1.0D, Math.max(0.0D, (double) (xp - currentFloor) / (double) nextCost));
        return new HudSnapshot(progress, Component.translatable("levelup.ui.level", level));
    }

    private static boolean usesBottomHud() {
        return "bottom".equalsIgnoreCase(LevelUpClientConfig.CLIENT.levelHudPosition.get());
    }

    private record HudSnapshot(float progress, Component label) {}
}
