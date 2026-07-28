# CurseForge release checklist

## File

- Upload `out_jars/createpipeconnector-forge-1.20.1-0.4.0-beta.jar`.

## Project metadata

- Name: `Create: Pipe Connector`
- Version: `0.4.0-beta`
- Game version: `Minecraft 1.20.1`
- Mod loader: `Forge`
- Java version: `Java 17`
- Environment: `Client and Server`

## Dependencies

- Required dependency: `Create`
- Recommended Create version: `6.0.8-289`
- Do not mark Ponder, Flywheel, Registrate, or MixinExtras as direct addon dependencies unless CurseForge requires them through Create's own dependency chain.

## Release notes

Use the existing `0.4.0-beta` notes:

- Live ghost preview while routing pipes
- Anchor waypoints with `C` and undo with `V`
- Preview lock with `Left Alt`
- Automatic mechanical pump placement with `P`
- Mechanical pump preview and inventory validation
- Wrench double-click action to toggle straight pipe segments between default and glass style
- Configurable key binds
- Survival pipe/pump counter and inventory validation
- Forge `1.20.1` port

## Validation

- Run `./gradlew buildAll`.
- Confirm the generated JAR contains `META-INF/mods.toml`.
- Confirm `mods.toml` declares:
  - `forge` range `[47,)`
  - `minecraft` range `[1.20.1, 1.21)`
  - `create` range `[6.0.8,6.1.0)`
