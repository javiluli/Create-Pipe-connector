# Create: Pipe Connector - Forge Changelog

All notable changes to the Forge edition are documented manually in this file.

Versions are listed from newest to oldest. Changes that have not been released yet are collected under **Unreleased**. New release entries follow Create-inspired sections so gameplay, rendering, optimization, fixes, and internal additions remain easy to identify.

## Unreleased

## 1.2.0 - 2026-08-15

### Gameplay Changes

- Added a unified manual tool for anchors, mechanical pumps, and copper casing, selected from the radial menu and used with the same contextual control.
- Added chronological undo for manual route edits, allowing the latest anchor, pump, or casing mark to be removed with one control regardless of its type.
- Added an optional setting that lets manual pumps and casing marks guide the route as anchors without changing their placement behavior.
- Added direct access to pipe and pump items, plus copper casing availability, inside carried vanilla shulker boxes.
- Added a client option to disable shulker material access; loose inventory items always remain the first source consumed.
- Added cascading construction with configurable `50`-`1000` ms delay, optional zoom, complete-route preview, and next-piece preview settings.
- Added contextual route interactions: a pipe is required only to start, while the active preview remains available after switching held items.
- Preserved normal use of chests, shulker boxes, Create machines, AE2 terminals, and other inventory blocks while routing.
- Added air confirmation, selected-pipe confirmation over normal blocks, and `Shift` + right-click as an explicit confirmation override.
- Kept active routes paused instead of cleared while inventory and configuration screens are open.
- Added a short error sound when placement is rejected because required materials are missing.

### Optimizations

- Counted direct inventory and shulker materials in shared passes while retaining inventory-first consumption.
- Cached repeated route plans and reused preview geometry and render paths across the normal, construction, and zoom previews.
- Limited the controls HUD to actions that are relevant to the current route and split long control rows before reducing text size.
- Scaled the radial menu automatically on displays with limited vertical space.
- Simplified payloads, route-state helpers, render parameters, enum methods, and repeated block-state lookups.
- Removed unused constants, overloads, imports, payload classes, and other dead code found during the project-wide cleanup.

### Bug Fixes

- Fixed route previews being cleared when the player stopped holding the selected pipe.
- Fixed inventories, modded terminals, machines, and shulker placement being blocked by route confirmation.
- Fixed selected-pipe confirmation being lost after restoring normal block interaction priority.
- Fixed anchor and freecam combinations recalculating routes from stale or inconsistent targets.
- Fixed undo stopping at an anchor when manual pumps and casing marks were also acting as route anchors.
- Fixed manual pump direction not following the shared reverse-pump setting.
- Fixed missing-material feedback highlighting the wrong material source or destabilizing the rest of the HUD.
- Fixed the Pipe Connector `OFF` message failing to appear and fixed the status text briefly returning at full opacity when its fade ended.
- Fixed construction settings and previews not updating consistently while an animated route was already active.
- Fixed overlapping construction previews remaining visible after their corresponding pipe pieces were placed.

### API Changes and Additions

- Added focused feature packages for manual route actions, shulker material access, interaction resolution, status feedback, and missing-material alerts.
- Added synchronized per-player shulker preferences so servers consume stored materials only when the owning player allows it.
- Added client configuration entries for shulker access, manual support anchors, cascade delay, zoom animation, full-route preview, and next-piece preview.
- Consolidated manual route history and pump direction payloads around shared action models.
- Added concise Spanish Javadocs to the new and changed gameplay, networking, rendering, configuration, and material classes.

### Art Changes

- Refined the radial menu layout, title hierarchy, selected-option description, background opacity, and responsive scaling.
- Added a permanent central manual-tool indicator and direct left/right cycling without opening another radial category.
- Simplified pump and casing disabled options with Minecraft item icons and removed pump direction from the radial menu in favor of `R`.
- Reworked the materials HUD to separate required, inventory, shulker, and reserved counts with consistent colors and spacing.
- Added stacked, color-preserving shulker icons with overflow counts and a compact casing availability indicator.
- Added a subtle shake and color pulse to missing-material feedback while keeping all other material values stable.
- Added a Pipe Connector `ON`/`OFF` status message that appears immediately, remains readable, and fades out smoothly.
- Added a smoother overlapping zoom and settling effect to progressive route construction.

## 1.1.0 - 2026-08-09

### Features

- Added an immediate ghost preview for the first selected pipe position.
- Added progressive route construction that places confirmed pipes one piece at a time.
- Added five construction speed presets: Very slow (`1` piece/s), Slow (`5` pieces/s), Normal (`10` pieces/s), Fast (`15` pieces/s), and Very fast (`20` pieces/s).
- Added an instant placement option by disabling progressive construction.
- Added per-player animation preferences synchronized with multiplayer servers.
- Added live animation setting updates for routes that are already being constructed.
- Added independent settings for the complete outline-free construction preview and the highlighted next-piece preview.
- Added support for several animated routes progressing at the same time.

### Bug Fixes

- Fixed Forge production jars not being reobfuscated correctly.
- Fixed runtime crashes caused by incompatible `ResourceLocation` helper methods in Forge `1.20.1` modpacks.
- Fixed preview pipes becoming opaque, partially invisible, or losing transparency with Embeddium, Oculus, shaders, and other rendering modifications.
- Fixed hidden cyan pipe outlines inside the yellow anchor overlay.
- Fixed triangular gaps and malformed faces in the translucent anchor cube.
- Fixed preview transparency and colors changing after adding or removing anchors.
- Fixed previews disappearing when the camera and route were on opposite sides of a water or fluid surface.
- Fixed placed pipes, elbows, pumps, and encased sections not consistently preserving waterlogging.
- Fixed anchor positions occasionally leaving an empty gap in the placed route.
- Fixed the all-glass style requesting or consuming separate glass pipe items instead of regular fluid pipes.
- Fixed missing-material highlighting so glass-styled sections use the available regular pipe count.
- Fixed placement animation settings not applying reliably after joining, closing the config screen, or changing an active route.
- Fixed disabling progressive construction not finishing already queued routes immediately.
- Fixed completed pieces remaining visible in the construction preview after being placed.

### Changes

- Expanded Create compatibility to versions from `6.0.6` up to, but not including, `6.1.0`.
- Copper casing now follows Create behavior: one casing is required in the inventory, but casing is not consumed for every pipe.
- Glass styling now uses regular Create fluid pipes as its only route material.
- Reorganized routing, geometry, interaction range, materials, sessions, payload handling, and UI code into focused feature packages.
- Converted the legacy multiloader workspace into a standard single-module Forge project.
- Removed the stale NeoForge module, obsolete loader scaffolding, unused code, translations, package metadata, and project files.
- Added concise Spanish Javadocs to the mod classes and methods for future maintenance.
- Simplified the Gradle tasks used to run and package the Forge build.
- Added dedicated ghost and anchor render types while preserving the established cyan, yellow, pump, and missing-material colors.
- Reused Create and Catnip model buffering through `SchematicLevel`, `ForgeBakedModelBufferer`, and `SuperByteBuffer`.
- Cached preview geometry, fluid groups, outline boxes, outline colors, anchor lookups, and visible sections instead of recalculating them every frame.
- Reduced temporary allocations and duplicate work while building routes, drawing outlines, updating anchors, and rendering long previews.
- Updated the README and Forge documentation for the final project structure, Create compatibility, controls, and placement animation settings.
