# Integration notes

This addon does not expose a public API yet.

## Current gameplay surface

- Connector Pipe mode + held pipe + targeted block right-click to start
- crosshair or air target plus optional anchors define the route
- right-click again to confirm the current preview target
- left-click cancels the current route while leaving Connector Pipe mode enabled
- the mod fills the shortest valid path between them
- the client shows a ghost preview before placement

## For pack authors

See `docs/MODPACK_GUIDE.md` for runtime requirements and pack compatibility notes.

## For modders

See `docs/DEV_GUIDE.md` for the current code structure and extension points.

If we add compatibility hooks later, they should be documented here first.
