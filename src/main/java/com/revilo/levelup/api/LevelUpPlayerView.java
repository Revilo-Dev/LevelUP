package com.revilo.levelup.api;

public interface LevelUpPlayerView {
    int getLevel();

    int getHighestLevelReached();

    long getCurrentExperience();

    long getExperienceForCurrentLevel();

    long getExperienceForNextLevel();

    long getTotalExperience();

    float getLevelProgress();
}
