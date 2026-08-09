<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

**Build Create fluid pipe routes faster: preview the path, adjust it, and place it in one action.**

A utility addon for [Create](https://github.com/Creators-of-Create/Create). Available builds are listed below.

![Version](https://img.shields.io/badge/Version-1.1.0-2E7D32?style=for-the-badge)
<a href="https://www.curseforge.com/minecraft/mc-mods/create-pipe-connector">
<img src="https://img.shields.io/curseforge/dt/1610354?style=for-the-badge&color=242629&labelColor=F16436&logo=curseforge&logoColor=white&label=" alt="CurseForge">
</a>
<a href="https://modrinth.com/mod/create-pipe-connector">
<img src="https://img.shields.io/modrinth/dt/create-pipe-connector?logo=modrinth&label=&suffix=%20&style=for-the-badge&color=242629&labelColor=5CA424&logoColor=1C1C1C" alt="Modrinth">
</a>
![Create](https://img.shields.io/badge/Create-6.0.6+-7B4F1D?style=for-the-badge)
![NeoForge](https://img.shields.io/badge/NeoForge-MC%201.21.1-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Forge](https://img.shields.io/badge/Forge-MC%201.20.1-8B3DFF?style=for-the-badge&logo=forge&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-not%20available-757575?style=for-the-badge)
</div>

---

## What Is It?

**Create: Pipe Connector** adds a dedicated pipe-connection mode for Create fluid pipes.

Instead of placing long pipe lines block by block, you select a start point, aim where the route should go, preview the result, and confirm once. The addon can also add pumps, casing, and glass pipe styling while showing material requirements before placement.

## Main Features

- **Ghost preview:** See the route before spending items.
- **Progressive construction:** Confirmed routes can build one visible piece at a time.
- **Animation settings:** Choose five speeds, instant placement, and independent construction previews.
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

## Placement Animation

Open **Mods > Create: Pipe Connector > Config** to configure confirmed route construction:

- Enable or disable progressive construction. Disabled routes are placed instantly.
- Choose Very slow (`1` piece/s), Slow (`5` pieces/s), Normal (`10` pieces/s), Fast (`15` pieces/s), or Very fast (`20` pieces/s).
- Keep the complete unbuilt route visible without outlines.
- Highlight the next piece immediately before it is placed.

Speed changes apply to routes already being built. Multiple players and multiple confirmed routes can progress independently.

## Survival Materials

The HUD shows material counts as:

```txt
required/available
```

If a route needs more items than you have, missing preview pieces are tinted red and placement is blocked.

Copper casing works like Create: you only need at least one `create:copper_casing` in your inventory to apply casing. It is not consumed per pipe.

## Requirements

| Loader | Minecraft | Java | Mod Version | Status |
| --- | --- | --- | --- | --- |
| NeoForge | `1.21.1` | `21` | `1.1.0` | Supported |
| Forge | `1.20.1` | `17` | `1.1.0` | Supported |
| Fabric | - | - | - | Not available yet |

For the NeoForge build:

- **NeoForge:** `21.1.218` or newer compatible `21.1.x`
- **Create:** `6.0.6` or compatible `6.0.x`

## Modpack Notes

- Install on both client and server.
- Requires Create at runtime.
- No extra runtime dependencies beyond Minecraft, the selected loader, and Create.
- Current NeoForge version: **`1.1.0`**.
- Report route or preview edge cases with screenshots and the relevant client or server log.

## Developer Notes

This branch is a NeoForge-only, single-module project. Gameplay, client, network, and render code live under `src/main`.

- Route orchestration: `src/main/java/com/javiluli/createpipeconnector/feature/connector/PipeConnectorLogic.java`
- Route planning: `src/main/java/com/javiluli/createpipeconnector/feature/connector/planning/ConnectionPlanBuilder.java`
- Pathfinding: `src/main/java/com/javiluli/createpipeconnector/feature/routing/PipePathfinder.java`
- Create interoperability: `src/main/java/com/javiluli/createpipeconnector/core/create/CreatePipeBlocks.java`
- Progressive placement: `src/main/java/com/javiluli/createpipeconnector/feature/placement`
- Ghost preview renderer: `src/main/java/com/javiluli/createpipeconnector/feature/preview/client/PipeGhostRenderer.java`
- NeoForge networking: `src/main/java/com/javiluli/createpipeconnector/platform/network`
- Radial menu: `src/main/java/com/javiluli/createpipeconnector/feature/ui/client/ConnectorOptionsRadialScreen.java`

Build commands:

- Run client: `./gradlew runClient`
- Build jar: `./gradlew build`
- Build release copy: `./gradlew buildRelease`

More docs:

- `CHANGELOG.md`
- `docs/PLAYER_GUIDE.md`
- `docs/MODPACK_GUIDE.md`
- `docs/DEV_GUIDE.md`
- `docs/API.md`
