package com.revilo.levelup.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record LevelChangeResult(
        int previousLevel,
        int newLevel,
        long previousExperience,
        long newExperience,
        List<Integer> crossedLevels
) {
    public LevelChangeResult {
        // immutable level list
        crossedLevels = List.copyOf(crossedLevels);
    }

    public static LevelChangeResult unchanged(int level, long experience) {
        return new LevelChangeResult(level, level, experience, experience, Collections.emptyList());
    }

    public List<Integer> crossedMilestones(int interval) {
        if (interval <= 0 || crossedLevels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> milestones = new ArrayList<>();
        // crossed milestone filter
        for (int level : crossedLevels) {
            if (level > 0 && level % interval == 0) {
                milestones.add(level);
            }
        }
        return List.copyOf(milestones);
    }

    public boolean changedLevel() {
        return previousLevel != newLevel;
    }
}
