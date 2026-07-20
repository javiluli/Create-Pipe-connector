# Modpack guide

## Requirements

- Minecraft `1.20.1`
- Forge `47.x`
- Create `6.0.8` or compatible `6.0.x`
- Java `17`

## Runtime dependencies

- `Create` is the only gameplay mod required by this addon.
- The release JAR does not bundle extra mods.
- Forge, Create, and Minecraft provide the runtime environment.

## What the addon does

- Connects Create fluid pipes automatically.
- Finds the shortest valid route around obstacles.
- Shows a ghost preview before placement.
- Supports anchor waypoints and configurable key binds.
- Checks survival inventory before placing pipes.

## Pack integration

- Include Create in the pack.
- Install this addon on both client and server.
- The addon declares Create as a required Forge dependency.
- Test long routes, anchor-heavy routes, and obstacle-heavy routes before releasing a pack update.

## Not yet provided

- No public config screen beyond vanilla key binding options.
- No public API guarantee yet.
- No compatibility promise with every render, shader, or optimization mod.

## Recommended versions

- Minecraft `1.20.1`
- Forge `47.4.10` or newer `47.x`
- Create `6.0.8-289`
- Create: Pipe Connector `0.2.0-beta`

## For modders

- The path and placement logic lives in `common/.../connector/PipeConnectorLogic.java`.
- The Forge preview lives in `forge/.../client/render/PipeGhostRenderer.java`.
- Reuse the existing selection and pathfinding flow instead of duplicating it.
