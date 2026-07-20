# Changelog

## Unreleased

### Added

- Public API v2 for integration mods:
  - `LevelUpApi.API_VERSION`, `getApiVersion()`, and `getCapabilities()`.
  - `LevelUpPlayerView` read-only player progression data.
  - `LevelCurve` access through `LevelUpApi.getActiveLevelCurve()`.
  - `LevelChangeResult` return values with crossed-level and crossed-milestone helpers.
  - Source-aware `addExperience`, `setExperience`, `addLevels`, `setLevel`, and `reset` methods.
  - `getHighestLevelReached()`, `getMaximumLevel()`, and `isAtMaximumLevel()`.
- New integration events:
  - `PlayerLevelChangeEvent`.
  - `PlayerLevelMilestoneEvent`.
  - `PlayerExperienceChangeEvent`.
  - `PlayerLevelDataLoadedEvent`.
- Milestone registration API via `LevelUpApi.registerMilestone(id, interval)`.
- Batched progression transactions now report skipped levels and milestones without repeated syncs.
- Highest-level tracking is serialized with player progression data and synchronized to clients.
- Client data update listener API:
  - `LevelUpClientApi.onDataUpdated(...)`
  - `LevelUpClientApi.removeDataUpdatedListener(...)`
  - `LevelUpClientApi.getLocalLevelProgressDisplay()`
- New mob level-drop config:
  - `sources.mobs_drop_levels`
  - `sources.mob_level_drop_base_value`
  - `sources.only_tagged_mobs_drop_levels`
- Common config migration for legacy mob XP keys so existing settings survive restarts.
- New command to toggle HUD level display:
  - `/level hud levelbar display disable`
  - `/level hud levelbar display enable`
- Added `LevelUpClientApi` helpers for rendering LevelUP bars in other mod GUIs:
  - Level requirement helpers.
  - Reward preview helpers.
  - Tooltip helper.
- Added translations for level requirements, reward previews, and requirement tooltip text.

### Fixed

- Fixed players receiving LevelUP XP from self-damage edge cases.
- Fixed mob LevelUP XP checks so only valid player melee kills (`minecraft:player_attack`) can spawn LevelUP orb XP.
- Fixed mob level-drop rules:
  - `sources.only_tagged_mobs_drop_levels=true` now means only `levelup:drops_levels` mobs can drop levels, without exception.
  - If both mob level-drop toggles are disabled, mobs never drop levels.
  - Drops are spawned as LevelUP orbs only and are not influenced by vanilla XP or damage dealt.
- Fixed mob level-drop amount:
  - Eligible kills now scale from `sources.mob_level_drop_base_value` and mob max health.
- Fixed legacy config compatibility:
  - Old `enable_mob_kill_xp`, `mobKillXp`, and `drop_levels_only_from_mobs_with_tag` values are migrated into the new config keys.
- Fixed bottom HUD background rendering in bottom mode.
  - Bottom style now uses `assets/gui/skill_bar/xp-bar-background-bottom.png`.

### Changed

- `/level` progression mutation commands now use source-aware API calls so events identify command-driven changes.
- Resetting LevelUP data now resets highest-level tracking to the resulting current level.
- HUD display toggle now acts as a hard override for the LevelUP HUD (including bottom-mode vanilla XP bar suppression behavior).
- Updated docs to clarify:
  - `drops_levels` is an entity type tag datapack entry.
  - How to use the new GUI helper APIs.
  - How integration mods consume player views, transactions, milestones, events, and curve data.

### Verified

- `.\gradlew.bat compileJava` currently blocked in this environment by Gradle SSL trust failures resolving Minecraft/NeoForge Maven artifacts.
