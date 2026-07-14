<div align="center">
<a><img src="./public/icon.png" width="180" /></a>

# Create: Pipe Connector

⚡ **Tired of laying pipes block by block? Connect them instantly.**

A utility addon for [Create](https://github.com/Creators-of-Create/Create) on Minecraft `1.21.1`.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-2E7D32?style=for-the-badge&logo=minecraft&logoColor=white)
![Loader](https://img.shields.io/badge/Loader-NeoForge-E65100?style=for-the-badge&logo=forge&logoColor=white)
![Create](https://img.shields.io/badge/Create-6.0.10-7B4F1D?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-0.1.0--beta-455A64?style=for-the-badge)

</div>

---

## 🚀 The Purpose: Why this addon?

Designing massive factories in **Create** is incredibly rewarding, but routing long, winding fluid pipelines block by block can quickly become a tedious chore.

**Create: Pipe Connector** is here to fix that. It eliminates the monotony of manual routing by letting you select two points and instantly filling the shortest valid path between them. Spend less time wrestling with pipe placement and more time optimizing your factory lines!

---

## 🕹️ PLAYER SECTION (Usage & Features)

### ✨ Key Features

- **Instant Auto-Connection:** Link two distant pipes with a simple click combination.
- **Smart Pathfinding:** The mod automatically calculates the shortest valid route around obstacles.
- **Zero-Waste Ghost Preview:** See exactly where the pipes will go before spending a single item.
- **Seamless Integration:** Fully refreshes Create's pipe networks instantly upon placement to avoid broken fluid flows.

### 📦 Supported Blocks

- `create:fluid_pipe`
- `create:smart_fluid_pipe`

### 🔧 How to Use It

1.  **Select Start:** Sneak and right-click a Create pipe with an empty hand.
2.  **Select End:** Sneak and right-click a second pipe of the same type.
3.  **Preview:** Look at the custom _ghost preview_ rendered in the world to verify the path.
4.  **Confirm:** Confirm the placement to instantly build the line!

### 📋 Requirements

- **Minecraft:** `1.21.1`
- **NeoForge:** `21.1.219` or compatible
- **Create:** `6.0.10-280` or compatible
- **Java:** `21`

---

## 📦 MODPACKMAKER SECTION

- `Create` is required at runtime.
- `Ponder` and `Flywheel` are utilized to ensure the preview renders smoothly during development and validation.
- _Note: This addon is currently in **Beta (0.1.0-beta)**, meaning features are evolving rapidly._

💬 **We need your feedback!**  
Are you a player with a cool feature idea, or a modpack maker who found a bug? We want to hear from you! Please **open an Issue** or drop a comment with your suggestions, tweaks, or feature requests to help us shape the definitive version of this tool.

---

---

## 💻 DEVELOPER SECTION (Modders & Devs)

> 💡 **Repository Note:** The project maintains a clean, NeoForge-first structure. All legacy beacon rendering code has been completely removed to focus exclusively on the Create pipe connector feature set.

### 🛠️ Project Structure & Flow

If you want to contribute, extend block support, or review the codebase, here is where the core logic lives:

- **Core Logic (`/common`):**  
  Shared backend logic lives in `common/src/main/java/com/javiluli/createpipeconnector/connector/PipeConnectorLogic.java`.  
  _If you want to extend block support, add it here first to keep loader-specific code thin._
- **NeoForge Implementation (`/neoforge`):**  
  Handles loader entrypoints, player input events, server-side placement, and preview pipelines under `neoforge/src/main/java/com/javiluli/createpipeconnector/`.
- **Rendering (`/neoforge/.../client`):**  
  The ghost preview system is managed by `PipeGhostRenderer.java` inside the client render package.

### 🚀 Building the Project

- Run the NeoForge dev client: `./gradlew :neoforge:runClient`
- Build the production artifact: `./gradlew :neoforge:build`

### 📖 Technical Documentation

For deeper insights, check out our internal markdown guides:

- `docs/PLAYER_GUIDE.md` - In-depth player usage
- `docs/MODPACK_GUIDE.md` - Packmaker notes & advanced integration
- `docs/DEV_GUIDE.md` - Implementation details & architecture flow
- `docs/API.md` - Cross-mod integration capabilities
