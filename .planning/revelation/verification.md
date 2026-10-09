# 终末天启首版验证记录

> 本文件保留初版历史证据，并单列用户后续要求的不可中断光束、金色十字 HUD 和音效修正。历史的中断／白色 HUD／无音效描述已由当前修正规则覆盖。整体阶段见 [task_plan.md](task_plan.md)，当前实现见 [findings.md](findings.md)，执行记录见 [progress.md](progress.md)。

## 实现

- Revelation／终末天启：Wiki ID 643 仅用于身份核对；模组 `ItemId.REVELATION(4)` 追加于枚举末尾，实际 ordinal 为 179。
- 新增 `Revelation`、`RevelationAttack` 和无额外效果的 `Seraphim` 套装；首版独立 Controller 已全部合并至攻击类并删除。
- 更新被动道具、能力、攻击、颜色、蓄力栏和套装注册，以及 `ItemId`、`SetId`。
- 在 `ServerTickEvent.IsaacHeadAttack` 和 `IsaacDisasterEvents.onPlayerRightClick` 临时接入，调用处已注明后续需重构。右键包仅发布实际状态转变。
- 复用 `LaserAttack` 的碰撞、命中去重、伤害和绘制，以及 `ScheduledFuncHelper`、`ChargeBarSync`、现有圆环贴图。没有新增触发类型、触发模块、递归模块或执行效果。
- 资源：`revelation.png`、`en_us.json` 中的中文名称及短行说明、`angel.json`、`greed_angel.json`。物品模型由 `runData` 生成。

## 已执行验证（上一轮首版实施）

- `git diff HEAD --check` 通过。
- Java 17.0.11（Microsoft）执行 `gradlew.bat runData --offline --no-daemon`：`BUILD SUCCESSFUL in 2m 6s`。只执行了 `runData` 及其自动依赖任务，没有额外调用 compile、build、JUnit、GameTest 或客户端任务。
- 初次启动被 Gradle 用户缓存路径及沙箱锁文件权限阻止；本次命令显式指定现有 `C:\Users\16136\.gradle`，经沙箱授权后完成，没有修改项目 Gradle 配置。
- 数据生成的全部 provider 完成，`run-data/logs/latest.log` 无 ERROR、Exception 或终末天启资源错误。输出 `src/generated/resources/assets/isaac_disaster/models/item/revelation.json` 指向 `isaac_disaster:item/revelation`。
- 已有 `SoulStateEffect` 弃用警告、Gradle 弃用提示及 Forge `union:` 资源协议警告仍存在。
- 从英文 Wiki 下载 `https://bindingofisaacrebirth.wiki.gg/images/Collectible_Revelation_icon.png?b1e304`；PNG 文件签名、IHDR 和 32×32 尺寸通过校验，已打开图像核对。SHA256：`0cdf2f8db2209e88315d543123bb57ff0bf17e0c0945436581061790f85b3909`。图标下载成功，无缺失的非图像资源；首版没有新增音效。
- 两个道具池分别有且只有一个终末天启条目，均无权重。初版道具本地化键位于 `"item end"` 前，使用计划中经字宽检查拆分的 8 个短行。

## 当前工作区说明（本轮整理核对）

- `Revelation.getDesc` 当前按三条专用 lore、动态飞行说明和公共魂心奖励说明（2）顺序返回；保留用户当前文案与顺序，本轮未修改。
- 道具本地化位于 `"item end"` 前，套装键位于原有套装本地化区。
- 上述说明简化发生在首版数据生成验证之后；本轮修正后的 `runData` 已重新通过。当前说明字宽与实际客户端排版仍待验收。

## 初版静态核对（历史记录）

- 能力仅每件增加／移除 1 单位 `FLY_TIME` 和 1 个天使套装贡献；魂心只在现有首次获得生命周期通过 `PlayerHelper.giveItem` 发放。
- 未调用 `StatManager.addAttackType` 或 `StatManager.addBulletColor`；没有修改主攻击候选或选择器。
- 独立状态只保存在服务端 UUID 映射中，不写 NBT、ItemStack 或玩家主攻击蓄力字段。每次同步明确使用 `ModChargeBars.REVELATION.getId()`；白色栏优先级为主攻击栏减一。
- 46 次有效玩家 tick 不满足释放条件，47 次满足；重复同状态右键包不重置状态或再次释放。
- 释放时建立新基础上下文，记录伤害、射程、尺寸与方向，不继承弹数、轨迹、分裂、追踪或弹体触发器。逐次发射只更新起点，使用 `ABILITY_EXTRA` 和 `EXECUTE_ONLY`。
- 下一玩家 tick 才建立定时任务；第一次在该 tick 的服务端 END 执行，随后每 2 tick 一次，共 15 次，即相对释放 tick 的 1、3、…、29。每次使用新的激光命中集合。
- 清理只使用当前玩家 UUID 和 `revelation_attack` 任务类型，并通过状态身份检查拒绝调度器延迟删除期间的旧回调。
- 再按右键、失去最后一件、死亡、退出、换维度、失去持头或攻击资格均清空独立状态、隐藏独立栏并取消该玩家的独立光束。服务端停止时清空所有终末天启状态。

## 待实际游戏验收（本轮未执行）

所有项目均尚未在客户端或游戏服务器中验证；静态核对与数据生成不替代这些测试。

| 场景 | 预期 |
| --- | --- |
| 46／47 tick 释放 | 46 tick 清零；47 tick 松开释放；再次按下可以开始新蓄力，但不会终止上一束 |
| 普通泪弹／蓄力主攻击 | 原主攻击继续，终末天启独立充能和释放，两个栏进度及颜色隔离 |
| 转身、移动 | 无可控泪弹时方向固定；有可控泪弹时方向随视角变化，起点随玩家移动；可控状态在释放时记录 |
| 单个高血量目标／多个目标 | 单次判定对每个目标最多命中一次，总共最多 15 次基础伤害判定 |
| 敌人和实心方块 | 贯穿敌人，穿过方块仍可命中后方目标 |
| 释放前后改变伤害、射程和尺寸 | 伤害与尺寸在下一次释放采用新属性，已释放光束保留原属性；终末天启射程始终为 64 格，不受射程属性影响；射速不改变 47 tick 蓄力 |
| 弹数、追踪、轨迹、分裂及弹体命中道具 | 保留对主攻击的作用，终末天启保持独立基础光束 |
| 重复获得和移除 | 不增加光束数、伤害或蓄力速度；飞行贡献逐件增加／扣除；失去最后一件清空蓄力及 HUD，已发射光束继续完成 |
| 首次获得和重新装备已用道具 | 新道具给予 2 个魂心物品，不直接吸收；已有首次标记的道具不再奖励 |
| 三件同名终末天启 | 天使套装贡献达到 3，显示门槛和暂无额外效果 |
| 蓄力资格丢失／光束持续 | 死亡、退出、换维度、切走头、获得无泪症、进入旁观会清理蓄力及 HUD；已发射光束完成 15 次判定后才消失 |
| 死亡、退出或换维度后的原维度 | 请另一玩家留在发射维度观察；光束保留上次有效回调的起点和方向，在原维度继续碰撞、伤害与显示，不转移到新维度 |
| 共用蓄力重置 | 普通／终末天启同时蓄力时死亡、退出或换维度，两个实际值、全部同步栏及按住标记都清空；未来按 ID 存取状态的栏自动纳入 |
| 停服后同进程重开 | Schedule 活动任务、待添加／待删除队列和玩家索引均空；没有旧世界光束或其他任务继续执行 |
| 图标、普通／Shift 说明和蓄力栏 | 图标正确，短行排版无异常，金色独立圆环与主攻击圆环同时可读；未蓄力不显示，蓄力时可见十字，释放／取消后隐藏 |
| 扩展充能栏 | 新增栏自动遵守正进度且持 IsaacHead 的统一规则；只注册样式与优先级，使用现有同步方法提供进度，无需注册条件或进度来源 |
| 发射音效 | 满蓄力释放时播放一次，15 次伤害判定不重复播放；与硫磺火及普通激光比较实际听感 |

## 本轮修正资源与静态核对
- 2026-10-09 固定射程：终末天启 `getRange` 返回 `AttackContext.MAX_RANGE`（64.0），基础上下文读取该方法、15 次判定沿用快照；不读取玩家射程属性。Microsoft Java 17.0.11 的 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 36s`，provider 318 ms；日志无 ERROR／Exception，静态和差异格式检查通过。实际游戏验收未执行。
- 2026-10-09 调度简化：删除 `firstTick`／`scheduleAtTick`，使用已有相对延迟 `schedule`；不再要求释放后严格下一 tick 首发，首发可在释放当 tick END 发生。保留 2 tick 间隔、15 次、不可中断和停服清理。Microsoft Java 17.0.11 的 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 41s`，11 tasks（4 executed、7 up-to-date），provider 483 ms；日志无 ERROR／Exception，静态检查及差异格式检查通过。只执行 runData 及其自动依赖，未执行游戏验收。

- Schedule／公共生命周期最终验证：Microsoft Java 17.0.11，`runData --offline --no-daemon` 为 `BUILD SUCCESSFUL in 1m 48s`；11 个任务（4 executed、7 up-to-date），provider 538 ms，日志无 ERROR／Exception。静态核对 Attack 无事件监听，共用重置清实际值与 HUD，停服清四个 Schedule 容器，首发与次数保持。差异格式和 25 个链接通过；实际游戏及同 JVM 重开未验收。
- 最新结构：`RevelationAttack` 管理累计／释放／HUD／光束回调，无 `SubscribeEvent`。实际蓄力和同步缓存归入共用玩家能力；`ChargeBarEvents` 统一清理死亡／退出／换维度；Schedule 统一停服清理。右键／tick／道具移除仍直接调用同一注册攻击实例。
- 合并验证：Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`，`BUILD SUCCESSFUL in 41s`，11 个任务（4 executed、7 up-to-date），provider 180 ms，日志无 ERROR／Exception。静态核对 47 tick、2 tick／15 次、可控及不可中断规则保持；差异格式与 21 个链接通过。实际游戏回归未执行。
- 统一显示规则最终验证：`ChargeBarType` 恢复仅样式和优先级，所有栏在 Overlay 统一检查主／副手持 IsaacHead 与进度大于 0；无需注册显示条件或进度来源，既有同步接口保留。Microsoft Java 17.0.11 的 `runData` 通过（1m 2s，provider 351 ms），无 ERROR／Exception；差异格式及 24 个链接检查通过。实际客户端未验收。
- 最新控制／HUD 修正：可控状态释放时记录，在每次 Schedule 回调更新起点和方向；无可控时保持释放方向，脱离玩家后保留上次有效回调方向，不中断剩余判定。
- 只有充能 tick 的正进度会同步可见；未开始、释放、取消和资格丢失都隐藏。最新统一规则由 Overlay 对全部栏执行：正进度且主／副手持 IsaacHead。此前的注册谓词已移除，类型仅保留三参数样式构造器，网络协议与进度同步接口不变。
- 最新 `runData`：Microsoft Java 17.0.11，`BUILD SUCCESSFUL in 1m 44s`，11 个任务（4 executed、7 up-to-date），provider 总耗时 563 ms，日志无 ERROR／Exception。未额外运行 compile／build／游戏测试。
- 本次脚本、预览、运行日志已从磁盘及待提交区删除；历史子项目保留。24 个相对链接及差异格式检查通过，实际游戏与客户端验收仍未执行。
- 后续高光修正：十字增加淡黄白高光、浅金主体和深金阴影，圆环内部白色填充，去掉挤占白色区域的内部描边；外圈像素及外部透明区域保持一致。已查看更新后的多进度／多倍数预览，实际客户端显示仍待验收。
- 高光最终资源以 Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 51s`，全部 provider 完成（476 ms），无 ERROR／Exception。生产 PNG 与构建资源 SHA256 相同，像素检查与差异格式检查通过。仅更新纹理、生成脚本、预览和规划记录。
- 已查看 temp 参考图；独立 12×12 PNG 保持现有外圈像素、外部透明，中央金色十字与白色内部背景，充能 `0xFFFFD75A`。曾查看空／部分／满环预览；临时预览已按用户要求删除，实际客户端视觉仍未执行。
- 资源脚本检查通过：原外圈非透明像素全部保持一致、中心金色、角落透明、声音注册条目正确，生产 OGG 与原件字节相同。
- 音效历史测量：原件平均 -20.1 dB，比硫磺火低 6.1 dB，保留原件，以 `0.8` 音量单次播放。独立测量文件已清理；未进行实际游戏听感验收。
- 已发射光束使用独立全局有限任务，不受资格检查或共用蓄力值清理影响；第 15 次后由 Schedule 删除任务并释放光束闭包，停服由 Schedule 清空运行时任务。
- 碰撞、直线视觉批次和伤害类型均通过 `getLaserLevel` 固定到发射世界；普通激光的默认解析仍为 owner 当前世界。
- 本轮 Microsoft Java 17.0.11 执行 `gradlew.bat runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 55s`，11 个任务（5 个执行、6 个 up-to-date）。全部 provider 完成，耗时 428 ms；日志无 ERROR／Exception。没有额外调用 compile／build 或客户端任务。
- HUD 与音效生产资源和 `build/resources/main` 副本 SHA256 一致；`git -c core.safecrlf=false diff HEAD --check` 通过。实际游戏、客户端视觉和主观听感验收仍未执行。
