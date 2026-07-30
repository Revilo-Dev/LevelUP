# Built-In Content

This page documents the concrete content shipped by the mod itself.

## Attachment

### `levelup:player_progression`

- type: NeoForge attachment
- Java storage class: `PlayerProgressionData`
- serialized fields:
- `level`
- `xp`
- `multiplier`
- `locked_level`
- `highest_level_reached`

## Entity

### `levelup:level_orb`

- Java class: `LevelUpXpOrbEntity`
- category: `MobCategory.MISC`
- size: `0.5 x 0.5`
- client tracking range: `6`
- update interval: `2`

Behavior:

- carries LevelUP XP reward value
- can merge with nearby identical-value orbs
- follows nearby players
- awards LevelUP XP on pickup

Orb split thresholds:

- `2477`
- `1237`
- `617`
- `307`
- `149`
- `73`
- `37`
- `17`
- `7`
- `3`
- `1`

## Item

### `levelup:test_skill_orb`

- Java class: `LevelUpXpRewardItem`
- stack size: `64`
- reward amount: `20` LevelUP XP
- source id used on consume: `LevelUpSources.ITEM_USE`

## Tags

### `levelup:drops_levels`

- entity-type tag used when tagged-only mob drops are enabled

### `levelup:drop_levels`

- legacy entity-type tag that is still accepted for compatibility

## GUI Assets

LevelUP ships these textures:

- `gui:skill_bar/xp-bar-background.png`
- `gui:skill_bar/xp-bar-background-bottom.png`
- `gui:skill_bar/xp-bar-progress.png`

## Built-In Translations

Important translation keys include:

- `levelup.ui.level`
- `levelup.ui.requirement`
- `levelup.ui.requirement_met`
- `levelup.ui.reward_preview`
- `levelup.tooltip.requires_level`
- `levelup.tooltip.requirement_met`
- `levelup.tooltip.current_level`
- `levelup.tooltip.xp_until_required`
