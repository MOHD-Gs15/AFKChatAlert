# 🔔 AFK Chat Alert

[![Platform](https://img.shields.io/badge/Platform-Fabric-blue.svg)](https://fabricmc.net/)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.x-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An ultra-lightweight, 100% client-side Fabric mod for Minecraft players who love to multi-task or go AFK (Away From Keyboard). Never miss an important chat message, mention, or server event ever again!

Whether you are browsing the web, rendering a video, or grabbing a snack, **AFK Chat Alert** instantly plays a customizable sound cue whenever your username or a custom keyword appears in the in-game chat — and stays **completely silent while you are actively playing**.

---

## ✨ Features

* **Smart AFK Detection** — automatically detects when you are idle (no movement, no keys) and arms the alert system after your configurable timeout.
* **Auto-Alert on Username** — instantly notifies you whenever someone mentions your in-game name; no manual configuration required.
* **Custom Keyword Triggers** — add any phrase, slang, or player name to your trigger list (`"afk"`, `"help"`, `"@everyone"`, ...).
* **Vanilla Sound Cues** — choose from four distinct vanilla sounds (Explosion, Experience Orb, Village Bell, Note Block Bell) with a volume slider — no custom sound files, no memory bloat.
* **Anti-Spam Cooldown** — a configurable cooldown prevents sound spam in fast-moving global chats.
* **Quick Toggle Keybind** — press **K** to enable/disable the mod at any time (rebindable in Controls → Misc).
* **In-Game Config UI** — clean settings screen via [Cloth Config](https://modrinth.com/mod/cloth-config) + [Mod Menu](https://modrinth.com/mod/modmenu).

## ⚙️ Configuration

Open **Mods → AFK Chat Alert → Settings** (requires Mod Menu + Cloth Config), or edit `config/afk-chat-alert.json` directly:

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | Master switch (also toggleable with the K key) |
| `autoDetectUsername` | `true` | Watch for your own username |
| `keywords` | `afk, hello, help, @everyone, urgent` | Extra trigger words |
| `cooldownSeconds` | `5` | Minimum seconds between alerts (0 = unlimited) |
| `smartAfkEnabled` | `true` | Only alert while AFK (disable = alert on every match) |
| `smartAfkTimeoutMinutes` | `2` | Inactivity time before AFK mode engages |
| `alertSound` | `EXPLOSION` | `EXPLOSION` / `EXPERIENCE_ORB` / `VILLAGE_BELL` / `NOTE_BLOCK_BELL` |
| `alertVolume` | `100` | Alert volume percentage |

## 📦 Requirements

* Minecraft **26.1.x** (26.1, 26.1.1, 26.1.2)
* [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.4
* [Fabric API](https://modrinth.com/mod/fabric-api)
* [Cloth Config](https://modrinth.com/mod/cloth-config) ≥ 26.1.x
* [Mod Menu](https://modrinth.com/mod/modmenu) ≥ 18.0.0

**100% client-side** — the mod does not need to be installed on the server.

## 🛠️ Building from source

```bash
./gradlew build
```

Requires a Java 25 toolchain (auto-downloaded via Gradle Foojay if missing). The jar is produced in `build/libs/`.

## 📜 Provenance

The original 2.0.0 source tree was lost; this repository was restored from the compiled `afk-chat-alert-f-2.0.0.jar` via CFR decompilation, cleaned of decompilation artifacts, and completed with the reconstructed metadata (mod manifest, language file, build pipeline) required for a working 26.1.x build.

## 📄 License

[MIT](LICENSE) © MOHD_Gs
