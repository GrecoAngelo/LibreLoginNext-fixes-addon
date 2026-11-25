# LimboPositionFix

[![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://adoptium.net/)
[![LibreLogin Next](https://img.shields.io/badge/LibreLogin%20Next-Compatible-green?style=flat-square)](https://github.com/kyngs/LibreLogin)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

Standalone Paper plugin that prevents LibreLogin Next from resetting a player's coordinates to limbo spawn every time they reconnect while unauthenticated. Works without hooking into LibreLogin Next.

## How it works

1. **Disconnect (`PlayerQuitEvent`):** If the player is in a world listed under `limbo-worlds` in `config.yml`, their exact location is cached in memory.
2. **Reconnect (`PlayerJoinEvent`):** A few ticks after LibreLogin Next runs its spawn teleport, this plugin teleports the player back to their cached coordinates.
3. **Authentication (`PlayerChangedWorldEvent`):** Once the player logs in and gets moved out of the limbo world, the cached location is cleared from memory.

Because this only listens to standard Bukkit/Paper events, it will not break across LibreLogin Next API changes.

## Compatibility Warning

This plugin tracks **worlds on a single Paper server**.

* **Standalone Paper:** Supported out of the box if your limbo is a world on the same Paper instance as your main lobby/world.
* **Velocity / BungeeCord (separate limbo server):** Not supported in this version. `PlayerJoinEvent` and `PlayerChangedWorldEvent` only fire within a single backend server. For proxy setups, the plugin would need to run on the limbo backend specifically and track proxy server switches instead of disconnects.

## Installation

1. Download `LimboPositionFix-1.0.0.jar` from [Releases](../../releases) or build it from source.
2. Drop the jar into your `plugins/` directory alongside LibreLogin Next.
3. Restart the server to generate `plugins/LimboPositionFix/config.yml`.
4. Set `limbo-worlds` to match the exact world name(s) configured under `limbo` in LibreLogin Next's `config.conf`.
5. Run `/limboposreload`.

## Configuration

```yaml
# World names treated as limbo worlds.
# Must match the limbo world(s) in LibreLogin Next's config.conf.
limbo-worlds:
  - "limbo"

# Ticks to wait after join before restoring the saved position.
# Increase this if LibreLogin Next's spawn teleport still overrides the position.
restore-delay-ticks: 2
```

## Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/limboposreload` | Reloads `config.yml` without a restart. | `limbopositionfix.reload` | `OP` |

## Building from Source

Requires **JDK 21**.

1. Clone the repo:
   ```bash
   git clone [https://github.com/yourusername/LimboPositionFix.git](https://github.com/yourusername/LimboPositionFix.git)
   cd LimboPositionFix
   ```
2. Open `build.gradle.kts` and verify the `paper-api` dependency version matches your server build (e.g. `1.21.4-R0.1-SNAPSHOT`).
3. If the Gradle wrapper scripts are missing from your clone, generate them once:
   ```bash
   gradle wrapper
   ```
4. Build the jar:
   ```bash
   ./gradlew build
   ```
5. Output jar will be at `build/libs/LimboPositionFix-1.0.0.jar`.

## Notes

* Saved positions are kept in memory only. A server restart clears them, meaning players will land at limbo spawn once after a restart before tracking resumes.

## License

[MIT](LICENSE)
