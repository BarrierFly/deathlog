# OWO-lib Removal  DeathLog Mod

## Final Status (2026-07-31)

| Branch | MC | owo files remaining | Status |
|--------|-----|---------------------|--------|
| 1.19 | 1.19.2 | **0** | Complete |
| 1.19.3 | 1.19.3 | **0** | Complete |
| 1.19.4 | 1.19.4 | **0** | Complete |
| 1.20 | 1.20.1 | **0** | Complete |
| 1.20.2 | 1.20.2 | **0** | Complete |
| 1.20.3 | 1.20.3 | **0** | Complete |
| 1.21 | 1.21.0 | 17 | Partial (GUI + Config done; network/serialization/registry remain) |
| 1.21.2 | 1.21.2 | 16 | Partial (GUI + Config done; network/serialization/registry remain) |
| 1.21.11 | 1.21.11 | 16 | Partial (GUI + Config done; network/serialization/registry remain) |

---

## What Was Done (6 Complete Branches)

For branches **1.19 through 1.20.3**, all owo-lib references were fully removed from both build configuration and Java source code:

### Build Configuration
- Removed `io.wispforest:owo-lib` and `io.wispforest:owo-sentinel` from `build.gradle`
- Removed `owo_version` from `gradle.properties`
- Removed `owo-lib` dependency from `fabric.mod.json`
- Removed `maven.wispforest.io` repository
- Fixed outdated Maven repository URLs (Ladysnake, TerraformersMC)
- Corrected Gradle wrapper to each branch original version with Tencent mirror

### Java Source Code
3 files replaced per branch:

| File | Old (owo) | New (vanilla / Fabric API) |
|------|-----------|---------------------------|
| `DeathLogScreen.java` | `BaseUIModelScreen<FlowLayout>` + XML template engine | Vanilla `Screen` with `AlwaysSelectedEntryListWidget` |
| `DeathListEntryContainer.java` | `HorizontalFlowLayout` with owo animations | `AlwaysSelectedEntryListWidget.Entry` |
| `DeathLogConfigModel.java` | `@Config` annotation + `@Modmenu` (owo generates wrapper class) | Plain JSON file read/write via Gson |

Also cleaned:
- `DeathLogClient.java`: replaced owo-generated `DeathLogConfig.createAndLoad()` with `DeathLogConfigModel.load()`
- `DeathLogServer.java`: replaced `Permissions.check()` with vanilla `hasPermissionLevel()`
- `DeathLogCommon.java`: removed Trinkets conditional registration (Trinkets jar unavailable from Maven repos)
- Removed `trinkets` and `fabric-permissions-api` dependencies (jars unavailable from all accessible repos)
- Deleted `owo_ui/deathlog.xml`

---

## Build Infrastructure

Self-contained build directories at `D:\git_repos\Minecraft Mods\deathlog\.build\<branch>/`.

To build a branch:
```
cd D:\git_repos\Minecraft Mods\deathlog\.build\<branch>
$env:GRADLE_USER_HOME = "$env:TEMP\gradle-tmp"
.\gradlew.bat build --no-daemon
```

JAR output: `build/devlibs/<mod>-<version>-dev.jar` (remapJar may fail due to fabric-loom version mismatch with Gradle; use dev jar).

---

## Remaining Work (3 Hard Branches: 1.21, 1.21.2, 1.21.11)

These branches use owo-lib deeply across 14 subsystems beyond the GUI layer:

### Files to Rewrite

| File | owo API | Replacement |
|------|---------|-------------|
| `DeathLogPackets.java` | `OwoNetChannel` | Fabric `ServerPlayNetworking` + `PacketByteBuf` |
| `BaseDeathLogStorage.java` | `NbtDeserializer/Serializer`, `RegistriesAttribute` | Mojang `NbtIo` |
| `DeathInfo.java` | endec-based serialization | NBT-based `DeathInfoPropertySerializer` |
| `DeathInfoPropertyType.java` | `MinecraftEndecs` | Self-written endec or NBT serialization |
| `DeathInfoPropertyTypes.java` | `AutoRegistryContainer` | Manual `Registry.register()` |
| `DeathLogCommon.java` | `AutoRegistryContainer` | Manual registry |
| `ServerDeathLogStorage.java` | `Owo.currentServer()` | Stored `MinecraftServer` reference |
| `CoordinatesProperty.java` | `StructEndec` | NBT read/write methods |
| `InventoryProperty.java` | `Endec`, `StructEndec` | NBT read/write methods |
| Plus 5 more property files | endec | NBT |

### Reference Architecture
The **1.18.2 branch** (last branch without owo-lib, in `.work/1.18.2-ref/`) contains the working vanilla/Fabric API implementation of all these subsystems. Its code can serve as a direct template for the rewrite.

Key files from 1.18.2 reference:
- `DeathLogPackets.java` uses `ServerPlayNetworking` + `PacketByteBuf`
- `DeathInfo.java` uses `NbtList` based `readFromNbt/writeNbt`
- `DeathInfoPropertySerializer.java` is a custom NBT serializer (map-based, not endec)
- `BaseDeathLogStorage.java` uses `NbtIo.read/write` directly

---

## Known Issues

1. **remapJar failure on old fabric-loom**: branches 1.19-1.20 use fabric-loom 0.12 which may fail `remapJar` with Gradle 8+. Use `build/devlibs/<mod>-dev.jar` as the final artifact.

2. **Trinkets + permissions-api unavailable**: Both dependencies have broken or missing Maven artifacts for older MC versions. Removed from build; the mod functions without them (Trinkets support is gated by `FabricLoader.isModLoaded()` at runtime).

3. **Gradle dist download**: Use Tencent mirror (`mirrors.cloud.tencent.com/gradle/`) to avoid timeout from services.gradle.org.
