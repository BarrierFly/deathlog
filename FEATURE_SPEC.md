# DeathLog — 功能与行为规格说明

## 1.  Mod 身份信息

| 字段         | 值                                                                     |
|--------------|------------------------------------------------------------------------|
| Mod ID       | `deathlog`                                                             |
| 名称         | DeathLog（fork 版本使用 "DeathLog [Fork]"）                            |
| 描述         | 记录你那些令人尴尬的死亡时刻                                           |
| 作者         | glisco（原版），ShinoiroKanashi（fork 版）                             |
| 协议         | MIT                                                                    |
| 运行环境     | 客户端和服务端（1.17+）；仅客户端（1.16 移植版）                       |
| 源码         | 原版: <https://github.com/gliscowo/deathlog>                           |
|              | Fork: <https://github.com/ShinoiroKanashi/deathlogMod>                  |

---

## 2.  版本分支与目标矩阵

上游仓库（`origin`，来自 glisco）覆盖 MC 1.16 到 1.21.1。
Fork 仓库（`ShinoiroKanashi`）从 1.21.1 起接手并新增 1.21.11 分支。

| 分支                  | Minecraft | Fabric Loader | Fabric API          | owo-lib             | Java | 权限 API             | Trinkets          |
|-----------------------|-----------|---------------|---------------------|---------------------|------|----------------------|--------------------|
| `origin/master`       | 1.17.1    | 0.11.6        | 0.39.0+1.17         | —                   | 16   | —                    | 3.0.2             |
| `origin/1.16-backport`| 1.16.5    | 0.11.6        | 0.36.0+1.16         | —                   | 8    | —                    | —                 |
| `origin/1.18`         | 1.18      | 0.12.8        | 0.43.1+1.18         | —                   | 17   | —                    | 3.1.0             |
| `origin/1.18.2`       | 1.18.2    | 0.14.6        | 0.51.1+1.18.2       | —                   | 17   | 0.1-SNAPSHOT         | 3.3.0             |
| `origin/1.19`         | 1.19.2    | 0.14.11       | 0.68.0+1.19.2       | 0.9.2+1.19          | 17   | —                    | 3.4.0             |
| `origin/1.19.3`       | 1.19.3    | 0.14.12       | 0.76.0+1.19.3       | 0.10.3+1.19.3       | 17   | —                    | 3.5.1             |
| `origin/1.19.4`       | 1.19.4    | 0.14.18       | 0.76.0+1.19.4       | 0.10.3+1.19.4       | 17   | —                    | 3.6.0             |
| `origin/1.20`         | 1.20      | 0.14.21       | 0.83.0+1.20         | 0.11.0+1.20         | 17   | —                    | 3.7.0-pre.3       |
| `origin/1.20.2`       | 1.20.2    | 0.14.22       | 0.89.3+1.20.2       | 0.11.3+1.20.2       | 17   | —                    | 3.8.0             |
| `origin/1.20.3`       | 1.20.4    | 0.15.3        | 0.92.0+1.20.4       | 0.12.0+1.20.3       | 17   | —                    | 3.8.0             |
| `origin/1.21`         | 1.21      | 0.15.11       | 0.100.1+1.21        | 0.12.10+1.21        | 21   | 0.2-SNAPSHOT         | 3.10.0            |
| `origin/1.21.2`       | 1.21.3    | 0.16.9        | 0.107.3+1.21.3      | 0.12.18+1.21.2      | 21   | —                    | 3.10.0            |
| `origin/1.21.11`      | 1.21.11   | 0.18.4        | 0.141.3+1.21.11     | 0.13.0+1.21.11      | 21   | 0.6.1                | （已移除）         |
| `1.21.11`（本地）     | 1.21.11   | 0.18.4        | 0.141.3+1.21.11     | 0.13.0+1.21.11      | 21   | 0.6.1                | （已移除）         |

**Trinkets 说明：** Trinkets 集成在 fork 的 1.21.11 分支和本地 `1.21.11` 分支中已被移除（注释掉）。
上游 1.21.11 分支可能有也可能没有。

**Mod Menu** 在所有有配置界面的分支上都是编译期可选依赖。它在 1.19 时代随 owo-lib 一同引入。

**fabric-permissions-api** 是编译期依赖；当模组不在场时，权限检查降级为原版 OP 等级（owners / 等级 4）。

---

## 3.  入口点

| 端          | 运行时机                                                                     |
|-------------|------------------------------------------------------------------------------|
| 通用端      | 双端运行。创建 `deathlog:property_type` 注册表，注册网络通道。                |
| 客户端      | 仅客户端。管理客户端存储生命周期、快捷键、GUI 注入。                          |
| 服务端      | 仅独立服务端。管理服务端存储生命周期，注册命令。                              |

---

## 4.  死亡检测

### 4.1  客户端

**现代检测（Minecraft ≥ 1.20.5）：**
Mod 拦截 `DeathMessageS2CPacket`（`onDeathMessage` 数据包）。
客户端收到该数据包时记录一条死亡条目。

**旧版检测：**
当 `useLegacyDeathDetection` 启用时，Mod 额外拦截 `HealthUpdateS2CPacket`
（`onHealthUpdate`）。若生命值 ≤ 0 且客户端尚未将本地玩家标记为死亡，
则记录一条死亡条目，死亡消息为空。

此旧版路径用于协议转换器（如 ViaFabric），这类工具可能不会发送
结构化的 `DeathMessageS2CPacket`。

两条路径均仅在渲染线程上触发（避免与客户端世界/玩家状态产生竞态条件）。

### 4.2  服务端

Mod 在 `HEAD` 位置拦截 `ServerPlayerEntity#onDeath(DamageSource)`。
通过 `player.getDamageTracker().getDeathMessage()` 获取死亡消息。

---

## 5.  死亡信息 — 数据模型

每条死亡记录表示为一个 **DeathInfo** 对象，包含以字符串为键的 **DeathInfoProperty** 映射。
每个属性具有：

- 一个**类型**（通过 `deathlog:property_type` 注册表中的 `Identifier` 标识）。
- 一个**格式化**显示文本。
- 一个**名称**（翻译文本，在 GUI 中作为标签显示）。
- 一个**可搜索字符串**，用于 GUI 搜索框。

### 5.1  标准属性

| 键                | 属性类型              | 可恢复 | 数据内容                                               |
|-------------------|-----------------------|:------:|--------------------------------------------------------|
| `inventory`       | InventoryProperty     |   ✓    | 玩家物品栏（36 格）、副手、盔甲                        |
| `coordinates`     | CoordinatesProperty   |   ✗    | BlockPos（x, y, z）                                    |
| `dimension`       | StringProperty        |   ✗    | 维度注册表键（如 `minecraft:overworld`）               |
| `location`        | LocationProperty      |   ✗    | 世界/服务器名称 + 多人模式标记                         |
| `score`           | ScoreProperty         |   ✓    | 分数、等级、进度、总经验值                             |
| `death_message`   | StringProperty        |   ✗    | 死亡消息文本                                           |
| `time_of_death`   | StringProperty        |   ✗    | `new Date().toString()`                                |
| `trinket_component`| TrinketComponentProperty| ✓    | 原始饰品 NBT（可选，需 Trinkets 模组）                 |

### 5.2  GUI 显示顺序

详情面板中属性的显示顺序：

1. `death_message`
2. `coordinates`
3. `time_of_death`
4. `score`
5. `dimension`
6. `location`

### 5.3  完整死亡信息与部分死亡信息

含有 `inventory` 属性时为**完整**信息，否则为**部分**信息。
服务端向客户端远程打开死亡界面时仅发送部分信息（节省带宽）。
客户端随后按需通过 `InfoRequest` 数据包请求完整数据。

### 5.4  可恢复属性

实现"可恢复"契约的属性可以恢复到玩家身上：

- **InventoryProperty：** 清空玩家物品栏，将保存的物品放入格子 0–35、副手（36）
  以及盔甲（脚 → 腿 → 胸 → 头）。
- **ScoreProperty：** 恢复 `experienceProgress` 和 `experienceLevel`。
- **TrinketComponentProperty：**（编译启用时）恢复饰品物品栏。

### 5.5  扩展性

其他模组可通过向 `deathlog:property_type` 注册表注册 `DeathInfoPropertyType`
来添加自定义 `DeathInfoProperty` 类型。反序列化时遇到的未知属性类型会以
`MissingDeathInfoProperty` 及原始 NBT 保留，数据不会丢失。

每次创建新的 DeathInfo 时都会触发 `DeathInfoCreatedCallback` Fabric 事件，
允许其他模组注入额外属性。

`SpecialPropertyProvider` 是一个 `(DeathInfo, PlayerEntity)` 消费函数注册表，
在 DeathInfo 创建时调用，用于可选的模组集成（如 Trinkets）。

---

## 6.  存储系统

### 6.1  文件格式

死亡数据以 **NBT**（GZip 压缩的 `.dat` 文件）存储。

**根复合标签：**
- `FormatRevision`（int）：始终为 `3`（1.18.2 及以上版本；更早的分支可能有不同的修订号）。
- `Deaths`（list）：每个元素是一个 NBT 复合标签，代表一个 DeathInfo。

每个 DeathInfo 复合标签中每个属性一个键；值的格式由属性类型自身的反序列化器
（基于 owo-lib 的 Endec 系统）决定。

### 6.2  客户端存储

**目录：** `<游戏目录>/deathlog/`
**文件名格式：** `deaths_<世界或服务器名>.dat`

后缀来源：
- **单人游戏：** `LevelStorage.Session` 的目录名。
- **多人游戏：** 服务器地址。

所有匹配 `[\\/:*?\"<>|]` 的字符均替换为 `_`。

客户端在进入世界时加载文件，并在每次存储/删除操作后立即保存。
读写操作在 I/O 工作线程上异步执行。

如果 `deathlog/` 目录无法创建或文件无法读写，存储进入**错误**状态：
显示弹出提示（"DeathLog Database Error" / "DeathLog Problem"），
当前会话中后续磁盘操作将被静默禁用。

### 6.3  服务端存储

**目录：** `<服务端目录>/deaths/`
**文件名格式：** `<UUID>.dat`

服务器启动时加载目录中的所有文件。

每个文件包含由文件名中的 UUID 标识的一名玩家的死亡列表。
不以有效 UUID 结尾的文件被视为错误，并禁用后续磁盘操作。

保存行为与客户端相同：每次存储/删除后异步 I/O。

### 6.4  远程存储

当客户端通过 `/deathlog view` 命令查看服务器死亡日志时，
服务端发送**部分** DeathInfo 列表（不含物品栏/饰品）。
客户端将其包装为远程存储。用户点击部分条目时，
客户端请求完整数据；服务端响应对应索引的完整 DeathInfo。

GUI 中的恢复和删除操作同样通过数据包转发到服务端。

---

## 7.  命令（服务端）

所有命令需要 OP 等级 4（owners），或者当 `fabric-permissions-api-v0` 在场时
对应的权限节点。

**根命令：** `/deathlog`

| 子命令                                             | 权限节点              | 行为                                                                                   |
|----------------------------------------------------|-----------------------|----------------------------------------------------------------------------------------|
| `/deathlog list <玩家>`                            | `deathlog.list`       | 列出指定玩家的所有死亡条目，每条一个信息块。                                           |
| `/deathlog list <玩家> <搜索词>`                   | `deathlog.list`       | 同上，但仅显示 `createSearchString()` 包含搜索词的条目（不区分大小写）。               |
| `/deathlog view <玩家>`                            | `deathlog.view`       | 在命令执行者的客户端上打开 DeathLog 界面，显示目标玩家的死亡记录。                     |
| `/deathlog restore <玩家> <索引>`                  | `deathlog.restore`    | 从指定索引的死亡条目恢复物品栏与经验。目标玩家必须在线。                               |
| `/deathlog restore <玩家> latest`                  | `deathlog.restore`    | 从最近（最后）一次死亡条目恢复。目标玩家必须在线。                                     |

玩家参数使用 `GameProfileArgumentType`，补全建议来自在线玩家。

**错误消息：**
- "No DeathInfo found for index X" — 无效索引。
- "Player X is not online" — 目标玩家未连接。
- "No DeathInfo found" — `latest` 恢复时死亡列表为空。

---

## 8.  网络协议

所有数据包通过 owo-lib 的 `OwoNetChannel`（标识为 `deathlog:channel`）传输。

### 8.1  服务端 → 客户端

#### OpenScreen

| 字段            | 类型               | 说明                                                         |
|-----------------|--------------------|--------------------------------------------------------------|
| `profile`       | UUID               | 目标玩家 UUID                                                |
| `can_restore`   | boolean            | 查看客户端是否有恢复/删除权限                                |
| `partial_infos` | List\<DeathInfo\>   | 部分死亡信息（不含物品栏/饰品）                              |

客户端创建远程存储并打开 DeathLogScreen。

#### DeathInfoData

| 字段      | 类型      | 说明                               |
|-----------|-----------|------------------------------------|
| `infoIdx` | int       | 要替换的死亡列表中的索引           |
| `info`    | DeathInfo | 完整死亡信息（含物品栏）           |

仅在 DeathLogScreen 打开时接收；替换部分条目。

### 8.2  客户端 → 服务端

#### InfoRequest

| 字段      | 类型 | 说明                                   |
|-----------|------|----------------------------------------|
| `profile` | UUID | 目标玩家                               |
| `index`   | int  | 要请求完整数据的死亡条目索引           |

需要 `deathlog.view` 权限。服务端回复 `DeathInfoData`。

#### RestoreRequest

| 字段      | 类型 | 说明                                 |
|-----------|------|--------------------------------------|
| `profile` | UUID | 目标玩家                             |
| `index`   | int  | 要恢复的死亡条目索引                 |

需要 `deathlog.restore` 权限。服务端将 DeathInfo 通过当前注册表管理器
解码并重新编码（处理注册表重映射），然后在目标玩家上调用恢复操作。

#### DeletionRequest

| 字段      | 类型 | 说明                               |
|-----------|------|------------------------------------|
| `profile` | UUID | 目标玩家                           |
| `index`   | int  | 要删除的死亡条目索引               |

需要 `deathlog.delete` 权限。服务端从其存储中删除。

---

## 9.  GUI — 死亡日志界面

### 9.1  布局

界面使用 **owo-ui** XML 模板（`deathlog:deathlog`）。

| 区域              | 内容                                                                               |
|-------------------|------------------------------------------------------------------------------------|
| 左上角标签        | "DeathLog — N total"（死亡计数）                                                    |
| 左侧面板（40%）   | 可滚动的死亡列表，底部有搜索框                                                      |
| 右侧面板（60%）   | 选中条目的详情视图（或"Select a death info from the list…"占位提示）                |
| 配置按钮          | 打开 owo-config 配置界面                                                            |

### 9.2  死亡列表

- 每个条目是一个可选中容器，显示：
  - **第一行：** 死亡时间（或"时间缺失"）。
  - **第二行：** 死亡消息（或"死亡消息缺失"）。
- 悬停/聚焦/选中时条目有滑入动画。
- 选中条目有灰色轮廓。
- 搜索框按 `createSearchString().contains(搜索词)` 实时筛选条目（不区分大小写）。
- **左键点击：** 选中条目，显示详情面板。
- **右键点击：** 打开右键菜单，包含：
  - **恢复**（仅当 `canRestore` 为 true 时显示）。
  - **删除**（红色文字）。

### 9.3  详情面板

选中完整条目时：

1. **标题：** 死亡消息文本（带阴影）。
2. **属性表：** 左列（蓝色标签）+ 右列（白色值）。
   两列为独立的 owo-ui FlowLayout，行间垂直间距 2px，列间水平间距 5px。
3. **物品栏显示：**
   - 背景：`deathlog:textures/gui/inventory_overlay.png`（210×107）。
   - **盔甲列：** 在像素位置 (185, 28) 纵向排列：
     脚 → 腿 → 胸 → 头（从下到上）。
   - **主物品栏：** 在像素位置 (7, 24) 的 9×4 网格：
     - 第 0 行（底部）：快捷栏格子 0–8。
     - 第 1–3 行：物品栏格子 9–35。
   - **副手槽：** 在像素位置 (186, 8)。
4. **物品交互：**
   - 物品提示框显示原版提示 + 灰色提示：
     - 创造模式："Press Mouse 3 to spawn"（按鼠标中键生成）
     - 生存模式："Press Mouse 3 to copy /give"（按鼠标中键复制 /give 命令）
   - **鼠标中键点击物品：**
     - 创造模式：调用 `dropCreativeStack(stack)` 生成物品。
     - 生存模式：构建含所有 NBT 组件数据的 `/give` 命令字符串并复制到剪贴板。

选中部分条目时（远程存储），详情面板显示"Loading…"直到完整数据到达。

### 9.4  配置界面

从死亡日志界面的配置按钮打开。使用 owo-config。两个选项：

1. **死亡时截图**（`screenshotsEnabled`，默认：`false`）
2. **使用旧版死亡检测**（`useLegacyDeathDetection`，默认：`false`）
   - 提示文字："Uses a less reliable but more sensitive method of detecting
     deaths that works with protocol translators like ViaFabric"

---

## 10.  快捷键

| 按键  | 名称                            | 分类      | 操作                                                         |
|-------|---------------------------------|-----------|--------------------------------------------------------------|
| `END` | `key.deathlog.death_screen`     | 杂项      | 打开当前客户端存储对应的死亡日志界面。                        |

---

## 11.  截图功能

当 `screenshotsEnabled` 为 `true` 且客户端检测到死亡时，
Mod 调用 `ScreenshotRecorder.saveScreenshot()` 并发送聊天消息：
`[DeathLog] <截图保存路径>`。

---

## 12.  统计界面集成

在原版统计界面上添加一个"DeathLog"按钮（60×20px，位于 (10, 5)）。
点击后打开当前客户端存储对应的死亡日志界面。

---

## 13.  权限（服务端）

| 权限节点              | 默认等级                 | 控制范围                              |
|-----------------------|--------------------------|---------------------------------------|
| `deathlog.list`       | 4（owners / OP 等级 4）  | `/deathlog list`                      |
| `deathlog.view`       | 4（owners / OP 等级 4）  | `/deathlog view`、`InfoRequest`        |
| `deathlog.restore`    | 4（owners / OP 等级 4）  | `/deathlog restore`、`RestoreRequest`  |
| `deathlog.delete`     | 4（owners / OP 等级 4）  | `DeletionRequest`                     |

当 `fabric-permissions-api-v0` **不在场**时，所有检查降级为
`serverCommandSource.getPermissions().hasPermission(DefaultPermissions.OWNERS)`。

当权限 API 在场时，使用 `Permissions.require(node, PermissionLevel.OWNERS)`。

---

## 14.  配置模型

| 字段                       | 类型    | 默认值  | 说明                                                           |
|----------------------------|---------|---------|----------------------------------------------------------------|
| `screenshotsEnabled`       | boolean | `false` | 死亡时截图（仅客户端）。                                        |
| `useLegacyDeathDetection`  | boolean | `false` | 使用 `HealthUpdateS2CPacket` 检测死亡（用于 ViaFabric 兼容）。 |

配置通过 owo-config 以文件名 `deathlog` 存储和加载。

---

## 15.  翻译键

### 15.1  属性标签

| 键                                                  | 中文          |
|-----------------------------------------------------|---------------|
| `deathlog.deathinfoproperty.coordinates`           | 坐标          |
| `deathlog.deathinfoproperty.dimension`             | 维度          |
| `deathlog.deathinfoproperty.location`              | 位置          |
| `deathlog.deathinfoproperty.location.singleplayer` | 单人游戏      |
| `deathlog.deathinfoproperty.location.multiplayer`  | 多人游戏      |
| `deathlog.deathinfoproperty.score`                 | 分数          |
| `deathlog.deathinfoproperty.death_message`         | 死亡消息      |
| `deathlog.deathinfoproperty.time_of_death`         | 死亡时间      |
| `deathlog.deathinfoproperty.trinket_component`     | 饰品          |

### 15.2  GUI 与交互

| 键                                                                | 中文                                                                     |
|-------------------------------------------------------------------|--------------------------------------------------------------------------|
| `key.deathlog.death_screen`                                       | 死亡日志界面                                                             |
| `text.deathlog.action.restore`                                    | 恢复                                                                     |
| `text.deathlog.action.delete`                                     | 删除                                                                     |
| `text.deathlog.action.give_item.spawn`                            | 按鼠标中键生成                                                           |
| `text.deathlog.action.give_item.copy_give`                        | 按鼠标中键复制 /give 命令                                                |
| `text.deathlog.info.time_missing`                                 | 时间缺失                                                                 |
| `text.deathlog.info.death_message_missing`                        | 死亡消息缺失                                                             |
| `text.deathlog.death_list_title`                                  | DeathLog — %s 条记录                                                     |
| `text.deathlog.death_info_loading`                                | 加载中…                                                                  |
| `text.deathlog.no_info_selected_hint`                             | 从列表中选择一条死亡信息以开始                                           |
| `text.deathlog.config`                                            | 配置                                                                     |
| `text.config.deathlog.title`                                      | DeathLog 配置                                                            |
| `text.config.deathlog.option.screenshotsEnabled`                  | 死亡时截图                                                               |
| `text.config.deathlog.option.useLegacyDeathDetection`             | 使用旧版死亡检测                                                         |
| `text.config.deathlog.option.useLegacyDeathDetection.tooltip`     | 使用一种可靠性较低但更敏感的\n死亡检测方法，兼容\nViaFabric 等协议转换器  |

### 15.3  值格式化字符串

| 键                                                    | 格式                |
|-------------------------------------------------------|---------------------|
| `deathlog.deathinfoproperty.coordinates.value`        | `%s %s %s`          |
| `deathlog.deathinfoproperty.location.value`           | `%s (%s)`           |
| `deathlog.deathinfoproperty.score.value`              | `%s (%s 级, %s xp)` |
| `deathlog.deathinfoproperty.trinket_component.value`  | `%s 件物品`         |

### 15.4  本地化文件

| 文件        | 语言          |
|-------------|---------------|
| `en_us.json`| 英语（美国）  |
| `pt_br.json`| 葡萄牙语（巴西）|
| `ru_ru.json`| 俄语          |

---

## 16.  纹理与资源

| 资源                                                  | 说明                                   |
|-------------------------------------------------------|----------------------------------------|
| `assets/deathlog/icon.png`                            | Mod 图标（用于 Mod Menu）              |
| `assets/deathlog/textures/gui/inventory_overlay.png`  | 210×107 物品栏背景叠加图               |
| `assets/deathlog/textures/gui/trash_can.png`          | 垃圾桶图标（当前代码路径中未使用）      |
| `assets/deathlog/owo_ui/deathlog.xml`                 | 定义死亡日志界面的 owo-ui XML          |

---

## 17.  混入（Mixin）

### 17.1  客户端

| 目标类                          | 用途                                                             |
|---------------------------------|------------------------------------------------------------------|
| `ClientPlayNetworkHandler`      | 拦截 `onDeathMessage` 和（可选）`onHealthUpdate`                 |
| `MinecraftServer`               | 访问器，暴露 `LevelStorage.Session session` 字段                 |
| `SystemToast`                   | 访问器，暴露 `startTime` 字段（被 DeathLogToast 使用）           |

### 17.2  服务端

| 目标类                | 用途                                    |
|-----------------------|-----------------------------------------|
| `ServerPlayerEntity`  | 拦截 `onDeath(DamageSource)`            |

---

## 18.  使用的 owo-lib 功能模块

- **owo-config：** 配置模型注解处理、Mod Menu 集成、配置界面生成。
- **owo-ui：** 基于 XML 的界面模板，FlowLayout、Scroll、TextBox、Button、
  Label、ItemComponent、TextureComponent、DropdownComponent（右键菜单）、
  动画框架（PaddingAnimation）、模板展开。
- **owo-networking（OwoNetChannel）：** 服务端/客户端数据包注册与处理。
- **owo-sentinel：** Jar-in-jar 内嵌用于运行时支持。
- **owo-serialization：** NBT 序列化/反序列化、Endec 系统、
  RegistriesAttribute（注册表感知编码）、MinecraftEndecs。
- **owo-registration：** AutoRegistryContainer 用于属性类型自动注册。

---

## 19.  使用的 Fabric API 模块

| 模块                                       | 用途                                                    |
|--------------------------------------------|---------------------------------------------------------|
| `fabric-api-base`                          | 入口点接口                                              |
| `fabric-networking-api-v1`                 | owo-net 通道的基础网络支持                              |
| `fabric-registry-sync-v0`                  | 底层注册表同步支持                                      |
| `fabric-key-binding-api-v1`                | `KeyBindingHelper.registerKeyBinding`                   |
| `fabric-lifecycle-events-v1`               | `ServerLifecycleEvents.SERVER_STARTED`                  |
| `fabric-screen-api-v1`                     | `ScreenEvents.AFTER_INIT`、`Screens.getButtons`         |
| `fabric-client-networking-api-v1`          | `ClientPlayConnectionEvents`                            |
| `fabric-command-api-v2`                    | `CommandRegistrationCallback.EVENT`                     |
| `fabric-events-lifecycle-v0`               | 底层事件                                                |

---

## 20.  重写注意事项

1.  **客户端存储生命周期：** 客户端存储在 `ClientPlayConnectionEvents.JOIN`
    时创建，在 `DISCONNECT` 时置空。这意味着 单人 → 退出到标题 → 加入服务器
    可以正确重新创建绑定到新世界/服务器的存储。

2.  **服务端存储是 DedicatedServerModInitializer：** 它不在集成服务器
    （单人世界的内部服务器）上运行。单人游戏中的客户端死亡追踪由客户端存储处理。

3.  **格式跨版本兼容性：** `FormatRevision` 字段阻止加载不兼容版本写入的数据库。
    如果修订号不匹配，文件不会加载，且磁盘操作永久禁用。

4.  **远程存储注册表重映射：** 处理 `RestoreRequest` 时，服务端将 DeathInfo
    从一个注册表上下文解码，再重新编码到当前注册表上下文
    （使用 `EdmSerializer`/`EdmDeserializer` 作为中间格式），
    以应对世界重连时注册表可能发生的偏移。

5.  **部分/完整信息拆分：** 序列化时使用特殊的部分 Endec，
    剥离 `InventoryProperty` 和 `TrinketComponentProperty`。
    这对网络效率至关重要——为每个条目发送完整物品栏 NBT 的代价过高。

6.  **异步 I/O：** 所有文件读写使用 `Util.getIoWorkerExecutor()`。
    加载返回 `CompletableFuture`；在当前客户端代码中 future 被 `.join()` 阻塞，
    （可接受，因为文件很小且在世界加入期间运行）。

7.  **owo-ui 版本差异：** `base_ui_model_screen` API 在不同 owo-lib 版本间
    有所变化。XML 命名空间和 schema 位置必须与使用的 owo-lib 版本匹配。
    在 0.9.x 之前的版本中，GUI 根本不基于 owo-ui。

8.  **Java 目标版本：** 1.16 移植版用 1.8，1.17 用 16，1.18–1.20.4 用 17，
    1.21+ 用 21。

9.  **权限 API 版本变化：** 1.16 移植版使用 `fabric-permissions-api:0.1-SNAPSHOT`；
    1.18.2 也是 0.1-SNAPSHOT；1.21 使用 0.2-SNAPSHOT；1.21.11 fork 使用 0.6.1。

10. **映射表：** fork 的 1.21.11 分支从 Yarn 切换到了 Mojang 官方映射表。
    所有上游分支使用 Yarn。

---

## 21.  完整文件清单

```
src/main/
├── java/com/glisco/deathlog/
│   ├── DeathLogCommon.java
│   ├── client/
│   │   ├── ClientDeathLogStorage.java
│   │   ├── DeathInfo.java
│   │   ├── DeathLogClient.java
│   │   ├── DeathLogConfigModel.java
│   │   └── gui/
│   │       ├── DeathListEntryContainer.java
│   │       ├── DeathLogScreen.java
│   │       └── DeathLogToast.java
│   ├── death_info/
│   │   ├── DeathInfoProperty.java
│   │   ├── DeathInfoPropertyType.java
│   │   ├── DeathInfoPropertyTypes.java
│   │   ├── RestorableDeathInfoProperty.java
│   │   ├── SpecialPropertyProvider.java
│   │   └── properties/
│   │       ├── CoordinatesProperty.java
│   │       ├── InventoryProperty.java
│   │       ├── LocationProperty.java
│   │       ├── MissingDeathInfoProperty.java
│   │       ├── ScoreProperty.java
│   │       ├── StringProperty.java
│   │       └── TrinketComponentProperty.java
│   ├── mixin/
│   │   ├── ClientPlayNetworkHandlerMixin.java
│   │   ├── MinecraftServerAccessor.java
│   │   ├── ServerPlayerEntityMixin.java
│   │   └── SystemToastAccessor.java
│   ├── network/
│   │   ├── DeathLogPackets.java
│   │   └── RemoteDeathLogStorage.java
│   ├── server/
│   │   ├── DeathLogServer.java
│   │   └── ServerDeathLogStorage.java
│   └── storage/
│       ├── BaseDeathLogStorage.java
│       ├── DeathInfoCreatedCallback.java
│       ├── DeathLogStorage.java
│       └── DirectDeathLogStorage.java
└── resources/
    ├── assets/deathlog/
    │   ├── icon.png
    │   ├── lang/
    │   │   ├── en_us.json
    │   │   ├── pt_br.json
    │   │   └── ru_ru.json
    │   ├── owo_ui/
    │   │   └── deathlog.xml
    │   └── textures/gui/
    │       ├── inventory_overlay.png
    │       └── trash_can.png
    ├── deathlog.mixins.json
    └── fabric.mod.json
```

---

*规格说明结束。*
