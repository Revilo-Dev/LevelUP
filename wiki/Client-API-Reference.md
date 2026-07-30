# Client API Reference

The client-facing integration surface is `com.revilo.levelup.api.LevelUpClientApi`.

This API is intended for GUI authors, client-only overlays, custom tooltips, and preview bars.

## General Notes

- It assumes a client environment.
- Some methods return `false` when no local player exists.
- Render helpers use LevelUP's built-in textures, dimensions, label style, and color rules.

## Reposition GUI

### `openHudLevelBarRepositionGui()`

- requests the built-in draggable screen for the HUD bar
- actual screen opening happens later on the client tick through `LevelBarRepositionController`

### `openInventoryLevelBarRepositionGui()`

- requests the built-in draggable screen for the inventory bar

## Offset Accessors And Mutators

### `setHudLevelBarOffset(int offsetX, int offsetY)`

- writes `hudLevelBarOffsetX` and `hudLevelBarOffsetY`

### `setInventoryLevelBarOffset(int offsetX, int offsetY)`

- writes `inventoryLevelBarOffsetX` and `inventoryLevelBarOffsetY`

### `getHudLevelBarOffsetX()`

- returns stored HUD X offset

### `getHudLevelBarOffsetY()`

- returns stored HUD Y offset

### `getInventoryLevelBarOffsetX()`

- returns stored inventory X offset

### `getInventoryLevelBarOffsetY()`

- returns stored inventory Y offset

## Bar Dimensions

### `getLevelBarWidth()`

- returns: `184`

### `getLevelBarHeight()`

- returns: `11`

## Local Display Snapshot

### `getLocalLevelProgressDisplay()`

- returns: `LevelProgressDisplay`
- fields:
- `level`
- `currentExperience`
- `requiredExperience`
- `progress`

If there is no local player, the method returns:

- level `0`
- current experience `0`
- required experience `0`
- progress `0.0F`

## Data Update Listener API

### `onDataUpdated(Consumer<LevelProgressDisplay> listener)`

- registers a client-side listener
- storage type: `CopyOnWriteArrayList`

### `removeDataUpdatedListener(Consumer<LevelProgressDisplay> listener)`

- unregisters a listener

### `notifyDataUpdated()`

- internal/public helper that pushes the current `LevelProgressDisplay` to every registered listener
- called after client sync packet handling

## Generic Render Helpers

### `renderLevelBar(GuiGraphics guiGraphics, int x, int y, float progress, Component label)`

- shorthand for top-HUD-style rendering

### `renderTopHudStyleLevelBar(GuiGraphics guiGraphics, int x, int y, float progress, Component label)`

- renders using:
- background enabled
- label enabled
- label Y offset `0`
- alpha `1.0`

### `renderLevelBar(GuiGraphics guiGraphics, int x, int y, float progress, Component label, boolean drawBackground, int labelYOffset, boolean drawLabel, float alpha)`

- lower-level helper exposing the common bar renderer controls

## Player Render Helpers

### `renderPlayerLevelBar(GuiGraphics guiGraphics, int x, int y)`

- returns: `false` if no local player exists
- otherwise renders the local player's current level and progress
- label format: `levelup.ui.level`

### `renderTopHudStylePlayerLevelBar(GuiGraphics guiGraphics, int x, int y)`

- current behavior: exact alias of `renderPlayerLevelBar(...)`

## Requirement Helpers

### `getProgressToRequiredLevel(Player player, int requiredLevel)`

- returns: float in `0..1`
- calculation: `current total xp / total xp required for target level`
- input handling: required level is clamped to at least `1`

### `getLevelRequirementLabel(Player player, int requiredLevel)`

- if the player already meets the requirement, returns the `levelup.ui.requirement_met` translation
- otherwise returns `levelup.ui.requirement`

### `renderRequiredLevelBar(GuiGraphics guiGraphics, int x, int y, int requiredLevel)`

- local-player convenience form
- returns `false` if no local player exists

### `renderRequiredLevelBar(GuiGraphics guiGraphics, int x, int y, Player player, int requiredLevel)`

- renders the requirement bar for a supplied player instance

## Reward Preview Helpers

### `getPreviewProgressAfterXp(Player player, long xpReward)`

- computes a hypothetical total XP after the non-negative reward
- resolves the preview level from that XP
- returns progress through the preview level

### `getRewardPreviewLabel(Player player, long xpReward)`

- returns the `levelup.ui.reward_preview` translation using the preview level and XP reward

### `renderRewardPreviewBar(GuiGraphics guiGraphics, int x, int y, long xpReward)`

- local-player convenience form
- returns `false` if no local player exists

### `renderRewardPreviewBar(GuiGraphics guiGraphics, int x, int y, Player player, long xpReward)`

- renders the hypothetical post-reward bar for a supplied player

## Tooltip Helper

### `appendLevelRequirementTooltip(List<Component> tooltip, Player player, int requiredLevel)`

If the player meets the requirement:

- adds one green line using `levelup.tooltip.requirement_met`

If the player does not meet the requirement:

- adds a red `requires_level` line
- adds a gray `current_level` line
- adds a gray `xp_until_required` line

## Rendering Style Source

The API ultimately delegates to `LevelBarRenderer`, which:

- uses a `184x11` bar
- reads tint color from `hud.levelHudColor`
- falls back to `0x53A4BC` when parsing fails
- draws an outlined label in a darker shade of the selected color
