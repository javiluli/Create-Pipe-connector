# Integration notes

This addon does not expose a public API yet.

## Current gameplay surface

- pipe in off-hand + empty main-hand sneak right-click to start
- crosshair target plus optional anchors define the route
- sneak right-click again to confirm placement
- the mod fills the shortest valid path between them
- the client shows a ghost preview before placement

## For pack authors

See `docs/MODPACK_GUIDE.md` for runtime requirements and pack compatibility notes.

## For modders

See `docs/DEV_GUIDE.md` for the current code structure and extension points.

If we add compatibility hooks later, they should be documented here first.
