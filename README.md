<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

⚡ **Connect Create pipes faster. Build less by hand.**

A utility addon for [Create](https://github.com/Creators-of-Create/Create) on Minecraft `1.21.1`.

<!-- Optional badges: remove this block if CurseForge does not allow shields.io -->

![Create](https://img.shields.io/badge/Create-6.0.10-7B4F1D?style=for-the-badge)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-NeoForge-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Version](https://img.shields.io/badge/Version-0.2.0--beta-455A64?style=for-the-badge)

<!-- End optional badges -->

</div>

---

## 🚀 What Is This Mod?

Building large factories in **Create** is fun, but placing long fluid pipe routes block by block can get repetitive fast.

**Create: Pipe Connector** lets you plan a pipe route, preview it, and place the whole line at once. It is designed to make pipe building faster while still giving you control over the final path.

---

## ✨ Main Features

- **Automatic Pipe Routing:** Quickly connect Create fluid pipes through a valid path.
- **Smart Pathfinding:** Routes around obstacles when possible.
- **Live Ghost Preview:** See where pipes will be placed before confirming.
- **Air Confirmation:** Confirm the current preview even when you are not looking at a block.
- **Quick Cancel:** Left-click during a route to cancel the current preview without leaving Connector Pipe mode.
- **Anchor Waypoints:** Press `C` to force the route through a point.
- **Undo Last Anchor:** Press `V` to remove the last anchor.
- **Preview Lock:** Press `Left Alt` to freeze/unfreeze the current preview target.
- **Survival Friendly:** Shows `required/available` pipes and blocks placement if you do not have enough.
- **Control HUD:** Shows the active connector controls above the hotbar while the mode is enabled.
- **Configurable Controls:** Rebind connector mode and route helper keys from Minecraft's Controls menu.
- **Create Integration:** Refreshes Create pipe networks after placement.

---

## 📦 Supported Pipes

- `create:fluid_pipe`
- `create:smart_fluid_pipe`

---

## 🎮 How To Use

1. Press `B` to enable **Connector Pipe** mode.
2. Hold a supported Create pipe in either hand.
3. Check the control HUD above the hotbar.
4. Look at a reachable block or pipe face and right-click to start the connection.
5. Move your crosshair to preview the route, even through open air.
6. Optional: press `C` to add an anchor.
7. Optional: press `V` to remove the last anchor.
8. Optional: press `Left Alt` to lock/unlock the preview target.
9. Right-click again to place the planned pipe line, even if no block is targeted.
10. Optional: left-click during the preview to cancel only the current route.
11. Press `B` again to return to normal gameplay.

In survival, the action bar shows:

```txt
required/available
```

Example:

```txt
10/32
```

If you do not have enough pipes, the required number turns red and placement is cancelled.

---

## 📋 Requirements

- **Minecraft:** `1.21.1`
- **Loader:** `NeoForge`
- **NeoForge:** `21.1.219` or compatible
- **Create:** `6.0.10-280` or compatible
- **Java:** `21`

---

## 📦 For Modpacks

- Required on both client and server.
- Requires `Create`.
- No extra runtime mods are required beyond `Minecraft`, `NeoForge`, and `Create`.
- Current recommended version: **Create `6.0.10-280`**.
- Current addon version: **Create: Pipe Connector `0.2.0-beta`**.

---

## 🌱 Beta Notice

This project is currently in **Beta**.

You may still encounter minor bugs, edge cases, or visuals that need more polish. Feedback is very welcome, especially with screenshots or short descriptions of routes that behave strangely.

---

## 💬 Feedback & Contributions

This project is built around player and modpack feedback.

You can help by:

- Reporting bugs
- Suggesting features
- Sharing screenshots or videos of issues
- Testing long or complex routes in real Create factories
- Opening pull requests for fixes or improvements

Thanks for helping make Create pipe building smoother!

---

## 💻 For Developers

> This branch is **NeoForge-only**. Shared placement logic lives in `common`, while the active runtime implementation lives in `neoforge`.

### Project Structure

- **Core Logic (`/common`):**
  Shared backend logic lives in `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`.

- **NeoForge Implementation (`/neoforge`):**
  Handles entrypoints, events, networking, keybinds, server-side placement, and client preview rendering.

- **Rendering (`/neoforge/.../client/render`):**
  The ghost preview system is managed by `PipeGhostRenderer.java`, with anchor highlights in the `overlay` package.

### Building

- Run the NeoForge dev client: `./gradlew :neoforge:runClient`
- Build the NeoForge artifact: `./gradlew :neoforge:build`
- Build and copy the release jar: `./gradlew buildAll`

### Documentation

- `docs/PLAYER_GUIDE.md` - In-depth player usage
- `docs/MODPACK_GUIDE.md` - Packmaker notes and integration
- `docs/DEV_GUIDE.md` - Implementation details and architecture
- `docs/API.md` - Cross-mod integration notes
