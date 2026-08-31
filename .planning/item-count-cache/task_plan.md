# 道具数量缓存与同步

## Goal

将 `PlayerIsaacItems` 的被动道具和 Trinket 数量查询改为缓存优先，并让普通增减只更新受影响 ID；登录和死亡重生时完成权威全量刷新并同步客户端。

## Phases

- [completed] 核对存储、查询、网络和生命周期入口
- [completed] 实现服务端双类型数量缓存及增减维护
- [completed] 实现客户端双类型全量/单条目同步
- [completed] 接入登录、重生和 Curios/背包操作
- [completed] 编译、测试和差异检查

## 后续解耦修复

- [completed] 固定道具键迁移到 ModPassiveItems/ModTrinkets 注册表对象
- [completed] 新增注册表键到数字 ID 的独立反查 Helper
- [completed] 拆分被动道具与 Trinket 增量同步包并重命名全量快照包

## 道具池 ResourceLocation 化

- [completed] 将个人池与共享池的增删记录迁移为 `ResourceLocation`
- [completed] 将道具池运行链路改为直接使用注册表键
- [completed] 将池状态 NBT 改为资源键字符串，并明确放弃旧整数记录
- [completed] 添加聚焦测试并完成完整验证

## Decisions

- 被动道具与 Trinket 使用两张独立 `HashMap<ResourceLocation, Integer>`，避免两类注册表键混用。
- 被动道具范围为能力背包和 Curios 被动槽位；Trinket 范围为吞下列表和 Curios Trinket 槽位。
- 增量同步发送最新绝对数量；缓存不写入 NBT。
- Rock Bottom 作为普通被动道具进入 `itemCountCache`，不再维护专用计数器。
- 惰性缓存保留已查询 ID 的 `0` 值，以区分“已确认不存在”和“尚未扫描”；全量同步只发送正数。

## Errors Encountered

| Error | Attempt | Resolution |
|---|---:|---|
| `ForgeEvents` patch context did not match import order | 1 | Split the change into smaller targeted patches using the current source layout |
| `JAVA_HOME` 指向已不存在的 JDK 21 路径 | 1 | 使用已安装的 Microsoft JDK 17 重跑验证 |
