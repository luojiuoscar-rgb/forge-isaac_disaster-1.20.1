# Findings & Decisions: 终末天启

## 当前修正规则（覆盖下文初版生命周期与白色 HUD 规格）

- 最新光柱优化已完成：末次伤害包标记结束，独立客户端 tick 插值及超时清理，视锥裁剪／圆周渐变缓存／增强光晕，终末天启直接查询扫掠 AABB 并复用现有快速直线碰撞。`runData` 通过（1m 53s），详见下文光柱性能修正；本轮实际客户端／多人／性能验收未执行。
- 最新公共生命周期要求已实施：各栏实际蓄力值按 ID 归入玩家能力，共用死亡／退出／换维度清理，无需注册获取函数；终末天启直接通过 Schedule 发射，光束自身回调检查是否还能跟随玩家。Schedule 统一停服清理静态任务表，避免同一 JVM 重开世界残留。最新 `runData` 通过（1m 48s），实际游戏未验收。
- `AttackType` 派生类继续管理累计／释放／光束执行行为，独立 Controller 已删除。最新公共生命周期要求将实际蓄力与同步缓存归入玩家能力；光束状态只由 Schedule 闭包持有，攻击类没有事件监听。实际游戏未回归。
- 最新要求：有可控泪弹时支持视角控制；沿用硫磺火的释放时快照方式，记录可控状态，存活且仍在发射维度时按当前视角更新，脱离后保存最后方向。无可控时继续固定释放方向。
- 最新 HUD 要求：所有充能栏统一在正进度且主／副手持 IsaacHead 时显示；取消上一阶段的注册谓词。`ChargeBarType` 仅记录样式与优先级，进度由既有同步方法提供，Overlay 不枚举 ID。
- 用户确认只清理本次临时文件，保留其他历史子项目；删除生成脚本、预览与运行日志，同时移出 Git 待提交区。生产纹理和原 temp 素材保留。
- 新增视觉修正：用户要求十字中心高光和白色背景。对照原图后已将白色填充限定在圆环内部，外圈形状及外部透明区域保留；金色十字增加淡黄白高光、浅金主体与深金阴影。预览已更新并查看，像素核对通过。
- 用户要求已发射终末天启不因任何玩家操作或状态中断，必须完成攻击后结束。蓄力资格检查与已发射光束生命周期需分离。
- 修正前控制器 `clear` 同时删除状态和定时任务；`onPressed`、死亡、退出、换维度、失去资格均能触发该清理，是旧中断行为的来源。
- 光束锁定发射时维度，玩家在原维度且存活时起点跟随；离开该维度、死亡或退出后保留最后起点完成剩余判定。基础激光当前从 owner.level() 解析碰撞、绘制与伤害世界，需要一致固定这些路径。
- `codex/temp/终末天启充能.png` 是带背景的截图参考：暗色轮廓、浅黄色环、中央金色十字。HUD 使用既有 12×12 圆环外圈加金色十字，不直接复制截图背景。
- 充能颜色改为金色；早期曾常驻显示空环，已由最新按需显示规则覆盖。光束颜色继续白色。
- 提供了 `codex/temp/终末天启.ogg`，应每束释放播放一次，不能每次伤害判定重播。先与现有 `laser_shot.ogg`、`brimstone_normal.ogg` 测量比较，再决定衰减。
- 已发现本机 `D:/Program Files/FormatFactory/ffmpeg.exe` 与 `FFModules/python/python.exe`，可用于本轮资源检查。
- 当前 `PlayerAbility.chargeAmounts` 按栏 ID 保存实际蓄力，`chargeBarProgress` 保存共用同步缓存。`clearCharge` 仅清该栏的实际蓄力与 HUD，不取消光束；光束由全局有限任务持有，不保留攻击类活动列表，不受玩家蓄力清理或再次按下影响。
- 每束记录发射时 `ServerLevel` 和属性快照。`LaserAttack.getLaserLevel` 提供统一的碰撞／伤害／绘制世界入口；普通激光仍使用拥有者当前世界，终末天启在每次基础上下文执行时固定发射世界，并在 finally 恢复执行环境。
- 玩家在原维度且存活时起点继续跟随，即使切走头、失去道具或攻击资格；死亡、退出或换维度后固定最后起点。伤害来源仍保留原玩家归属。
- HUD 底图为 `revelation_charge_base.png`，12×12、外部透明、内部白色填充；原环所有非透明像素保持一致，中央金色十字带淡黄白高光与深金阴影。填充色 `0xFFFFD75A`，保留通用满蓄力闪白；中心十字保持独立纹理。
- 曾生成并查看 0／25／50／100% 与 1×／3×／7× 预览；用户要求清理后，生成脚本和预览已删除。生产 PNG 保留，实际客户端仍待视觉验收。
- 原音效平均 -20.1 dB、峰值 -6.2 dB，低于现有硫磺火；保留原文件，以 `0.8` 音量播放一次。独立测量文件已清理，数值保留于本记录。

## Requirements

- 来源为本会话已批准的“终末天启首版实现”计划。
- 用户本轮明确调用 `planning-with-files`，要求将本次修改整理到 `.planning` 子项目文件夹。
- 已有目录 `.planning/revelation/` 仅包含 `verification.md`，缺少技能约定的三份工作文件。
- 当前无 `.planning/.active_plan` 文件；本子项目可通过 `PLAN_ID=revelation` 或明确指定计划路径定位。
- 上一轮维护子项目文档；本轮实施用户追加的三项修正，保留其他已有源码改动。

## Research Findings

- 当前源码包含终末天启被动能力、管理完整生命周期的激光派生攻击和天使占位套装；独立服务端控制器已删除。
- 首次奖励为 `PlayerHelper.giveItem(player, ModItems.SOUL_HEART.get(), 2)`；飞行和套装贡献通过 `StatManager` 逐件增加／移除。
- 实际蓄力位于各玩家能力的按栏 ID 映射；普通攻击的无 ID getter/setter 映射到 `attack_charge`，终末天启使用 `revelation`，两者隔离。公共清理一次清空全部 ID、同步缓存及按住标记。
- 当前 `Revelation.getDesc` 返回 `revelation.lore.1` 至 `.3`、动态飞行说明及公共魂心奖励说明。保留用户已调整的顺序与说明，不借合并重写文案。
- 首版 `runData` 成功耗时 2m 6s；本轮三项修正后重新运行，通过，耗时 1m 55s。历史与当前验证分别记录。
- 技能的 `session-catchup.py` 依赖 Python；当前 PATH 未找到 Python 命令，默认安装目录也未发现运行时。本轮按会话、现有文件和 Git 差异恢复上下文。

## 已批准规格与当前实现

| 项目 | 规格／实现 |
| --- | --- |
| 原版身份 | Revelation／终末天启，Wiki ID 643，品质 4；中文 Wiki 无法访问，英文数值与 Minecraft 转换已在原计划中确认 |
| 模组身份 | `ItemId.REVELATION(4)` 末尾追加，ordinal 179；注册名 `revelation`，Java 能力 `Revelation`，攻击 `RevelationAttack`，贴图 `revelation.png` |
| 版本行为 | 使用 Repentance 的魂心奖励；道具池按 Repentance+，不采用其移除魂心奖励的改动 |
| 飞行与奖励 | 每件 `StatManager.FLY_TIME +1`，实际时间来自公共配置；移除扣除贡献；首次获得发放 2 个魂心物品，沿用现有首次标记，不直接吸收 |
| 主攻击边界 | 参考 Technology 2 注册独立攻击，但不加入主攻击候选、不调用 `StatManager.addAttackType`、不修改优先级选择器；普通泪弹和蓄力主攻击保留 |
| 临时接入 | 以撒头 tick 流程在主攻击冷却判断前调用注册攻击实例 `onTick`；现有右键事件调用 `onPressed`／`onReleased`；释放直接安排 Schedule，全局任务不依赖玩家 tick；同状态包不重复发布事件 |
| 蓄力 | 持头按右键蓄力 47 tick／2.35 秒，不受射速影响；不足时释放清零，满蓄力释放单束光束 |
| 光束快照 | 释放时记录伤害、固定 64 格射程、尺寸、初始方向和可控状态；无可控固定方向，有可控随视角转向，脱离玩家后保留最后方向；基础宽度 1 格，沿用激光尺寸计算 |
| 伤害节奏 | 首个后续 tick 开始，每 2 tick 一次，共 15 次，约 1.5 秒；每次基础伤害为释放时伤害的 100%，每次同一目标最多命中一次 |
| 攻击上下文 | 新建基础上下文；`ABILITY_EXTRA` 与 `EXECUTE_ONLY`；跳过攻击规划和准备事件，不继承弹数、追踪、轨迹、分裂或弹体命中模块；保留底层通用碰撞与伤害事件 |
| 穿透与颜色 | 穿透敌人与方块；专用白色光束优先级 0，只设置在终末天启上下文，不改变主攻击颜色；每束释放播放提供的音效一次 |
| 蓄力栏 | ID `revelation`，金色填充、独立金色十字底图，优先级主攻击栏减一；所有栏统一要求正进度且持 IsaacHead，释放／取消隐藏，同步显式使用专用 ID；显示条件与进度获取方式均不注册 |
| 重复与套装 | 重复不增加光束数量、伤害或充能速度；每件仅提供飞行与 1 个 SERAPHIM 贡献，3 件达标，允许同名重复计数，无额外效果，不补绑现有道具 |
| 生命周期 | 已释放光束不因再次按下、失去最后一件、死亡、退出、换维度、不持头或失去攻击资格而中断；完成 15 次判定后结束。资格丢失仍清理蓄力和 HUD。停服清理运行时状态 |
| 验证边界 | 道具修改只运行 `runData`，不额外 compile／build；真实游戏与视觉检查单列，未执行不能标记通过 |
| 范围排除 | 无新触发类型、触发模块、递归模块、执行效果、原版特殊联动或旧配置／存档兼容实现 |

## 源码入口与修改清单

下列路径均相对仓库根目录，Java 文件共同前缀为 `src/main/java/net/luojiuoscar/isaac_disaster/`。

| 文件 | 职责 |
| --- | --- |
| [Revelation.java](../../src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/passive/impl/Revelation.java) | 首次奖励、飞行、套装贡献、移除与说明 |
| [RevelationAttack.java](../../src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/RevelationAttack.java) | 输入蓄力、HUD、直接 Schedule 发射、可控方向及基础激光执行；无独立 Controller、无事件监听 |
| [PlayerAbility.java](../../src/main/java/net/luojiuoscar/isaac_disaster/capability/player/PlayerAbility.java)、[ChargeBarSync.java](../../src/main/java/net/luojiuoscar/isaac_disaster/networking/ChargeBarSync.java) | 按栏 ID 的共用蓄力、同步缓存与一次性清理；不保存或克隆运行时蓄力 |
| [ChargeBarEvents.java](../../src/main/java/net/luojiuoscar/isaac_disaster/event/ChargeBarEvents.java) | 死亡、退出、换维度时统一清空所有栏状态，无 ID 枚举或清理回调注册 |
| [ScheduledFuncHelper.java](../../src/main/java/net/luojiuoscar/isaac_disaster/helper/ScheduledFuncHelper.java) | 相对 tick 调度与共用停服清理；绝对 firstTick 参数已移除 |
| [Seraphim.java](../../src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/set/impl/Seraphim.java) | 三件门槛、无效果的天使套装 |
| `manager/id/ItemId.java`、`manager/id/SetId.java` | 末尾追加道具与套装身份 |
| `item/ModPassiveItems.java`、`registries/ability/passive/ModPassiveAbilities.java` | 道具、能力和数据生成列表注册 |
| `registries/attack_type/ModAttackTypes.java`、`registries/bullet_color/ModBulletColors.java`、`registries/charge_bar/ModChargeBars.java` | 独立攻击、白色光束和金色十字栏注册 |
| [LaserAttack.java](../../src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/LaserAttack.java) | 统一碰撞、伤害与视觉世界入口，支持固定发射维度 |
| [RevelationBeamRenderer.java](../../src/main/java/net/luojiuoscar/isaac_disaster/client/laser/RevelationBeamRenderer.java) | 客户端有限光柱、结束淡出、视锥裁剪与光晕 |
| [RevelationBeamS2CPacket.java](../../src/main/java/net/luojiuoscar/isaac_disaster/networking/packet/laser/RevelationBeamS2CPacket.java) | 独立 UUID、位置／方向／尺寸／跟随状态和末次伤害结束标记 |
| `client/gui/charge_bar/ChargeBarHudOverlay.java`、`registries/charge_bar/ChargeBarType.java` | Overlay 对全部栏统一过滤持头与正进度，不枚举 ID；类型仅记录样式与优先级，已撤销上轮谓词扩展 |
| `sound/ModSounds.java`、[sounds.json](../../src/main/resources/assets/isaac_disaster/sounds.json) | 发射音效注册 |
| [revelation_shot.ogg](../../src/main/resources/assets/isaac_disaster/sounds/revelation_shot.ogg) | 用户提供的原音效，释放时播放一次 |
| [revelation_charge_base.png](../../src/main/resources/assets/isaac_disaster/textures/hud/charge/revelation_charge_base.png) | 12×12 金色十字充能栏底图 |
| `registries/ability/set/ModSetAbilities.java` | 套装注册 |
| `event/ServerTickEvent.java`、`event/IsaacDisasterEvents.java` | 临时调用入口 |
| `networking/packet/SetRightClickC2SPacket.java` | 右键包状态转变去重 |
| [en_us.json](../../src/main/resources/assets/isaac_disaster/lang/en_us.json) | 当前中文名称、普通说明和套装说明 |
| [revelation.png](../../src/main/resources/assets/isaac_disaster/textures/item/revelation.png) | Wiki 32×32 图标 |
| [angel.json](../../src/main/resources/data/isaac_disaster/loot_tables/pools/item/angel.json)、[greed_angel.json](../../src/main/resources/data/isaac_disaster/loot_tables/pools/item/greed_angel.json) | 无权重池条目 |
| `src/generated/resources/assets/isaac_disaster/models/item/revelation.json` | `runData` 生成，指向 `isaac_disaster:item/revelation`，不手写模型 |

## 关键复用与状态边界

- 复用 `LaserAttack` 碰撞／绘制／目标去重、`ScheduledFuncHelper` 全局有限定时调度、`ChargeBarSync` 和既有圆环，不复制激光引擎。
- `AttackContext.copy()` 保留冻结状态；当前逐次发射使用 `snapshot.toBuilder()` 构建可更新位置的上下文，并显式保持释放时最终尺寸。
- 满蓄力释放建立快照并调用现有 `schedule`，间隔 2 tick、初始位置 1、总计 15 次；首发在下一次调度执行时发生，允许释放当 tick 的 END 发射，不再要求绝对服务器 tick。
- 初版曾通过状态对象身份取消旧回调；当前已取消这一光束中断路径，蓄力状态不存在也不影响已发射光束。
- 专用任务类型为 `IsaacDisaster.MOD_ID:revelation_attack`，采用 15 次全局有限任务。玩家的共用蓄力清理不触碰任何攻击任务；停服时由 Schedule 统一清空所有任务、索引及待处理队列。
- 每次伤害回调更新起点／可控方向；失去有效原玩家时冻结上一次有效回调位置和方向，固定发射维度，继续剩余判定。无需在死亡／退出／换维度事件中操作光束。
- 所有新增模组资源位置使用 `IsaacDisaster.MOD_ID`。不保存独立蓄力到 NBT 或 ItemStack。

## 当前说明与布局

当前普通说明按 `revelation.lore.1` 至 `.3`、`StatManager.FLY_TIME.description(1)`、公共 `item.isaac_disaster.action.give_soul_heart`（数量 2）顺序返回。保留用户当前工作区的文案及顺序，区别于首版的 8 条和后来的 4 条方案。套装门槛通过现有 synergy 描述显示，Shift 扩展说明为“天使: 暂无额外效果”。本轮未重做字体测量或客户端视觉验收。

## 图像与浏览证据

- 英文页面：`https://bindingofisaacrebirth.wiki.gg/wiki/Revelation`。
- 图标：`https://bindingofisaacrebirth.wiki.gg/images/Collectible_Revelation_icon.png?b1e304`。
- 上一轮已核验 PNG 签名和 IHDR 的 32×32 尺寸，并实际打开图像检查。
- 本轮再次核对本地图标 SHA256：`0cdf2f8db2209e88315d543123bb57ff0bf17e0c0945436581061790f85b3909`，与上一轮记录一致。
- 上一轮物品图标下载成功；本轮使用用户提供的充能参考和音效，原 temp 文件保留。

## 验证状态与后续工作

- 历史 `runData` 结果为 `BUILD SUCCESSFUL in 2m 6s`；本轮为 `BUILD SUCCESSFUL in 1m 55s`，均运行于 Microsoft Java 17.0.11。本轮全部 provider 完成，耗时 428 ms，最新日志无 ERROR／Exception。
- 环境设置仅作用于命令：Java 17 路径为 `C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot`，Gradle 用户缓存为 `C:\Users\16136\.gradle`。
- 保留已有 `SoulStateEffect` 弃用警告、Gradle 弃用提示及 Forge `union:` 资源协议警告；本轮仅执行 `runData` 及其自动依赖任务，没有额外 compile／build。
- 音效和 HUD 生产资源与 `build/resources/main` 副本 SHA256 相同；差异格式检查通过。
- 下一阶段依 [verification.md](verification.md) 的游戏矩阵逐项验收，记录游戏版本、场景、实际结果和视觉证据；发现问题后再按批准范围修复。
- 当前四条说明及其排版仍待实际验收；临时接入后续重构属于未来范围，不扩入本轮修正。

## 本轮简化调度与继承核对

- `scheduleAtTick` 只有终末天启一个调用者；移除该 API、`firstTick` 字段及内部重载，普通相对倒计时逻辑不变。
- 终末天启使用全局有限任务 `schedule(SCHEDULE_TYPE, HIT_INTERVAL, HIT_INTERVAL - 1, HIT_COUNT, false, callback)`；首发等待一次调度执行，之后每 2 tick 一次，共 15 次。允许同 tick END 首发。
- `LaserAttack.performAttack`／`shoot` 执行当前一次激光，提供碰撞、绘制、命中去重及全额伤害；`BrimstoneAttack` 重写这些方法，安排 13 次、每 tick 一次的玩家任务，并将伤害乘 0.6。
- `BrimstoneAttack` 还实现主攻击蓄力接口、射速相关蓄力及再按键取消。终末天启复用底层激光执行，不继承这些硫磺火规则，避免每个终末天启判定再次安排 13 次发射。

## 实心光柱首版设计（用户已批准尝试）

- 复用世界渲染事件和共享顶点缓冲模式，新增仅客户端的光束绘制；不生成 Minecraft 实体。
- 独立光束 UUID，使用专用同步包明确样式；不按白色或玩家 UUID 判断同一束，支持重叠发射。
- 每次原 Schedule 判定后同步快照和剩余寿命；客户端有玩家实体时平滑跟随，无实体时插值快照，脱离玩家后留在原位置。
- 白色封闭多棱柱内核、淡金渐变光晕、轻微能量波动；全亮几何无需新增贴图或屏幕 Bloom。视觉短暂淡入淡出不影响碰撞宽度和伤害。
- Laser 增加默认开启的粒子绘制钩子，仅终末天启关闭，覆盖直线批量和分段回退两条粒子路径。
- 维度检查、有限寿命、客户端世界卸载／退出清理，避免残留；其他维度的客户端不展示原维度光束。
- 本轮不改道具说明／图标／音效；不引入外部模组依赖或复制最终棱镜未核实的源码。

## 降低起点修正

- 用户游戏反馈首版严重遮挡视野。旧起点眼睛下方 `0.15 × 身高`，站姿约 0.27 格，低于基础内核半径 0.5 格，摄像机接近／进入光柱截面。
- 改为眼睛下方 `0.45 × 身高`（站姿约 0.81 格），相较原版再下移约 0.54 格。
- `RevelationAttack.getBeamOrigin(Entity, float)` 统一释放快照、每次服务端移动锚点及客户端插值跟随；服务端使用 partialTick=1，客户端使用当前帧值。
- 客户端已有第一人称可见起点裁剪保留；不同姿态采用当前包围盒高度，射程／伤害／宽度／方向规则不变。
- 普通激光及硫磺火起点不受该专用方法影响；实际遮挡改善需游戏验收。

## 光柱性能修正（2026-10-09）

- 用户已游戏确认起点不遮挡视野，并接受 32 格联机可见范围；这两项保持。
- `LaserAttack` 已有整条直线扫描，但首次墙体／实体接触后保守回退分段路径；终末天启单束调用仍独立重建 `EntityGrid`。
- 客户端每包重设相对结束时间会受抖动和游戏时间校正影响。采用最后一次伤害包的 `finished` 标记，独立客户端 tick 插值／超时，避免引入服务端时钟估计模块。
- 光晕加强仅调整视觉，视锥包围盒必须覆盖光晕最大半径；加法混合不需要面排序。缓存圆周与固定渐变，保留正常墙体深度遮挡。
- 已实现：`finished` 替代包的 `remainingTicks`，最后一次伤害后通知原维度玩家；客户端忽略未知 UUID 的结束包。收到结束后 2 tick 淡出，重复结束包不延长寿命；活动状态在最后一次更新后 100 个客户端 tick 无包时超时清理。TCP 同一通道有序，不新增序号／时钟估计／额外结束任务。
- 客户端独立 tick 不受世界时间同步跳变影响，单人暂停时停止计时。100 tick 仅为异常断联兜底，不保证超过约 5 秒的网络停顿仍连续显示；正常结束由服务端末次伤害包决定。
- 绘制：世界坐标 AABB 加最大光晕半径 2.6 倍裁剪；复用帧列表和单位矩阵，缓存圆周及 16 带渐变，保留 12 边内核和 8 轴向分段。光晕基准半径 2.1→2.5 倍，强度第一人称 0.12→0.18、其他 0.20→0.32，源环外半径 1.6→1.8 倍、透明度 0.25→0.35；不增加顶点数。
- 碰撞：新增默认 true 的 `LaserAttack.usesCollisionBatch()`，仅终末天启覆盖 false。跳过批次及 EntityGrid 构建，单次直线扫描直接查询扫掠 AABB 的活体目标，再复用原裁剪、命中顺序、去重和伤害事件。命中回退路径仍保留，但无网格时不重建索引；普通激光／硫磺火保持批次模式。
- 当前 PATH 无 Python；沿用会话、Git 差异与已有工作文件恢复，不重复尝试不可用的 catchup 脚本。

## Technical Decisions

| 决策 | 原因 |
| --- | --- |
| 三份工作文件与验证矩阵共存 | 分别提供计划、知识、执行日志和逐项验收信息 |
| 以实时源码记录说明 | 保留用户当前三条专用说明、飞行和公共魂心奖励说明，不覆盖已有改动 |
| 不将历史成功改写为本轮重新通过 | 避免混淆验证时间与源码状态 |
| 不写全局记忆或根目录计划 | 用户明确指定 `.planning` 子项目位置 |

## Resources

- [task_plan.md](task_plan.md)
- [progress.md](progress.md)
- [verification.md](verification.md)
- `C:/Users/16136/.codex/skills/planning-with-files/SKILL.md`
- `.agents/skills/isaac-disaster-item-creation/SKILL.md`
