# Integration notes

This addon does not expose a public API yet.

## Current gameplay surface

- Connector Pipe mode + pipe in either hand + right-click a reachable block to start
- crosshair target plus optional anchors define the route
- right-click again to confirm placement, even while looking at air
- the mod fills the shortest valid path between them
- the client shows a ghost preview before placement

## For pack authors

See `docs/MODPACK_GUIDE.md` for runtime requirements and pack compatibility notes.

## For modders

See `docs/DEV_GUIDE.md` for the current code structure and extension points.

If we add compatibility hooks later, they should be documented here first.
