# Development guide

## Goal

Keep the project focused on the NeoForge runtime while preserving shared logic in `common`.

## Main folders

- `common/`: shared gameplay and placement logic
- `neoforge/`: NeoForge entrypoints, input handling, preview rendering, and server placement

## Key classes

- `common/.../connector/PipeConnectorLogic.java`
  - public connector facade
  - pathfinding and connection plan creation
  - preview world generation
- `common/.../connector/CreatePipeBlocks.java`
  - Create block/item lookup
  - pipe and pump block state helpers
- `common/.../connector/AutoPumpPlanner.java`
  - automatic Mechanical Pump slot generation
- `common/.../connector/PipePreviewBuilder.java`
  - ghost preview block states and preview world proxy
- `common/.../connector/PipeDisplayToggler.java`
  - pipe display segment toggling between default and glass
- `common/.../connector/PipeInventory.java`
  - survival inventory counting and item consumption
- `common/.../connector/PipeConnectorSessions.java`
  - server-side connector mode, selections, anchors, and auto-pump state
- `neoforge/.../client/input/ClientPipeConnectorInputHandler.java`
  - first selection
  - live preview refresh
  - key-driven anchors and preview locking
- `neoforge/.../client/render/PipeGhostRenderer.java`
  - blueprint-style preview rendering
- `neoforge/.../client/render/hud/PipeConnectorControlsHud.java`
  - active connector controls above the hotbar
- `neoforge/.../network/CreatePipeConnectorNetwork.java`
  - client-to-server mode, anchor, target, auto-pump, and wrench shortcut sync
- `neoforge/.../connector/ServerPipeConnectorEvents.java`
  - server-side placement, wrench double-click handling, and pipe refresh

## Feature flow

1. Player toggles Connector Pipe mode with the configurable `B` key.
2. Client syncs the mode state to the server.
3. Player starts a route by targeting a reachable block with a pipe in either hand.
4. Client sends the selected target to the server and stores the local selection.
5. Crosshair or air target plus optional anchors drive preview generation.
6. If auto-pumps are enabled, the connection plan marks straight pipe slots for Mechanical Pumps.
7. The preview world is built from the computed placement plan.
8. Right-click confirms the current preview; left-click cancels the current route.
9. Server validates mode, anchors, inventory, pumps, and placement before consuming items.
10. Server placement fills the path and refreshes Create connections.
11. With a wrench in Connector Pipe mode, client sends a pipe display payload and the server requires two clicks on the same pipe before converting the connected segment.

## Useful commands

- `./gradlew :neoforge:runClient`
- `./gradlew :neoforge:build`

## Extension points

- Add more connectable blocks in `PipeConnectorLogic`.
- Keep Create-specific block IDs and state helpers in `CreatePipeBlocks`.
- Keep inventory rules in `PipeInventory`.
- Keep display-style conversion rules in `PipeDisplayToggler`.
- Tune pathfinding without touching the renderer.
- Split visual behavior from placement behavior if the addon grows.

## Beta notes

- Keep changes small and easy to validate.
- If you touch preview or pathfinding, verify with a NeoForge build.
- Avoid broad refactors unless they directly support the connector feature.
