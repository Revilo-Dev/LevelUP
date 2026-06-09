# Changelog

## Unreleased

### Added

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

- HUD display toggle now acts as a hard override for the LevelUP HUD (including bottom-mode vanilla XP bar suppression behavior).
- Updated docs to clarify:
  - `drops_levels` is an entity type tag datapack entry.
  - How to use the new GUI helper APIs.

### Verified

- `.\gradlew.bat compileJava`
