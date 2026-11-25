# LimboPositionFix

Small standalone Paper plugin. Fixes the "I get teleported to limbo spawn
every time I rejoin while not logged in" behavior from LibreLogin Next,
without touching LibreLogin Next itself.

## How it works
- On quit, if you're standing in a world listed under `limbo-worlds` in this
  plugin's `config.yml`, your exact location is remembered in memory.
- On join, a couple ticks after LibreLogin Next does its own
  teleport-to-limbo-spawn, this plugin teleports you back to where you were.
- The moment you leave the limbo world (i.e. you actually log in and get
  sent to the real lobby/world), the saved location is discarded.

It uses only standard Bukkit/Paper events (`PlayerQuitEvent`,
`PlayerJoinEvent`, `PlayerChangedWorldEvent`) — it does **not** call
LibreLogin Next's internal API, so it won't break if that API changes
between LibreLogin Next versions.

## IMPORTANT — read before installing
This plugin tracks **worlds on a single Paper server**. That matches your
config if you're running **Paper standalone** (limbo = a world on the same
server as everything else).

If you're running **Velocity** with `limbo` as a *separate backend server*
(a different Paper/Spigot process, not just a different world), this
approach needs to change: `PlayerJoinEvent`/`PlayerChangedWorldEvent` only
fire within one backend server, so you'd instead need to install this on
the limbo backend specifically and detect "quit" as a server switch rather
than a real disconnect. Let me know which setup you're on (standalone Paper
vs. Velocity + separate limbo server) and I'll adjust it.

## Build
Requires JDK 21.

```bash
./gradlew build
```

The compiled jar will be at `build/libs/LimboPositionFix-1.0.0.jar`.
(First open the project in your IDE or run `gradle wrapper` once if the
`gradlew` wrapper scripts aren't present — this project only ships the
Gradle config, not the wrapper binaries.)

Before building, open `build.gradle.kts` and make sure the `paper-api`
version matches the Paper build your server runs (e.g. `1.21.4-R0.1-SNAPSHOT`).

## Install
1. Drop the built jar into `plugins/` alongside LibreLogin Next.
2. Edit `plugins/LimboPositionFix/config.yml` — `limbo-worlds` must list the
   exact same world name(s) as LibreLogin Next's `limbo` setting in its own
   `config.conf`.
3. `/reload confirm` or restart.

## Commands
- `/limboposreload` (permission `limbopositionfix.reload`, op by default) —
  reloads `config.yml` without a full restart.

## Notes
- Saved positions are in-memory only; a server restart clears them (you'll
  just land at limbo spawn once after a restart, same as before — not a
  regression).
- `restore-delay-ticks` in config.yml controls how long after join this
  plugin waits before re-applying your saved position. 2 ticks is usually
  enough; raise it if LibreLogin Next's own teleport is still winning the
  race on your server.
