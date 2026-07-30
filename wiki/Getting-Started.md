# Getting Started

## For Players And Pack Makers

LevelUP adds a separate progression bar and level system. The important gameplay consequences are:

- vanilla XP and LevelUP XP are different systems
- LevelUP XP is used for integrations, gating, and custom rewards
- mob kills can drop LevelUP orb entities when enabled
- the HUD and inventory can show LevelUP progress independently of vanilla XP

## For Developers

The main entry points are:

- `LevelUpApi` for server-side progression reads and writes
- `LevelUpClientApi` for client-side rendering and GUI helpers
- NeoForge events for reactive integrations

## Development Setup

1. Clone the repository.
2. Import it as a Gradle project.
3. Use Java `21`.
4. Run the NeoForge client development launch.

## Recommended Reading Order

1. [How It Works](How-It-Works)
2. [Configuration](Configuration)
3. [Server API Reference](Server-API-Reference)
4. [Events Reference](Events-Reference)
5. [Client API Reference](Client-API-Reference)
6. [Integration Guide](Integration-Guide)

## Typical Integration Pattern

For most other mods, the common pattern is:

1. Grant LevelUP XP for an action with `LevelUpApi.awardXp(...)`.
2. Gate features with `LevelUpApi.meetsLevelRequirement(...)`.
3. Listen to `PlayerLevelMilestoneEvent` or `LevelUpLevelChangedEvent.LevelUp`.
4. Use `LevelUpClientApi` to render requirement bars or reward previews in your GUI.
