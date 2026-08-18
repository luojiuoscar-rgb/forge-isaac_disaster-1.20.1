# 冻结与时停系统归档

## Goal

记录当前 Forge 1.20.1 模组中金化、石化、冻结效果、冰壳、撞击碎裂和时停系统的最终架构、关键决策、已修复问题与后续扩展边界，供后续对话直接恢复上下文。

## Current Phase

归档完成。当前对话的主要实现任务已结束；后续如继续开发，应先阅读本目录的三个文件。

## Phases

### Phase 1: 冻结语义与共享基础设施

- [x] 用 `ExtraData.freezeSources` 表示实体自身的冻结效果来源。
- [x] 保留 `EntityVisualState.isFrozen(entity)` 表示是否存在任意实体冻结来源。
- [x] 用 `ResourceLocation` 区分 `golden`、`petrified`、`frozen` 来源。
- [x] 将共同效果逻辑抽到 `EntityFreezeEffect`，具体效果只提供来源和视觉层。
- **Status:** complete

### Phase 2: 冻结移动与冰壳表现

- [x] 金化/石化冻结 AI 和自主输入，但继续执行 `travel`，保留重力与碰撞。
- [x] `frozen` 效果保留外部水平速度，使用低地面摩擦，允许推动和滑动。
- [x] 攻击冻结 Mob 时取消伤害，并通过 `LivingAttackEvent` 立即施加纯水平推动。
- [x] 添加按实时碰撞盒生成的透明冰壳，不拉伸冰纹理，支持普通 Mob 和 Slime renderer。
- **Status:** complete

### Phase 3: 冻结撞击碎裂

- [x] 使用服务端瞬态 `FrozenImpactState` 保存速度快照和最后一次推动来源。
- [x] 水平撞墙或硬着陆达到阈值后触发正常伤害死亡流程、冰块破坏音、冰粒子和击杀归属。
- [x] 使用 `frozen_shatter` 伤害类型与原版伤害标签绕过护甲、抗性、冷却和无敌帧。
- [x] 死亡/死亡动画期间排除再次记录碰撞或重复碎裂。
- **Status:** complete

### Phase 4: 时停接入与服务端权威

- [x] 删除旧的独立时停 `LivingTickEvent` 取消路径。
- [x] 时停来源只由服务端按维度缓存拥有 `THE_WORLD` 的玩家 UUID。
- [x] 服务端 `END` 阶段刷新来源；每个 Mob 在自身 `Mob.tick` 开始时更新同步冻结字段。
- [x] `serverAiStep` 不跳过状态更新，避免时停结束后永久冻结。
- [x] 时停 Mob 在 `Mob.aiStep` HEAD 跳过 `travel`；金化/石化不跳过 `travel`。
- [x] 玩家只锁定位置和速度，保留普通 tick、朝向和视角；时停来源玩家显式免疫。
- **Status:** complete

### Phase 5: 竞态修复、整理与验证

- [x] 将服务端与客户端时停玩家快照清理接口分离。
- [x] 修复集成服务器中服务端与客户端共同操作 `HashMap` 造成的 `ConcurrentModificationException`。
- [x] 清理 Git 暂存区，使已暂存内容等于工作树；未跟踪规划和笔记不进入暂存区。
- [x] 完成干净编译与测试检查。
- [x] 将本对话上下文归档到本目录。
- **Status:** complete

## Key Questions For Future Sessions

1. `EntityVisualState.isFrozen` 只代表实体效果冻结；时停状态不写入 `freezeSources`，而是读取同步实体字段。
2. 时停、`frozen`、金化/石化的移动优先级是否仍应保持 `时停 > frozen > 金化/石化`？若修改，必须同时检查 `MobMixin`、`LivingEntityMixin`、冻结事件和客户端姿态冻结。
3. 任何需要客户端展示的时停结果都应优先复用 `SynchedEntityData`；不要重新引入逐实体自定义状态包或客户端阵营判断。
4. 继续拆分事件时，注册位置保持在原有 `ClientModEvents` 等注册类；只将逻辑监听迁入按 Forge Bus/客户端 Forge Bus 分类的事件类。

## Decisions Made

| Decision | Rationale |
|----------|-----------|
| 冻结来源存入 `ExtraData.freezeSources` | 支持多个效果共存、独立移除，并保持 `isFrozen` 的稳定语义 |
| `EntityFreezeRules` 只组合规则，不保存状态 | 避免把查询规则和瞬态缓存混在一起 |
| 时停来源按维度缓存 UUID | 服务器只需扫描玩家，Mob 自己按 tick 计算最终状态 |
| 使用 `LivingEntity` 的 `SynchedEntityData` 传递时停结果 | 客户端只读取最终布尔值，不接收来源、不做阵营判断，也不增加自定义状态包 |
| 不设置 `NoAI` | 避免改变实体持久属性和第三方实体兼容边界 |
| 不取消完整 `LivingTickEvent` | 避免受伤无敌帧、药水计时、死亡状态异常；时停只在 `Mob.aiStep` 阶段跳过移动行为 |
| `FrozenImpactState` 只允许服务端读写 | 碰撞、归属、音效、粒子和碎裂伤害都是服务端权威，避免客户端线程并发修改服务端 `HashMap` |
| 服务端/客户端快照清理方法分开命名 | 防止生命周期事件误清理另一端的静态状态 |

## Errors Encountered

| Error | Attempt | Resolution |
|-------|---------|------------|
| PowerShell 中 `python` 命令不存在 | 1 | 改用绝对路径的 bundled Python runtime |
| Windows `py -3` 启动器不存在 | 2 | 改用 `C:\Users\16136\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe` |
| 集成服务器中出现 `ConcurrentModificationException` | 1 | 隔离 `FrozenImpactState` 和 `TimeStopState` 的服务端/客户端状态访问边界 |
| Git 暂存区出现 `AM`/`AD` 和旧路径新增 | 1 | `git restore --staged -- .` 后使用 `git add -u` 与显式路径暂存；未跟踪内容单独移出暂存区 |

## Notes

- 当前工作区的 `.planning/` 与 `codex/` 笔记默认不应被自动加入提交，除非后续明确要求。
- 不要把 `isaac_disaster.refmap.json` 当作手写源文件；它由 Mixin annotation processor 在构建时生成。
- `LivingEntityFreezeAccess` 位于 `accessor` 包，不应放在 `mixin.*` 包内并被生产代码直接引用，否则可能触发 `IllegalClassLoadError`。
- 继续开发前先检查当前源码和 Git 状态，不要假设旧对话中的暂存区状态仍然存在。
