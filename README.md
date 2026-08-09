<div align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/create-pipe-connector">
<img src="https://raw.githubusercontent.com/javiluli/Create-Pipe-connector/master/public/icon.png" width="180" alt="Create: Pipe Connector icon" />
</a>

# Create: Pipe Connector

**Plan and place complete Create fluid pipe routes in seconds.**

A building utility addon for [Create](https://github.com/Creators-of-Create/Create), available for Forge and NeoForge.

![Version](https://img.shields.io/badge/Version-1.1.0-2E7D32?style=for-the-badge)
<a href="https://www.curseforge.com/minecraft/mc-mods/create-pipe-connector">
<img src="https://img.shields.io/curseforge/dt/1610354?style=for-the-badge&color=242629&labelColor=F16436&logo=curseforge&logoColor=white&label=" alt="CurseForge">
</a>
<a href="https://modrinth.com/mod/create-pipe-connector">
<img src="https://img.shields.io/modrinth/dt/create-pipe-connector?logo=modrinth&label=&suffix=%20&style=for-the-badge&color=242629&labelColor=5CA424&logoColor=1C1C1C" alt="Modrinth">
</a>
![Create](https://img.shields.io/badge/Create-6.0.6+-7B4F1D?style=for-the-badge)
![NeoForge](https://img.shields.io/badge/NeoForge--21.1.219-MC%201.21.1-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Forge](https://img.shields.io/badge/Forge--47.4.20-MC%201.20.1-8B3DFF?style=for-the-badge&logo=forge&logoColor=white)

</div>

---

## What Does It Do?

**Create: Pipe Connector** replaces block-by-block pipe building with a simple workflow: select a start, preview the route, adjust it, and confirm. The addon routes Create fluid pipes around obstacles and can include pumps, casing, and glass sections.

## Main Features

- **Live preview and smart routing:** See a valid path around obstacles before placing anything.
- **Anchors:** Add waypoints to guide turns and complex routes.
- **Pumps and styles:** Add automatic pumps, glass sections, or copper casing.
- **Survival checks:** See required materials and prevent incomplete placement.
- **Construction animation:** Place routes progressively at five speeds or instantly.
- **Radial menu:** Change route, pump, flow, casing, and style options in-game.

## Quick Start

1. Hold a `create:fluid_pipe` and press `B` to enable **Connector Pipe mode**.
2. Right-click a reachable block or pipe face to start the route.
3. Aim at the destination and review the preview.
4. Use `C` for anchors or `N` for route options when needed.
5. Right-click to confirm, or left-click to cancel the current route.
6. Press `B` again to return to normal gameplay.

## Main Controls

| Action                                | Default control |
| ------------------------------------- | --------------- |
| Enable or disable Connector Pipe mode | `B`             |
| Start or confirm a route              | Right-click     |
| Cancel the current route              | Left-click      |
| Open connector options                | `N`             |
| Add an anchor                         | `C`             |
| Lock or unlock the preview target     | `Left Alt`      |

Other shortcuts are available in Minecraft's Controls menu but are unassigned by default.

## Connector Options

- **Route:** Automatic, horizontal first, vertical first, X first, Z first, or avoid vertical.
- **Pumps:** Disabled, efficient spacing, or safer shorter spacing.
- **Flow:** Normal or reversed automatic pump direction.
- **Casing:** None, manually marked positions, or the complete route.
- **Style:** Default pipes or glass straight sections.

Animation speed, instant placement, and construction preview settings are available under **Mods → Create: Pipe Connector → Config**.

## Compatible Versions

| Edition  | Minecraft | Loader              | Java | Create           | Mod     |
| -------- | --------- | ------------------- | ---- | ---------------- | ------- |
| NeoForge | `1.21.1`  | NeoForge `21.1.219` | `21` | `6.0.6`–`6.0.10` | `1.1.0` |
| Forge    | `1.20.1`  | Forge `47.4.20`     | `17` | `6.0.6`–`6.0.10` | `1.1.0` |

Download the file matching your loader. Fabric is not currently supported.

## Modpacks and Shaders

- Install the addon on **both the client and server**.
- Create is the only required dependency.
- The preview supports common renderer mods and shaders, although transparency can vary between shader packs.
- For visual issues, test without shaders and report the renderer, shader, screenshot, and client log.
- Very long **Avoid vertical** routes are more demanding; anchors or a simpler route mode can improve performance.

This project is still improving. Bug reports and gameplay suggestions are welcome on the project pages.
