# Configuration

LevelUP has two config files:

- common: `levelup-common.toml`
- client: `levelup-client.toml`

## Common Config

The common config controls progression math and reward sources.

### `progression.baseXpPerLevel`

- type: integer
- default: `100`
- minimum: `1`
- purpose: base XP term used by `LevelFormula.xpForNextLevel(...)`

### `progression.linearXpPerLevel`

- type: integer
- default: `20`
- minimum: `0`
- purpose: additive linear scaling term applied each level

### `progression.exponent`

- type: double
- default: `1.35`
- range: `1.0` to `5.0`
- purpose: exponential growth factor for the main progression curve

### `progression.levelMultiplier`

- type: double
- default: `0.75`
- range: `0.01` to `100.0`
- purpose: global multiplier applied after the main XP formula
- note: runtime overrides can replace this value through `LevelUpApi.setLevelMultiplierOverride(...)`

### `progression.maxLevel`

- type: integer
- default: `500`
- minimum: `1`
- purpose: hard framework-level cap before per-player lock caps are considered
- note: runtime overrides can replace this value through `LevelUpApi.setMaxLevelOverride(...)`

## Source Config

### `sources.mobs_drop_levels`

- type: boolean
- default: `true`
- purpose: enables generic hostile mob LevelUP orb drops
- behavior: when disabled, hostile-enemy fallback drops are turned off

### `sources.mob_level_drop_base_value`

- type: integer
- default: `8`
- minimum: `0`
- purpose: base value for mob-generated orb rewards
- formula: `base_value * max_health / 20`, rounded down, minimum `1`
- behavior: `0` disables actual mob orb payout even if dropping is otherwise enabled

### `sources.only_tagged_mobs_drop_levels`

- type: boolean
- default: `false`
- purpose: switches mob rewards to entity-tag mode
- behavior: when enabled, only entity types in `levelup:drops_levels` or legacy `levelup:drop_levels` can drop LevelUP orbs
- precedence: this overrides generic hostile mob detection

## Mob Eligibility Rules

`LevelUpConfig.Common.shouldDropLevels(...)` works like this:

1. If `only_tagged_mobs_drop_levels=true`, only tagged entity types are eligible.
2. Otherwise, if `mobs_drop_levels=false`, nothing is eligible.
3. Otherwise, any entity implementing `Enemy` is eligible.

## Legacy Config Migration

On startup, `LevelUpConfig.prepareCommonConfigFile()` looks for legacy keys inside the `[sources]` section:

- `enable_mob_kill_xp`
- `drop_levels_only_from_mobs_with_tag`
- `mobKillXp`

If found, it appends the modern replacements and removes the legacy entries.

## Client Config

The client config controls display behavior only.

### `hud.showTopCenterLevelOverlay`

- type: boolean
- default: `true`
- purpose: master flag for the LevelUP HUD display

### `hud.showTemporaryLevelOverlay`

- type: boolean
- default: `true`
- purpose: enables temporary HUD popups for XP updates and custom HUD events

### `hud.levelHudPosition`

- type: string
- default: `"top"`
- valid values: `top`, `bottom`
- purpose: chooses whether the HUD uses the animated top position or bottom-bar mode
- effect: `bottom` can replace the vanilla experience bar

### `hud.levelHudStayOnScreen`

- type: boolean
- default: `false`
- purpose: keeps the HUD visible even when no XP gain is currently animating

### `hud.levelHudColor`

- type: string
- default: `#53a4bc`
- format: six-digit hex color, optionally prefixed with `#`
- purpose: tint for the fill and level label
- fallback: invalid values fall back to the built-in default color

### `hud.showInventoryLevelBar`

- type: boolean
- default: `true`
- purpose: controls the inventory-screen LevelUP bar

### `hud.hudLevelBarOffsetX`

- type: integer
- default: `0`
- range: `-2000` to `2000`
- purpose: horizontal HUD offset in pixels

### `hud.hudLevelBarOffsetY`

- type: integer
- default: `0`
- range: `-2000` to `2000`
- purpose: vertical HUD offset in pixels

### `hud.inventoryLevelBarOffsetX`

- type: integer
- default: `0`
- range: `-2000` to `2000`
- purpose: horizontal inventory-bar offset in pixels

### `hud.inventoryLevelBarOffsetY`

- type: integer
- default: `0`
- range: `-2000` to `2000`
- purpose: vertical inventory-bar offset in pixels

### `hud.openHudLevelBarRepositionGui`

- type: boolean
- default: `false`
- purpose: one-shot trigger that opens the HUD reposition screen on the next client tick
- behavior: LevelUP resets it to `false` after consuming it

### `hud.openInventoryLevelBarRepositionGui`

- type: boolean
- default: `false`
- purpose: one-shot trigger that opens the inventory reposition screen on the next client tick
- behavior: LevelUP resets it to `false` after consuming it
