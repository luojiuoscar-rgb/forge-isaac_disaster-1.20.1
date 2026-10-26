# 撕裂苍穹实现计划

执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和对应道具分支规则。

范围：feature/feature-c，跳过主分支同步，不提交。普通主动，Wiki ID160，品质2，4格使用/最大充能；ItemId末尾追加CRACK_THE_SKY，以ordinal取模组ID。

- [complete] 主动能力/效果/物品/数据列表注册、宝箱房及贪婪宝箱房池、中文描述、英文Wiki32×32PNG图标。
- [complete] 每玩家单一有限次数schedule：7道，车载14道，可累积21道；首次跟随调度器下一次自然执行（允许使用当tick末尾），此后间隔3tick，追加不重置间隔；次数和时间由ScheduledFuncHelper管理。
- [complete] 每道生成时读玩家位置、当前用户调整后的0.75×nearbyRange和4×面板伤害+20×DAMAGE_BONUS。5tick预警，普通圣光视觉、半径及遮挡规则。
- [complete] 均匀圆盘落点，从玩家高度向下寻找碰撞顶面；3格内最近合格敌人纠正，排除友方和圆盘外目标，允许连续锁定同一个敌人。纠正无法落地则保留原地面点。
- [complete] 初选+最多5次重选，最后回到玩家位置，先落地再允许空中兜底；兜底不锁敌。
- [complete] 按2026-10-26用户决定移除五槽位目标历史和近期目标排除；无专属Runtime/State或玩家状态Map，有限任务结束自动清理，效果类处理退出/死亡/跨维度取消，调度器统一处理停服清理。
- [complete] HolyLight新增末尾亡灵倍率参数，旧接口默认1，本物品传2；倍率仅服务端保存，对实际命中目标分别应用。
- [complete] 新增6项队列JUnit、3项落点GameTest及2项亡灵倍率/兼容性GameTest源码；一次runData与静态复审通过，未运行JUnit/GameTest/客户端或独立编译/build。

复用现有ActiveAbility/EMTPY/CompositeTrigger、HolyLight、ScheduledFuncHelper；不新增触发类型、物品NBT或使用声音。充能联动保持公共流程。

描述四行：在附近依次降下7道圣光；每道造成4倍面板伤害；并附加%s（StatManager.DAMAGE.description(20)）；对亡灵伤害翻倍。Shift显示范围、锁敌、短预警；车载联动显示数量翻倍。

最终结构（2026-10-26）：CrackTheSkyRuntime和CrackTheSkyState均删除，逻辑合回IAbilityEffect实现CrackTheSky。ScheduledFuncHelper新增原地追加次数的入口；GameTest迁回效果包，队列测试由10项通用调度器JUnit替代。10项JUnit、4项GameTest及一次runData通过，保留用户现有0.75×nearbyRange散布和描述。详细记录见progress.md。

最新首发约定：按用户要求移除notBeforeTick，不强制跨到下一服务器tick；同时删除serverTick参数和tick(int)重载。当前9项调度器JUnit及一次runData通过，本轮未重跑GameTest或客户端。
