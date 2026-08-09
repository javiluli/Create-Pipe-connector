# CurseForge release checklist

## File

- Upload `out_jars/createpipeconnector-forge-1.20.1-1.1.0.jar`.

## Project metadata

- Name: `Create: Pipe Connector`
- Version: `1.1.0`
- Game version: `Minecraft 1.20.1`
- Mod loader: `Forge`
- Java version: `Java 17`
- Environment: `Client and Server`

## Dependencies

- Required dependency: `Create`
- Recommended Create version: `6.0.6-168`
- Do not mark Ponder, Flywheel, or Registrate as direct addon dependencies unless CurseForge requires them through Create's own dependency chain.

## Release notes

Use the Forge `1.1.0` notes:

- Expanded Create compatibility to `6.0.6+`
- Improved translucent previews with common rendering mods and shaders
- Preserved pipe outlines and transparency through anchor overlays
- Added water-aware preview rendering and consistent route waterlogging
- Fixed anchor gaps and glass-style material accounting
- Reorganized routing and rendering helpers for lower allocation overhead

## Validation

- Run `./gradlew buildRelease` or `./gradlew build`.
- Confirm the generated JAR contains `META-INF/mods.toml`.
- Confirm `mods.toml` declares:
  - `forge` range `[47,)`
  - `minecraft` range `[1.20.1, 1.21)`
  - `create` range `[6.0.6,6.1.0)`
