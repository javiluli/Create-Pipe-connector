# Player guide

## What it does

- Press `B` to enable Connector Pipe mode.
- Hold a Create pipe in either hand.
- Right-click a reachable block or pipe face to start a connection.
- Right-click again to confirm the current preview, even through open air.
- Left-click during a preview to cancel the current route without disabling Connector Pipe mode.
- The mod fills the shortest valid route automatically.
- A ghost preview shows the result before placement.
- A control HUD appears above the hotbar while Connector Pipe mode is enabled.
- Press `P` to automatically place Mechanical Pumps along long routes.
- With Create's wrench, double right-click a pipe to toggle a straight connected segment between default and glass.
- The action bar shows `required/available` counts in survival.

## Supported pipes

- `create:fluid_pipe`
- `create:smart_fluid_pipe`

## How to use it

1. Press `B` to enable Connector Pipe mode.
2. Hold a supported Create pipe in either hand.
3. Check the control HUD above the hotbar.
4. Look at a reachable block or pipe face and right-click to select the start point.
5. Move the crosshair to guide the live preview, even through open air.
6. Press `C` to add an anchor, or `V` to remove the last anchor.
7. Press `Left Alt` to lock or unlock the current preview target.
8. Press `P` to enable or disable automatic Mechanical Pumps.
9. Right-click again to place the planned route, even if no block is targeted.
10. Left-click during a preview to cancel only the current route.
11. Press `B` again to disable Connector Pipe mode.

## Preview behavior

- The preview tries to look like the final pipe line.
- Mechanical Pumps are included in the ghost preview when auto-pumps are enabled.
- When no block is targeted, the preview uses the point at your normal block interaction range.
- Anchors are highlighted with a yellow transparent box.
- If obstacles block the path, the mod finds an alternate route.
- If no valid route exists, nothing is placed.
- If survival inventory is insufficient, the required count turns red and placement is cancelled.
- With auto-pumps enabled, pipe and pump requirements are shown separately.

## Wrench shortcut

- Enable Connector Pipe mode with `B`.
- Hold Create's wrench.
- Double right-click a `create:fluid_pipe` or `create:glass_fluid_pipe`.
- The connected straight segment toggles between default and glass.
- The shortcut stops at Mechanical Pumps and leaves elbows unchanged when glass cannot represent the shape.

## Beta status

- This addon is in beta.
- The name and visuals may still change later.
- If something looks wrong, report it with a screenshot and the approximate location.
