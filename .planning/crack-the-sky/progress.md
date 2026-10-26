# 执行记录

- 2026-10-25：读取item-creation主动分支、检查工作树/已有接口，按批准计划开始。
- 初始化关键Markdown；未进行主分支同步或提交。
- 实现、素材、复审与runData进行中。
- 已实现按玩家队列、5槽位历史、均匀随机取点、落地/锁敌/重选/兜底及生命周期清理，复用原schedule，不修改公共主动使用流程。
- 已新增圣光亡灵倍率重载，旧重载默认1，倍率仅服务端逐目标计算。
- 已补充队列与调度节奏JUnit、地面/未加载区块/锁敌GameTest及圣光混合目标/兼容性GameTest源码；按批准计划暂不运行这些测试。
- 主动注册、品质2、4/4充能、主动列表、中文描述及两个宝箱房池完成。英文Wiki32×32原始PNG下载成功，来源及哈希见findings.md。
- 静态复审发现首道可能在使用当tick的END生成，已改为队列明确记录now+1；复审确认追加不改变到期时间、后续间隔3tick。无需改公共调度器或测试模板复制逻辑。
- 使用Java17（C:/Program Files/Microsoft/jdk-17.0.11.9-hotspot）执行一次runData --offline --no-daemon，退出0，BUILD SUCCESSFUL in 2m 22s。日志：build/crack-the-sky-runData.log。
- 已读取生成的crack_the_sky模型，确认layer0纹理正确；active_items标签包含该物品，生成主动池通过该标签接入；两个手工维护的宝箱池各有一条无权重记录。
- 本次未执行独立compile/build、JUnit、GameTest或客户端；6项新JUnit及5项新GameTest仅为已补充的源码，不计为通过。没有新增音效、弹丸模块或物品持久NBT。
- 没有提交代码；实施资料仅保留本目录三份Markdown，未引入参考源码、截图或音频中间文件。

## 运行时归属调整（2026-10-25）

- 将调度、索敌、落点和生命周期事件从效果类迁至system.combat_effect.crackthesky.CrackTheSkyRuntime；CrackTheSkyQueue迁移并重命名为包内CrackTheSkyState。同步迁移6项JUnit和3项GameTest，保留原有效果注册。
- 通过与迁移前源码的规范化对照，确认发射、索敌、兜底、清理及状态算法未改变；用户已有0.75散布倍率、主动描述及语言文件修改均保留。
- 实际使用Java17运行test --tests '*CrackTheSky*' runData --offline --no-daemon：BUILD SUCCESSFUL in 2m 27s；6项JUnit通过，失败/错误/跳过均为0；runData一次通过，模型及主动标签核对正常，数据生成写入0个变更文件。
- 首次自动审批超时未执行，按提示重试后验证成功。日志build/crack-the-sky-package-move.log。
- 本轮为结构迁移，未增加重复行为测试；没有独立执行compile/build，未运行GameTest或客户端，不将JUnit与runData作为游戏内验收。

## 移除近期目标排除（2026-10-26）

- 已移除五槽位近期目标历史及排除条件，简化落点返回值，序列结束立即清除运行状态；待发数量和发射节奏保持不变。
- 新增GameTest验证实际生成的七道光柱均能锁定同一个附近敌人；删除2项已废弃的历史规则JUnit，并将另一项改为验证重新使用和玩家状态隔离，保留总量/边界/调度间隔检查。
- 先运行旧实现的4项GameTest：新增连续锁敌场景失败，其余3项通过。日志build/crack-the-sky-repeat-target-red.log。测试世界仅位于build/gametest，未启动客户端或操作普通存档。
- 首次修改后4项JUnit通过，新增GameTest仍失败；排查确认测试误改旧TOML值，未影响运行中的数据包快照，因此初次失败也不能作为仅由目标排除导致的独立证据。测试改为临时安装数据包快照并在finally恢复，生产算法未再修改。中间日志build/crack-the-sky-repeat-target-green.log。
- 最终使用Java17执行test --tests '*CrackTheSky*' runGameTestServer -PgameTestNamespaces=isaac_disaster_crack_the_sky runData --offline --no-daemon：BUILD SUCCESSFUL in 1m 37s；4项JUnit零失败/错误/跳过，4项真实Forge GameTest全部通过，一次runData成功，生成文件写入0个变更；模型和主动标签核对正常。日志build/crack-the-sky-repeat-target-final.log。
- 未运行完整JUnit、其他命名空间GameTest或客户端；未独立执行compile/build。现有主动描述及语言文件哈希未变，保留用户已有修改；本次改动暂存后交用户审查，不提交。

## 补全效果类合并（2026-10-26）

- 上轮仅移除目标历史，漏做用户讨论的Runtime/State合并。本轮已删除这两个专属类及旧状态测试，把完整效果合回CrackTheSky，不再维护玩家待发状态Map。
- 将有限次数追加和首发时间交给ScheduledFuncHelper，保留下一tick首发、每3tick及追加不重置间隔。补全待加入任务取消时的索引清理，以及已取消任务不执行/不接受追加的处理。
- 先运行8项调度器行为测试，缺失新入口时全部按断言失败；实现后8项通过，再补充计数器溢出和间隔规范化边界，最终测试直接调用正式入口，无测试反射适配代码。日志build/crack-the-sky-scheduler-red.log、build/crack-the-sky-scheduler-green.log。
- 最终Java17执行test --tests '*ScheduledFuncHelperTest' runGameTestServer -PgameTestNamespaces=isaac_disaster_crack_the_sky runData --offline --no-daemon，BUILD SUCCESSFUL in 2m 54s。10项JUnit零失败/错误/跳过，4项实际Forge GameTest全部通过，一次runData成功，模型及主动标签核对正常，生成文件写入0个变更。日志build/crack-the-sky-effect-consolidation.log。
- 已确认编译输出不再含CrackTheSkyRuntime/State旧类。旧空包目录一并移除；用户主动描述和语言文件哈希保持原样。只在build/gametest隔离世界验证，未启动客户端或操作普通run存档。
- 未运行完整JUnit、其他命名空间GameTest或客户端；未独立执行compile/build。本轮改动及删除整理至暂存区，不提交。

## 移除首发绝对tick限制（2026-10-26）

- 用户选择让调度器自然运行，接受使用当tick末尾首发。删除notBeforeTick、serverTick入参及tick(int)执行重载；有限次数追加、3tick间隔、回调和取消清理保持原状。
- 先调整首发相关测试，旧实现实际运行9项中4项按预期失败、5项通过；日志build/scheduler-natural-start-red.log。
- 修改后Java17执行test --tests '*ScheduledFuncHelperTest' runData --offline --no-daemon：BUILD SUCCESSFUL in 52s，9项JUnit零失败/错误/跳过，一次runData通过，生成文件写入0个变更，模型及主动标签正常。日志build/scheduler-natural-start-validation.log。
- 本轮未运行GameTest、客户端或独立compile/build；未启动测试世界或操作存档。ItemId、主动能力与语言文件均保持本轮开始时用户版本的哈希；仅暂存本轮修改，不提交。
