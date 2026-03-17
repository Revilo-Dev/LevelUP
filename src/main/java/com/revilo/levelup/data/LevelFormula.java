package com.revilo.levelup.data;

import com.revilo.levelup.config.LevelUpConfig;

public final class LevelFormula {
    private LevelFormula() {}

    public static long xpForNextLevel(int currentLevel) {
        int safeLevel = Math.max(0, currentLevel);
        double base = LevelUpConfig.COMMON.baseXpPerLevel.get();
        double linear = LevelUpConfig.COMMON.linearXpPerLevel.get();
        double exponent = LevelUpConfig.COMMON.exponent.get();
        double raw = base * Math.pow(safeLevel + 1.0D, exponent) + (linear * safeLevel);
        long value = Math.max(1L, Math.round(raw));
        return Math.min(value, Integer.MAX_VALUE);
    }

    public static long totalXpForLevel(int level) {
        int safeLevel = Math.max(0, level);
        long total = 0L;
        for (int i = 0; i < safeLevel; i++) {
            total = saturatingAdd(total, xpForNextLevel(i));
        }
        return total;
    }

    public static int levelForXp(long totalXp, int maxLevel) {
        long safeXp = Math.max(0L, totalXp);
        int clampedMax = Math.max(0, maxLevel);
        int level = 0;
        while (level < clampedMax) {
            long nextCost = xpForNextLevel(level);
            if (safeXp < nextCost) {
                break;
            }
            safeXp -= nextCost;
            level++;
        }
        return level;
    }

    public static long saturatingAdd(long left, long right) {
        long result = left + right;
        if (((left ^ result) & (right ^ result)) < 0) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, result);
    }
}
