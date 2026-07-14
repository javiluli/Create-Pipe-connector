# Development guide

## Goal

Keep the multiloader structure intact while the NeoForge runtime focuses on the Create pipe connector feature.

## Main folders

- `common/`: shared logic between loaders
- `neoforge/`: NeoForge entrypoints, input handling, preview rendering, and server placement
- `fabric/`: kept for structure and future support

## Key classes

- `common/.../connector/PipeConnectorLogic.java`
  - pathfinding
  - pipe state creation
  - preview world generation
- `neoforge/.../client/input/ClientPipeConnectorInputHandler.java`
  - first selection
  - live preview refresh
- `neoforge/.../client/render/PipeGhostRenderer.java`
  - blueprint-style preview rendering
- `neoforge/.../connector/ServerPipeConnectorEvents.java`
  - server-side placement and pipe refresh

## Feature flow

1. Player selects the first pipe.
2. Client stores the selection.
3. The second pipe under the crosshair drives preview generation.
4. The preview world is built from the computed path.
5. Server placement fills the path and refreshes Create connections.

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
