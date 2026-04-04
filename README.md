# Rift

Rift is a Spigot warp plugin focused on admin-managed global warps, simple command flow, and YAML-backed persistence.

## Current Status (2026-04-04)

Rift is in active development and already usable for core warp management.

### Implemented
- Java 21 + Maven project setup (`pom.xml`)
- Spigot plugin bootstrap (`src/main/java/com/kartersanamo/rift/Rift.java`)
- Config bootstrap for `config.yml` and `messages.yml`
- YAML persistence for warps via `warps.yml`
- Core warp manager load/save/update/delete logic (`WarpManager`)
- Registered commands:
  - `/setwarp <name>` - create warp; updates location if warp exists
  - `/warp <name>` - teleport to warp
  - `/deletewarp <name>` - delete warp
  - `/warps` - list loaded warps
  - `/warpinfo <name>` - basic warp info output

### Stability Updates Recently Added
- Default resources are now bundled (`config.yml`, `messages.yml`)
- `plugin.yml` version filtering now resolves correctly with Maven (`${project.version}`)
- Warp load is delayed shortly after plugin enable to reduce race conditions with world-management plugins
- Warp loading skips invalid locations/world references instead of hard-failing plugin startup
- `creator` UUID is persisted for each warp when saved

## Command Registration Notes

Commands declared in `plugin.yml`:
- `rift`, `setwarp`, `deletewarp`, `warpinfo`, `warp`, `warps`

Commands currently registered in code (`Rift.registerCommands()`):
- `setwarp`, `warp`, `deletewarp`, `warpinfo`, `warps`

`/rift` is declared but not currently registered by `CommandManager`.

## Configuration Files

- `config.yml`
  - teleport delay values
  - teleport complete effects
  - warp name min/max length
- `messages.yml`
  - command and teleport messages/prefixes
- `warps.yml`
  - persisted warp data (name, description, category, location, material, created-at, uses, creator)

## Known Limitations / In Progress

- GUI flows exist in source but are not fully wired for production usage
- Listener registration is currently minimal (`registerListeners()` is mostly placeholder)
- Some command responses are still basic/plain-text and need message-key standardization
- Warp info output is currently minimal (full formatted info block is partially scaffolded)
- World load ordering can still depend on external world plugins; `softdepend: [Multiverse-Core]` is set, but edge cases may still require additional startup coordination

## Build and Package

```bash
cd /Users/kartersanamo/SoftwareEngineering/Rift
mvn clean package
```

Output jar:
- `target/Rift-1.0.0.jar` (shaded artifact replaces original during package phase)

## Short Roadmap

1. Finalize command UX and message consistency across all warp commands
2. Complete and wire GUI/listener features
3. Improve world-load resilience for late-loaded/custom worlds
4. Add automated tests for warp load/save and command behavior
5. Add polish for permissions, validation, and admin tooling

## License

This project is licensed under the MIT License. See `LICENSE`.
