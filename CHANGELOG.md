# Changelog

## Unreleased

### Added

- Added `sources.enable_mob_kill_xp` common config.
  - Defaults to `true`.
  - Set to `false` to completely disable LevelUP XP from mob kills.
- Added `LevelUpClientApi` for other mods to render the LevelUP progress bar in custom GUIs.
- Added top-HUD-style rendering helpers so external GUIs can use the same bar background, tint, label placement, and alpha as the LevelUP HUD.
- Added level requirement GUI helpers:
  - `getProgressToRequiredLevel(...)`
  - `renderRequiredLevelBar(...)`
  - `getLevelRequirementLabel(...)`
- Added reward preview GUI helpers:
  - `getPreviewProgressAfterXp(...)`
  - `getRewardPreviewLabel(...)`
  - `renderRewardPreviewBar(...)`
- Added tooltip helper:
  - `appendLevelRequirementTooltip(...)`
- Added translations for level requirements, reward previews, and tooltip requirement text.

### Fixed

- Fixed LevelUP XP being awarded or the level bar appearing from player damage side effects.
  - Mob LevelUP XP now requires the damage source to be `minecraft:player_attack`.
  - This blocks thorns, retaliation effects, magic, explosions, projectiles, and other player-attributed damage sources from triggering mob XP.
- Fixed `sources.mobKillXp=0` still allowing vanilla mob XP rewards to become LevelUP orb drops.
  - `sources.mobKillXp=0` now hard-disables mob LevelUP drops.
  - `sources.enable_mob_kill_xp=false` also hard-disables mob LevelUP drops.
- Mob LevelUP drops no longer add vanilla mob XP reward values.
  - Eligible mob kills now drop exactly `sources.mobKillXp`.
- Fixed mob XP eligibility in tag-only mode.
  - `sources.drop_levels_only_from_mobs_with_tag=true` now checks entity type tags correctly.
  - Uses `levelup:drops_levels`.
  - Accepts legacy `levelup:drop_levels`.
- Fixed passive mobs and untagged mobs giving LevelUP XP when they should not.
- Mob kill XP now requires a direct melee player attack, preventing indirect player-associated damage sources from incorrectly spawning LevelUP XP.

### Changed

- Updated mob XP documentation to clarify that `drops_levels` is an entity type tag datapack entry, not a spawned entity NBT scoreboard tag.
- Updated README with examples for:
  - Rendering the LevelUP bar in another mod's GUI.
  - Level-gated GUI buttons.
  - Boss/key/dungeon entry screens.
  - Item/tooltips integration.
  - Custom reward preview bars.

### Verified

- `.\gradlew.bat compileJava`
