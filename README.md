# Rift

Rift is a Spigot warp plugin focused on admin-managed global warps, simple command flow, and YAML-backed persistence. Features a complete GUI-based warp management system and production-ready configuration.

## Current Status (2026-04-05) - PRODUCTION READY

Rift is production-ready with all core features and advanced GUI management implemented and tested.

### ✅ Fully Implemented
- **Java 21 + Maven project setup** (`pom.xml`) with Spigot API and Bukkit dependencies
- **Spigot plugin bootstrap** with config/message reloading (`Rift.java`)
- **Config bootstrap** for `config.yml`, `messages.yml`, and `warps.yml` with graceful degradation
- **YAML persistence** with per-warp metadata (name, description, category, location, material, creator UUID, created-at timestamp, uses counter)
- **Core warp manager** with load/save/update/delete/validation logic (`WarpManager`)
- **All registered commands** with tab-completion:
  - `/setwarp <name>` - create warp; updates location if warp exists
  - `/warp <name>` - teleport to warp with configurable countdown + effects
  - `/deletewarp <name>` - delete warp (aliases: `/delwarp`)
  - `/warps` - open GUI warp browser
  - `/warpinfo <name>` - formatted warp info output with full details
  - `/rift reload` - reload all configs and warps

- **Full GUI system**:
  - `WarpsGUI` - main warp browser with pagination
  - `ManageWarpsGUI` - complete warp editor with 7 actions:
    - Change name (with uniqueness validation)
    - Change material (with format parsing)
    - Change description (multi-line with `|` delimiter, line/length limits)
    - Change category (organize warps by category)
    - Change location (update warp location to current player location)
    - Delete warp (confirmation required)
    - View warp information
  - `AdminGUI` - admin panel with:
    - Statistics (total warps, total uses, average uses)
    - Manage all warps button
    - Plugin info display
    - Config reload button
    - Warp backup button (creates timestamped backups)

- **Message system**:
  - 130+ message keys organized hierarchically in `messages.yml`
  - Message cache in `MessagesUtil` with fallback defaults
  - All user-facing text externalized and color-coded
  - Placeholder support for dynamic content (%name%, %value%, %location%, etc.)

- **Shared validation architecture**:
  - `WarpManager.validateWarpName()` enum validates format (size, color codes)
  - Reusable validation between `/setwarp` command and GUI rename flow
  - Name uniqueness check with duplicate prevention

- **Chat input manager**:
  - `ChatInputManager.awaitInput()` for interactive flows
  - Configurable cancel keyword (default: "cancel")
  - Supports callbacks for success and cancellation
  - Used for all rename/material/description/category/delete flows

- **Description validation**:
  - Configurable max lines (default: 10)
  - Configurable max length per line (default: 100 chars)
  - Multi-line support with `|` delimiter for line separation

- **Teleport system**:
  - Configurable countdown delay (default: 5 seconds)
  - Particle and sound effects during countdown
  - Teleport cancellation on player movement
  - Per-warp use counter tracking

- **Production features**:
  - Tab-completion for all warp-based commands
  - Graceful world-loading with `softdepend: [Multiverse-Core]`
  - Warp loading skips invalid locations instead of failing startup
  - Location deserialization with proper error handling
  - Config reloading with message cache updates
  - Warp backup functionality with timestamped files

### Stability Updates
- Config and messages bundled as default resources (`saveDefaultConfig()`, `saveResource()`)
- `plugin.yml` version resolved via Maven properties
- Plugin enable coordinated with world plugins to avoid location deserialization failures
- Warp load gracefully skips corrupted/missing world references
- All compile errors resolved; clean Maven build

### Testing Status
- ✅ Compiles without errors
- ✅ Packages successfully into Rift-1.0.0.jar (86 KB)
- ✅ All 130+ message keys configured with sensible defaults
- ✅ All 6 command handlers implemented and wired
- ✅ All 7 GUI management actions implemented and tested
- ✅ Tab-completion working for `/warp`, `/deletewarp`, `/warpinfo` commands

## Configuration Files

- **config.yml**
  - Teleport delay: enabled, seconds, particle, sound
  - Teleport complete effects
  - Warp name constraints (min: 3, max: 16 chars)
  - Description limits (max: 10 lines, 100 chars per line)
  - GUI display settings

- **messages.yml** (130+ keys)
  - Command messages: usage, permissions, errors
  - Chat input hints and cancel keyword
  - Teleport messages: countdown, success, cancellation
  - Warp info display: all detail fields
  - Warps GUI: title, instructions, empty state
  - Manage GUI: all 7 action prompts, validations, confirmations

- **warps.yml** (auto-generated)
  - Persisted warp data with full metadata
  - One entry per warp with unique ID
  - Creator UUID, timestamp, usage counter

## Command Usage Examples

```bash
# Create/update warp
/setwarp spawn

# Teleport to warp
/warp spawn

# View warp info
/warpinfo spawn

# Open warp GUI browser
/warps

# Delete warp
/deletewarp old_warp

# Reload all configs
/rift reload
```

## GUI Navigation

1. `/warps` → Opens `WarpsGUI`
2. Right-click warp → Opens `ManageWarpsGUI` for editing
3. Left-click warp → Teleports to warp
4. `Manage All Warps` in AdminGUI → Opens `WarpsGUI`
5. `Reload Configs` → Triggers config reload with message
6. `Backup Warps` → Creates timestamped backup file

## Build and Package

```bash
cd /Users/kartersanamo/SoftwareEngineering/Rift
mvn clean package
```

Output JAR:
- `target/Rift-1.0.0.jar` (86 KB, fully shaded)

## Installation

1. Place `Rift-1.0.0.jar` into server's `plugins/` directory
2. Install Multiverse-Core or other world plugin (optional, recommended)
3. Restart server or `/reload` (if supported by your server software)
4. Plugin will generate default `config.yml`, `messages.yml`, and `warps.yml` in `plugins/Rift/`

## Permissions

- `rift.command` - use all Rift commands
- `rift.reload` - use `/rift reload` command
- Each command inherits `rift.command` permission

## Architecture Highlights

### Message-Driven Design
All user-facing text is externalized to `messages.yml` with keys cached in `MessagesUtil`. This enables:
- Easy translations
- Consistent formatting with color codes
- Quick customization without recompiling
- Fallback defaults for missing keys

### Shared Validation Pattern
`WarpManager.WarpNameValidationResult` enum enables reusable validation:
- Same validation logic for `/setwarp` command and GUI rename
- Single source of truth for warp name rules
- Message mapping: `WarpManager.getWarpNameValidationMessage(result)`

### Chat Input Abstraction
`ChatInputManager.awaitInput()` enables interactive flows:
- Used for: rename, material, description, category, delete confirmations
- Supports cancel keyword ("cancel" by default)
- Callbacks for success and cancellation
- Reusable across multiple features

### Graceful Degradation
- Warp load skips invalid locations instead of crashing
- Missing messages default to sensible fallbacks
- Config reload doesn't interrupt gameplay
- Backup system handles IO errors gracefully

## File Structure

```
Rift/
├── src/main/java/com/kartersanamo/rift/
│   ├── Rift.java                        (Main plugin class)
│   ├── api/                             (Framework code)
│   │   ├── chat/                        (ChatFormat, ChatInputManager, ColorUtil)
│   │   ├── command/                     (BaseCommand, CommandManager, SubCommand)
│   │   ├── config/                      (ConfigFile, ConfigUtil, MessagesUtil)
│   │   ├── gui/                         (GUI, GUIManager)
│   │   ├── item/                        (ItemBuilder)
│   │   ├── logging/                     (CoreLogger)
│   │   ├── particle/                    (ParticleUtil)
│   │   ├── sound/                       (SoundUtil)
│   │   └── util/                        (LocationUtil, PlaceholderUtil, TimeUtil)
│   ├── command/                         (Plugin commands)
│   │   ├── SetwarpCommand.java
│   │   ├── WarpCommand.java
│   │   ├── DeletewarpCommand.java
│   │   ├── WarpinfoCommand.java
│   │   ├── WarpsCommand.java
│   │   ├── RiftCommand.java
│   │   └── ReloadCommand.java
│   ├── gui/                             (GUI implementations)
│   │   ├── WarpsGUI.java
│   │   ├── ManageWarpsGUI.java
│   │   └── AdminGUI.java
│   ├── listeners/                       (Event handlers)
│   │   └── TeleportMoveListener.java
│   └── warp/                            (Warp system)
│       ├── Warp.java
│       ├── WarpManager.java
│       └── TeleportManager.java
├── src/main/resources/
│   ├── config.yml                       (Default config)
│   ├── messages.yml                     (All message keys)
│   └── plugin.yml                       (Plugin metadata)
├── pom.xml                              (Maven configuration)
└── LICENSE                              (MIT License)
```

## Known Limitations / Future Enhancements

- GUI pagination system is basic (9 warps per page with next/prev buttons)
- No admin commands for bulk operations (yet)
- Warp categories are stored but not used for filtering in GUI (planned)
- No per-player warp permissions (planned)
- Backup system uses filesystem copies (consider database for large servers)

## License

This project is licensed under the MIT License. See `LICENSE`.
