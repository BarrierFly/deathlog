# OWO-lib Removal -- DeathLog Mod

## Status (2026-07-31): 9 Working Branches Complete

Nine `owo-free-*` branches are committed and their worktrees are clean. The local `1.21.11` branch is the delivery merge for the 1.21.11 fork.

| Branch | MC | Base | Tip | Commits | owo-lib |
|--------|----|------|-----|---------|---------|
| owo-free-1.19 | 1.19.2 | origin/1.19 `f410afe` | `1260eb9` | 3 | Complete (0 active imports/deps) |
| owo-free-1.19.3 | 1.19.3 | origin/1.19.3 `7629d10` | `35cc8ee` | 5 | Complete (0 active imports/deps) |
| owo-free-1.19.4 | 1.19.4 | origin/1.19.4 `ff85e2d` | `dcc1778` | 4 | Complete (0 active imports/deps) |
| owo-free-1.20 | 1.20 | origin/1.20 `8286f5c` | `7fbba59` | 4 | Complete (0 active imports/deps) |
| owo-free-1.20.2 | 1.20.2 | origin/1.20.2 `d2b3b8d` | `978f1e9` | 4 | Complete (0 active imports/deps) |
| owo-free-1.20.3 | 1.20.4 | origin/1.20.3 `833fab8` | `dd93599` | 4 | Complete (0 active imports/deps) |
| owo-free-1.21 | 1.21 | origin/1.21 `a0bc169` | `5e670d3` | 4 | Complete (0 active imports/deps) |
| owo-free-1.21.2 | 1.21.3 | origin/1.21.2 `8b42efe` | `795355d` | 4 | Complete (0 active imports/deps) |
| owo-free-1.21.11 | 1.21.11 | origin/1.21.11 `481844e` | `773ab8f` | 6 | Complete (0 active imports/deps) |

The local `1.21.11` branch merges `owo-free-1.21.11` into the previous delivery history at `042e96e`; it is 9 commits ahead of `origin/1.21.11`.

## Modification History

### History overview

- `31555da` added `.gitignore`, `FEATURE_SPEC.md`, and the first report. It recorded 1.19-1.20.3 as complete and 1.21+ as partial (GUI/config done, 14 infra files remained), with a JAR built for 1.19.
- `3ade101` removed owo build/config/XML pieces on the 1.21.11 line and replaced config loading, but left the infra imports.
- `b276821` rewrote the report into the 9-branch status format; at that commit the 1.21+ branches were still partial.
- `0379d09` completed the 1.21.11 infra migration: networking, storage, serialization, registry, and property types.
- `a548956` and the equivalent branch-specific commits restored the vanilla UI text and list background.
- `bbf883a` merged the 1.21.11 work into local `1.21.11`.
- `773ab8f`, `c9ba9ed`, and `042e96e` applied and merged the follow-up UI and refmap fixes.

## Follow-up (2026-07-31): UI and refmap fixes

- Added `"refmap": "deathlog-refmap.json"` to every branch's `deathlog.mixins.json` so dev jars remap mixin accessors/injectors at runtime; this fixes the 1.19.4 `MinecraftServerAccessor` startup crash.
- Corrected `DeathListWidget` on 1.20.3 and newer so the old `bot` argument is not interpreted as `itemHeight`; the list now uses `bot - top` for its height and passes the real item height.
- Kept the death list rows in the left panel, moved detail rendering after `super.render`, and placed the inventory background below the selected info columns instead of near the screen bottom.
- Follow-up branch tips: `1260eb9` (1.19), `35cc8ee` (1.19.3), `dcc1778` (1.19.4), `7fbba59` (1.20), `978f1e9` (1.20.2), `dd93599` (1.20.3), `5e670d3` (1.21), `795355d` (1.21.2), `773ab8f` (1.21.11).

### Per-branch record

Diff stats below are cumulative against each `origin/<version>` base and only include `src/`, build config, and wrapper files.

#### owo-free-1.19 (1.19.2, 2 commits)
- `cca0173` remove owo-lib dependency: replaced GUI+Config with vanilla/Fabric API; cleaned build config
- `1a422d6` fix: restore death log UI text and list background

Final diff vs `origin/1.19`: 16 files changed, +234/-516.

#### owo-free-1.19.3 (1.19.3, 4 commits)
- `bdcb580` remove owo-lib: replaced GUI+Config with vanilla/Fabric API
- `05a0478` remove owo-lib dependency for MC 1.19.3
- `63fd514` finish vanilla GUI/config migration for MC 1.19.3
- `a2482f3` fix: restore death log UI text and list background

Final diff vs `origin/1.19.3`: 15 files changed, +189/-555.

#### owo-free-1.19.4 (1.19.4, 3 commits)
- `1efc7a0` remove owo-lib dependency for MC 1.19.4
- `b889e2e` finish vanilla GUI/config migration for MC 1.19.4
- `69708d1` fix: restore death log UI text and list background

Final diff vs `origin/1.19.4`: 14 files changed, +188/-538.

#### owo-free-1.20 (1.20, 3 commits)
- `a76ebe9` remove owo-lib dependency for MC 1.20
- `ab6a79f` finish vanilla GUI/config migration for MC 1.20
- `c56ba2b` fix: restore death log UI text and list background

Final diff vs `origin/1.20`: 15 files changed, +191/-542.

#### owo-free-1.20.2 (1.20.2, 3 commits)
- `32ea2c6` remove owo-lib dependency for MC 1.20.2
- `73a52af` finish vanilla GUI/config migration for MC 1.20.2
- `6bb9f31` fix: restore death log UI text and list background

Final diff vs `origin/1.20.2`: 15 files changed, +190/-541.

#### owo-free-1.20.3 (1.20.4, 3 commits)
- `785c7ba` remove owo-lib dependency for MC 1.20.3
- `b739e2c` finish vanilla GUI/config migration for MC 1.20.3
- `f0f499d` fix: restore death log UI text and list background

Final diff vs `origin/1.20.3`: 15 files changed, +190/-541.

#### owo-free-1.21 (1.21, 3 commits)
- `410c3c4` remove owo-lib dependency for MC 1.21
- `08620bf` finish vanilla GUI/config migration for MC 1.21
- `72be531` fix: restore death log UI text and list background

Final diff vs `origin/1.21`: 33 files changed, +762/-962.

#### owo-free-1.21.2 (1.21.3, 3 commits)
- `1e30f44` remove owo-lib dependency for MC 1.21.2
- `4dda523` finish vanilla GUI/config migration for MC 1.21.2
- `bc4377f` fix: restore death log UI text and list background

Final diff vs `origin/1.21.2`: 34 files changed, +780/-963.

#### owo-free-1.21.11 (1.21.11, 5 commits)
- `31555da` docs: owo-lib removal complete for 1.19-1.20.3; partial for 1.21+; JAR built for 1.19
- `3ade101` remove owo-lib dependency (partial: GUI+Config done; 14 infra files remain)
- `b276821` docs: owo-removal final report for all 9 branches
- `0379d09` remove owo-lib dependency for MC 1.21.11
- `a548956` fix: restore death log UI text and list background

Final diff vs `origin/1.21.11`: 34 files changed, +827/-996.

#### local 1.21.11 delivery branch
- `bbf883a` merge: finish owo-lib removal for 1.21.11 and fix vanilla UI

This merges `owo-free-1.21.11` into the previous 1.21.11 history and is the current delivery branch.

## Changes By Area

### Build configuration (all 9)
- Removed `io.wispforest:owo-lib`, `io.wispforest:owo-sentinel`, and the wispforest Maven repository.
- Removed `owo_version` from `gradle.properties` and `owo-lib` from `fabric.mod.json`.
- Removed Trinkets as a build dependency; `fabric.mod.json` keeps `"trinkets"` in `suggests`.
- Removed `me.lucko:fabric-permissions-api` from 1.19-1.21.2. The 1.21.11 branch still declares it as `modCompileOnly`, but runtime permission use is disabled.
- Fixed TerraformersMC/Ladysnake Maven URLs and switched branches that still used `services.gradle.org` to the Tencent Gradle mirror.

### GUI and config (all 9)
- `DeathLogScreen.java`: owo `BaseUIModelScreen<FlowLayout>` + `owo_ui/deathlog.xml` -> vanilla `Screen`.
- `DeathListEntryContainer.java`: owo `HorizontalFlowLayout` -> `AlwaysSelectedEntryListWidget.Entry`.
- Added `DeathListWidget.java` for the vanilla list/entry behavior.
- `DeathLogConfigModel.java`: owo `@Config`/ModMenu -> plain JSON config; `DeathLogClient` load path updated.
- Deleted `assets/deathlog/owo_ui/deathlog.xml`.
- Final commits restored the death log UI text and list background.

### 1.21+ infrastructure (1.21, 1.21.2, 1.21.11)
- `DeathLogPackets.java`: `OwoNetChannel` -> Fabric `PayloadTypeRegistry` + `ServerPlayNetworking`/`ClientPlayNetworking` + `PacketByteBuf`.
- `DeathInfo` / `DeathInfoPropertySerializer`: endec -> custom NBT read/write serialization.
- Property types: removed the endec-based registry path.
- Property classes: `CoordinatesProperty`, `InventoryProperty`, `LocationProperty`, `MissingDeathInfoProperty`, `ScoreProperty`, and `StringProperty` moved to NBT serialization.
- `BaseDeathLogStorage`: owo NBT serialization -> `NbtIo`.
- `ServerDeathLogStorage`: `Owo.currentServer()` -> stored server reference.
- `DeathLogCommon`, `DeathLogClient`, and `ClientDeathLogStorage`: registration/init cleanup.
- `TrinketComponentProperty.java` is disabled; old owo/endec code only remains in the `.disabled` file.

## Notes

- All nine `owo-free-*` worktrees under `.build/wt/<version>` are clean.
- No active `io.wispforest` imports or dependencies remain. A literal grep for "owo" still finds `Util.getIoWorkerExecutor()` and `github.com/gliscowo/deathlog`, which are not owo-lib references.
- Some intermediate commits are worktree snapshots rather than clean incremental migrations; the authoritative view is `git diff <origin-branch> <owo-free-branch>`.
- `owo-free-1.19` has an unrelated tracked-worktree issue: `cca0173` included `.build/` (454 files) and `.work/` (508 files). Its meaningful source diff is the 16 files above. The other 8 branches do not have this noise.
- The branches are committed locally but have not been pushed (`git branch -vv` shows them ahead of their origin branches).

## Build Instructions

Each branch can be built independently:

    git checkout owo-free-<version>
    $env:GRADLE_USER_HOME = "C:\Users\86136\AppData\Local\Temp\gradle-tmp"
    .\gradlew.bat build --no-daemon

Gradle wrapper properties use Tencent mirror by default. For older branches (1.19-1.20) use the `build/devlibs/<mod>-dev.jar` as `remapJar` may fail with fabric-loom 0.12 + newer Gradle.
