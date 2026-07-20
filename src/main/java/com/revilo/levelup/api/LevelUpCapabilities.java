package com.revilo.levelup.api;

public record LevelUpCapabilities(
        boolean levelChangeEvents,
        boolean milestoneEvents,
        boolean highestLevelTracking,
        boolean progressionCurveAccess,
        boolean experienceSources,
        boolean dataLoadedEvents,
        boolean readOnlyPlayerView,
        boolean levelChangeResults
) {}
