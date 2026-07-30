# API Overview

The LevelUP integration surface is split into two halves:

- server and common logic: `LevelUpApi`
- client rendering and GUI helpers: `LevelUpClientApi`

## Server/Common API

Use `LevelUpApi` when you need to:

- read a player's LevelUP level or XP
- grant XP rewards
- set level or XP directly
- register milestone streams
- enforce level requirements
- lock or unlock progression
- spawn LevelUP orb entities
- adjust runtime progression overrides

Detailed reference:

- [Server API Reference](Server-API-Reference)

## Client API

Use `LevelUpClientApi` when you need to:

- draw a LevelUP bar inside your own GUI
- preview post-reward progress
- render level-requirement bars
- append standardized tooltips
- observe client-side data refreshes
- open the built-in bar reposition screen

Detailed reference:

- [Client API Reference](Client-API-Reference)

## Events

Use the event system when you need to react to progression changes rather than pulling state every tick.

Detailed reference:

- [Events Reference](Events-Reference)

## Built-In Sources

Use `LevelUpSources` constants when possible:

- `UNKNOWN`
- `MOB_KILL`
- `GATEWAY_COMPLETE`
- `QUEST_COMPLETE`
- `OBJECTIVE_COMPLETE`
- `ORB_PICKUP`
- `ITEM_USE`
- `COMMAND`

If you need a custom source id, create it with `ResourceLocation.fromNamespaceAndPath(...)` or `LevelUpSources.id(...)` when the source belongs to the `levelup` namespace.
