# LevelUP

LevelUP is a NeoForge progression framework that adds a separate XP and level track for players.
It provides a public API and event hooks so other mods can read player level, grant XP, enforce level requirements, and react to progression changes.

## Core Features

- Separate LevelUP XP and level progression from vanilla XP.
- Configurable XP curve, max level cap, hostile mob XP drops, and tagged mob XP drops.
- Eligible mob kills drop LevelUP XP equal to `sources.mobKillXp`.
- Server-side LevelUP XP orb entity and pickup flow.
- Animated top-center HUD overlay for LevelUP XP gain feedback that stays active during chained XP gains, slides down from off-screen when shown, and slides back up when finished.
- Optional always-on HUD mode that keeps the level bar in the same on-screen position as the temporary overlay.
- Bottom HUD mode can replace the vanilla XP bar with a persistent LevelUP bar while the HUD is enabled.
- Commands for admins to grant XP and manage progression values.
- Includes a `test_skill_orb` item that grants LevelUP XP on use.
- Integration hooks for external mods through static API calls and NeoForge events.

## Config

Common config:

- `progression.baseXpPerLevel` default `100`
- `progression.linearXpPerLevel` default `20`
- `progression.exponent` default `1.35`
- `progression.levelMultiplier` default `0.75`
- `progression.maxLevel` default `500`
- `sources.enable_mob_kill_xp` default `true`
  When `false`, mob kills will not drop any LevelUP XP.
- `sources.mobKillXp` default `8`
  Set to `0` to disable all LevelUP XP from mob kills.
- `sources.drop_levels_only_from_mobs_with_tag` default `false`
  When `false`, all hostile mobs drop LevelUP XP.
  When `true`, only entity types in the `levelup:drops_levels` entity type tag drop LevelUP XP (`levelup:drop_levels` is also accepted for legacy setups).

Client config:

- `hud.showTopCenterLevelOverlay` default `true`
  Controls the LevelUP HUD display.
- `hud.showTemporaryLevelOverlay` default `true`
  Controls temporary LevelUP HUD popups from XP updates and custom HUD events.
- `hud.levelHudPosition` default `top`
  Accepts `top` or `bottom`.
  `bottom` replaces the vanilla XP bar with a persistent LevelUP bar in normal HUD gameplay and skips the top progress animation.
- `hud.levelHudStayOnScreen` default `false`
  Keeps the LevelUP HUD visible even when no XP gain is currently animating.
  Uses the same animated top HUD bar and keeps it visible until this option is turned off, then it slides out normally.
- `hud.levelHudColor` default `#53a4bc`
  Tint color for the LevelUP progress fill and level text.
- `hud.showInventoryLevelBar` default `true`
  Controls the LevelUP progress bar shown in the inventory screen.

## Mob Tagging

LevelUP supports entity type tagging to mark mobs as eligible for LevelUP XP drops when `sources.drop_levels_only_from_mobs_with_tag=true`.

- Entity type tag: add entity IDs to `data/levelup/tags/entity_types/drops_levels.json` in a datapack.

Examples:

```json
{
  "replace": false,
  "values": [
    "minecraft:zombie",
    "minecraft:skeleton"
  ]
}
```

Runtime scoreboard tags such as `Tags:["drops_levels"]` are not used for LevelUP mob XP eligibility.

## Commands

- `/levels add xp <amount> <id>`
- `/levels add level <amount> <id>`
- `/levels add multiplier <amount> <id>`
- `/levels set xp <amount> <id>`
- `/levels set level <amount> <id>`
- `/levels set multiplier <amount> <id>`
- `/levels reset <id>`
- `/levels spawnorbs <amount>`
- `/levels pause <true|false>`
- `/levels max <value>`
- `/levels query <id>`

## Included Content

- `levelup:test_skill_orb` grants `20` LevelUP XP on use.
- `levelup:level_orb` is the custom XP orb entity used by LevelUP rewards.

## Integration API (for other mods)

Use static methods from `com.revilo.levelup.api.LevelUpApi`.

- `int getLevel(Player player)` returns the player LevelUP level.
- `long getXp(Player player)` returns total stored LevelUP XP.
- `int getXpMultiplier(Player player)` returns the stored per-player XP multiplier.
- `long getXpIntoCurrentLevel(Player player)` returns the player's current progress inside their active level band.
- `long getXpNeededForNextLevel(Player player)` returns remaining XP to level up.
- `float getProgressToNextLevel(Player player)` returns 0..1 progress for current level.
- `int getMaxLevel()` returns the active max level after config or runtime override resolution.
- `double getLevelMultiplier()` returns the active XP cost multiplier after config or runtime override resolution.
- `long getXpForNextLevel(int currentLevel)` returns the XP cost to advance from the supplied level.
- `long getTotalXpForLevel(int level)` returns the total XP floor for the supplied level.
- `int levelForTotalXp(long totalXp)` resolves a raw total XP value into a LevelUP level using the active max level.
- `boolean meetsLevelRequirement(Player player, int requiredLevel)` checks level gates.
- `long addXp(ServerPlayer player, long amount, ResourceLocation source)` grants XP and returns applied amount after event modifiers.
- `void awardXp(ServerPlayer player, long amount, ResourceLocation source)` convenience wrapper for granting XP.
- `long addLevels(ServerPlayer player, int levels, ResourceLocation source)` grants enough XP to add whole levels from the player's current progression, capped by max level.
- `void setXp(ServerPlayer player, long xp)` directly sets total XP and recalculates level.
- `void setLevel(ServerPlayer player, int level)` sets level by assigning total XP floor for that level.
- `void setXpMultiplier(ServerPlayer player, int multiplier)` sets the stored per-player XP multiplier.
- `boolean isLevelLocked(Player player)` returns whether player progression is currently level-locked.
- `int getLockedLevel(Player player)` returns locked cap level, or `-1` when unlocked.
- `void lockLevel(ServerPlayer player, int level)` locks progression to the supplied level cap for that player.
- `void unlockLevel(ServerPlayer player)` clears the player's lock and allows progression to continue.
- `boolean isPaused()` returns whether LevelUP XP gains are currently paused.
- `void setPaused(boolean paused)` pauses or resumes LevelUP XP gains.
- `void setMaxLevelOverride(int maxLevel)` overrides the configured hard cap until cleared.
- `void clearMaxLevelOverride()` restores the configured hard cap.
- `void setLevelMultiplierOverride(double levelMultiplier)` overrides the configured XP scaling multiplier until cleared.
- `void clearLevelMultiplierOverride()` restores the configured XP scaling multiplier.
- `void sync(ServerPlayer player)` forces network sync of progression data.
- `void spawnLevelUpXpOrb(ServerLevel level, Vec3 position, int amount)` spawns LevelUP XP orbs.
- `void spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount)` coordinate overload for orb spawning.
- `void spawnLevelUpXpOrbForLevel(ServerLevel level, Vec3 position, int level)` spawns LevelUP XP orbs worth the total XP floor for the supplied level.
- `void spawnLevelUpXpOrbForLevel(ServerLevel level, double x, double y, double z, int level)` coordinate overload for level-based orb spawning.

Client GUI rendering API:

- `com.revilo.levelup.api.LevelUpClientApi.getLevelBarWidth()` returns level bar width.
- `com.revilo.levelup.api.LevelUpClientApi.getLevelBarHeight()` returns level bar height.
- `com.revilo.levelup.api.LevelUpClientApi.renderLevelBar(...)` renders the LevelUP bar with your provided progress/label.
- `com.revilo.levelup.api.LevelUpClientApi.renderTopHudStyleLevelBar(...)` renders with the same background, label placement, alpha, and tint as the LevelUP top HUD.
- `com.revilo.levelup.api.LevelUpClientApi.renderPlayerLevelBar(gui, x, y)` renders the local player's current level/progress and returns `false` if no local player is available.
- `com.revilo.levelup.api.LevelUpClientApi.renderTopHudStylePlayerLevelBar(gui, x, y)` renders the local player's current level/progress using the exact top HUD bar style.
- `com.revilo.levelup.api.LevelUpClientApi.getProgressToRequiredLevel(player, requiredLevel)` returns 0..1 progress toward a specific required level.
- `com.revilo.levelup.api.LevelUpClientApi.renderRequiredLevelBar(...)` renders a top-HUD-style bar for locked buttons, boss keys, dungeons, or other level requirements.
- `com.revilo.levelup.api.LevelUpClientApi.renderRewardPreviewBar(...)` renders a top-HUD-style preview of the player's level progress after a proposed XP reward.
- `com.revilo.levelup.api.LevelUpClientApi.appendLevelRequirementTooltip(...)` appends standard LevelUP requirement lines to an item or GUI tooltip.

## Events You Can Subscribe To

All events are posted on `NeoForge.EVENT_BUS`.

- `LevelUpXpGainedEvent` is cancellable and fires before XP is applied.
- `LevelUpXpGainedEvent#setAmount(long)` lets integrations scale or clamp incoming XP.
- `LevelUpLevelChangedEvent` fires when stored level changes in either direction.
- `LevelUpLevelChangedEvent.LevelUp` fires once per level gained when multiple levels are gained in one update.
- `LevelUpLevelLockChangedEvent` fires when a player's lock cap changes (`-1` means unlocked).
- `LevelUpOutputEvent` fires only when level increases and includes old/new level, old/new XP, source, and levels gained.
- `LevelUpHudDisplayEvent` can be posted client-side to force the HUD on-screen with custom text/progress for a duration.

## XP Source IDs

Use `com.revilo.levelup.api.LevelUpSources` constants when possible:

- `UNKNOWN`
- `MOB_KILL`
- `GATEWAY_COMPLETE`
- `QUEST_COMPLETE`
- `OBJECTIVE_COMPLETE`
- `ORB_PICKUP`
- `ITEM_USE`
- `COMMAND`

Create custom source IDs with `LevelUpSources.id("your_path")`.

## Minimal Example

```java
LevelUpApi.awardXp(player, 25, LevelUpSources.QUEST_COMPLETE);
LevelUpApi.setMaxLevelOverride(1000);
LevelUpApi.setLevelMultiplierOverride(0.60D);

if (LevelUpApi.meetsLevelRequirement(player, 20)) {
    // Unlock feature
}
```

## Rendering The LevelUP Bar In Your Own GUI

Use the client API from your `Screen#render(...)` (or equivalent GUI draw event).

```java
import com.revilo.levelup.api.LevelUpClientApi;
import net.minecraft.client.gui.GuiGraphics;

@Override
public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    int x = (this.width - LevelUpClientApi.getLevelBarWidth()) / 2;
    int y = this.topPos + 16; // place where your GUI needs it
    LevelUpClientApi.renderTopHudStylePlayerLevelBar(guiGraphics, x, y);
}
```

If you want to use custom text/progress instead of the current player value:

```java
LevelUpClientApi.renderTopHudStyleLevelBar(
        guiGraphics,
        x,
        y,
        0.42F,
        Component.literal("Level 12")
);
```

## Useful GUI Patterns

Level-gated GUI button:

```java
int requiredLevel = 20;
boolean unlocked = LevelUpApi.meetsLevelRequirement(this.minecraft.player, requiredLevel);
this.miningMasteryButton.active = unlocked;

LevelUpClientApi.renderRequiredLevelBar(
        guiGraphics,
        this.leftPos + 8,
        this.topPos + 42,
        requiredLevel
);
```

Boss key or dungeon entry confirmation:

```java
int requiredLevel = 35;
LevelUpClientApi.renderRequiredLevelBar(
        guiGraphics,
        (this.width - LevelUpClientApi.getLevelBarWidth()) / 2,
        this.topPos + 64,
        requiredLevel
);

this.enterButton.active = LevelUpApi.meetsLevelRequirement(this.minecraft.player, requiredLevel);
```

Item tooltip requirement:

```java
@Override
public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
        LevelUpClientApi.appendLevelRequirementTooltip(tooltip, player, 15);
    }
}
```

Custom reward preview before claiming:

```java
long xpReward = 250L;
LevelUpClientApi.renderRewardPreviewBar(
        guiGraphics,
        this.leftPos + 8,
        this.topPos + 92,
        xpReward
);
```
