# LevelUP

LevelUP is a NeoForge progression framework that adds a separate XP and level track for players.
It provides a public API and event hooks so other mods can read player level, grant XP, enforce level requirements, and react to progression changes.

## Core Features

- Separate LevelUP XP and level progression from vanilla XP.
- Configurable XP curve, max level cap, hostile mob level-orb drops, and tagged mob level-orb drops.
- Eligible mob kills drop LevelUP level orbs based on `sources.mob_level_drop_base_value` and mob max health.
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
- `sources.mobs_drop_levels` default `true`
  When `true`, hostile enemy mobs can drop LevelUP level orbs on player kills.
- `sources.mob_level_drop_base_value` default `8`
  Actual orb value is `base_value * max_health / 20`, rounded down with a minimum of `1`.
  Set to `0` to disable all mob LevelUP level-orb drops.
- `sources.only_tagged_mobs_drop_levels` default `false`
  When `true`, only entity types in the `levelup:drops_levels` entity type tag drop LevelUP level orbs (`levelup:drop_levels` is also accepted for legacy setups).
  This overrides the generic hostile mob mode.
  If both `sources.mobs_drop_levels` and `sources.only_tagged_mobs_drop_levels` are `false`, mobs never drop levels.

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
- `hud.hudLevelBarOffsetX` default `0`
  Horizontal HUD LevelUP bar offset in pixels.
- `hud.hudLevelBarOffsetY` default `0`
  Vertical HUD LevelUP bar offset in pixels.
- `hud.inventoryLevelBarOffsetX` default `0`
  Horizontal inventory LevelUP bar offset in pixels.
- `hud.inventoryLevelBarOffsetY` default `0`
  Vertical inventory LevelUP bar offset in pixels.
- `hud.openHudLevelBarRepositionGui` default `false`
  Set to `true` to open the HUD bar reposition GUI. It resets to `false` after opening.
- `hud.openInventoryLevelBarRepositionGui` default `false`
  Set to `true` to open the inventory bar reposition GUI. It resets to `false` after opening.

## Mob Tagging

LevelUP supports entity type tagging to mark mobs as eligible for LevelUP level-orb drops when `sources.only_tagged_mobs_drop_levels=true`.

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

Runtime scoreboard tags such as `Tags:["drops_levels"]` are not used for LevelUP mob drop eligibility.

## Commands

- `/level add xp <amount> <id>`
- `/level add level <amount> <id>`
- `/level add multiplier <amount> <id>`
- `/level set xp <amount> <id>`
- `/level set level <amount> <id>`
- `/level set multiplier <amount> <id>`
- `/level reset <id>`
- `/level spawnorbs <amount>`
- `/level pause <true|false>`
- `/level max <value>`
- `/level query <id>`

## Included Content

- `levelup:test_skill_orb` grants `20` LevelUP XP on use.
- `levelup:level_orb` is the custom XP orb entity used by LevelUP rewards.

## Integration API (for other mods)

Use static methods from `com.revilo.levelup.api.LevelUpApi`.

- `String API_VERSION` is the published API surface version.
- `String getApiVersion()` returns the same API version at runtime.
- `LevelUpCapabilities getCapabilities()` reports supported integration features.
- `Optional<LevelUpPlayerView> getPlayerData(ServerPlayer player)` returns a stable read-only view without exposing attachments.
- `LevelCurve getActiveLevelCurve()` exposes the active configured progression curve.
- `int getLevel(Player player)` returns the player LevelUP level.
- `int getHighestLevelReached(Player player)` returns the highest LevelUP level this player has reached.
- `long getXp(Player player)` returns total stored LevelUP XP.
- `int getXpMultiplier(Player player)` returns the stored per-player XP multiplier.
- `long getXpIntoCurrentLevel(Player player)` returns the player's current progress inside their active level band.
- `long getXpNeededForNextLevel(Player player)` returns remaining XP to level up.
- `float getProgressToNextLevel(Player player)` returns 0..1 progress for current level.
- `int getMaxLevel()` returns the active max level after config or runtime override resolution.
- `OptionalInt getMaximumLevel()` returns the configured maximum level.
- `boolean isAtMaximumLevel(ServerPlayer player)` checks whether the player is capped by the active max or lock.
- `double getLevelMultiplier()` returns the active XP cost multiplier after config or runtime override resolution.
- `long getXpForNextLevel(int currentLevel)` returns the XP cost to advance from the supplied level.
- `long getTotalXpForLevel(int level)` returns the total XP floor for the supplied level.
- `int levelForTotalXp(long totalXp)` resolves a raw total XP value into a LevelUP level using the active max level.
- `boolean meetsLevelRequirement(Player player, int requiredLevel)` checks level gates.
- `LevelChangeResult addExperience(ServerPlayer player, long amount, ResourceLocation source)` grants XP and returns previous/new XP, previous/new level, and crossed levels.
- `long addXp(ServerPlayer player, long amount, ResourceLocation source)` grants XP and returns applied amount after event modifiers.
- `void awardXp(ServerPlayer player, long amount, ResourceLocation source)` convenience wrapper for granting XP.
- `LevelChangeResult setExperience(ServerPlayer player, long xp, ResourceLocation source)` directly sets total XP, recalculates level, syncs, and fires events.
- `LevelChangeResult addLevels(ServerPlayer player, int levels, ResourceLocation source)` grants enough XP to add whole levels from the player's current progression, capped by max level.
- `LevelChangeResult setLevel(ServerPlayer player, int level, ResourceLocation source)` sets level by assigning total XP floor for that level.
- `LevelChangeResult reset(ServerPlayer player, ResourceLocation source)` resets XP, level, multiplier, and highest-level tracking.
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
- `void registerMilestone(ResourceLocation id, int interval)` registers a milestone interval. Crossing level 4 to 11 with interval `5` fires milestones `5` and `10`.
- `void spawnLevelUpXpOrb(ServerLevel level, Vec3 position, int amount)` spawns LevelUP XP orbs.
- `void spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount)` coordinate overload for orb spawning.
- `void spawnLevelUpXpOrbForLevel(ServerLevel level, Vec3 position, int level)` spawns LevelUP XP orbs worth the total XP floor for the supplied level.
- `void spawnLevelUpXpOrbForLevel(ServerLevel level, double x, double y, double z, int level)` coordinate overload for level-based orb spawning.

Client GUI rendering API:

- `com.revilo.levelup.api.LevelUpClientApi.onDataUpdated(listener)` registers a client display-data sync listener.
- `com.revilo.levelup.api.LevelUpClientApi.removeDataUpdatedListener(listener)` unregisters a client sync listener.
- `com.revilo.levelup.api.LevelUpClientApi.getLocalLevelProgressDisplay()` returns local display data for GUI refreshes.
- `com.revilo.levelup.api.LevelUpClientApi.getLevelBarWidth()` returns level bar width.
- `com.revilo.levelup.api.LevelUpClientApi.getLevelBarHeight()` returns level bar height.
- `com.revilo.levelup.api.LevelUpClientApi.renderLevelBar(...)` renders the LevelUP bar with your provided progress/label.
- `com.revilo.levelup.api.LevelUpClientApi.renderTopHudStyleLevelBar(...)` renders with the same background, label placement, alpha, and tint as the LevelUP top HUD.
- `com.revilo.levelup.api.LevelUpClientApi.renderPlayerLevelBar(gui, x, y)` renders the local player's current level/progress and returns `false` if no local player is available.
- `com.revilo.levelup.api.LevelUpClientApi.renderTopHudStylePlayerLevelBar(gui, x, y)` renders the local player's current level/progress using the exact top HUD bar style.
- `com.revilo.levelup.api.LevelUpClientApi.openHudLevelBarRepositionGui()` opens the draggable HUD bar reposition GUI.
- `com.revilo.levelup.api.LevelUpClientApi.openInventoryLevelBarRepositionGui()` opens the draggable inventory bar reposition GUI.
- `com.revilo.levelup.api.LevelUpClientApi.setHudLevelBarOffset(x, y)` updates HUD bar offsets.
- `com.revilo.levelup.api.LevelUpClientApi.setInventoryLevelBarOffset(x, y)` updates inventory bar offsets.
- `com.revilo.levelup.api.LevelUpClientApi.getHudLevelBarOffsetX/Y()` reads HUD bar offsets.
- `com.revilo.levelup.api.LevelUpClientApi.getInventoryLevelBarOffsetX/Y()` reads inventory bar offsets.
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
- `com.revilo.levelup.api.event.PlayerExperienceChangeEvent` fires after stored LevelUP XP changes and includes old XP, new XP, reason, and source.
- `com.revilo.levelup.api.event.PlayerLevelChangeEvent` fires server-side after a player level changes and includes previous level, new level, reason, and source.
- `com.revilo.levelup.api.event.PlayerLevelMilestoneEvent` fires for every registered milestone crossed during a level increase.
- `com.revilo.levelup.api.event.PlayerLevelDataLoadedEvent` fires on the logical server during player login after LevelUP attachment data is available and before sync.
- `LevelUpLevelLockChangedEvent` fires when a player's lock cap changes (`-1` means unlocked).
- `LevelUpOutputEvent` fires only when level increases and includes old/new level, old/new XP, source, and levels gained.
- `LevelUpHudDisplayEvent` can be posted client-side to force the HUD on-screen with custom text/progress for a duration.

## Progression Transactions

Use the result-returning APIs when integrating rewards, commands, quests, or another progression system.

```java
LevelChangeResult result = LevelUpApi.addExperience(
        player,
        100,
        ResourceLocation.fromNamespaceAndPath("examplemod", "quest_reward")
);

for (int milestone : result.crossedMilestones(5)) {
    // Award external milestone rewards from your own mod data.
}
```

Large XP gains are treated as one transaction. A change from level `4` to `16` returns crossed levels `5` through `16`, and a 5-level milestone interval reports `5`, `10`, and `15`.

## Milestone Events

Register milestone intervals during common setup or integration initialization.

```java
LevelUpApi.registerMilestone(
        ResourceLocation.fromNamespaceAndPath("knowledge", "knowledge_point"),
        5
);
```

Then consume milestone events without polling player data.

```java
@SubscribeEvent
public static void onMilestone(PlayerLevelMilestoneEvent event) {
    if (event.getMilestoneId().equals(ResourceLocation.fromNamespaceAndPath("knowledge", "knowledge_point"))) {
        // Award Knowledge Points in the Knowledge mod.
    }
}
```

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

## Let Other Mods Reposition LevelUP Bars

Other mods can trigger the built-in draggable GUI from client code:

```java
import com.revilo.levelup.api.LevelUpClientApi;

// Opens the HUD bar reposition screen.
LevelUpClientApi.openHudLevelBarRepositionGui();

// Opens the inventory bar reposition screen.
LevelUpClientApi.openInventoryLevelBarRepositionGui();
```

Other mods can also update config positions directly:

```java
LevelUpClientApi.setHudLevelBarOffset(12, -6);
LevelUpClientApi.setInventoryLevelBarOffset(0, 18);
```

If a mod wants to trigger repositioning through config mutation (for example from a config GUI button), set:

- `hud.openHudLevelBarRepositionGui=true`, or
- `hud.openInventoryLevelBarRepositionGui=true`

LevelUP consumes these flags and resets them to `false` after opening the GUI.
