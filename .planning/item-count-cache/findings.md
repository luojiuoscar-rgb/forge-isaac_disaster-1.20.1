# 本轮发现

- `PlayerIsaacItems.getItemCountFromAll` 当前扫描 `playerPassiveItems` 与 `activeCurioSlots`；`getTrinketCount` 当前通过吞下列表加实时 Curios 查询。
- `activeCurioSlots` 同时保存被动道具和 Trinket，需按 ItemStack 类型分别计入两张缓存。
- `ItemId` 和 `TrinketId` 都从 0 开始，不能共享客户端或服务端的单一整数 Map。
- 现有 `PassiveItemMapSyncS2CPacket` 是全量包，处理时会清空客户端表；当前没有服务端单条目包，只有 `ClientDataManager.modifyItemCount`。
- 登录和 `PlayerRespawnEvent` 会先调用 `CuriosHelper.syncAllIsaacCurios`，随后调用 `syncAllDataToClient`；死亡换实体由 `PlayerCloneEvent` 复制能力后进入重生流程。
- Rock Bottom 已由 `rockBottomCount` 和 `modifyRockBottomCount` 单独维护，不需要通用容器扫描。
- 惰性缓存不能仅依赖 `Map.get(id) == null` 判定未缓存，否则不存在的 ID 会每次重新扫描；缓存内部保留 0 值，导出的全量同步 Map 过滤非正数。
- 增量包携带绝对数量，客户端 setter 在收到 0 时删除对应记录，避免旧记录残留。
