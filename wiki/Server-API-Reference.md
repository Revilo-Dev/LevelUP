# Server API Reference

The main server-facing integration surface is `com.revilo.levelup.api.LevelUpApi`.

## General Rules

- Read methods accept `Player` or `ServerPlayer` depending on context.
- Write methods that change progression require `ServerPlayer`.
- `source` arguments should be meaningful `ResourceLocation` values so downstream listeners can classify the change.
- `LevelUpSources` provides common built-ins.

## Constants

### `API_VERSION`

- type: `String`
- current value: `"2"`
- purpose: published API surface version marker

## Capability And Metadata Methods

### `getApiVersion()`

- returns: `String`
- behavior: returns the same value as `API_VERSION`

### `getCapabilities()`

- returns: `LevelUpCapabilities`
- purpose: feature discovery for integrations that want to adapt to exposed behavior

`LevelUpCapabilities` fields:

- `levelChangeEvents`
- `milestoneEvents`
- `highestLevelTracking`
- `progressionCurveAccess`
- `experienceSources`
- `dataLoadedEvents`
- `readOnlyPlayerView`
- `levelChangeResults`

## Progression State Reads

### `getMaxLevel()`

- returns: `int`
- meaning: active global cap after runtime override resolution
- note: this is not the per-player effective cap if that player is locked lower

### `getMaximumLevel()`

- returns: `OptionalInt`
- current behavior: always returns `OptionalInt.of(getMaxLevel())`

### `getLevelMultiplier()`

- returns: `double`
- meaning: active global XP-cost multiplier after override resolution

### `getPlayerData(ServerPlayer player)`

- returns: `Optional<LevelUpPlayerView>`
- current behavior: always returns a non-empty read-only view
- purpose: avoids exposing raw attachment storage

`LevelUpPlayerView` methods:

- `getLevel()`
- `getHighestLevelReached()`
- `getCurrentExperience()`
- `getExperienceForCurrentLevel()`
- `getExperienceForNextLevel()`
- `getTotalExperience()`
- `getLevelProgress()`

### `getActiveLevelCurve()`

- returns: `LevelCurve`
- purpose: exposes a read-only curve adapter

`LevelCurve` methods:

- `experienceRequiredForLevel(int level)`
- `cumulativeExperienceForLevel(int level)`
- `levelFromTotalExperience(long experience)`

### `getLevel(Player player)`

- returns: current LevelUP level

### `getHighestLevelReached(Player player)`

- returns: highest LevelUP level ever reached by that player according to stored attachment state

### `getXp(Player player)`

- returns: total LevelUP XP, not current-level progress

### `getXpMultiplier(Player player)`

- returns: stored per-player multiplier
- note: multiplier values are clamped to `>= 0`

### `getXpIntoCurrentLevel(Player player)`

- returns: `totalXp - floorXpForCurrentLevel`, clamped to `>= 0`

### `getXpNeededForNextLevel(Player player)`

- returns: remaining XP inside the current level band
- special case: returns `0` if the player is already at their effective maximum level

### `getProgressToNextLevel(Player player)`

- returns: float in the `0..1` range
- special case: returns `1.0F` at the effective cap

### `getXpForNextLevel(int currentLevel)`

- returns: cost to advance from the supplied level to the next one

### `getTotalXpForLevel(int level)`

- returns: cumulative XP floor for the supplied level
- example: level `0` returns `0`

### `levelForTotalXp(long totalXp)`

- returns: derived LevelUP level for the supplied total XP using the active max level

### `meetsLevelRequirement(Player player, int requiredLevel)`

- returns: whether current level is at least the requested level
- input handling: negative required levels are treated as `0`

### `isAtMaximumLevel(ServerPlayer player)`

- returns: whether the current level is at least the player's effective cap

## Milestone Registration

### `registerMilestone(ResourceLocation id, int interval)`

- purpose: registers a named milestone stream
- validation:
- `id` must be non-null
- `interval` must be `> 0`
- failure mode: throws `IllegalArgumentException` on invalid input
- event behavior: every level increase that crosses a multiple of `interval` will emit `PlayerLevelMilestoneEvent` with the supplied `id`

There is also a built-in default milestone:

- id: `levelup:default_5_level_milestone`
- interval: `5`

## XP And Level Mutation Methods

### `addExperience(ServerPlayer player, long amount, ResourceLocation source)`

- returns: `LevelChangeResult`
- purpose: canonical reward-granting entry point
- preprocessing:
- pauses are respected
- negative amounts become `0`
- `LevelUpXpGainedEvent` can cancel or edit the amount
- player multiplier is applied after the event
- overlay sync: yes
- reason classification: `COMMAND` if the source is `LevelUpSources.COMMAND`, otherwise `EXPERIENCE_GAIN`

### `addXp(ServerPlayer player, long amount, ResourceLocation source)`

- returns: applied XP delta after event mutation and multiplier handling
- implementation detail: calculated as `newExperience - previousExperience`

### `awardXp(ServerPlayer player, long amount, ResourceLocation source)`

- returns: `void`
- purpose: convenience wrapper around `addXp(...)`

### `setExperience(ServerPlayer player, long xp, ResourceLocation source)`

- returns: `LevelChangeResult`
- behavior: directly sets total XP, clamps it into valid range, recalculates level, fires sync and events
- overlay sync: no
- reason classification: `COMMAND` for command source, otherwise `API`

### `addLevels(ServerPlayer player, int levels, ResourceLocation source)`

- returns: `LevelChangeResult`
- behavior:
- negative values are clamped to `0`
- target level is current level plus supplied levels
- target is capped by the player's effective max
- implementation: delegates to `setLevel(...)`

### `setLevel(ServerPlayer player, int level, ResourceLocation source)`

- returns: `LevelChangeResult`
- behavior:
- level is clamped to `0..effectiveMax`
- total XP is set to the cumulative floor of that level
- the player is not placed partway through the level

### `reset(ServerPlayer player, ResourceLocation source)`

- returns: `LevelChangeResult`
- behavior:
- sets multiplier to `1`
- sets XP to `0`
- recalculates level to `0`
- resets `highestLevelReached` to the resulting current level
- syncs the result
- reason classification: `RESET`

### `setXp(ServerPlayer player, long xp)`

- convenience form of `setExperience(player, xp, LevelUpSources.UNKNOWN)`

### `setLevel(ServerPlayer player, int level)`

- convenience form of `setLevel(player, level, LevelUpSources.UNKNOWN)`

### `setXpMultiplier(ServerPlayer player, int multiplier)`

- behavior:
- writes the stored multiplier
- clamps to `>= 0`
- immediately syncs the player
- does not post a dedicated multiplier-changed event

## Locking And Cap Control

### `isLevelLocked(Player player)`

- returns: whether `getLockedLevel(player) >= 0`

### `getLockedLevel(Player player)`

- returns: stored lock cap
- unlocked value: `-1`

### `lockLevel(ServerPlayer player, int level)`

- behavior:
- clamps the requested lock into `0..getMaxLevel()`
- posts `LevelUpLevelLockChangedEvent` if the value changed
- if current XP or current level exceeds the new effective cap, LevelUP immediately clamps the player down through `applyExperience(...)`
- otherwise the new state is simply synced

### `unlockLevel(ServerPlayer player)`

- behavior:
- if already unlocked, does nothing
- sets locked level to `-1`
- posts `LevelUpLevelLockChangedEvent`
- syncs the player

## Global Runtime Controls

### `isPaused()`

- returns: global paused flag for reward-style XP grants

### `setPaused(boolean paused)`

- behavior: toggles the global paused flag
- scope: process-local runtime state, not per-player
- important: this only affects `addExperience(...)` / reward flow, not direct setters such as `setExperience(...)`

### `setMaxLevelOverride(int maxLevel)`

- behavior: replaces config max level with a runtime override
- minimum enforced by implementation: `1`

### `clearMaxLevelOverride()`

- behavior: restores config-driven max level

### `setLevelMultiplierOverride(double levelMultiplier)`

- behavior: replaces config multiplier with a runtime override
- minimum enforced by implementation: `0.01D`

### `clearLevelMultiplierOverride()`

- behavior: restores config-driven multiplier

## Sync And Orb Helpers

### `sync(ServerPlayer player)`

- behavior: sends a progression packet with `showOverlay=false`

### `spawnLevelUpXpOrb(ServerLevel level, Vec3 position, int amount)`

- behavior: splits the amount into orb values using LevelUP's orb threshold table and spawns or merges entities

### `spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount)`

- coordinate overload for the same behavior

### `spawnLevelUpXpOrbForLevel(ServerLevel level, Vec3 position, int targetLevel)`

- behavior:
- computes `getTotalXpForLevel(targetLevel)`
- clamps into `0..Integer.MAX_VALUE`
- spawns nothing if the value resolves to `0`

### `spawnLevelUpXpOrbForLevel(ServerLevel level, double x, double y, double z, int targetLevel)`

- coordinate overload for the same behavior

## `LevelChangeResult`

Fields:

- `previousLevel`
- `newLevel`
- `previousExperience`
- `newExperience`
- `crossedLevels`

Helper methods:

### `changedLevel()`

- returns: whether `previousLevel != newLevel`

### `crossedMilestones(int interval)`

- returns: all crossed levels that are divisible by `interval`
- validation: non-positive intervals return an empty list
- scope: only upward level transitions produce crossed levels
