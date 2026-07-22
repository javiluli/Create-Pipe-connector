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
- Press `N` to open the route style radial when you want the preview to move through different axes first.
- Press `P` to automatically place Mechanical Pumps along long routes.
- Press `R` while auto-pumps are enabled to reverse the planned pump direction.
- With Create's wrench, double right-click a pipe to toggle a straight connected segment between default and glass.
- The action bar shows `required/available` counts in survival.
- Missing pipes or pumps are tinted red in the ghost preview.

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
8. Press `N` to open the route style radial without losing a locked preview target.
9. Press `P` to enable or disable automatic Mechanical Pumps.
10. Press `R` to reverse the planned Mechanical Pump direction.
11. Right-click again to place the planned route, even if no block is targeted.
12. Left-click during a preview to cancel only the current route.
13. Press `B` again to disable Connector Pipe mode.

## Route styles

- **Auto:** default behavior.
- **Horizontal first:** tries horizontal axes before vertical movement.
- **Vertical first:** tries vertical movement before horizontal axes.
- **X first:** tries the X axis before Z/Y.
- **Z first:** tries the Z axis before X/Y.
- **Avoid vertical:** strongly avoids up/down movement unless needed.
- The active style is shown in the Connector Pipe HUD while the mode is enabled.
- Use left-click or the mouse wheel to change styles without closing the radial; release `N`, right-click, or press `Esc` to close it.

## Preview behavior

- The preview tries to look like the final pipe line.
- Mechanical Pumps are included in the ghost preview when auto-pumps are enabled.
- When no block is targeted, the preview uses the point at your normal block interaction range.
- Anchors are highlighted with a yellow transparent box.
- If obstacles block the path, the mod finds an alternate route.
- If no valid route exists, nothing is placed.
- If survival inventory is insufficient, the required count turns red and placement is cancelled.
- Missing pipe or pump preview pieces are tinted red so you can see what cannot be afforded.
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
