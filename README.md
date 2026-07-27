<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

**Plan Create pipe routes, preview them, and place the whole line at once.**

A utility addon for [Create](https://github.com/Creators-of-Create/Create) on Minecraft `1.21.1`.

![Create](https://img.shields.io/badge/Create-6.0.10-7B4F1D?style=for-the-badge)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-NeoForge-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Version](https://img.shields.io/badge/Version-0.4.0--beta-455A64?style=for-the-badge)
</div>

---

## What It Does

Create factories are fun; placing long fluid pipe runs block by block is not.

**Create: Pipe Connector** adds a connector mode that lets you choose a start point, preview a route, adjust it with anchors or radial options, and place the full pipe line in one action.

## Highlights

- **Live ghost preview:** See pipes, pumps, glass sections, and casing before placement.
- **Obstacle-aware routing:** The route searches for a valid path around blocked spaces.
- **Anchor support:** Add route waypoints when you want a specific turn or support point.
- **Radial options menu:** Configure route style, pump mode, pump direction, casing, and pipe style.
- **Survival checks:** Required/available materials are shown and missing parts are tinted red.
- **Create integration:** Placement refreshes Create pipe networks and supports Create's wrench for pipe style toggles.

## Supported Blocks

Pipe routing starts from and places:

- `create:fluid_pipe`

Optional route features use Create blocks only:

- `create:mechanical_pump`
- `create:glass_fluid_pipe`
- `create:copper_casing`
- `create:encased_fluid_pipe`

`create:smart_fluid_pipe` is intentionally not used as a connector pipe because it is a filtering pipe, not a normal routing pipe.

## Default Controls

Only the core controls are assigned by default:

| Action | Default |
| --- | --- |
| Toggle Connector Pipe mode | `B` |
| Start / confirm route | Right-click |
| Open radial options | `N` |
| Add anchor | `C` |
| Lock / unlock preview target | `Left Alt` |
| Cancel current route | Left-click |

Advanced actions, such as undo anchor, manual pump marks, casing marks, pump cycling, pipe style cycling, and pump direction reversal, are available in Minecraft's Controls menu but are **unassigned by default**. Use the radial menu for those options, or bind keys if you prefer direct shortcuts.

## How To Use

1. Press `B` to enable **Connector Pipe** mode.
2. Hold `create:fluid_pipe` in either hand.
3. Right-click a reachable block or pipe face to start a route.
4. Move your crosshair to guide the live preview.
5. Optional: press `C` to add an anchor.
6. Optional: press `Left Alt` to lock the preview target while you move.
7. Optional: press `N` to open the radial menu and adjust route options.
8. Right-click again to place the route, even if you are looking at air.
9. Left-click during preview to cancel only the current route.
10. Press `B` again to return to normal gameplay.

## Radial Options

The radial menu keeps advanced settings in one place:

- **Route:** Auto, horizontal first, vertical first, X first, Z first, or avoid vertical.
- **Pumps:** Off, efficient spacing, or safer shorter spacing.
- **Flow:** Normal or reversed automatic pump direction.
- **Casing:** No casing, manual marks, or full-route casing.
- **Style:** Default fluid pipes or glass straight sections with regular elbows.

## Survival Materials

The HUD shows material counts as:

```txt
required/available
```

If a route needs more items than you have, the missing preview pieces are tinted red and placement is blocked. Copper casing only requires at least one `create:copper_casing` in inventory to unlock casing placement; it is not consumed.

## Wrench Shortcut

With Connector Pipe mode enabled, double right-click a straight connected pipe segment with Create's wrench to toggle it between default and glass style. The shortcut stops at mechanical pumps and leaves elbows as regular fluid pipes when glass cannot represent the shape.

## Requirements

- **Minecraft:** `1.21.1`
- **Loader:** `NeoForge`
- **NeoForge:** `21.1.219` or compatible
- **Create:** `6.0.10-280` or compatible
- **Java:** `21`

## For Modpacks

- Required on both client and server.
- Requires `Create`.
- No runtime dependencies beyond Minecraft, NeoForge, and Create.
- Current addon version: **`0.4.0-beta`**.
- This is a beta: please report route issues with screenshots when possible.

## For Developers

This branch is **NeoForge-only**. Shared logic lives in `common`; loader-specific input, networking, events, and rendering live in `neoforge`.

- Core logic: `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`
- Create block helpers: `common/src/main/java/com/javiluli/createpipeconnector/connector/CreatePipeBlocks.java`
- NeoForge client input: `neoforge/src/main/java/com/javiluli/createpipeconnector/client/input`
- Ghost preview rendering: `neoforge/src/main/java/com/javiluli/createpipeconnector/client/render`
- Radial menu: `neoforge/src/main/java/com/javiluli/createpipeconnector/client/screen/ConnectorOptionsRadialScreen.java`

Build commands:

- Run client: `./gradlew :neoforge:runClient`
- Build jar: `./gradlew :neoforge:build`
- Build release copy: `./gradlew buildAll`

Additional docs:

- `docs/PLAYER_GUIDE.md`
- `docs/MODPACK_GUIDE.md`
- `docs/DEV_GUIDE.md`
- `docs/API.md`
