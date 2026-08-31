# 进度记录

## 2026-08-31

- 已读取执行/验证规范并检查当前工作区；保留此前未提交的射速相关改动。
- 已核对 `PlayerIsaacItems`、`CuriosHelper`、`ForgeEvents`、`ClientDataManager` 和网络包。
- 已确认当前工作树缺少服务端单条目同步包，需要补齐该设计。
- 首次批量修改 `ForgeEvents` 因导入顺序不匹配未应用，已改用分段补丁策略。
- 待处理：实现双类型缓存、所有增减入口维护、全量/单条目同步和验证。

## 2026-08-31 收尾

- 已完成服务端被动道具/Trinket 双 HashMap 缓存，缓存缺失 ID 的 0 值，避免重复扫描。
- 已完成客户端独立 Trinket 表、全量重建和单条目绝对数量同步链路。
- 已接入能力背包、吞下 Trinket、Curios 装备/卸下/替换，以及登录和重生刷新。
- 已删除旧 `FiringModifierRules` 及其测试文件，保留并收束工具方法到对应 Effect 类；该部分属于工作树中此前的射速重构改动。
- 验证：完整 `test` 成功；使用 Microsoft JDK 17 重跑 `compileJava` 成功；`git diff --check` 成功。
- 生命周期收束后再次运行完整 `test`，仍然成功。
- 验证备注：默认 shell 的 `JAVA_HOME` 指向不存在的 JDK 21，重编译时显式指定 `C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot`。

## ResourceLocation 重构

- 已确认本轮不保留数字 ID 数量兼容接口，数量查询与同步全链路迁移到 `ResourceLocation`。
- 已确认 `ItemId`/`TrinketId` 的数字反查表不完整，注册表键需要由枚举名直接生成。
- 已实施：重构 capability、客户端表和网络包，并迁移数量调用点与 Curios 增量同步。

## 收尾

- 已完成 ResourceLocation 缓存统一、Rock Bottom 普通化、Curios 增量同步迁移及全部数量调用点迁移。
- `compileJava`、`test`、`git diff --check` 均通过；编译仅保留既有 Forge 弃用警告。

## 注册表键与数量同步解耦

- 已移除 `ItemId`/`TrinketId` 的资源键拼接与不完整反查表。
- 已新增 `IsaacItemRegistryHelper`，在 `commonSetup` 中从 `ForgeRegistries.ITEMS` 重建被动道具和 Trinket 两张数字 ID 反查表，并拒绝同类重复 ID。
- 所有固定数量查询已改用 `ModPassiveItems` 或 `ModTrinkets` 的注册表键；道具池的数字 ID 入口改经独立 Helper 反查。
- 已拆分增量同步为被动道具和 Trinket 两个包，并将双表全量快照重命名为 `IsaacItemCountMapSyncS2CPacket`。
- 验证：`compileJava`、`test`、精确旧 API 静态审计与 `git diff --check` 均通过；仅保留既有 Forge 弃用警告。

## 道具池 ResourceLocation 化

- `PlayerItemPools` 与 `ServerItemPoolsData` 的池内记录、全局记录及公开接口均迁移为 `ResourceLocation`；全局与指定池集合聚合现在返回副本。
- NBT 现在只读取和写入 `StringTag` 资源键；旧 `IntTag` 记录按约定读为空，不迁移。
- `ItemDisplayContainerBlockEntity`、`GlitchedCrown` 和 `ItemPoolLootModifier` 直接使用 `ForgeRegistries.ITEMS` 取得或解析注册表键；未知键不会形成 Loot 条目。
- 新增 `PlayerItemPoolsTest` 与 `ServerItemPoolsDataTest`，覆盖资源键池状态、全局/局部聚合副本和新旧 NBT 行为。
- 验证：使用 Microsoft JDK 17 运行完整 `test` 成功；`compileJava` 成功；旧整数道具池 API 的精确静态审计无匹配；`git diff --check` 成功。
