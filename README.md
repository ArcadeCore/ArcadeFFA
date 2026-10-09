# ArcadeFFA

Free For All addon for [ArcadeCore](../ArcadeCore). Last player standing wins, or last team standing
when `team-size` is above 0. Game id: `ffa`.

One jar runs on Minecraft 1.8 to 26.3 (Spigot, Paper and forks). It is compiled to Java 8 bytecode
against spigot-api 1.8.8 and depends on ArcadeAPI 1.1.0. It has no tests of its own.

## Install

1. Put the ArcadeCore jar and the ArcadeFFA jar in `plugins/`. ArcadeFFA declares
   `depend: [ArcadeCore]` in `plugin.yml`, so the server will not enable it without the core.
2. Start the server once so `plugins/ArcadeFFA/config.yml` is written.
3. Create a map and add spawns (see below). A map needs at least `players-required` spawns.

## Commands

| Command | What it does |
| --- | --- |
| `/ffa join` | Join the FFA queue. Players only. A failed join prints the reason from `JoinResult`. |
| `/ffa leave` | Leave the queue. Players only. |

`/ffa` with anything else prints the usage line. There is no permission node on the command.

Map creation is done through ArcadeCore, and `addspawn` must be run by a player, not the console:

```
/arcade map create <id> ffa
/arcade map addspawn <id>
/arcade map config <id> set <key> <value>
```

## config.yml

| Key | Default | Meaning |
| --- | --- | --- |
| `players-required` | `4` | Players needed to start a match. |
| `max-players` | `0` | Upper bound per match. `0`, or anything below `players-required`, means the same as `players-required`. |
| `team-size` | `0` | `0` is solo free-for-all. Above 0 the game uses teams of that size; team count is `max(2, max-players / team-size)`. |
| `friendly-fire` | `false` | Only used when `team-size` is above 0. When false, damage between teammates is cancelled, including projectiles. |

Edit the defaults in `src/main/resources/config.yml`, not the generated file under `plugins/`, if you
are changing the packaged defaults.

## Per-map options

Set with `/arcade map config <map> set <key> <value>`.

| Key | Type | Default | Range | Effect |
| --- | --- | --- | --- | --- |
| `border-size` | integer | `0` | 0 to 60000000 | Above 0, the map world's border is centred on the average of the spawn points and set to this size when the match starts. The previous centre and size are restored when it ends. `0` leaves the border alone. |
| `kill-heal` | boolean | `false` | | The killer is healed to max health on each kill. |
| `time-limit-seconds` | integer | `0` | 0 to 3600 | Above 0, the match ends after this many seconds. Alive players with the most kills win, and ties share the win. `0` means no limit. |

## Stats

Recorded through the core stats manager under game id `ffa`:

- `kills`: +1 to the killer when a player is eliminated by another participant that is still alive.
  Self-kills and environmental deaths give no kill.
- `survival_seconds`: seconds from match start to elimination, added for the player who was eliminated.
  Players who are never eliminated (the winners) do not get an entry.

Wins and losses are recorded by ArcadeCore because FFA declares winners with `endWithWinners`.

## Version notes

- Verified: the plugin loads and `/ffa` works alongside ArcadeCore, ArcadeBedrockPillars and ArcadeHub on
  Paper 1.8.8, 1.12.2, 1.16.5, 1.20.4, 1.21.4 and 26.3 with no errors in the log. A full two-bot match
  (queue, countdown, kill, win, stats via /hub top) was played to completion on 1.8.8, 1.12.2, 1.16.5,
  1.20.4, 1.21.4 and 26.1.2 with TestServer-matrix/match.js.
- Titles and action bars come from ArcadeCore (XSeries). There is no boss bar support on any version;
  boss bar messages show as an action bar instead.
- `kill-heal` uses `getMaxHealth()`, which is deprecated on newer servers but exists on all of them.
- `border-size` relies on the world border, which exists from 1.8 on.

## Building

```bash
(cd ArcadeFFA && ./gradlew build)
```

Run Gradle from this directory and never at the same time as a build in another ArcadeEcosystem project;
they share the included ArcadeCore build directory. There is no `runServer` task. Test on a manual
Paper server or with the scripts in `TestServer-matrix`.
