# Integration notes

This addon does not expose a public API yet.

## Current gameplay surface

- Connector Pipe mode + held pipe + targeted block right-click to start
- crosshair or air target plus optional anchors define the route
- right-click again to confirm the current preview target
- left-click cancels the current route while leaving Connector Pipe mode enabled
- optional auto-pump mode inserts Mechanical Pumps into straight sections of the plan
- auto-pump direction can be reversed before confirming the route
- Connector Pipe mode also adds a wrench shortcut for toggling straight connected pipe segments between default and glass
- the mod fills the shortest valid path between them
- the client shows a ghost preview before placement, including red-tinted missing materials in survival

## For pack authors

See `docs/MODPACK_GUIDE.md` for runtime requirements and pack compatibility notes.

## For modders

See `docs/DEV_GUIDE.md` for the current code structure and extension points.

If we add compatibility hooks later, they should be documented here first.
