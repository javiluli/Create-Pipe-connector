<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

**Connect Create fluid pipes faster with a live preview and one-click placement.**

Forge build for Minecraft `1.20.1`.

![Create](https://img.shields.io/badge/Create-6.0.8-7B4F1D?style=for-the-badge)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-Forge-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Version](https://img.shields.io/badge/Version-0.4.0--beta-455A64?style=for-the-badge)
</div>

---

## What Is It?

**Create: Pipe Connector** adds a Connector Pipe mode for Create fluid pipes.

Enable the mode, choose a start point, preview the route, adjust it with anchors, and confirm once to place the whole pipe line. It is built to remove repetitive manual pipe placement while keeping survival item checks and a clear ghost preview.

## Main Features

- **Connector Pipe mode:** Press `B` to enable or disable the addon workflow.
- **Live ghost preview:** See the planned pipe route before placement.
- **Air confirmation:** Start on a block, then confirm the route even while looking at air.
- **Anchor waypoints:** Press `C` to add an anchor and `V` to undo the last one.
- **Preview lock:** Press `Left Alt` to freeze the current preview target while moving.
- **Auto mechanical pumps:** Press `P` to preview and place pumps along the route automatically.
- **Pipe style toggle:** In Connector Pipe mode, double right-click a pipe segment with Create's wrench to switch straight pipes between default and glass style.
- **Survival checks:** Shows `required/available` pipe counts and blocks placement if you do not have enough.
- **Configurable controls:** Rebind connector keys from Minecraft's Controls menu.

## Supported Pipes

- `create:fluid_pipe`
- `create:smart_fluid_pipe`

## How To Use

1. Press `B` to enable **Connector Pipe** mode.
2. Hold a supported Create pipe in either hand.
3. Right-click a reachable block or pipe face to start the route.
4. Move your crosshair to guide the preview.
5. Optional: press `C` to add an anchor.
6. Optional: press `V` to remove the last anchor.
7. Optional: press `Left Alt` to lock or unlock the preview target.
8. Optional: press `P` to enable automatic mechanical pumps for the route.
9. Right-click again to place the route, even if you are looking at air.
10. Left-click during preview to cancel only the current route.
11. Press `B` again to return to normal gameplay.

In survival, the action bar shows:

```txt
required/available
```

If you do not have enough pipes or pumps, the required number turns red and placement is cancelled.

## Requirements

- **Minecraft:** `1.20.1`
- **Loader:** `Forge`
- **Forge:** `47.4.20` or compatible `47.x`
- **Create:** `6.0.8-289` or compatible `6.0.x`
- **Java:** `17`

## For Modpacks

- Required on both client and server.
- Requires `Create`.
- No extra runtime mods are required beyond Minecraft, Forge, and Create.
- Current addon version: **`0.4.0-beta`**.

## Beta Notice

This Forge build is catching up with the NeoForge feature set in stages. Please report route issues with screenshots when possible.

## For Developers

This branch is **Forge-only** for Minecraft `1.20.1`. Shared placement logic lives in `common`, while Forge-specific input, networking, events, and rendering live in `forge`.

- Core logic: `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`
- Forge client input: `forge/src/main/java/com/javiluli/createpipeconnector/client/input`
- Forge networking: `forge/src/main/java/com/javiluli/createpipeconnector/network`
- Ghost preview renderer: `forge/src/main/java/com/javiluli/createpipeconnector/client/render/PipeGhostRenderer.java`
- Controls HUD: `forge/src/main/java/com/javiluli/createpipeconnector/client/render/hud/PipeConnectorControlsHud.java`

Build commands:

- Run the Forge dev client: `./gradlew :forge:Client`
- Build the Forge artifact: `./gradlew :forge:build`
- Build and copy the release jar: `./gradlew buildAll`
