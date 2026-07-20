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
  - key-driven anchors and preview locking
- `neoforge/.../client/render/PipeGhostRenderer.java`
  - blueprint-style preview rendering
- `neoforge/.../network/CreatePipeConnectorNetwork.java`
  - client-to-server anchor sync
- `neoforge/.../connector/ServerPipeConnectorEvents.java`
  - server-side placement and pipe refresh

## Feature flow

1. Player enters pipe mode with a pipe in the off-hand and an empty main hand.
2. Client stores the selection.
3. Crosshair target and optional anchors drive preview generation.
4. The preview world is built from the computed placement plan.
5. Server validates anchors, inventory, and placement before consuming pipes.
6. Server placement fills the path and refreshes Create connections.

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
