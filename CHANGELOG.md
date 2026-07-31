# Create: Pipe Connector — Forge Changelog

All notable changes to the Forge edition are documented manually in this file.

Versions are listed from newest to oldest. Changes that have not been released yet are collected under **Unreleased**.
The categories follow Create's changelog style: **Additions**, **Art Changes**, **Gameplay Changes**, **Optimizations**, **Bug Fixes**, and **API Changes**. Empty categories are omitted. The addon does not currently expose a stable public API.

## Unreleased

## 1.1.0 - 2026-07-30

### Additions

- Added synchronized multiplayer route previews so two players can see each other's live pipes and anchors while planning.

### Gameplay Changes

- Glass pipe styling now uses regular fluid pipes as its only consumable material.
- Copper casing continues to require one casing in the inventory without consuming one casing per pipe.
- Expanded Create compatibility from `6.0.8+` to `6.0.6+`.

### Optimizations

- Reorganized pathfinding, route geometry, interaction range, material evaluation, and shared constants into focused utilities.
- Removed unused connector members and obsolete translation entries.
- Removed the stale NeoForge module and loader-specific references from the Forge branch.
- Simplified the root Gradle commands used to run and package the Forge build without colliding with subproject lifecycle tasks.
- Added dedicated ghost and anchor render types to make preview rendering more predictable across modpacks.
- Applied ghost transparency directly to preview vertices instead of relying on global shader color state.
- Reused the cached preview geometry while preserving separate colors for valid and missing materials.
- Reduced temporary allocations while redrawing pipe outlines around anchors.
- Rendered the preview at the appropriate world stage when the camera and route are separated by water or another fluid.

### Bug Fixes

- Fixed production Forge jars not being reobfuscated correctly.
- Fixed runtime crashes caused by incompatible `ResourceLocation` helper methods in Forge `1.20.1` modpacks.
- Fixed preview pipes becoming opaque or partially invisible with Embeddium, Oculus, shaders, and other rendering modifications.
- Fixed hidden pipe outlines inside the yellow anchor overlay.
- Fixed triangular gaps and malformed faces in the translucent anchor cube.
- Fixed preview transparency changing after adding or removing an anchor.
- Fixed previews disappearing across the water surface when the player and route were in different environments.
- Fixed placed pipes, elbows, pumps, and encased sections not consistently preserving waterlogging.
- Fixed anchor positions occasionally leaving an empty gap in the placed route.
- Fixed the all-glass style requesting and consuming separate glass pipe items.
- Fixed missing-material highlighting so styled glass sections count against the available regular pipes.
- Fixed the first selected pipe not appearing until the route reached a second block.

### Art Changes

- Preserved the established cyan pipe outlines, yellow anchor overlay, pump highlights, and red missing-material tint.
- Restored transparent ghost models so rear pipe edges remain visible through the preview.
- Improved anchor layering so the route remains visible without changing the original preview colors.
