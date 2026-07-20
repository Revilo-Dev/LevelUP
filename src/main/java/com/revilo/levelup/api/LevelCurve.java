package com.revilo.levelup.api;

public interface LevelCurve {
    long experienceRequiredForLevel(int level);

    long cumulativeExperienceForLevel(int level);

    int levelFromTotalExperience(long experience);
}
