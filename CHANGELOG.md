# Create: Pipe Connector - NeoForge Changelog

All notable changes to the NeoForge edition are documented manually in this file.

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
- Fixed the copper casing availability check/X by using the dedicated Minecraft 1.21 beacon sprites.

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

### Additions

- Add progressive route construction that places confirmed pipes one piece at a time
- Add five construction speed presets: Very slow (`1` piece/s), Slow (`5` pieces/s), Normal (`10` pieces/s), Fast (`15` pieces/s), and Very fast (`20` pieces/s)
- Add an option to disable progressive construction for instant placement
- Synchronize per-player animation preferences with multiplayer servers
- Apply animation setting changes to routes already being constructed
- Add independent settings for the complete outline-free construction preview and the highlighted next-piece preview
- Allow several confirmed routes to animate at the same time

### Art Changes

- Add dedicated ghost and anchor render types while preserving the established cyan, yellow, pump, and missing-material colors
- Restore transparent ghost models so rear pipe edges remain visible through the preview
- Improve anchor layering so pipe models and cyan outlines remain visible through the yellow overlay
- Keep preview colors and transparency consistent when routes cross water or other fluid surfaces

### Gameplay Changes

- Support Create versions from `6.0.6` up to, but not including, `6.1.0`
- Require one copper casing in the inventory without consuming one casing for every encased pipe
- Use regular Create fluid pipes as the only route material for glass styling

### Optimizations

- Reorganize routing, geometry, interaction range, materials, sessions, payload handling, and UI code into focused feature packages
- Convert the legacy multiloader workspace into a standard single-module NeoForge project
- Remove obsolete loader scaffolding, package metadata, unused code, and stale project files
- Document mod classes and methods with concise Spanish Javadocs
- Reuse Create and Catnip model buffering through `SchematicLevel`, `BakedModelBufferer`, and `SuperByteBuffer`
- Cache preview geometry, fluid groups, outline boxes, outline colors, anchor lookups, and visible sections instead of recalculating them every frame
- Reuse prepared route modifiers and rebuild preview states only when the route, inventory, or surrounding blocks change
- Precompute pipe refresh directions and cache Create connection methods so completing long routes remains linear
- Reduce temporary allocations and duplicate work while building routes, drawing outlines, updating anchors, and rendering long previews
- Optimize progressive placement updates to reduce per-piece frame hitches
- Update the README and NeoForge documentation for the final project structure, Create compatibility, controls, and placement animation settings

### Bug Fixes

- Fix the initial pipe preview not appearing until the route contains at least two positions
- Fix preview pipes becoming opaque, partially invisible, or losing transparency with Sodium-derived renderers, shaders, and other rendering modifications
- Fix hidden cyan pipe outlines inside the yellow anchor overlay
- Fix triangular gaps and malformed faces in the translucent anchor cube
- Fix preview transparency and colors changing after adding or removing anchors
- Fix previews disappearing when the camera and route are on opposite sides of a water or fluid surface
- Fix placed pipes, elbows, pumps, and encased sections not consistently preserving waterlogging
- Fix progressive pieces using stale waterlogging when the fluid changes before their placement tick
- Fix anchor positions occasionally leaving an empty gap in the placed route
- Fix the all-glass style requesting or consuming separate glass pipe items instead of regular fluid pipes
- Fix missing-material highlighting so glass-styled sections use the available regular pipe count
- Fix placement animation settings not applying reliably after joining, closing the config screen, or changing an active route
- Fix disabling progressive construction not finishing already queued routes immediately
- Fix completed pieces remaining visible in the construction preview after being placed
- Fix the addon rejecting NeoForge `21.1.218` installations when their Create version is supported
