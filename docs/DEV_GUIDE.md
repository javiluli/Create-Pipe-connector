# Development guide

## Goal

Keep the Forge branch focused on Minecraft `1.20.1` as a single Gradle module. All production code and resources use the standard `src/main` layout.

## Main folders

- `src/main/java/.../core/`: constants, shared immutable models, and small Create/player adapters
- `src/main/java/.../feature/`: connector mechanics grouped by gameplay responsibility
- `src/main/java/.../feature/<name>/client/`: client-only input, state, rendering, and UI
- `src/main/java/.../feature/<name>/network/`: payloads and feature-owned network handlers
- `src/main/java/.../feature/<name>/server/`: server events and authoritative world changes
- `src/main/java/.../bootstrap/`: registration and application composition
- `src/main/java/.../platform/`: Forge services that are independent from gameplay features
- `src/main/resources/`: Forge metadata, translations, icon, and pack metadata

## Key classes

- `src/main/java/com/javiluli/createpipeconnector/feature/connector/PipeConnectorLogic.java`: stable facade for connector mechanics
- `src/main/java/com/javiluli/createpipeconnector/feature/connector/planning/ConnectionPlanBuilder.java`: route and waypoint composition
- `src/main/java/com/javiluli/createpipeconnector/core/model/ConnectionPlan.java`: immutable placement plan
- `src/main/java/com/javiluli/createpipeconnector/feature/connector/session/ConnectorSessionStore.java`: per-player route state
- `src/main/java/com/javiluli/createpipeconnector/feature/preview/PipePreviewBuilder.java`: preview data construction
- `src/main/java/com/javiluli/createpipeconnector/feature/preview/client/PipeGhostRenderer.java`: in-world ghost rendering
- `src/main/java/com/javiluli/createpipeconnector/feature/ui/client/ConnectorOptionsRadialScreen.java`: radial options menu
- `src/main/java/com/javiluli/createpipeconnector/bootstrap/ForgePayloadRegistry.java`: payload registration
- `src/main/java/com/javiluli/createpipeconnector/platform/network/CreatePipeConnectorNetwork.java`: feature-independent transport

## Architecture rules

- Put gameplay behavior beside the feature that owns it.
- Keep client-only classes inside a `client` package.
- Keep server authority and validation inside `server` handlers.
- Keep payloads and their handlers inside the owning feature.
- Dependencies flow from `bootstrap` to `feature` and `platform`, and from `feature` to `core`.
- `platform` must not import feature code.
- Shared feature services must not call the `PipeConnectorLogic` facade.
- Avoid bidirectional dependencies between features.
- Prefer composition; add interfaces or inheritance only for genuinely interchangeable behavior.
- Do not create generic `util` packages, empty `package-info.java` files, or wrappers without their own responsibility.
- Split large classes only when the extracted responsibility has independent state, inputs, or reuse.

## Create reference

The project follows the same feature-first principle used by Create, adapted to this addon's smaller size:

| Create | This project | Responsibility |
| --- | --- | --- |
| `content` | `feature` | Player-facing mechanics |
| `foundation` | `core` and `platform` | Shared models, adapters, and services |
| `infrastructure` | `bootstrap` and the root mod class | Registration and composition |

New mechanics should start with only the folders they need:

```text
src/main/java/.../feature/<mechanic>/
src/main/java/.../feature/<mechanic>/client/
src/main/java/.../feature/<mechanic>/network/
src/main/java/.../feature/<mechanic>/server/
```

References:

- Forge package organization: `https://docs.minecraftforge.net/en/1.20.x/gettingstarted/structuring/`
- Create source layout: `https://github.com/Creators-of-Create/Create/tree/mc1.20.1/dev/src/main/java/com/simibubi/create`

## Useful commands

- `./gradlew runClient`
- `./gradlew runServer`
- `./gradlew build`
- `./gradlew buildRelease`

## Release checklist

1. Run `./gradlew buildRelease` or `./gradlew build`.
2. Upload `out_jars/createpipeconnector-forge-1.20.1-1.1.0.jar` to CurseForge.
3. Mark Minecraft `1.20.1`, Forge, and Java `17`.
4. Add Create as a required dependency.
5. Mark the environment as both client and server.
