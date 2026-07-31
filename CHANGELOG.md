## 1.1.0

### Gameplay Changes

- Glass pipe styling now uses regular fluid pipes as its only consumable material.
- Copper casing continues to require one casing in the inventory without consuming one casing per pipe.
- Expanded Create compatibility from `6.0.10` to `6.0.6+`.
- Expanded NeoForge compatibility to include `21.1.218+`.

### Optimizations

- Reorganized pathfinding, route geometry, interaction range, material evaluation, and shared constants into focused utilities.
- Moved client material evaluation into a dedicated preview helper.
- Added concise Javadocs and package documentation for future contributors.
- Removed unused connector members and obsolete translation entries.
- Added dedicated ghost and anchor render types to make preview rendering more predictable across modpacks.
- Applied ghost transparency directly to preview vertices instead of relying on global shader color state.
- Reused cached preview geometry while preserving separate colors for valid and missing materials.
- Reduced temporary allocations while redrawing pipe outlines around anchors.
- Rendered the preview at the appropriate world stage when the camera and route are separated by water or another fluid.

### Bug Fixes

- Fixed the initial pipe preview not appearing until the route contained at least two positions.
- Fixed preview pipes becoming opaque or partially invisible with Embeddium, Sodium-derived renderers, shaders, and other rendering modifications.
- Fixed hidden pipe outlines inside the yellow anchor overlay.
- Fixed triangular gaps and malformed faces in the translucent anchor cube.
- Fixed preview transparency changing after adding or removing an anchor.
- Fixed previews disappearing across the water surface when the player and route were in different environments.
- Fixed placed pipes, elbows, pumps, and encased sections not consistently preserving waterlogging.
- Fixed anchor positions occasionally leaving an empty gap in the placed route.
- Fixed the all-glass style requesting and consuming separate glass pipe items.
- Fixed missing-material highlighting so styled glass sections count against the available regular pipes.
- Fixed the addon rejecting modpacks using NeoForge `21.1.218` even when their Create version was supported.

### Art Changes

- Preserved the established cyan pipe outlines, yellow anchor overlay, pump highlights, and red missing-material tint.
- Restored transparent ghost models so rear pipe edges remain visible through the preview.
- Improved anchor layering so the route remains visible without changing the original preview colors.
