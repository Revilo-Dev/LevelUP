# Commands

LevelUP registers the root command:

- `/level`

Permission requirement:

- command source must have permission level `2`

## Add Commands

### `/level add xp <amount> <id>`

- grants LevelUP XP through `LevelUpApi.addXp(...)`
- source id used: `LevelUpSources.COMMAND`
- amount type: non-negative long

### `/level add level <amount> <id>`

- adds to the player's current level by reading current level and calling `setLevel(...)`
- amount type: integer
- negative values are accepted by Brigadier but the implementation clamps the resulting target level to `>= 0`

### `/level add multiplier <amount> <id>`

- adds to the player's stored XP multiplier
- negative total results are clamped to `0`

## Set Commands

### `/level set xp <amount> <id>`

- directly sets total LevelUP XP
- amount type: non-negative long

### `/level set level <amount> <id>`

- directly sets level
- amount type: integer `>= 0`

### `/level set multiplier <amount> <id>`

- directly sets stored multiplier
- amount type: integer `>= 0`

## Reset Command

### `/level reset <id>`

- resets XP, level, multiplier, and highest-level tracking for the target player

## Orb Spawn Command

### `/level spawnorbs <amount>`

- spawns LevelUP orb entities at the command source position
- amount type: integer `>= 1`

## Runtime Control Commands

### `/level pause <true|false>`

- toggles the global paused flag used by reward-style XP grants

### `/level max <value>`

- sets a runtime max-level override
- value type: integer `>= 1`

### `/level query <id>`

- prints:
- current level
- total XP
- multiplier
- active max level
- paused state

## HUD Commands

These do not edit config files. They post client HUD events.

### `/level hud levelbar stayonscreen <value>`

- posts `LevelUpHudStayOnScreenEvent`

### `/level hud levelbar display enable`

- posts `LevelUpHudEnabledEvent(true)`

### `/level hud levelbar display disable`

- posts `LevelUpHudEnabledEvent(false)`

### `/level hud levelbar display top`

- posts `LevelUpHudPositionEvent("top")`

### `/level hud levelbar display bottom`

- posts `LevelUpHudPositionEvent("bottom")`
