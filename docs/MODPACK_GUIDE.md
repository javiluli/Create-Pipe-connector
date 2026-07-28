# Modpack guide

## Requirements

- Minecraft `1.20.1`
- Forge `47.4.20` or compatible `47.x`
- Create `6.0.8-289` or compatible `6.0.x`
- Java `17`

## Runtime dependencies

- `Create` is the only gameplay mod required by this addon.
- The release JAR does not bundle extra mods.
- Forge, Create, and Minecraft provide the runtime environment.

## What the addon does

- Connects Create fluid pipes automatically.
- Finds a valid route around obstacles.
- Shows a ghost preview before placement.
- Adds Connector Pipe mode with a configurable `B` key.
- Allows route confirmation while looking at air after a route has started.
- Supports anchor waypoints and preview locking.
- Checks survival inventory before placing pipes.

## Pack integration

- Include Create in the pack.
- Install this addon on both client and server.
- The addon declares Create as a required Forge dependency.
- Test long routes, anchor-heavy routes, and obstacle-heavy routes before releasing a pack update.

## Recommended versions

- Minecraft `1.20.1`
- Forge `47.4.20` or newer `47.x`
- Create `6.0.8-289`
- Create: Pipe Connector `0.3.0-beta`

## For modders

- The path and placement logic lives in `common/.../connector/PipeConnectorLogic.java`.
- The Forge preview lives in `forge/.../client/render/PipeGhostRenderer.java`.
- Reuse the existing selection and pathfinding flow instead of duplicating it.