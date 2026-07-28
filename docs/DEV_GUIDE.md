# Development guide

## Goal

Keep the Forge branch focused on Minecraft `1.20.1` while preserving shared logic in `common`.

## Main folders

- `common/`: shared gameplay and placement logic
- `forge/`: Forge entrypoints, input handling, preview rendering, networking, and server placement
- `neoforge/`: inactive reference kept for parity with the NeoForge branch; it is not included by `settings.gradle`

## Key classes

- `common/.../connector/PipeConnectorLogic.java`
  - pathfinding
  - pipe state creation
  - auto-pump planning
  - pipe display toggling helpers
  - preview pipe generation
- `forge/.../client/input/ClientPipeConnectorInputHandler.java`
  - first selection
  - live preview refresh
  - key-driven anchors and preview locking
- `forge/.../client/render/PipeGhostRenderer.java`
  - blueprint-style preview rendering
  - Forge-adapted version of the NeoForge renderer
- `forge/.../network/CreatePipeConnectorNetwork.java`
  - client-to-server mode, anchor, target, auto-pump, and wrench sync
- `forge/.../connector/ServerPipeConnectorEvents.java`
  - server-side placement and pipe refresh

## Feature flow

1. Player enables Connector Pipe mode with `B` and holds a supported pipe in either hand.
2. Client stores the selection.
3. Crosshair target and optional anchors drive preview generation.
4. The preview world is built from the computed placement plan.
5. Optional auto-pumps are added to the plan before preview and placement.
6. Server validates anchors, inventory, and placement before consuming items.
7. Server placement fills the path and refreshes Create connections.

## Useful commands

- `./gradlew :forge:Client`
- `./gradlew :forge:Server`
- `./gradlew :forge:build`
- `./gradlew buildAll`

## Release checklist

1. Run `./gradlew buildAll`.
2. Upload `out_jars/createpipeconnector-forge-1.20.1-0.4.0-beta.jar` to CurseForge.
3. Mark Minecraft `1.20.1`, Forge, and Java `17`.
4. Add Create as a required dependency.
5. Mark the environment as both client and server.

## Extension points

- Add more connectable blocks in `PipeConnectorLogic`.
- Tune pathfinding without touching the renderer.
- Keep Forge and NeoForge class structure aligned where loader APIs allow it.

## Beta notes

- Keep changes small and easy to validate.
- If you touch preview or pathfinding, verify with a Forge build.
- Avoid broad refactors unless they directly support the connector feature.
