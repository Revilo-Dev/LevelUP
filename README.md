# LevelUP

LevelUP is a NeoForge progression framework that adds a separate XP and level track for players.
It provides a public API and event hooks so other mods can read player level, grant XP, enforce level requirements, and react to progression changes.

## Core Features

- Separate LevelUP XP and level progression from vanilla XP.
- Configurable XP curve, max level cap, and XP conversion options.
- Server-side LevelUP XP orb entity and pickup flow.
- Commands for admins to grant XP and manage progression values.
- Integration hooks for external mods through static API calls and NeoForge events.

## Integration API (for other mods)

Use static methods from `com.revilo.levelup.api.LevelUpApi`.

- `int getLevel(Player player)` returns the player LevelUP level.
- `long getXp(Player player)` returns total stored LevelUP XP.
- `long getXpNeededForNextLevel(Player player)` returns remaining XP to level up.
- `float getProgressToNextLevel(Player player)` returns 0..1 progress for current level.
- `int getMaxLevel()` returns the active max level after config or runtime override resolution.
- `double getLevelMultiplier()` returns the active XP cost multiplier after config or runtime override resolution.
- `boolean meetsLevelRequirement(Player player, int requiredLevel)` checks level gates.
- `long addXp(ServerPlayer player, long amount, ResourceLocation source)` grants XP and returns applied amount after event modifiers.
- `void awardXp(ServerPlayer player, long amount, ResourceLocation source)` convenience wrapper for granting XP.
- `void setXp(ServerPlayer player, long xp)` directly sets total XP and recalculates level.
- `void setLevel(ServerPlayer player, int level)` sets level by assigning total XP floor for that level.
- `void setMaxLevelOverride(int maxLevel)` overrides the configured hard cap until cleared.
- `void clearMaxLevelOverride()` restores the configured hard cap.
- `void setLevelMultiplierOverride(double levelMultiplier)` overrides the configured XP scaling multiplier until cleared.
- `void clearLevelMultiplierOverride()` restores the configured XP scaling multiplier.
- `void sync(ServerPlayer player)` forces network sync of progression data.
- `void spawnLevelUpXpOrb(ServerLevel level, Vec3 position, int amount)` spawns LevelUP XP orbs.
- `void spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount)` coordinate overload for orb spawning.

## Events You Can Subscribe To

All events are posted on `NeoForge.EVENT_BUS`.

- `LevelUpXpGainedEvent` is cancellable and fires before XP is applied.
- `LevelUpXpGainedEvent#setAmount(long)` lets integrations scale or clamp incoming XP.
- `LevelUpLevelChangedEvent` fires when stored level changes in either direction.
- `LevelUpLevelChangedEvent.LevelUp` fires once per level gained when multiple levels are gained in one update.
- `LevelUpOutputEvent` fires only when level increases and includes old/new level, old/new XP, source, and levels gained.

## XP Source IDs

Use `com.revilo.levelup.api.LevelUpSources` constants when possible:

- `UNKNOWN`
- `VANILLA_XP`
- `MOB_KILL`
- `GATEWAY_COMPLETE`
- `QUEST_COMPLETE`
- `OBJECTIVE_COMPLETE`
- `ORB_PICKUP`
- `ITEM_USE`
- `COMMAND`

Create custom source IDs with `LevelUpSources.id("your_path")`.

## Minimal Example

```java
LevelUpApi.awardXp(player, 25, LevelUpSources.QUEST_COMPLETE);
LevelUpApi.setMaxLevelOverride(1000);
LevelUpApi.setLevelMultiplierOverride(0.60D);

if (LevelUpApi.meetsLevelRequirement(player, 20)) {
    // Unlock feature
}
```
