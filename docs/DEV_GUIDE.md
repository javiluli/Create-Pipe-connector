# Development guide

## Goal

Keep the Forge branch focused on Minecraft `1.20.1` while preserving shared connector logic in `common`.

## Main folders

- `common/`: shared routing, preview-state, inventory, pump, casing, and session logic
- `forge/`: Forge entrypoints, keybinds, input handling, networking, preview rendering, HUD, radial menu, and server placement

## Key classes

- `common/.../connector/PipeConnectorLogic.java`: route planning and placement plan composition
- `common/.../connector/PipePreviewBuilder.java`: preview states for pipes, pumps, casing, glass style, and missing materials
- `forge/.../client/input/ClientPipeConnectorInputHandler.java`: client workflow and preview updates
- `forge/.../client/screen/ConnectorOptionsRadialScreen.java`: radial option menu
- `forge/.../client/render/PipeGhostRenderer.java`: in-world ghost preview
- `forge/.../network/CreatePipeConnectorNetwork.java`: Forge `SimpleChannel` payload registration
- `forge/.../connector/ServerPipeConnectorEvents.java`: server validation, placement, and wrench action

## Useful commands

- `./gradlew runClient`
- `./gradlew runServer`
- `./gradlew :forge:build`
- `./gradlew buildRelease`

## Release checklist

1. Run `./gradlew buildRelease` or `./gradlew :forge:build`.
2. Upload `out_jars/createpipeconnector-forge-1.20.1-1.1.0.jar` to CurseForge.
3. Mark Minecraft `1.20.1`, Forge, and Java `17`.
4. Add Create as a required dependency.
5. Mark the environment as both client and server.
