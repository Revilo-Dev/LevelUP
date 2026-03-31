package com.revilo.levelup.api;

import com.revilo.levelup.config.LevelUpConfig;

public final class LevelUpProgressionOverrides {
    private static volatile Integer maxLevelOverride;
    private static volatile Double levelMultiplierOverride;

    private LevelUpProgressionOverrides() {}

    public static int getMaxLevel() {
        Integer override = maxLevelOverride;
        if (override != null) {
            return override;
        }
        return LevelUpConfig.COMMON.maxLevel.get();
    }

    public static double getLevelMultiplier() {
        Double override = levelMultiplierOverride;
        if (override != null) {
            return override;
        }
        return LevelUpConfig.COMMON.levelMultiplier.get();
    }

    public static void setMaxLevelOverride(int maxLevel) {
        maxLevelOverride = Math.max(1, maxLevel);
    }

    public static void clearMaxLevelOverride() {
        maxLevelOverride = null;
    }

    public static void setLevelMultiplierOverride(double levelMultiplier) {
        levelMultiplierOverride = Math.max(0.01D, levelMultiplier);
    }

    public static void clearLevelMultiplierOverride() {
        levelMultiplierOverride = null;
    }
}
