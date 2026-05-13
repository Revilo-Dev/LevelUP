# Changelog

## Unreleased

### Added

- New config: `sources.enable_mob_kill_xp` (default `true`).
  - Set to `false` to fully disable LevelUP XP from mob kills.
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
- Fixed mob XP disable behavior:
  - `sources.enable_mob_kill_xp=false` now reliably prevents mob LevelUP XP.
  - `sources.mobKillXp=0` also prevents mob LevelUP XP.
- Fixed mob LevelUP drop amount:
  - Eligible kills now drop exactly `sources.mobKillXp` (no extra vanilla XP conversion).
- Fixed tag-only mob drop mode:
  - Correctly uses `levelup:drops_levels`.
  - Still accepts legacy `levelup:drop_levels`.
- Fixed bottom HUD background rendering in bottom mode.
  - Bottom style now uses `assets/gui/skill_bar/xp-bar-background-bottom.png`.

### Changed

- HUD display toggle now acts as a hard override for the LevelUP HUD (including bottom-mode vanilla XP bar suppression behavior).
- Updated docs to clarify:
  - `drops_levels` is an entity type tag datapack entry.
  - How to use the new GUI helper APIs.

### Verified

- `.\gradlew.bat compileJava`
