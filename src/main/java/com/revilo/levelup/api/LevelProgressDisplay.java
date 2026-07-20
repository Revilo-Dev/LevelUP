package com.revilo.levelup.api;

public record LevelProgressDisplay(
        int level,
        long currentExperience,
        long requiredExperience,
        float progress
) {}
