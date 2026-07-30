<div align="center">
<a><img src="https://raw.githubusercontent.com/javiluli/Create-Pipe-connector/master/public/icon.png" width="180" /></a>

# Create: Pipe Connector

**Build Create fluid pipe routes faster: preview the path, adjust it, and place it in one action.**

Forge build for Minecraft `1.20.1`.

![Version](https://img.shields.io/badge/Version-1.0.0-2E7D32?style=for-the-badge)
![Create](https://img.shields.io/badge/Create-6.0.8-7B4F1D?style=for-the-badge)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-Forge-E65100?style=for-the-badge&logo=forge&logoColor=white)
</div>

---

## What Is It?

**Create: Pipe Connector** adds a dedicated pipe-connection mode for Create fluid pipes.

Instead of placing long pipe lines block by block, you select a start point, aim where the route should go, preview the result, and confirm once. The addon can also add pumps, casing, and glass pipe styling while showing material requirements before placement.

## Main Features

- **Ghost preview:** See the route before spending items.
- **Smart routing:** Finds a valid path around obstacles.
- **Anchors:** Add waypoints to force turns or guide the route.
- **Radial menu:** Change route style, pumps, flow direction, casing, and pipe style.
- **Survival checks:** Shows required/available materials and blocks placement if items are missing.
- **Create wrench shortcut:** Toggle straight pipe segments between default and glass style.

## Supported Create Blocks

The connector places and routes:

- `create:fluid_pipe`

Optional features use:

- `create:mechanical_pump`
- `create:glass_fluid_pipe`
- `create:copper_casing`
- `create:encased_fluid_pipe`

`create:smart_fluid_pipe` is intentionally not used as a route pipe because it is a filtering pipe, not a normal connector pipe.

## Default Controls

Only the most important controls are bound by default:

| Action | Default |
| --- | --- |
| Toggle Connector Pipe mode | `B` |
| Start / confirm route | Right-click |
| Open radial menu | `N` |
| Add anchor | `C` |
| Lock preview target / freecam-style planning | `Left Alt` |
| Cancel current route | Left-click |

Extra shortcuts for pump mode, pump direction, casing, glass style, manual pumps, and undo actions are available in Minecraft's Controls menu but are **unassigned by default**. You can use the radial menu instead, or bind them yourself.

## Quick Start

1. Press `B` to enable **Connector Pipe** mode.
2. Hold a `create:fluid_pipe` in either hand.
3. Right-click a reachable block or pipe face to start.
4. Move your crosshair to preview the route.
5. Optional: press `C` to add anchors.
6. Optional: press `Left Alt` to lock the preview target while moving.
7. Optional: press `N` to open the radial menu.
8. Right-click again to place the route.
9. Left-click during preview to cancel only the current route.
10. Press `B` again to leave Connector Pipe mode.

## Radial Menu Options

- **Route:** Auto, horizontal first, vertical first, X first, Z first, or avoid vertical.
- **Pumps:** Off, efficient spacing, or safer shorter spacing.
- **Flow:** Normal or reversed automatic pump direction.
- **Casing:** No casing, manual casing marks, or full-route casing.
- **Style:** Default pipes or glass straight sections.

Tip: `Avoid vertical` is useful for natural terrain, but very long or complex routes can be heavier than the other route modes.

## Survival Materials

The HUD shows material counts as:

```txt
required/available
```

If a route needs more items than you have, missing preview pieces are tinted red and placement is blocked.

Copper casing works like Create: you only need at least one `create:copper_casing` in your inventory to apply casing. It is not consumed per pipe.

## Requirements

- **Minecraft:** `1.20.1`
- **Loader:** `Forge`
- **Forge:** `47.4.20` or compatible `47.x`
- **Create:** `6.0.8-289` or compatible `6.0.x`
- **Java:** `17`

## Modpack Notes

- Install on both client and server.
- Requires Create at runtime.
- No extra runtime dependencies beyond Minecraft, Forge, and Create.
- Current Forge version: **`1.0.0`**.
- This build matches the current NeoForge gameplay feature set, adapted for Forge `1.20.1`.

## Developer Notes

This branch is Forge-only for Minecraft `1.20.1`. Shared connector logic lives in `common`; Forge input, networking, events, HUD, and rendering live in `forge`.

- Core routing: `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`
- Preview state building: `common/src/main/java/com/javiluli/createpipeconnector/connector/PipePreviewBuilder.java`
- Forge client input: `forge/src/main/java/com/javiluli/createpipeconnector/client/input`
- Ghost preview renderer: `forge/src/main/java/com/javiluli/createpipeconnector/client/render/PipeGhostRenderer.java`
- Radial menu: `forge/src/main/java/com/javiluli/createpipeconnector/client/screen/ConnectorOptionsRadialScreen.java`

Build commands:

- Run client: `./gradlew :forge:Client`
- Build jar: `./gradlew :forge:build`
- Build release copy: `./gradlew buildAll`
