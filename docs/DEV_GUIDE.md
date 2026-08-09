# Development guide

## Project layout

The NeoForge edition is a single-module project:

- `src/main/java`: gameplay, client, network, render, and NeoForge bootstrap code
- `src/main/resources`: mod metadata, translations, and the in-game icon
- `docs`: player, modpack, API, and development notes

The project is organized by feature. Stable adapters remain in `core`; connector flow, progressive placement, preview rendering, materials, anchors, pumps, casing, styles, and UI each have focused packages.

## Main areas

- `feature/connector/PipeConnectorLogic.java`: public connector facade
- `feature/connector/planning/ConnectionPlanBuilder.java`: route and waypoint orchestration
- `feature/connector/session/ConnectorSessionStore.java`: transient per-player state
- `feature/routing/PipePathfinder.java`: obstacle-aware route search
- `feature/routing/PipeRouteGeometry.java`: directions, corners, and pump orientation
- `core/create/CreatePipeBlocks.java`: Create registry and block-state interoperability
- `feature/material/PipeInventory.java`: survival counting, reservation, consumption, and refunds
- `feature/connector`: client controls, server interaction flow, and connector state
- `feature/material`: client material evaluation and survival warnings
- `feature/placement`: progressive construction settings, synchronization, previews, and server queues
- `feature/preview`: cached ghost geometry, fluid classification, outlines, and render layers
- `feature/anchor`: anchor overlay rendering
- `feature/ui`: radial options and connector HUD
- `platform/network`: NeoForge payload registration
- `feature/*/network`: compact payload records and server handlers owned by each feature

## Route flow

1. The client enables Connector Pipe mode and synchronizes it with the server.
2. A reachable first target creates the selection and immediate one-piece preview.
3. Crosshair movement, route priority, and anchors rebuild the connection plan.
4. Pump, casing, and glass rules transform valid route positions.
5. The preview builder applies final connection states and material warnings.
6. Confirmation sends the target to the server for reach, route, and inventory validation.
7. The server reserves materials and either places instantly or enqueues progressive construction.
8. Completed or blocked routes refresh Create connections; blocked pending materials are refunded.

## Preview architecture

`PipeGhostRenderer` uses Create and Catnip model buffering through `SchematicLevel`, `BakedModelBufferer`, and `SuperByteBuffer`.

- Editable routes are cached in spatial sections for frustum culling.
- Confirmed progressive routes are cached by piece so placed sections disappear without rebuilding every frame.
- Body geometry and outline geometry are cached separately.
- Fluid groups choose the correct world render stage across water and other fluid surfaces.
- Anchor overlays redraw nearby pipe outlines without changing established preview colors.
- TODO: evaluate an optional contrast improvement for previews viewed through water without changing the established palette or reducing shader compatibility.

## Commands

- Run client: `./gradlew runClient`
- Run dedicated server: `./gradlew runServer`
- Build jar: `./gradlew build`
- Copy release jar: `./gradlew buildRelease`

## Maintenance rules

- Keep loader APIs out of connector algorithms when possible.
- Keep Create block-state compatibility in `CreatePipeBlocks`.
- Keep material rules in `PipeInventory` and preview availability helpers.
- Add concise Spanish ASCII Javadocs to new public classes and methods.
- Validate client, dedicated server, survival materials, waterlogging, and long previews after relevant changes.

## Commit and changelog style

Follow Create's concise changelog style so Git history and release notes remain easy to match:

- Write commit subjects in the imperative form: `Add`, `Fix`, `Optimize`, `Update`, `Remove`, or `Reorganize`.
- Do not add Conventional Commit prefixes or scopes.
- Keep one coherent behavior, fix, or maintenance change per commit when practical.
- Reuse the commit subject, or a very close version of it, as the related changelog bullet.
- Group release bullets under `Additions`, `Art Changes`, `Gameplay Changes`, `Optimizations`, `Bug Fixes`, or `API Changes`.
- Omit empty changelog categories.

Examples:

- `Add progressive route construction`
- `Optimize cached preview geometry`
- `Fix previews across fluid surfaces`
