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
- Press `N` to open the connector options radial when you want to adjust the route.
- Press `N` to configure route style, automatic pumps, pump direction, casing, and pipe style.
- Advanced direct shortcuts for pump/casing actions exist in Minecraft's Controls menu, but are unassigned by default.
- With Create's wrench, double right-click a pipe to toggle a straight connected segment between default and glass.
- The connector HUD shows item icons with `required/available` counts in survival.
- Confirmed routes can be built progressively instead of appearing all at once.
- Missing pipes, pumps, or required copper casing access are tinted red in the ghost preview.

## Supported pipes

- `create:fluid_pipe`

`create:smart_fluid_pipe` is not supported as a connector pipe because it is a filtering pipe.

## How to use it

1. Press `B` to enable Connector Pipe mode.
2. Hold a supported Create pipe in either hand.
3. Check the control HUD above the hotbar.
4. Look at a reachable block or pipe face and right-click to select the start point.
5. Move the crosshair to guide the live preview, even through open air.
6. Press `C` to add an anchor.
7. Use the radial menu, or custom keybinds if assigned, for casing and pump options.
8. Press `Left Alt` to lock or unlock the current preview target.
9. Press `N` to open the connector options radial without losing a locked preview target.
10. Use the radial menu to configure automatic Mechanical Pump modes for the active route.
11. Use the radial menu to configure pump direction, casing mode, and pipe style.
12. Right-click again to place the planned route, even if no block is targeted.
13. Left-click during a preview to cancel only the current route.
14. Press `B` again to disable Connector Pipe mode.

## Construction animation

Open **Mods > Create: Pipe Connector > Config** to choose how confirmed routes are built.

- **Animate route construction:** enables progressive one-piece-at-a-time placement.
- **Construction speed:** Very slow, Slow, Normal, Fast, or Very fast.
- **Complete construction preview:** keeps all unbuilt pieces visible without outlines.
- **Next piece preview:** highlights the piece that will be placed next.

Disabling the animation places new routes instantly and immediately completes routes already in progress. Speed changes also apply to active routes.

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
- Mechanical Pumps are included in the ghost preview when an automatic pump mode or manual pump marks are enabled.
- Copper-cased pipe marks and full-route casing mode are included in the ghost preview before placement.
- Glass pipe style uses glass pipes on straight sections and regular pipes on corners.
- When no block is targeted, the preview uses the point at your normal block interaction range.
- Anchors are highlighted with a yellow transparent box.
- If obstacles block the path, the mod finds an alternate route.
- If no valid route exists, nothing is placed.
- If survival inventory is insufficient, the required count turns red and placement is cancelled.
- Missing pipe, pump, or casing preview pieces are tinted red so you can see what cannot be afforded.
- If you confirm without enough materials, the route stays active and the HUD shows what is missing.
- Glass style uses the same regular `create:fluid_pipe` inventory as default style; it does not require a separate glass item.
- Pump and copper casing requirements are shown separately when those features are enabled.
- Manual pumps use Create's `create:mechanical_pump` item and only apply to straight route slots.
- Copper casing requires at least one Create `create:copper_casing` item in your inventory, but it is not consumed.
- Copper casing applies to regular `create:fluid_pipe` routes, producing `create:encased_fluid_pipe`.
- Copper casing marks stay attached to their world position during the current route and become active again if the route passes through that position.
- If a casing mark and an automatic Mechanical Pump want the same position, the pump is placed and the casing is skipped.

## Wrench shortcut

- Enable Connector Pipe mode with `B`.
- Hold Create's wrench.
- Double right-click a `create:fluid_pipe` or `create:glass_fluid_pipe`.
- The connected straight segment toggles between default and glass.
- The shortcut stops at Mechanical Pumps and leaves elbows unchanged when glass cannot represent the shape.

## Release status

- The `1.x` line is the stable public release of the addon.
- If something looks wrong, report it with a screenshot, the approximate location, and the relevant client or server log.
