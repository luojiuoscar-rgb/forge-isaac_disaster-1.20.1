# 调查结果

- 玩家确认跳变出现在数量不变、空地静止时；因此成员增减引起的槽位变化不能解释本次固定队伍症状。
- 已核对本地Forge1.20.1映射源码：ServerEntity在teleportDelay>400时发ClientboundTeleportEntityPacket；客户端handleTeleportEntity总是传lerpTo(...,3,true)。苍蝇当前teleport=true直接调用Entity.lerpTo，其实现立即setPos/setRot并清空插值。
- 固定半径2.1、每tick服务端移动0.3的数值回放中，普通客户端步长约0.283，校正瞬间约0.850，随后短暂变慢。需要用实际生产插值逻辑补回归测试，不能以数学模型代替客户端实测。
- FlyOwnerFrame原先每tick按PlayerFamiliarData的按类型LinkedHashMap及每种entityIds拼接，新同类实体被插在其他类型前面。最终在现有registerEntity维护跨类型LinkedHashSet运行时队列（追加/移除为O(1)），FlyOwnerFrame仅按此队列拍摄快照，不按类型拼接或每tick排序。这样支持预创建后逆序注册，以及同一UUID在两次快照之间被移除并重新加入队尾。召回不移除注册，继续保留队伍位置。
- 小距离绝对校正的平滑上限为4格，覆盖正常0.3/0.6每tick运动带来的插值滞后；超过4格的绝对位置变化立即应用，保留原有16格外召回行为。服务端lerpTo仍直接设置位置。数值和姿态插值状态独立成包内FlyClientInterpolation，供实体调用及无世界JUnit回放。
- 修改前feature/feature-b，HEAD a7780f02；已有蝗虫饰品、COUNT迁移及其他任务的未提交修改，不改动其内容。既有GameTest支持-PgameTestNamespaces选择范围。
