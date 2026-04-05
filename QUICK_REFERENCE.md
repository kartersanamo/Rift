# Rift Plugin - Quick Reference Guide

## 🚀 Quick Start

### Deploy
```bash
cp target/Rift-1.0.0.jar /path/to/server/plugins/
# Restart server
```

### Commands
```
/setwarp <name>           Create/update warp at your location
/warp <name>              Teleport to warp
/warps                    Open warp browser GUI
/warpinfo <name>          Show warp details
/deletewarp <name>        Delete warp
/rift reload              Reload config and warps
```

### GUI Navigation
- `/warps` → Warp browser
- Left-click warp → Teleport
- Right-click warp → Edit warp
- Admin GUI → Statistics, reload, backup

---

## 🎮 Features at a Glance

### Warp Data
- **Name**: 3-16 chars, no color codes
- **Description**: Multi-line (max 10 lines, 100 chars each)
- **Category**: For organization
- **Location**: World, X, Y, Z, Yaw, Pitch
- **Material**: GUI icon (default: ENDER_PEARL)
- **Creator**: UUID of creator
- **Timestamp**: When created
- **Uses**: Usage counter

### GUI System
| GUI | Purpose | Actions |
|-----|---------|---------|
| WarpsGUI | Browse warps | Teleport, edit, paginate |
| ManageWarpsGUI | Edit warp | 7 actions (name, material, desc, category, location, delete, info) |
| AdminGUI | Admin panel | Stats, reload, backup |

### Message Keys
- 130+ customizable keys in `messages.yml`
- All command outputs configurable
- Color code support
- Placeholder substitution

---

## ⚙️ Configuration

### config.yml
```yaml
teleport-delay:
  enabled: true            # Enable countdown
  seconds: 5               # Countdown duration
  particle: HAPPY_VILLAGER # Countdown particle
  sound: ENTITY_ENDERMAN_TELEPORT

warp-name:
  min-length: 3
  max-length: 16

warp-description:
  max-lines: 10
  max-length-per-line: 100
```

### Customize Messages
Edit `messages.yml`:
- Change colors: `&a` (green), `&c` (red), `&e` (yellow), `&b` (blue)
- Use placeholders: `%name%`, `%value%`, `%location%`
- Reload via `/rift reload`

---

## 📊 Architecture

### Design Patterns
1. **Message-Driven**: All text externalized to YAML
2. **Shared Validation**: Reusable `WarpManager.validateWarpName()`
3. **Chat Input**: Reusable `ChatInputManager.awaitInput()`
4. **Graceful Degradation**: Errors don't crash plugin

### Code Organization
```
api/                    Framework components
├── chat/              Message handling
├── command/           Command system
├── config/            Configuration loading
├── gui/               GUI framework
├── item/              Item building
├── logging/           Logging utilities
├── particle/          Particle effects
├── sound/             Sound effects
└── util/              Utility functions

command/                Command implementations
gui/                    GUI implementations
listeners/              Event handlers
warp/                   Warp system core
```

---

## 🔍 Admin Commands

```
/rift reload              Reload all configs and warps
```

After editing `config.yml` or `messages.yml`, run `/rift reload` to apply changes.

---

## 🛡️ Permissions

```
rift.command              Use all Rift commands
rift.reload               Use /rift reload command
```

---

## 📦 Build & Deploy

### Build
```bash
cd /Users/kartersanamo/SoftwareEngineering/Rift
mvn clean package
```

### Output
- JAR: `target/Rift-1.0.0.jar` (86 KB)
- Fully shaded with all dependencies

### Verify
```bash
# Check JAR exists
ls -lh target/Rift-1.0.0.jar

# Copy to server
cp target/Rift-1.0.0.jar /path/to/server/plugins/

# Server will generate configs on first load
# plugins/Rift/config.yml
# plugins/Rift/messages.yml
# plugins/Rift/warps.yml
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| Plugin won't load | Check Java 21+, verify Spigot dependency |
| Commands not working | Run `/reload`, restart server |
| Warps not saving | Check `plugins/Rift/warps.yml` writable |
| GUI clicks not responding | Check `GUIManager` in startup logs |
| Config not updating | Run `/rift reload` |

---

## 📝 Message Customization Examples

### Change Prefix Color
In `messages.yml`:
```yaml
chat-prefix-success: "&8&l[&a&lRIFT&8&l]"  # Change &a color
```

### Change Teleport Message
```yaml
teleport-complete:
  success: "&7You arrived at &b%location%"  # Customize
```

### Add Category-Specific Messages
Edit any key and run `/rift reload`

---

## 🎯 Common Use Cases

### Create Spawn Warp
```
/setwarp spawn
```

### Create Multiple Warps
```
/setwarp home
/setwarp shop
/setwarp farm
/warps                    # View all
```

### Customize Warp Details
```
/warps
Right-click warp → Change name/description/material/category
```

### Backup Warps
```
Admin Panel → Click backup button
# Creates plugins/Rift/backups/warps_backup_YYYY-MM-DD_HH-mm-ss.yml
```

### Reload Configuration
```
/rift reload
# Applies config.yml and messages.yml changes
```

---

## 📋 Version Information

- **Version**: 1.0.0
- **Release Date**: April 5, 2026
- **Java**: 21+
- **Spigot**: 1.20+
- **Status**: Production Ready ✅

---

## 📚 Documentation

- `README.md` - Full feature documentation
- `PRODUCTION_CHECKLIST.md` - Feature verification
- `FINAL_SUMMARY.md` - Comprehensive guide
- `config.yml` - Configuration options
- `messages.yml` - Message keys

---

## 🤝 Support

For issues or questions:
1. Check console for error messages
2. Review `README.md` for detailed documentation
3. Verify configuration in `config.yml`
4. Check message keys in `messages.yml`
5. Run `/rift reload` after config changes

---

## ✅ Production Readiness Checklist

- ✅ Zero compilation errors
- ✅ All commands implemented
- ✅ All GUIs functional
- ✅ All validations in place
- ✅ All messages externalized
- ✅ JAR successfully built
- ✅ Ready for deployment

**Rift is production-ready!**

