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
    private static final int BOTTOM_MARGIN = 32;
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
    private static boolean lastStayOnScreen;
    private static Boolean eventStayOnScreenOverride;
    private static String eventHudPosition;
    private static Boolean eventHudEnabledOverride;

    private TopCenterLevelOverlay() {}

    public static ResourceLocation layerId() {
        return HUD_LAYER_ID;
    }

    public static void onProgressionUpdated(long oldXp, long newXp) {
        long now = Util.getMillis();
        long safeNewXp = Math.max(0L, newXp);
        boolean temporaryOverlayEnabled = isTemporaryOverlayEnabled();
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
        long currentDisplayedXp = usesBottomHud() || !temporaryOverlayEnabled ? safeNewXp : getDisplayedXp(now);
        startXp = currentDisplayedXp;
        displayedXp = currentDisplayedXp;
        targetXp = safeNewXp;
        progressAnimationStartedAt = now;
        progressAnimationEndsAt = usesBottomHud() || !temporaryOverlayEnabled
                ? now
                : now + getAnimationDuration(currentDisplayedXp, safeNewXp);
        if (wasHidden) {
            visibleStartedAt = now;
        }
        visibleUntil = temporaryOverlayEnabled ? progressAnimationEndsAt + HOLD_MILLIS : now;
    }

    public static void showCustom(Component label, float progress, long durationMillis) {
        if (!isTemporaryOverlayEnabled()) {
            return;
        }
        long now = Util.getMillis();
        customLabel = label;
        customProgress = Math.max(0.0F, Math.min(1.0F, progress));
        customVisibleUntil = Math.max(customVisibleUntil, now + Math.max(0L, durationMillis));
        if (now >= visibleUntil) {
            visibleStartedAt = now;
        }
    }

    public static void setEventStayOnScreenLock(boolean lockEnabled) {
        long now = Util.getMillis();
        boolean wasStaying = isStayOnScreenActive();
        eventStayOnScreenOverride = lockEnabled;
        boolean isStaying = isStayOnScreenActive();
        if (wasStaying && !isStaying) {
            visibleUntil = Math.max(visibleUntil, now + SLIDE_OUT_MILLIS);
        }
        lastStayOnScreen = isStaying;
    }

    public static void setEventHudPosition(String position) {
        eventHudPosition = position;
    }

    public static void setEventHudEnabled(boolean enabled) {
        eventHudEnabledOverride = enabled;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        if (!isHudEnabled()) {
            return;
        }
        if (!LevelUpClientConfig.CLIENT.showTopCenterLevelOverlay.get()) {
            return;
        }

        long now = Util.getMillis();
        boolean stayOnScreen = isStayOnScreenActive();
        if (lastStayOnScreen && !stayOnScreen) {
            visibleUntil = Math.max(visibleUntil, now + SLIDE_OUT_MILLIS);
        }
        lastStayOnScreen = stayOnScreen;

        if (!shouldRender(now, minecraft.player)) {
            return;
        }

        HudSnapshot snapshot = resolveSnapshot(now, minecraft.player);
        int x = (minecraft.getWindow().getGuiScaledWidth() - LevelBarRenderer.BAR_WIDTH) / 2;
        boolean bottomHud = usesBottomHud();
        if (bottomHud) {
            x -= 1;
        }
        int y = bottomHud
                ? minecraft.getWindow().getGuiScaledHeight() - BOTTOM_MARGIN
                : getTopHudY(now);
        int labelYOffset = bottomHud ? 2 : 0;
        float alpha = bottomHud ? getBottomHudAlpha(now) : 1.0F;
        if (alpha <= 0.0F) {
            return;
        }
        LevelBarRenderer.render(guiGraphics, x, y, snapshot.progress(), snapshot.label(), !bottomHud, labelYOffset, !bottomHud, alpha, bottomHud);
    }

    public static boolean shouldHideVanillaExperienceBar() {
        if (!isHudEnabled()) {
            return false;
        }
        if (!LevelUpClientConfig.CLIENT.showTopCenterLevelOverlay.get()) {
            return false;
        }
        return usesBottomHud();
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
        if (usesBottomHud() || !isTemporaryOverlayEnabled()) {
            return 0;
        }
        if (isStayOnScreenActive()) {
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
        if (isStayOnScreenActive()) {
            return true;
        }
        if (!isTemporaryOverlayEnabled()) {
            return false;
        }
        return (initialized && now < visibleUntil) || now < customVisibleUntil;
    }

    private static HudSnapshot resolveSnapshot(long now, Player player) {
        if (isTemporaryOverlayEnabled() && now < customVisibleUntil && customLabel != null) {
            return new HudSnapshot(customProgress, customLabel);
        }

        boolean bottomHud = usesBottomHud();
        if (bottomHud) {
            return new HudSnapshot(
                    LevelUpApi.getProgressToNextLevel(player),
                    bottomHud
                            ? Component.literal(Integer.toString(LevelUpApi.getLevel(player)))
                            : Component.translatable("levelup.ui.level", LevelUpApi.getLevel(player))
            );
        }

        if (!initialized) {
            return new HudSnapshot(
                    LevelUpApi.getProgressToNextLevel(player),
                    Component.translatable("levelup.ui.level", LevelUpApi.getLevel(player))
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
        String position = eventHudPosition != null ? eventHudPosition : LevelUpClientConfig.CLIENT.levelHudPosition.get();
        return "bottom".equalsIgnoreCase(position);
    }

    private static int getTopHudY(long now) {
        return TOP_MARGIN + getAnimatedYOffset(now);
    }

    private static boolean isTemporaryOverlayEnabled() {
        return LevelUpClientConfig.CLIENT.showTemporaryLevelOverlay.get();
    }

    private static float getBottomHudAlpha(long now) {
        if (!usesBottomHud()) {
            return 1.0F;
        }
        if (isStayOnScreenActive() || !isTemporaryOverlayEnabled()) {
            return 1.0F;
        }
        long activeUntil = Math.max(visibleUntil, customVisibleUntil);
        if (now >= activeUntil) {
            return 0.0F;
        }
        long fadeStart = activeUntil - SLIDE_OUT_MILLIS;
        if (now <= fadeStart) {
            return 1.0F;
        }
        float progress = (float) (activeUntil - now) / (float) SLIDE_OUT_MILLIS;
        return Math.max(0.0F, Math.min(1.0F, progress));
    }

    private static boolean isStayOnScreenActive() {
        return eventStayOnScreenOverride != null
                ? eventStayOnScreenOverride
                : LevelUpClientConfig.CLIENT.levelHudStayOnScreen.get();
    }

    private static boolean isHudEnabled() {
        return eventHudEnabledOverride != null
                ? eventHudEnabledOverride
                : LevelUpClientConfig.CLIENT.showTopCenterLevelOverlay.get();
    }

    private record HudSnapshot(float progress, Component label) {}
}
