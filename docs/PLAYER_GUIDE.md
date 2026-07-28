# Player guide

## What it does

- Press `B` to enable Connector Pipe mode.
- Hold `create:fluid_pipe` in either hand.
- Right-click a reachable block or pipe face to start a connection.
- Right-click again to confirm the current preview, even while looking at air.
- Left-click during a preview to cancel the current route without disabling Connector Pipe mode.
- Use anchors, preview lock, and the radial menu to shape the route before placing.
- The mod can place automatic/manual mechanical pumps, copper casing, and glass straight pipe sections.
- The HUD shows required/available materials in survival and blocks placement if something is missing.

## Supported route pipe

- `create:fluid_pipe`

`create:smart_fluid_pipe` is not used as a route pipe because it is a filtering pipe.

## Quick use

1. Press `B` to enable Connector Pipe mode.
2. Hold a `create:fluid_pipe`.
3. Right-click a reachable block or pipe face to select the start point.
4. Move the crosshair to guide the live preview.
5. Press `C` to add anchors.
6. Press `Left Alt` to lock or unlock the current preview target.
7. Press `N` to open the radial options menu.
8. Right-click again to place the planned route.
9. Left-click during preview to cancel only the current route.
10. Press `B` again to disable Connector Pipe mode.

## Radial menu

- Route priority: Auto, horizontal first, vertical first, X first, Z first, avoid vertical.
- Pumps: Off, efficient spacing, safe spacing.
- Flow: Normal or reversed pump direction.
- Casing: None, manual marks, or full route.
- Style: Default or glass straight sections.

## Preview behavior

- The preview tries to match the final placed route.
- Missing materials are tinted red.
- Anchors are highlighted.
- Automatic pumps are included in the preview when enabled.
- Copper casing requires at least one `create:copper_casing` in inventory, but it is not consumed per pipe.
