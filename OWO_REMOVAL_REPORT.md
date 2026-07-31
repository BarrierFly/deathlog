# OWO-lib Removal -- DeathLog Mod

## Status (2026-07-31) -- 9 Branches Complete

| Branch | MC | owo status | Commit |
|--------|----|------------|--------|
| owo-free-1.19 | 1.19.2 | Complete (0 owo files) | committed |
| owo-free-1.19.3 | 1.19.3 | Complete (0 owo files) | committed |
| owo-free-1.19.4 | 1.19.4 | Complete (0 owo files) | committed |
| owo-free-1.20 | 1.20.1 | Complete (0 owo files) | committed |
| owo-free-1.20.2 | 1.20.2 | Complete (0 owo files) | committed |
| owo-free-1.20.3 | 1.20.3 | Complete (0 owo files) | committed |
| owo-free-1.21 | 1.21.0 | Partial: GUI+Config done; 14 infra files remain (network/serialization/registry) | committed |
| owo-free-1.21.2 | 1.21.2 | Partial: GUI+Config done; 14 infra files remain | committed |
| 1.21.11 | 1.21.11 | Partial: GUI+Config done; 14 infra files remain | committed |

## Full vs Partial

### Complete (6 branches: 1.19-1.20.3)

- Removed owo-lib, owo-sentinel from build.gradle
- Removed owo_version from gradle.properties
- Removed owo-lib dependency from fabric.mod.json
- Replaced DeathLogScreen.java: BaseUIModelScreen<FlowLayout> + XML -> vanilla Screen
- Replaced DeathListEntryContainer.java: HorizontalFlowLayout -> AlwaysSelectedEntryListWidget.Entry
- Replaced DeathLogConfigModel.java: @Config + @Modmenu -> plain JSON config
- Fixed DeathLogClient.java config references
- Removed trinkets and fabric-permissions-api dependencies (Maven artifacts unavailable)
- Fixed outdated Maven repository URLs

### Partial (3 branches: 1.21+)

Same as above for GUI + Config. Remaining 14 infra files still import owo:
- DeathLogPackets.java (OwoNetChannel -> Fabric networking)
- BaseDeathLogStorage.java (owo.serialization -> NbtIo)
- DeathInfo.java (endec serialization -> NBT)
- 11 more property/registration files (endec, AutoRegistryContainer, Owo.currentServer())

Reference architecture exists in the 1.18.2 branch (last version without owo-lib).

## Build Instructions

Each branch can be built independently:

    git checkout owo-free-<version>
     = "C:\Users\86136\AppData\Local\Temp\gradle-tmp"
    .\gradlew.bat build --no-daemon

Gradle wrapper properties use Tencent mirror by default. For older branches (1.19-1.20) use the build/devlibs/<mod>-dev.jar as remapJar may fail with fabric-loom 0.12 + newer Gradle.
