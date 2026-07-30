# Events Reference

All listed events are posted on `NeoForge.EVENT_BUS`.

## Server-Side Progression Events

### `LevelUpXpGainedEvent`

- package: `com.revilo.levelup.event`
- cancellable: yes
- fired from: `LevelUpService.addExperience(...)`
- timing: before XP is multiplied and before XP is committed

Fields and methods:

- `getPlayer()`
- `getSource()`
- `getAmount()`
- `setAmount(long amount)`

Behavior notes:

- constructor and setter clamp negative amounts to `0`
- canceling the event prevents the transaction from progressing

## XP/Level Result Events

### `PlayerExperienceChangeEvent`

- package: `com.revilo.levelup.api.event`
- cancellable: no
- fired when: stored total XP changes

Fields:

- `getPlayer()`
- `getOldExperience()`
- `getNewExperience()`
- `getReason()`
- `getSource()`

### `LevelUpLevelChangedEvent`

- package: `com.revilo.levelup.event`
- fired when: player level changes in either direction

Fields:

- `getPlayer()`
- `getOldLevel()`
- `getNewLevel()`

### `LevelUpLevelChangedEvent.LevelUp`

- subclass of `LevelUpLevelChangedEvent`
- fired once per gained level during an upward transition
- example: jumping from level `4` to `7` emits three instances for `4->5`, `5->6`, and `6->7`

### `PlayerLevelChangeEvent`

- package: `com.revilo.levelup.api.event`
- fired when: player level changes in either direction
- adds source and reason metadata

Fields:

- `getPlayer()`
- `getPreviousLevel()`
- `getNewLevel()`
- `getReason()`
- `getSource()`

### `LevelUpOutputEvent`

- package: `com.revilo.levelup.event`
- fired when: level increases only
- purpose: high-signal integration hook for "the player leveled up"

Fields:

- `getPlayer()`
- `getOldLevel()`
- `getNewLevel()`
- `getOldXp()`
- `getNewXp()`
- `getSource()`
- `getLevelsGained()`

### `PlayerLevelMilestoneEvent`

- package: `com.revilo.levelup.api.event`
- fired when: a registered milestone multiple is crossed during an upward level transition

Fields:

- `getPlayer()`
- `getMilestoneId()`
- `getMilestone()`
- `getPreviousLevel()`
- `getNewLevel()`
- `getReason()`
- `getSource()`

Notes:

- the default LevelUP milestone stream is every `5` levels
- custom streams can be added with `registerMilestone(...)`

### `PlayerLevelDataLoadedEvent`

- package: `com.revilo.levelup.api.event`
- fired from: login handling
- timing: after the server can read attachment data and before `LevelUpApi.sync(player)` is called in the login handler

Fields:

- `getPlayer()`
- `getData()`

## Lock Events

### `LevelUpLevelLockChangedEvent`

- package: `com.revilo.levelup.event`
- fired when: a player's lock state changes

Fields:

- `getPlayer()`
- `getOldLockedLevel()`
- `getNewLockedLevel()`
- `isLocked()`

Conventions:

- `-1` means unlocked

## Client HUD Events

These are consumed by `LevelUpClientEvents.GameEvents`.

### `LevelUpHudDisplayEvent`

- package: `com.revilo.levelup.event`
- purpose: force a temporary custom HUD message

Fields:

- `getLabel()`
- `getProgress()`
- `getDurationMillis()`

Clamping:

- progress is clamped into `0..1`
- duration is clamped to `>= 0`

### `LevelUpHudEnabledEvent`

- purpose: force-enable or force-disable the HUD through an event-side override

Field:

- `isEnabled()`

### `LevelUpHudStayOnScreenEvent`

- purpose: lock the HUD into stay-on-screen mode through an event-side override

Field:

- `shouldStayOnScreen()`

### `LevelUpHudPositionEvent`

- purpose: force the HUD into `top` or `bottom` mode through an event-side override

Field:

- `getPosition()`

## `LevelChangeReason`

Many events include `LevelChangeReason`.

Possible values:

- `EXPERIENCE_GAIN`
- `COMMAND`
- `API`
- `DEATH_PENALTY`
- `DATA_LOAD`
- `RESET`
- `UNKNOWN`

Current implementation notes:

- reward-style XP grants use `EXPERIENCE_GAIN` unless the source is `LevelUpSources.COMMAND`
- direct setters use `API` unless the source is `LevelUpSources.COMMAND`
- `RESET` is used by `reset(...)`
- some enum members exist for future or external use and are not emitted by the current code paths in this repository
