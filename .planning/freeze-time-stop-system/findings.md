# 冻结与时停系统发现记录

## Requirements

- 金化、石化、冻结都作用于普通 Mob；玩家和 Boss 由效果基类的约束处理。
- 金化/石化：停止 AI、自主输入和跳跃，但继续 `travel`，保留自然重力、碰撞和药水/死亡等基础 tick。
- `frozen`：第一版使用独立效果来源和冰壳；低地面摩擦，保留外部速度，可以被攻击推动和滑动；不受伤害但允许推动。
- 时停：只停止目标 Mob 的 AI 和 `travel`，不暂停服务器、世界、方块、红石、网络或非活体实体；时停来源玩家和普通玩家不应因来源条件被误冻结。
- 时停同阵营排除由配置 `time_stop_exclude_friendly`（代码字段 `TIME_STOP_EXCLUDE_FRIENDLY`）控制，并通过 `EntityHelper.isFriendly(...)` 在服务端判断。
- 时停玩家只锁定位置和速度，不锁定 yaw/pitch；客户端只接收同步冻结结果，不接收时停来源或阵营数据。

## Source Map

### 状态与规则

- `src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/EntityFreezeRules.java`
  - 组合 `EntityVisualState.isFrozen(entity)` 和 `TimeStopState.isTimeStopTarget(entity)`。
  - 提供 `shouldFreeze`、`shouldClearHorizontalMotion`、`shouldCancelKnockback`、`usesLowFriction`。
- `src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/state/EntityVisualState.java`
  - 读取实体 `ExtraData` 的冻结来源和视觉层。
  - `isFrozen` 表示 `freezeSources` 非空，不包含时停。
- `src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/state/TimeStopState.java`
  - 服务端：`SERVER_SOURCES`、`SERVER_PLAYER_POSITIONS`。
  - 客户端：`CLIENT_PLAYER_POSITIONS`、`clientLevel`。
  - 服务端清理 API：`clearServerPlayerSnapshots(UUID)`、`clearServerLevelState(Level)`、`clearServerState()`。
  - 客户端清理 API：`clearClientPlayerSnapshots()`。
- `src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/state/FrozenImpactState.java`
  - 只允许 `ServerLevel` 操作状态表；客户端立即返回。

### 效果与事件

- `effect/custom/EntityFreezeEffect.java`：冻结效果基类，维护来源、视觉状态、实体资格和静音行为。
- `effect/custom/GoldenEffect.java`、`PetrifiedEffect.java`、`FrozenEffect.java`：具体效果来源和视觉层。
- `event/effect/FreezeEffectEvents.java`：Forge Bus 的冻结/时停共享逻辑，包括攻击、伤害、击退、Living tick、生命周期和服务器 tick。
- `event/effect/FreezeEffectClientEvents.java`：客户端 Forge Bus 的位置快照和客户端退出清理。
- `event/ClientModEvents.java`：仍负责客户端 Mod Bus 注册逻辑，包括 renderer layer 注册；不要把注册本身迁移到事件逻辑类。

### Mixin 与客户端

- `mixin/LivingEntityMixin.java`
  - 注册 `SynchedEntityData` 布尔字段，重定向 `travel` 地面摩擦；`frozen` 使用固定低摩擦 `0.989F`。
- `mixin/MobMixin.java`
  - `Mob.tick` HEAD 在服务端更新自身时停字段。
  - `Mob.aiStep` HEAD 仅对时停目标取消。
  - `serverAiStep` 继续使用统一冻结规则处理金化/石化 AI。
- `accessor/LivingEntityFreezeAccess.java`
  - 是生产代码访问 Mixin 同步字段的契约，必须位于 `mixin` 包之外。
- `renderer/layer/frozen/FrozenShellGeometry.java`、`FrozenShellLayer.java`
  - 按实时碰撞盒逐格生成六面冰壳；不拉伸冰贴图，不依赖纹理 repeat。
- `client/item_related/EntityRenderFreeze.java`
  - 缓存普通 Mob 的冻结姿态；时停 Mob 复用该表现，玩家不接入 Mob 姿态缓存。

## Behavior Matrix

| 来源 | AI | `travel`/重力 | 外部水平速度 | 伤害 | 击退 | 视觉来源 |
|------|----|---------------|--------------|------|------|----------|
| 金化/石化 | 停止 | 继续，保留重力/碰撞 | 自主水平运动清除 | 正常 | 现有冻结规则 | Capability source + layer |
| `frozen` | 停止 | 继续，低摩擦 | 保留，可被推动滑动 | 取消，不红闪 | 允许推动 | Capability source + ice shell |
| 时停 Mob | 停止 | 跳过，不推进重力 | 不清零，结束后继续 | 基础 Living tick 仍执行 | 取消 | SynchedEntityData + pose |
| 时停玩家 | 不取消完整 tick | 位置快照复位，速度清零 | 位置固定 | 普通玩家逻辑继续 | 由现有规则处理 | SynchedEntityData + client position snapshot |

## Race Condition Root Cause And Fix

- 根因：集成服务器同时存在逻辑服务端线程和逻辑客户端线程；旧实现的时停/冻结状态清理方法操作同一静态 `HashMap`，客户端 tick 可能在服务端 `computeIfAbsent` 或遍历时修改表，导致 `ConcurrentModificationException`。
- 修复：`TimeStopState` 的服务端方法只操作 `SERVER_SOURCES` 与 `SERVER_PLAYER_POSITIONS`；客户端只通过 `clearClientPlayerSnapshots()` 操作 `CLIENT_PLAYER_POSITIONS` 与 `clientLevel`。不使用 `ConcurrentHashMap` 或全局锁。
- `FrozenImpactState.tick`、`recordAttack`、`clear` 均以 `ServerLevel` 为边界；客户端事件不创建、不读取、不清理碎裂状态表。

## Event Bus Boundaries

- 通用 Forge Bus：`FreezeEffectEvents`。
- 客户端 Forge Bus：`FreezeEffectClientEvents`。
- 客户端 Mod Bus：注册仍在 `ClientModEvents`，只将相关逻辑辅助内容按需要复用；不要为了分类而重复注册 layer。
- 增加事件文件不会造成额外 tick 性能开销；性能由实际监听次数和循环复杂度决定，不由文件数量决定。

## Known Boundaries And Deferred Work

- 当前没有解决“冻结效果结束后恢复实体原始静音状态”的问题；效果基类的静音行为暂时保留。
- 没有为所有第三方实体的声音、动画或落地事件做通用冻结兼容；这属于实体自身实现差异。
- `FrozenImpactState` 不持久化、不同步客户端；碎裂结果由服务端音效/粒子/实体同步传播。
- `frozen_shatter` 使用正常 `hurt` 死亡流程以保留掉落、经验和归属，不使用 `discard()`。
- 玩家冻结坐标快照是瞬态状态，不写入存档；世界切换、死亡、退出、世界卸载和服务器停止必须继续清理。
- 没有完整的游戏内自动化测试；`test` 任务可能为 `NO-SOURCE`，真实碰撞、多人同步、第三方实体兼容仍需人工验证。

## Resources And References

- Forge 版本：1.20.1 / 47.4.9。
- Mixin 配置：`src/main/resources/isaac_disaster.mixin.json`。
- 时停配置：`src/main/java/net/luojiuoscar/isaac_disaster/Config.java` 和 `client/config/IsaacConfigCatalog.java`。
- 损伤资源：`src/main/resources/data/isaac_disaster/damage_type/frozen_shatter.json` 及 `data/minecraft/tags/damage_type/bypasses_*.json`。
