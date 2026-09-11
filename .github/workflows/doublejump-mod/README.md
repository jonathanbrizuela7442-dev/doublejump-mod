# Double Jump (Fabric, MC 1.21.11)

A tiny client-side Fabric mod that lets you press jump a second time while
airborne to get an extra hop. **It only activates in singleplayer (including
worlds you host over LAN) and does nothing on multiplayer servers**, so it
won't get you flagged by server-side anti-cheat.

## How it works

- Everything lives in `net.doublejump.client.DoubleJumpClient`, a
  `ClientModInitializer` — there's no server-side code at all, and
  `fabric.mod.json` sets `"environment": "client"` so Loader won't even try
  to load it on a dedicated server.
- Every client tick, it checks `MinecraftClient.isInSingleplayer()`. That's
  `true` for local worlds and LAN-hosted worlds, and `false` the moment
  you're connected to any real remote server — the double jump is skipped
  entirely in that case.
- While airborne and singleplayer, one extra jump-key press per "hang time"
  gives your player an upward + forward velocity boost, resets fall damage
  for that boost, and plays a small sound cue. Landing resets the charge.

## Building

Requires JDK 21.

```bash
./gradlew build
```

The compiled mod jar will be in `build/libs/doublejump-1.0.0.jar`.

> The versions pinned in `gradle.properties` (Yarn mappings, Fabric Loader,
> Fabric API, Loom) were current as of build time. If Gradle complains about
> a missing mapping/loader version, check https://fabricmc.net/develop for
> the latest numbers for Minecraft 1.21.11 and update `gradle.properties`
> accordingly — Fabric publishes new small point releases often.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11
   and drop it in your `.minecraft/mods` folder.
3. Copy `build/libs/doublejump-1.0.0.jar` into the same `mods` folder.
4. Launch the game with the Fabric profile and load into a singleplayer
   world — press jump twice (once on the ground, once in the air) to try it.

## Tuning

Two constants at the top of `DoubleJumpClient.java` control the feel:

- `DOUBLE_JUMP_VELOCITY` — how high the extra jump boosts you (vanilla jump
  is about `0.42`).
- `FORWARD_BOOST` — extra forward push added in the direction you're moving.

Adjust and rebuild as you like.
