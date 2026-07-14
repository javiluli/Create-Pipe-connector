<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

Beta addon for [Create](https://github.com/Creators-of-Create/Create) on Minecraft `1.21.1`.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-NeoForge-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Create](https://img.shields.io/badge/Create-6.0.10-7B4F1D?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-0.1.0Beta-455A64?style=for-the-badge)

</div>

## What it does

Create: Pipe Connector removes the tedious part of routing Create fluid pipes. Select two pipes and the mod fills the shortest valid route between them, so you can build faster and with less manual placement.

### Player features

- Connects two Create pipes automatically
- Finds the shortest valid path around obstacles
- Shows a ghost preview before placing anything
- Refreshes Create pipe connections after placement

### Supported blocks

- `create:fluid_pipe`
- `create:smart_fluid_pipe`

## How to use it

1. Sneak and right-click a Create pipe with an empty hand.
2. Sneak and right-click a second pipe of the same type.
3. Check the ghost preview of the final route.
4. Confirm the placement to build the line automatically.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.219` or compatible
- Create `6.0.10-280` or compatible
- Java `21`

## For modpacks

- `Create` is required at runtime.
- `Ponder` and `Flywheel` are only used to render the preview during development and modpack validation.
- This addon is currently marked as beta, so the feature set may still evolve.

## Documentation

- `docs/PLAYER_GUIDE.md` - player-facing usage guide
- `docs/MODPACK_GUIDE.md` - packmaker notes and requirements
- `docs/DEV_GUIDE.md` - project layout and implementation flow
- `docs/API.md` - integration notes for other mods

## Build

- `./gradlew :neoforge:runClient` starts the NeoForge dev client
- `./gradlew :neoforge:build` builds the NeoForge artifact

## For devs and modders

- Shared logic lives in `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`.
- NeoForge entrypoints, input handling, preview rendering, and server placement live under `neoforge/src/main/java/com/javiluli/createpipeconnector/`.
- The ghost preview renderer is `neoforge/src/main/java/com/javiluli/createpipeconnector/client/render/PipeGhostRenderer.java`.
- If you extend the connector logic, add new block support in `PipeConnectorLogic` first and keep loader code thin.

## Status

- The old beacon behavior is no longer active.
- The project is now focused on the Create pipe connector feature.
- The repository keeps a clean NeoForge-first structure for the current release.
