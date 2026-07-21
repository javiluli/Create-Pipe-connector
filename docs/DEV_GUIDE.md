# Development guide

## Goal

Keep the project focused on the NeoForge runtime while preserving shared logic in `common`.

## Main folders

- `common/`: shared gameplay and placement logic
- `neoforge/`: NeoForge entrypoints, input handling, preview rendering, and server placement

## Key classes

- `common/.../connector/PipeConnectorLogic.java`
  - pathfinding
  - pipe state creation
  - preview world generation
- `neoforge/.../client/input/ClientPipeConnectorInputHandler.java`
  - first selection
  - live preview refresh
  - key-driven anchors and preview locking
- `neoforge/.../client/render/PipeGhostRenderer.java`
  - blueprint-style preview rendering
- `neoforge/.../client/render/hud/PipeConnectorControlsHud.java`
  - active connector controls above the hotbar
- `neoforge/.../network/CreatePipeConnectorNetwork.java`
  - client-to-server anchor sync
- `neoforge/.../connector/ServerPipeConnectorEvents.java`
  - server-side placement and pipe refresh

## Feature flow

1. Player toggles Connector Pipe mode with the configurable `B` key.
2. Client syncs the mode state to the server.
3. Player starts a route by targeting a reachable block with a pipe in either hand.
4. Client sends the selected target to the server and stores the local selection.
5. Crosshair or air target plus optional anchors drive preview generation.
6. The preview world is built from the computed placement plan.
7. Right-click confirms the current preview; left-click cancels the current route.
8. Server validates mode, anchors, inventory, and placement before consuming pipes.
9. Server placement fills the path and refreshes Create connections.

## Useful commands

- `./gradlew :neoforge:runClient`
- `./gradlew :neoforge:build`

## Extension points

- Add more connectable blocks in `PipeConnectorLogic`.
- Tune pathfinding without touching the renderer.
- Split visual behavior from placement behavior if the addon grows.

## Beta notes

- Keep changes small and easy to validate.
- If you touch preview or pathfinding, verify with a NeoForge build.
- Avoid broad refactors unless they directly support the connector feature.
