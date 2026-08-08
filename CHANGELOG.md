# Create: Pipe Connector - Forge Changelog

All notable changes to the Forge edition are documented manually in this file.

Versions are listed from newest to oldest. Changes that have not been released yet are collected under **Unreleased**. Release entries use **Features**, **Bug Fixes**, and **Changes** so their wording can stay aligned with the related Git commits.

## Unreleased

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
