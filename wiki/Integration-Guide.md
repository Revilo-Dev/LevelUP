# Integration Guide

This page shows recommended patterns for consuming LevelUP from another mod.

## Granting XP For A Custom Action

Use `awardXp(...)` for simple rewards:

```java
LevelUpApi.awardXp(player, 25, LevelUpSources.QUEST_COMPLETE);
```

Use `addExperience(...)` when you need the full transaction result:

```java
LevelChangeResult result = LevelUpApi.addExperience(
        player,
        250L,
        ResourceLocation.fromNamespaceAndPath("examplemod", "boss_clear")
);

if (result.changedLevel()) {
    // trigger your own reward logic
}
```

## Reading Player State

Use the direct getters when you only need one value:

```java
int level = LevelUpApi.getLevel(player);
long totalXp = LevelUpApi.getXp(player);
```

Use the view when you want a structured snapshot:

```java
LevelUpApi.getPlayerData(player).ifPresent(view -> {
    int level = view.getLevel();
    long currentXp = view.getCurrentExperience();
    float progress = view.getLevelProgress();
});
```

## Feature Gating

```java
if (LevelUpApi.meetsLevelRequirement(player, 20)) {
    // unlock feature
}
```

## Listening For Single-Level Rewards

Use `LevelUpLevelChangedEvent.LevelUp` if your reward should run once per gained level:

```java
@SubscribeEvent
public static void onLevelUp(LevelUpLevelChangedEvent.LevelUp event) {
    ServerPlayer player = event.getPlayer();
    int newLevel = event.getNewLevel();
}
```

## Listening For Big Transactions

Use `LevelUpOutputEvent` if you want one event per upward transaction:

```java
@SubscribeEvent
public static void onOutput(LevelUpOutputEvent event) {
    int levelsGained = event.getLevelsGained();
    long xpDelta = event.getNewXp() - event.getOldXp();
}
```

## Registering Milestones

```java
LevelUpApi.registerMilestone(
        ResourceLocation.fromNamespaceAndPath("examplemod", "talent_point"),
        5
);
```

Then listen for milestone events:

```java
@SubscribeEvent
public static void onMilestone(PlayerLevelMilestoneEvent event) {
    if (event.getMilestoneId().equals(
            ResourceLocation.fromNamespaceAndPath("examplemod", "talent_point"))) {
        // grant your reward
    }
}
```

## Rendering A Bar In Your GUI

```java
LevelUpClientApi.renderTopHudStylePlayerLevelBar(
        guiGraphics,
        leftPos + 8,
        topPos + 16
);
```

## Rendering A Requirement Bar

```java
LevelUpClientApi.renderRequiredLevelBar(
        guiGraphics,
        leftPos + 8,
        topPos + 40,
        15
);
```

## Rendering A Reward Preview

```java
LevelUpClientApi.renderRewardPreviewBar(
        guiGraphics,
        leftPos + 8,
        topPos + 64,
        250L
);
```

## Appending A Requirement Tooltip

```java
LevelUpClientApi.appendLevelRequirementTooltip(tooltip, player, 15);
```

## Runtime Overrides

These are global runtime controls, not per-player settings:

```java
LevelUpApi.setMaxLevelOverride(1000);
LevelUpApi.setLevelMultiplierOverride(0.60D);
```

Clear them when no longer needed:

```java
LevelUpApi.clearMaxLevelOverride();
LevelUpApi.clearLevelMultiplierOverride();
```

## Per-Player Locking

```java
LevelUpApi.lockLevel(player, 10);
LevelUpApi.unlockLevel(player);
```

Use this when a story arc, dungeon key, or research tier should temporarily stop progression past a threshold.
