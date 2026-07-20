# Modpack guide

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.219` or compatible
- Create `6.0.10-280` or compatible
- Java `21`

## Runtime dependencies

- `Create` is the only gameplay mod required at runtime.
- No extra runtime mods are required for the connector feature.

## What the addon does

- Connects two Create fluid pipes automatically
- Finds the shortest valid route around obstacles
- Shows a ghost preview before placement
- Supports anchor waypoints and configurable key binds
- Checks survival inventory before placing pipes

## Pack integration

- Include Create in the pack.
- This addon already declares Create as a required NeoForge dependency.
- Test the mod with Create pipe variants and long routes before releasing the pack.

## Not yet provided

- No public API
- No config screen beyond vanilla key binding options
- No compatibility promise with every render or optimization mod

## Recommended version

- Use `Create 6.0.10-280` for the exact version this addon is being validated against.

## For modders

- The path and placement logic lives in `common/.../connector/PipeConnectorLogic.java`.
- The ghost preview lives in `neoforge/.../client/render/PipeGhostRenderer.java`.
- Reuse the existing selection and pathfinding flow instead of duplicating it.
