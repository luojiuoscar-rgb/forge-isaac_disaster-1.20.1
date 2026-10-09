# Progress Log: 终末天启

## 2026-10-08：Schedule 发射与公共蓄力生命周期

- **Status:** complete
- 用户要求直接使用已存在的 Schedule 发射，三个清蓄力监听成为共同行为，光束固定位置在回调中检测，停服清理由调度器统一承担。
- 核对当前 Schedule 静态任务表没有停服清理；不写入存档并不保证同 JVM 再开世界不会残留。
- 方案：能力中按栏 ID 保存实际蓄力及同步缓存，保留普通攻击原 getter/setter；公共监听清空全部栏，攻击仅管理累计／释放／光束回调；保留指定下一 tick 首判定及 2 tick／15 次。
- 已实现共用能力状态与同步缓存，三个 `ChargeBarEvents` 监听调用统一清理，实际蓄力、HUD 和按住标记同时清零；普通攻击接口保留，新增栏可通过按 ID 的方法管理自身状态。
- 已移除 `RevelationAttack` 的全部 `SubscribeEvent` 和私有玩家映射／活动光束列表；直接安排有限全局任务，光束记录只被任务闭包持有，回调判断能否继续跟随。
- Schedule 新增指定首个 tick 的入口，原调用方式保持；统一停服清空任务、玩家索引、待添加与待删除队列。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 48s`，11 个任务（4 executed、7 up-to-date），全部 provider 完成（538 ms），最新日志无 ERROR／Exception。
- 静态核对通过：Attack 无事件监听、活动光束列表或任务取消；普通攻击默认栏与终末天启专用栏隔离；三个公共事件全部调用 `clearAll`；Schedule 停服清空四个容器。下一 tick 首次、2 tick 周期和 15 次保持。
- 差异格式和 25 个相对链接检查通过；没有额外运行 compile／build／游戏测试，实际中断及同 JVM 重开仍待验收。

## 2026-10-08：合并 Controller 至攻击类

- **Status:** complete
- 用户确认全部合并：攻击类负责输入蓄力、HUD、持续光束和清理，不保留独立 Controller。
- 已核对注册和全部调用入口；保留用户当前道具说明、资源及其他已有修改，仅调整状态归属和调用关系。
- 运行时状态迁为注册攻击实例的私有字段，仍按玩家 UUID 隔离；保持蓄力可清理、已发射光束完成 15 次的现有行为。
- 已完成合并及调用入口更新，移除公开的跨类控制查询桥接，起点与单次脉冲执行改为私有；注册类型为 `RegistryObject<RevelationAttack>`，无需外部强转。
- 独立 Controller 已从磁盘及待提交区删除；事件监听移至攻击类，所有监听均转发到同一注册实例。
- 核对时发现用户已清理独立音效测量文档并调整普通说明；更新规划中的失效链接和现状记录，保留其源码修改。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 41s`，11 个任务（4 executed、7 up-to-date），全部 provider 完成（180 ms），最新日志无 ERROR／Exception。
- 静态核对通过：Controller 源码、Git 索引条目和旧 class 产物均不存在，无旧源码引用；外部四个入口均调用同一注册实例。47 tick、2 tick 间隔／15 次、可控快照、不可中断与 HUD 规则均保留。
- 差异格式和 21 个相对链接检查通过。未额外执行 compile／build；实际游戏回归仍未执行。

## 2026-10-08：统一充能栏显示规则

- **Status:** complete
- 用户明确不注册显示条件：所有栏统一按正进度与持 IsaacHead 显示，充能来源使用既有方法提供。
- 本轮取消上轮注册谓词方案，修改范围仅充能栏类型、注册和 Overlay，以及本子项目记录。
- 已移除显示谓词和四参数构造器，`ChargeBarType` 恢复原三参数样式结构；Overlay 统一检查主／副手持头，再筛选进度大于 0 的栏，不枚举 ID。
- 进度同步与获取接口均保持原样；不引入新的注册项或充能来源回调。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 2s`，11 个任务（4 executed、7 up-to-date），全部 provider 完成（351 ms），无 ERROR／Exception。
- 静态核对通过：注册谓词已移除、Overlay 无 ID 枚举、类型与原版 HEAD 无内容差异。差异格式和 24 个相对链接检查通过；客户端验收未执行。

## 2026-10-08：可控方向、按需显示与临时文件清理

- **Status:** complete
- 原实现固定方向，且主动排除可控；当前要求改为释放时记录可控泪弹状态，并在持续期间更新视角方向。
- 原 Overlay 中枚举 ID 是为本地立即隐藏持头充能栏；改为注册可选显示谓词，保留即时隐藏且无需新增 ID 分支。
- 用户明确保留历史子项目，清理范围仅本次临时文件；本轮继续使用道具创建与文件规划技能，仅运行 `runData`。
- 已实现释放时记录可控状态，每 tick 更新方向并用于后续判定；脱离玩家后保留最后方向，光束仍完成 15 次。
- 已取消空闲时可见零进度同步，释放／取消隐藏；普通与终末天启栏的持头条件注册为谓词，Overlay 不再枚举 ID。三参数注册 API 保留，默认不限制显示。
- 已删除 `render_charge_assets.py`、`charge_preview.png`、`runData-highlight.log` 并移出待提交区；Git 首次写索引被拒绝，经授权后完成。历史子项目、生产图标与用户 temp 素材保留。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 44s`，11 个任务（4 executed、7 up-to-date），全部 provider 完成（563 ms），最新日志无 ERROR／Exception。
- 静态核对：唯一可见同步路径在 charge 增至正数后；各释放／取消路径隐藏；Overlay 无普通／终末天启 ID 分支；可控快照只在跟随玩家时更新方向，不改变剩余光束寿命。
- 清理核对：三个临时文件在磁盘和 Git 待提交区均不存在，24 个相对链接有效，差异格式检查通过。游戏内视角控制与 HUD 仍待实际验收。

## 2026-10-08：十字高光与白色背景

- **Status:** complete
- 已查看原图，准备为十字增加中心高光和金色阴影，内部空白区域改为白色，外圈与外部透明区域保留。
- 初次读取推测的绘制类路径不存在；后续使用文件搜索定位，不重复猜测路径。
- 已更新生成脚本、HUD 纹理及预览；查看 0／25／50／100% 和 1×／3×／7× 预览，十字高光与内部白色均可见。
- 像素核对通过：12×12、原外圈非透明像素一致、角落透明、内部白色、中心高光与底部阴影正确。
- `runData` 首次因缓存锁文件权限失败，经授权后通过（23s）；预览中描边挤占白色区域，去掉内部描边后再次验证最终资源：`BUILD SUCCESSFUL in 51s`，11 个任务（4 executed、7 up-to-date），全部 provider 完成，476 ms。
- 像素检查曾误选描边坐标，随后预设白色像素阈值过高；最终按实际几何核对通过，白色区域为 8 像素，外圈非透明像素完全保留。
- 最终资源 SHA256 为 `5f6dd85ce81933260a6085d4a81fdd89dc8263bfedde7f76e0dd2eb493963f9d`，与 `build/resources/main` 一致；最新数据日志无 ERROR／Exception，差异格式检查通过。
- 没有修改 Java、音效或道具行为；Phase 7 complete，实际客户端视觉仍未验收。

## 2026-10-08：用户提出三项修正

- **Status:** complete
- 已核对两个 temp 素材并查看充能参考图，确认当前控制器将蓄力与光束共同清理。
- 方案：独立光束寿命与原维度；蓄力状态清理不影响已发射光束；金色填充与中央十字底图；释放音效单次播放。
- 保留现有未提交修改，包括文案简化及无关计划文件删除。
- 只按道具技能运行 `runData`；实际客户端、听感与游戏验收单列。
- 已将蓄力、HUD 同步缓存和光束记录分开；光束以 15 次有限全局任务运行，不再检查玩家资格或取消旧光束。
- 已统一激光碰撞、伤害及视觉批次的世界解析，终末天启固定在发射维度，普通攻击保留默认解析。
- 已生成金色十字底图与 0／25／50／100% 预览，保留原圆环所有非透明像素；资源检查通过，实际客户端仍待验收。
- 音效测量低于现有硫磺火，保留原件并以 `0.8` 播放音量单次接入；详见 `audio_comparison.md`。
- 资源检查脚本初次因 PowerShell 管道将中文文件名变成问号而失败，改为在 Python 内枚举原 OGG 路径后检查通过。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 55s`，11 个任务，5 个执行、6 个 up-to-date。全部 provider 完成，耗时 428 ms；最新日志无 ERROR／Exception。
- HUD 和音效生产资源与 `build/resources/main` 副本 SHA256 一致；`git -c core.safecrlf=false diff HEAD --check` 通过。
- Phase 6 完成；Phase 5 的实际游戏、客户端视觉与主观听感验收仍为 pending。
- 收尾核对：27 个 Markdown 相对链接有效；差异格式检查通过；技能阶段检查报告 5／6 complete，唯一 pending 为实际游戏验收。再次查看金色十字预览，空环中心与各进度均可见十字。

## 2026-10-08：首版实施（本会话上一轮）

- **Status:** complete
- 按已批准的道具计划完成被动能力、独立攻击、控制器、注册、套装、图标、池和本地化。
- 从英文 Wiki 获取图标并校验 PNG 签名与 32×32 尺寸。
- 使用 Microsoft Java 17.0.11 完成 `gradlew.bat runData --offline --no-daemon`，结果 `BUILD SUCCESSFUL in 2m 6s`。
- 进行静态核对和 `git diff HEAD --check`，创建 `verification.md`。
- 未运行 JUnit、GameTest、客户端或实际游戏验收。

## 2026-10-08：planning-with-files 子项目整理

- **Status:** complete
- 已读取技能原文件及三份模板，并检查现有子项目、源码和 Git 变更。
- 已建立 `task_plan.md`、`findings.md`、`progress.md`，保留原 `verification.md`。
- 核对当前普通说明为 4 条，记录与初版 8 条拆分方案的区别，并更新 `verification.md` 的历史／现状说明。
- 在 `findings.md` 补齐已批准参数、边界、完整修改清单、源码入口、复用方式、资源证据和后续验收。
- 子项目文件为 `task_plan.md`、`findings.md`、`progress.md`、`verification.md`；Phase 1–4 完成，Phase 5 待实际游戏与视觉验收。
- `session-catchup.py` 尚未执行，原因是当前 PATH 未找到 Python 运行时；已用会话和当前工作区手工核对。
- 本轮不运行 Gradle；历史数据生成结果按原时间与范围保留。
- 检查四份文档均存在、非空，18 个相对链接全部有效；阶段计数为 4 个 complete、1 个 pending。
- 技能完成检查以单次 PowerShell 子进程运行，报告 4／5 阶段完成、1 阶段待完成，与真实游戏验收状态一致。
- 对本轮记录前后的 19 个已有源码／资源变更文件进行 SHA256 比较，内容全部保持一致；本轮只修改 `.planning/revelation/` 文档。

## Test Results

| 检查 | 结果 | 证据范围 |
| --- | --- | --- |
| 初版 `runData` | 成功 | 上一轮实施，Java 17.0.11；详见 `verification.md` |
| 初版静态核对与 diff 检查 | 通过 | 上一轮实施，不代表游戏运行结果 |
| 三项修正后 `runData` | 成功 | Java 17.0.11，1m 55s，全部 provider 完成 |
| 修正后资源核对 | 通过 | 外圈像素、透明背景、金色十字、音效注册及文件哈希 |
| 上一轮工作记录完整性 | 通过 | 四份非空文件、18 个有效相对链接、4／5 阶段完成 |
| 上一轮文档整理时源码与资源保留 | 通过 | 19 个既有源码／资源文件 SHA256 前后相同 |
| 子项目差异格式检查 | 通过 | `git diff HEAD --check -- .planning/revelation` |
| 实际游戏和视觉验收 | 未执行 | `task_plan.md` Phase 5 为 pending |

## 2026-10-09：简化首发调度（Phase 12）

- 用户取消严格相差 1 tick 的首发要求，授权删除 `firstTick`。
- 删除 Schedule 绝对 tick 字段、API、内部重载和 tick 检查；终末天启改用已有全局 `schedule`。
- 静态核对保留间隔 2 tick、15 次、全局任务不覆盖，以及停服公共清理。源码无 `firstTick`／`scheduleAtTick`，差异格式检查通过。
- 核对当前 Laser 和 Brimstone 方法覆盖，记录继承原因。Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 41s`，11 tasks（4 executed、7 up-to-date），provider 483 ms；日志无 ERROR／Exception，静态检查和差异格式检查通过。游戏验收保持未执行。
- 技能读取首次路径拼写错误后使用目录表中的正确路径；`python` 本轮仍不可用，catchup 使用提供的会话摘要、磁盘记录和 Git 状态恢复。

## 2026-10-09：固定最高射程（Phase 13）

- 用户指定终末天启固定 64 格射程；通过本攻击类覆盖 `getRange` 返回 `AttackContext.MAX_RANGE`，上下文创建及后续快照沿用该值。
- 更新当前行为与射程属性变化的验收预期；此次不涉及说明文字／贴图变化。
- Microsoft Java 17.0.11 执行 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 36s`，11 tasks（4 executed、7 up-to-date），全部 provider 完成（318 ms），日志无 ERROR／Exception。固定射程及上下文路径静态检查、差异格式检查通过；实际游戏验收未执行。

## 2026-10-09：实心光柱首版（Phase 14）

- 用户批准上一轮调研方案的首版制作。新增客户端光束 Renderer 和专用 S2C 包，独立 UUID、有限寿命、原维度及跟随／可控状态。
- Attack 仍通过原有限 Schedule 执行 15 次伤害，仅同步视觉状态；客户端每帧跟随有效玩家，其他情况插值服务端快照，并在退出／卸载时清理。
- 几何为 12 边白色封闭内核、带顶点透明度渐变的淡金色光晕及起点光环；轻微光晕波动。POSITION_COLOR shader 不依赖环境光，无新增贴图或 Bloom。
- 第一人称仅调整可见起点，碰撞与 64 格射程不变。两个 Laser 粒子入口通过默认开启钩子控制，终末天启关闭。
- 只读 javap 核对 Forge 1.20.1 的 RenderStateShard 与 Entity 客户端插值方法。
- Microsoft Java 17.0.11 的 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 46s`，11 tasks（4 executed、7 up-to-date），全部 provider 完成（720 ms）；日志无 ERROR／Exception。仅运行 runData 及其自动依赖。
- 静态核对两条粒子入口、默认开启／终末天启关闭、47 tick 蓄力／2 tick 间隔／15 次／64 格／无 Attack 监听或任务取消、独立 ID／有限寿命／维度检查／深度测试及差异格式，均通过。新增网络包编码解码顺序已核对，追加注册不改变既有包 ID。
- 实际客户端、光影组合和多人游戏验收未运行；首版亮度、第一人称裁剪与平滑表现需按 verification.md 的新增场景验收。

## 2026-10-09：降低发射起点（Phase 15）

- 对用户报告的遮挡按当前代码核对：旧 0.27 格眼睛偏移小于 0.5 格基础内核半径。
- 统一专用起点方法，偏移改为当前身高 45%，站姿再降低约 0.54 格；服务端释放和跟随、客户端插值均调用同一方法。
- Microsoft Java 17.0.11 的 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 57s`，11 tasks（4 executed、7 up-to-date），全部 provider 完成（592 ms），日志无 ERROR／Exception。两个服务端入口与客户端共同起点、旧偏移已移除、差异格式静态核对通过。仅执行 runData 及其自动依赖。
- 第一／第三人称、蹲下和游泳姿态的实际遮挡待游戏验收。

## 2026-10-09：光柱优化（Phase 16）

- 用户授权简化寿命同步、视锥裁剪与增强光晕，并考虑专用直线碰撞；保留已确认无遮挡的起点与 32 格范围。
- 已恢复实时源码与当前未提交改动，继续在 `.planning/revelation` 记录，不新增临时图标、日志或历史子项目。
- 已完成源码修改：包的结束标记与末次同步、独立客户端计时与超时、视锥裁剪／缓存／光晕增强、单束查询覆盖钩子。
- Microsoft Java 17.0.11 的 `runData --offline --no-daemon`：`BUILD SUCCESSFUL in 1m 53s`，11 tasks（4 executed、7 up-to-date），provider 532 ms。检查本次 `run-data/logs/latest.log`（19:42:24）无 ERROR／Exception，生成模型纹理路径正确；只有已有 API／Gradle 弃用及启动环境警告。
- 差异格式、包编码解码顺序、末次标记、世界切换清理、47 tick／2 tick／15 次／64 格、无主攻击候选或 Attack 事件监听变更、默认批次开启／终末天启关闭与空批次回退静态核对通过。确认 Forge 渲染事件提供 `getFrustum`，裁剪包围盒覆盖最大光晕。
- 没有单独执行 compile／build／测试／客户端任务；runData 自动依赖按 Gradle 原配置执行。没有新增临时图标脚本、预览或独立运行日志。实际光晕、多束、网络抖动和性能改善待游戏验收。
- 路径搜索误设了 `bullet/projectile/LaserProjectile.java`；用 `rg --files` 的类名子串重新定位，避免依赖 Windows 反斜杠结尾正则。

## Error Log

| 问题 | 处理 |
| --- | --- |
| Gradle 用户缓存锁文件路径／权限 | 上一轮修正命令环境并获沙箱授权后成功 |
| curl／PowerShell 图标连接失败 | 上一轮改用 Node fetch 成功 |
| 本轮 Python 命令不可用 | 记录 catchup 脚本未运行，改用已有会话及实时 Git 差异恢复 |
| 本轮 JSON 摘要解析失败 | Git 换行提示混入输出；分离 stderr 后成功记录 19 个源码／资源文件摘要 |
| 本轮 PowerShell 技能脚本被执行策略阻止 | 使用单次子进程执行策略完成只读检查，未改变系统策略 |
| 中文素材路径经 PowerShell 管道变成问号 | 改为 Python 内枚举原 OGG，资源核对通过 |

## 5-Question Reboot Check

| 问题 | 答案 |
| --- | --- |
| 当前在哪里 | Schedule 发射与公共蓄力生命周期完成；Phase 11 complete，Phase 5 pending |
| 下一步是什么 | 按 Phase 5 和验证矩阵做实际游戏与视觉验收 |
| 目标是什么 | 维护可恢复的终末天启需求、实现、证据与剩余验收 |
| 已发现什么 | 见 `findings.md`，包含光束独立生命周期、金色十字资源及响度比较 |
| 已完成什么 | 见本文件执行记录与 `verification.md` |
