# Create: Pipe Connector Forge

Forge branch for **Create: Pipe Connector**.

This branch keeps the modular project layout and targets:

- **Minecraft:** `1.20.1`
- **Forge:** `47.4.20`
- **Create:** `6.0.8`
- **Java:** `17`

## Branch Purpose

This branch is isolated from the NeoForge `master` branch.

The shared logic remains in `common`, while Forge-specific loader code lives in `forge`.

## Current State

This is the clean Forge baseline.

The gameplay port is developed from `dev/forge`.

## Build

- Run client: `./gradlew :forge:Client`
- Build jar: `./gradlew :forge:build`
- Build and copy release jar: `./gradlew buildAll`
