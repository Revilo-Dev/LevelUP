# How It Works

This page explains the internal progression flow from storage to rendering.

## Storage Model

LevelUP stores all player progression in the NeoForge attachment:

- id: `levelup:player_progression`
- Java type: `PlayerProgressionData`
- registration: `LevelUpAttachments.PLAYER_PROGRESSION`
- serialization: `PlayerProgressionData.CODEC`
- death behavior: `copyOnDeath()`

The stored fields are:

- `level`
- `xp`
- `multiplier`
- `locked_level`
- `highest_level_reached`

## Authoritative Side

The server is authoritative for all progression changes.

Public write APIs like `addExperience(...)`, `addXp(...)`, `setExperience(...)`, `setLevel(...)`, `reset(...)`, `lockLevel(...)`, and `setXpMultiplier(...)` all operate on server-side attachment data and then sync to the client.

## XP Grant Flow

`LevelUpApi.addExperience(player, amount, source)` resolves like this:

1. If the global paused flag is set, the transaction is ignored.
2. The requested amount is clamped to `>= 0`.
3. `LevelUpXpGainedEvent` is posted and may cancel or modify the amount.
4. The resulting amount is multiplied by the player's stored XP multiplier.
5. XP is added using saturating arithmetic.
6. `applyExperience(...)` clamps the final total to the effective max level.
7. Events are posted.
8. The player is synced to the client, with `showOverlay=true`.

## Direct Set Flow

`setExperience(...)` and `setLevel(...)` bypass the pre-award event and call `applyExperience(...)` directly.

Important differences:

- no `LevelUpXpGainedEvent`
- no reward multiplier application
- no automatic overlay request
- reason classification comes from source mapping, not from XP-gain logic

## Effective Max Level

The effective cap is not always the same as the global cap.

It is calculated as:

1. start with `LevelUpApi.getMaxLevel()`
2. if the player has a lock cap, use the lower of the two
3. clamp the result to at least `0`

This means a player's lock can reduce progression below the global cap, but never above it.

## Event Order During A Successful XP Change

Inside `applyExperience(...)`, the observable order is:

1. update stored XP
2. recalculate and store level
3. if XP changed, post `PlayerExperienceChangeEvent`
4. if level changed, post `LevelUpLevelChangedEvent`
5. if level changed, post `PlayerLevelChangeEvent`
6. if the level increased:
7. post `LevelUpOutputEvent`
8. post one `LevelUpLevelChangedEvent.LevelUp` for each gained level
9. post `PlayerLevelMilestoneEvent` for each crossed registered milestone
10. sync the new data to the client

## Level Curve

The XP formula lives in `LevelFormula`.

Per-level cost:

- base term: `baseXpPerLevel * (level + 1)^exponent`
- linear term: `linearXpPerLevel * level`
- band multiplier:
- levels `0-9`: `0.70`
- levels `10-49`: `1.00`
- levels `50-89`: `1.25`
- levels `90-99`: handled by the peak-band distribution logic
- global multiplier: `LevelUpProgressionOverrides.getLevelMultiplier()`

### Peak Band Behavior

For levels `90-99`, LevelUP does not use the normal formula directly. Instead, it sums the total XP cost from levels `0-89`, divides that total across ten levels, and distributes any remainder across the first entries in the band.

## Total XP And Level Resolution

- `totalXpForLevel(level)` sums every per-level cost before that level
- `levelForXp(totalXp, maxLevel)` walks the curve forward until the remaining XP is less than the next level cost

This means LevelUP levels are derived from stored total XP, not tracked independently as a separate source of truth.

## Networking

The client sync packet is `S2CPlayerProgressionSyncPacket`.

Payload fields:

- `level`
- `xp`
- `multiplier`
- `highestLevelReached`
- `showOverlay`

When received on the client, LevelUP:

1. reads the local player attachment
2. captures the old XP value
3. writes the synced values into the local attachment
4. if `showOverlay=true`, calls `TopCenterLevelOverlay.onProgressionUpdated(oldXp, newXp)`
5. notifies `LevelUpClientApi` data listeners

## HUD Model

The HUD is registered above the vanilla experience bar.

Two major modes exist:

- `top`: animated popup-style progress bar
- `bottom`: persistent bottom-bar mode that can hide the vanilla XP bar and jump meter

### Top Mode

When XP increases:

- displayed XP interpolates from the current visible value to the new target
- the animation duration is clamped between `650ms` and `1800ms`
- the bar slides in over `150ms`
- it remains visible for a hold period after the animation
- it slides out over `400ms`

### Bottom Mode

When `levelHudPosition=bottom`:

- the bar uses the bottom background texture
- the label is the bare level number instead of `Level X`
- the vanilla XP bar and jump meter are canceled in the GUI-layer pre-event
- the animation behaves more like a state display than a slide-in popup

## Inventory Bar

The inventory bar is drawn only on `InventoryScreen`.

Anchor position:

- horizontally centered
- vertically at `(screenHeight / 2) + 95`

Configured offsets are then added to that anchor.

## Reposition GUI

`LevelBarRepositionController` opens a dedicated drag screen on the next client tick when:

- a direct API request is made, or
- one of the config trigger flags is set

The reposition screen:

- previews a sample `Level 42` bar at `65%`
- supports drag repositioning
- supports Done, Reset, and Cancel
- writes offsets directly into the client config values

## Orb Runtime

The custom entity `levelup:level_orb` is the LevelUP reward carrier.

Notable behavior:

- value splitting uses vanilla-style thresholds through `getOrbValue(...)`
- identical nearby orbs may merge
- merged orb stacks track `count`
- the orb searches for a nearby player and moves toward them
- pickup uses `player.takeXpDelay = 2`
- pickup awards `LevelUpSources.ORB_PICKUP`
- only the orb's `value` is granted per pickup event; `count` controls how many repeated pickups remain in the merged stack

## Mob Drop Flow

On `LivingDeathEvent`, LevelUP:

1. requires the dead entity to be a `Mob`
2. requires the world to be a `ServerLevel`
3. requires the damage source to be a direct `DamageTypes.PLAYER_ATTACK`
4. requires the attacker to be a `ServerPlayer`
5. requires kill credit to belong to that player
6. checks config eligibility
7. computes orb payout from base value and target max health
8. spawns LevelUP orb entities at the mob position
