# Create: Pipe Connector - NeoForge Changelog

------------------------------------------------------
Create: Pipe Connector 1.1.0 - NeoForge
------------------------------------------------------

#### Additions

- Add progressive route construction that places confirmed pipes one piece at a time
- Add five construction speed presets: Very slow (`1` piece/s), Slow (`5` pieces/s), Normal (`10` pieces/s), Fast (`15` pieces/s), and Very fast (`20` pieces/s)
- Add an option to disable progressive construction for instant placement
- Synchronize per-player animation preferences with multiplayer servers
- Apply animation setting changes to routes already being constructed
- Add independent settings for the complete outline-free construction preview and the highlighted next-piece preview
- Allow several confirmed routes to animate at the same time

#### Art Changes

- Add dedicated ghost and anchor render types while preserving the established cyan, yellow, pump, and missing-material colors
- Restore transparent ghost models so rear pipe edges remain visible through the preview
- Improve anchor layering so pipe models and cyan outlines remain visible through the yellow overlay
- Keep preview colors and transparency consistent when routes cross water or other fluid surfaces

#### Gameplay Changes

- Support Create versions from `6.0.6` up to, but not including, `6.1.0`
- Require one copper casing in the inventory without consuming one casing for every encased pipe
- Use regular Create fluid pipes as the only route material for glass styling

#### Optimizations

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

#### Bug Fixes

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
