# CurseForge release checklist

## File

- Upload `out_jars/createpipeconnector-forge-1.20.1-1.0.0.jar`.

## Project metadata

- Name: `Create: Pipe Connector`
- Version: `1.0.0`
- Game version: `Minecraft 1.20.1`
- Mod loader: `Forge`
- Java version: `Java 17`
- Environment: `Client and Server`

## Dependencies

- Required dependency: `Create`
- Recommended Create version: `6.0.8-289`
- Do not mark Ponder, Flywheel, Registrate, or MixinExtras as direct addon dependencies unless CurseForge requires them through Create's own dependency chain.

## Release notes

Use the Forge `1.0.0` notes:

- Connector Pipe mode with ghost preview and one-click route placement
- Radial menu for route style, pumps, flow direction, casing, and pipe style
- Anchor waypoints, preview lock, quick cancel, and air confirmation
- Automatic and manual mechanical pump placement
- Copper casing and glass/default pipe style modes
- Survival material HUD with missing-material blocking and red preview tint
- Create wrench shortcut for default/glass straight pipe segments
- Forge `1.20.1` feature parity with the current NeoForge gameplay set

## Validation

- Run `./gradlew buildAll` or `./gradlew :forge:build`.
- Confirm the generated JAR contains `META-INF/mods.toml`.
- Confirm `mods.toml` declares:
  - `forge` range `[47,)`
  - `minecraft` range `[1.20.1, 1.21)`
  - `create` range `[6.0.8,6.1.0)`
