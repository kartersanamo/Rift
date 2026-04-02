# Rift

A Spigot warp plugin focused on fast travel, admin control, and clear player UX.

## Project Intent

Rift is being built as a full-featured warp system for Spigot servers, with a clean command model, permission-aware behavior, and extensible storage.

Core goals:
- Admin created warps for players
- Player-only warps not supported due to Rift's precessor, H0M3
- Fast, safe teleport flow
- Flexible cooldown and cost controls
- Strong permission granularity
- Scalable storage and future network support

## Current Status (as of 2026-04-02)

### Implemented
- Maven Java plugin project is set up in `pom.xml`
- Spigot API dependency is present (`org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT`, provided scope)
- Plugin entrypoint is configured in `src/main/resources/plugin.yml`
- Main plugin bootstrap class exists in `src/main/java/com/kartersanamo/rift/Rift.java`

### Not Implemented Yet
- Warp creation/teleport commands
- Permission nodes and command registration
- Persistent warp storage
- Cooldowns, warmups, delays, and safety checks
- GUIs, placeholders, economy hooks, and migration paths
- Automated tests

## Planned Feature Scope

### Warp Types
- **Global/Public warps**: server-wide fixed travel points

### Teleport Behavior
- Warmup timer with movement cancel
- Cooldown windows per command/warp category
- Safe teleport checks (solid blocks, lava, void, world border)
- Cross-world restrictions and world allow/deny lists

### Management and Discovery
- `/warps` paging for all warps
- Categories for warps
- Favorites and recently used warps
- GUI browser and command support

### Moderation and Security
- Permission-gated actions for create/edit/delete/teleport/bypass
- Ability to modify all warp settings in game
- Toggling of warps and restricting from players
- Audit events for administrative warp actions
- Anti-spam and anti-abuse controls for command usage

## Roadmap

## Phase 0 - Foundation
- Define package/module structure
- Add command framework and parser layer
- Add config loader and validation
- Add service interfaces for warp, user profile, cooldowns

## Phase 1 - MVP Warp Core
- Implement `/setwarp`, `/warp`, `/delwarp`, `/warps`
- Add permission checks and limit enforcement
- Add YAML-backed persistence
- Add essential messages and localization-ready keys

## Phase 2 - Safety + Controls
- Warmups, cooldowns, and movement cancel
- Teleport safety resolution logic
- World-based restrictions and bypass permissions
- Improved error/reporting UX

## Phase 3 - Advanced Admin + UX
- Toggling/restriction controls for warps
- GUI warp browser
- Rich paging/categories/list commands

## Phase 4 - Storage + Ecosystem
- Storage abstraction completion (`YAML` and `SQLite` adapters)
- Optional placeholder integrations
- Economy integration hooks (optional warp costs)
- Data migration tooling and backup flow

## Phase 5 - Release Hardening
- Command and permission docs finalized
- Integration tests on representative server versions
- Performance and memory tuning pass
- Tagged release with upgrade notes

## Technical Architecture

Planned package layout:
- `com.kartersanamo.rift.command` - command handlers, parsers, tab completion
- `com.kartersanamo.rift.warp` - domain models and warp service logic
- `com.kartersanamo.rift.storage` - repositories and storage adapters
- `com.kartersanamo.rift.teleport` - warmup/cooldown/safety pipeline
- `com.kartersanamo.rift.config` - typed config loading and validation
- `com.kartersanamo.rift.permission` - permission constants and policy checks
- `com.kartersanamo.rift.message` - message keys, formatting, localization

Design principles:
- Keep Bukkit API calls near edge layers
- Isolate domain logic from storage backend details
- Make command logic deterministic and testable
- Use explicit interfaces for swap-in components (storage, economy, placeholder provider)

## Planned Commands (Draft)

Player-focused:
- `/warp <name>`
- `/setwarp <name>`
- `/delwarp <name>`
- `/warps [category]`
- `/warpinfo <name>`
- `/rift [help]`

Admin-focused:
- `/warpadmin create <name>`
- `/warpadmin move <name>`
- `/warpadmin delete <name>`
- `/warpadmin toggle <name|category>`
- `/warpadmin restrict <name|category> <player>`
- `/warpadmin reload`

Quality-of-life:
- Aliases (draft): `/w`, `/setw`, `/delw`
- Tab completion for warp names and subcommands
- Optional click-to-teleport list output for supported chat components

## Planned Permissions (Draft)

Base:
- `rift.warp.use`
- `rift.warp.set`
- `rift.warp.delete`
- `rift.warp.list`

Limits and bypasses:
- `rift.warp.cooldown.bypass`
- `rift.warp.warmup.bypass`
- `rift.warp.safety.bypass`

Admin:
- `rift.admin.*`
- `rift.admin.reload`
- `rift.admin.warp.manage`

## Configuration Direction (Draft)

Planned files:
- `config.yml` - feature toggles, warmup/cooldown defaults, world rules
- `messages.yml` - all user-facing strings and formatting
- `warps.yml` (initial) or `rift.db` (SQLite path)

Potential config keys:
- `teleport.warmup-seconds`
- `teleport.cancel-on-move`
- `teleport.default-cooldown-seconds`
- `limits.default-warps-per-player`
- `worlds.blocked`
- `safety.require-safe-destination`

## Development Setup

The project is for Java 21 using Maven.

```bash
cd /Users/kartersanamo/SoftwareEngineering/Rift
mvn clean package
```

Generated plugin jar will be in `target/` after a successful build.

## Testing Strategy (Planned)
JUnit 5 is the primary testing framework.

- Unit tests for:
  - Name validation and normalization
  - Permission/policy decisions
  - Cooldown and warmup timing logic
  - Safe-location resolution
- Integration checks on a local Spigot test server for command behavior and persistence lifecycle
- Regression checklist for upgrades and migration scenarios

## Release Checklist (Planned)

- [ ] Finalize commands and permissions in `plugin.yml`
- [ ] Validate default config generation
- [ ] Add migration notes for any storage/schema changes
- [ ] Verify startup/shutdown persistence integrity
- [ ] Run compatibility pass on target server versions
- [ ] Publish changelog and semantic version tag

## Contribution Direction

Until MVP is complete, prioritize:
1. Core warp domain model and storage interface
2. Essential command flow (`set`, `teleport`, `delete`, `list`)
3. Teleport safety/cooldown correctness
4. Test coverage around decision logic

## License

This project is licensed under the MIT License. See `LICENSE`.
