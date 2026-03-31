package com.revilo.levelup.data;

import com.revilo.levelup.api.LevelUpProgressionOverrides;
import com.revilo.levelup.config.LevelUpConfig;

public final class LevelFormula {
    private static final int SLOW_END_LEVEL_EXCLUSIVE = 10;
    private static final int MEDIUM_END_LEVEL_EXCLUSIVE = 50;
    private static final int FAST_END_LEVEL_EXCLUSIVE = 90;
    private static final int PEAK_END_LEVEL_EXCLUSIVE = 100;

    private LevelFormula() {}

    public static int getConfiguredMaxLevel() {
        return Math.max(1, LevelUpConfig.COMMON.maxLevel.get());
    }

    public static long xpForNextLevel(int currentLevel) {
        int safeLevel = Math.max(0, currentLevel);
        if (safeLevel >= FAST_END_LEVEL_EXCLUSIVE && safeLevel < PEAK_END_LEVEL_EXCLUSIVE) {
            return peakBandXpForLevel(safeLevel);
        }

        double base = LevelUpConfig.COMMON.baseXpPerLevel.get();
        double linear = LevelUpConfig.COMMON.linearXpPerLevel.get();
        double exponent = LevelUpConfig.COMMON.exponent.get();
        double raw = base * Math.pow(safeLevel + 1.0D, exponent) + (linear * safeLevel);
        raw *= bandMultiplier(safeLevel);
        raw *= LevelUpProgressionOverrides.getLevelMultiplier();
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

    private static double bandMultiplier(int level) {
        if (level < SLOW_END_LEVEL_EXCLUSIVE) {
            return 0.70D;
        }
        if (level < MEDIUM_END_LEVEL_EXCLUSIVE) {
            return 1.00D;
        }
        if (level < FAST_END_LEVEL_EXCLUSIVE) {
            return 1.25D;
        }
        return 1.25D;
    }

    private static long peakBandXpForLevel(int level) {
        long totalToNinety = 0L;
        for (int i = 0; i < FAST_END_LEVEL_EXCLUSIVE; i++) {
            totalToNinety = saturatingAdd(totalToNinety, xpForNextLevel(i));
        }

        int peakSpan = PEAK_END_LEVEL_EXCLUSIVE - FAST_END_LEVEL_EXCLUSIVE;
        long baseShare = Math.max(1L, totalToNinety / peakSpan);
        long remainder = totalToNinety % peakSpan;
        int peakIndex = level - FAST_END_LEVEL_EXCLUSIVE;
        long distributed = baseShare + (peakIndex < remainder ? 1L : 0L);
        return Math.min(distributed, Integer.MAX_VALUE);
    }
}
