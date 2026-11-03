# LevelUP Wiki

What the mod Does:

- stores total LevelUP XP
- current LevelUP level
- per-player XP multiplier
- optional per-player level lock
- highest level reached

The mod is designed for:

- creators and pack makers who want a configurable progression system
- other mod authors who want a stable API for level-gated gameplay, custom XP rewards, progression UI, and milestone-based unlocks

## What The Mod Does

LevelUP adds:

- a server-side LevelUP XP and level system that does not use vanilla XP
- a configurable progression curve and configurable hard level cap
- custom LevelUP XP orb entities that award LevelUP XP on pickup
- optional mob drops that convert eligible kills into LevelUP orb rewards
- a HUD bar and inventory bar that display LevelUP progression
- server commands for testing, administration, and balancing
- a public Java API and NeoForge event surface for integrations

## Runtime Model

At runtime, the system works like this:

1. Player progression is stored in the `player_progression` attachment.
2. Server-side rewards call `LevelUpApi.addExperience(...)`, `addXp(...)`, `awardXp(...)`, `setExperience(...)`, or `setLevel(...)`.
3. The service clamps values, recalculates level, fires events, and syncs the result to the client.
4. The client updates the local attachment snapshot and optionally animates the HUD overlay.
5. Other mods can read progression at any time and react through events instead of polling.

## Page Index

- [Getting Started](Getting-Started)
- [How It Works](How-It-Works)
- [Configuration](Configuration)
- [Commands](Commands)
- [Server API Reference](Server-API-Reference)
- [Client API Reference](Client-API-Reference)
- [Events Reference](Events-Reference)
- [Built-In Content](Built-In-Content)
- [Integration Guide](Integration-Guide)

## Quick Facts

- Minecraft version: `1.21.1`
- NeoForge baseline in this repository: `21.1.240`
- Main public API entry point: `com.revilo.levelup.api.LevelUpApi`
- Client integration entry point: `com.revilo.levelup.api.LevelUpClientApi`
- API version constant: `LevelUpApi.API_VERSION = "2"`

## Important Design Notes

- LevelUP XP is separate from vanilla XP.
- All authoritative progression mutations happen on the server.
- The client receives synced progression snapshots through `S2CPlayerProgressionSyncPacket`.
- Player progression data persists through the `player_progression` attachment and is copied on death.
- The mod exposes both high-level convenience APIs and lower-level events so integrations can choose either push or pull style.
