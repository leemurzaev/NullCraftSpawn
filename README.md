# NullCraftSpawn

A Paper 1.21 plugin that spreads players out: new players and players who die are placed on a random, safe spot inside a square around the world spawn.

## Features

- **Random first-join spawn.** A player who joins the server for the first time is moved to a random safe spot and greeted with a title and a chat message.
- **Random respawn after death.** Players who die in one of the configured worlds respawn on a random safe spot in the spawn world. Only respawns caused by death count, so leaving the End through the exit portal is not affected.
- **Beds and respawn anchors come first** (optional). When the server respawns a player at a bed or a charged respawn anchor, that location is kept. If another plugin replaced it, the original bed or anchor location is put back.
- **Square spawn area.** X and Z are each within `center +/- radius`. The center is the world spawn or a fixed X/Z.
- **Safety checks.** The ground block must be solid and not a liquid. The feet and head blocks must be free (passable, not a liquid). None of the three may be on a configurable list of harmful blocks (lava, magma, cactus, fire, powder snow, cobwebs and more). Tree leaves are skipped when the ground is found, so players never land on top of tree leaves.
- **Respawns without a wait.** The plugin keeps a small pool of checked spots ready, with their chunks held loaded through plugin chunk tickets, so a respawn never waits for a search. A stored spot is checked again just before it is used, because players may have built there in the meantime.
- **Light on the server.** Searches load chunks asynchronously and never generate new terrain. If the pool is empty, a fallback search uses only chunks that are already generated. If no safe spot turns up, the player gets the normal server spawn.
- **Configurable messages.** All texts support `&` colour codes and the placeholders `{tag}`, `{player}`, `{x}` and `{z}`. Set a text to `''` to turn it off.
- **Admin command** with tab completion to reload, inspect and test the setup in-game.
- **Unit tests** (JUnit 5) for the spawn area, the safety rule, the respawn decision, settings parsing and message placeholders.

## Requirements

| | |
|---|---|
| Server | [Paper](https://papermc.io) 1.21.x, or a Paper fork such as [Purpur](https://purpurmc.org) (built against the Purpur 1.21.11 API) |
| Java | 21 |

Spigot and CraftBukkit are not supported. The plugin uses Paper-only API: async chunk loading, async teleports and Adventure text.

## Installation

1. Build the jar (see [Building](#building)) and copy `NullCraftSpawn-1.0.0.jar` into the server's `plugins/` folder.
2. Start the server once. The plugin creates `plugins/NullCraftSpawn/config.yml` and logs the active area, for example `Random spawn in world: X -350 to 350, Z -350 to 350`.
3. Edit `config.yml` and run `/ncspawn reload`. You can also restart the server instead.
4. Run `/ncspawn status` to check the area and the ready spots, and `/ncspawn test` to try a spot yourself.

> **Tip:** the plugin only uses chunks that already exist. Pre-generate the spawn area (for example with a pre-generation plugin such as Chunky) so the whole square can be used.

If the world named in `world` does not exist, the plugin logs an error and random spawn stays off until the config is fixed and reloaded.

## Commands and permissions

| Command | Description | Permission |
|---|---|---|
| `/ncspawn reload` | Re-reads `config.yml` and rebuilds the pool of ready spots. | `nullcraft.spawn.admin` |
| `/ncspawn status` | Shows the world, the area bounds, which features are on, and the ready spots with their coordinates. Works from the console. | `nullcraft.spawn.admin` |
| `/ncspawn test` | Teleports you to a random safe spot from the pool. Players only. | `nullcraft.spawn.admin` |

| Permission | Default | Description |
|---|---|---|
| `nullcraft.spawn.admin` | op | Use `/ncspawn` |

Players need no permission to get a random spawn.

## Configuration

`plugins/NullCraftSpawn/config.yml`

| Key | Default | Description |
|---|---|---|
| `world` | `world` | World the random spots are in. Death respawns also go to this world. |
| `center.use-world-spawn` | `true` | `true` uses the world's spawn point as the center; `false` uses `center.x` and `center.z`. |
| `center.x`, `center.z` | `0` | Fixed center, used when `use-world-spawn` is `false`. |
| `radius` | `350` | Half the side of the square. Values below 1 fall back to 350. |
| `new-players` | `true` | Random spot on a player's first join. |
| `on-death` | `true` | Random spot when a player respawns after dying. |
| `respect-beds` | `true` | Beds and charged respawn anchors take priority over the random spot. |
| `death-worlds` | `world`, `world_nether`, `world_the_end` | Deaths in these worlds lead to a random spot. Deaths elsewhere are left alone. |
| `max-attempts` | `40` | Random columns tried per spot before the search gives up. |
| `pool-size` | `6` | Number of checked spots kept ready, with their chunks loaded. |
| `unsafe-blocks` | see below | Block names (case-insensitive) a player is never placed on or inside. |
| `messages.*` | | `tag`, `first-join-title`, `first-join-subtitle`, `first-join` and `respawn` texts. |

Trimmed example:

```yaml
world: world

center:
  use-world-spawn: true
  x: 0
  z: 0

radius: 350

new-players: true
on-death: true
respect-beds: true

death-worlds:
  - world
  - world_nether
  - world_the_end

max-attempts: 40
pool-size: 6

unsafe-blocks:
  - lava
  - magma_block
  - cactus
  - fire
  - powder_snow
  - cobweb
  # ...

# & colour codes; '' sends nothing. Placeholders: {tag} {player} {x} {z}
messages:
  tag: '&8[&5&lNULL&d&lCRAFT &f&lBETA&8]'
  first-join-title: '&5&lNULL&d&lCRAFT'
  first-join-subtitle: '&7OP ANARCHY &8| &f&lBETA'
  first-join: '{tag} &7Welcome &f{player}&7! You spawned at &f{x}&7, &f{z}&7.'
  respawn: '{tag} &7You died and respawned at &f{x}&7, &f{z}&7.'
```

The default messages carry NullCraft branding. Change the `messages` section to match your own server.

## How it works

- **Respawns.** At `LOWEST` priority, the plugin records the server's own respawn location. At `HIGHEST` priority, it picks one of three outcomes: a random spot, the recorded bed or anchor location, or no change (the respawn was not caused by death, or the death happened outside `death-worlds`).
- **New players** are moved two ticks after they join, with an async teleport, so other join handlers finish first. The welcome title and message appear only after the teleport succeeds.
- **Spot pool.** At most `pool-size` searches run at once. Each search loads its chunk asynchronously and checks the column on the main thread. A spot's chunk stays held for 100 ticks (about five seconds) after the spot is used, so it is still loaded when the player arrives.

## Building

You need JDK 21 and Maven.

```bash
mvn package
```

The jar is written to `target/NullCraftSpawn-1.0.0.jar`. The unit tests run as part of the build; add `-DskipTests` to skip them.

## Project layout

| Class | Role |
|---|---|
| `NullCraftSpawn` | Plugin entry point; loads settings and builds the spot pool |
| `SpawnListener` | First-join and respawn handling |
| `SpawnCommand` | `/ncspawn reload/status/test` and its tab completion |
| `SpotPool` | Ready spots, chunk tickets and refills |
| `SafeSpotFinder` | Async and fallback search for safe columns |
| `SafetyRule`, `SpawnArea`, `RespawnDecision` | Pure logic, covered by unit tests |
| `SpawnSettings`, `Messenger`, `MessageTemplate` | Config parsing and player messages |

## About

Built for the NullCraft network by ParkDawg ([@leemurzaev](https://github.com/leemurzaev)).
