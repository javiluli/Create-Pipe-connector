# Modpack guide

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.218` or newer compatible `21.1.x`
- Create `6.0.6` up to, but not including, `6.1.0`
- Java `21`

Install the addon on both client and server. Create is the only gameplay mod required at runtime.

## Included behavior

- Obstacle-aware fluid pipe routing with anchors
- Cached ghost preview and survival material warnings
- Automatic and manual Mechanical Pump placement
- Optional copper casing and glass straight sections
- Progressive or instant construction with five speed presets
- Independent complete-route and next-piece construction previews
- Create wrench shortcut for default/glass straight pipe segments

## Compatibility notes

- The renderer uses Create/Catnip model buffering and dedicated translucent layers.
- It is designed for Sodium-derived renderers, shaders, and large modpacks, but unusual render pipelines can still require testing.
- Test long routes, water crossings, anchors, casing, pumps, and glass style before publishing a pack update.
- Report visual issues with screenshots, the exact shader or renderer versions, and the client log.

## Material rules

- Route and glass-style sections consume regular `create:fluid_pipe` items.
- Mechanical Pumps consume `create:mechanical_pump` items.
- Copper casing requires at least one `create:copper_casing`, but does not consume one casing per pipe.
- Missing materials block confirmation in survival and tint affected preview pieces red.
