# 发现

- 当前分支 feature/feature-c；已有黏着、鼻窦炎和用户未提交改动。
- 当前脱落实现使用 FALLING 独立阶段，仅重力、最多200tick、不耗原射程、不再碰实体；需按本轮需求改为带重力的正常飞行。
- 用户已将 AttackContext 的部分负 tick 异常改为静默修复；BulletState.Builder、BulletStickState 仍有负数 throw。
- 本轮创建的待迁移文件：docs/bullet-sticking.md、docs/sinus-infection.md、docs/superpowers/plans/2026-10-19-bullet-stick.md。
- 将检查渲染器、网络退出可见范围时间戳与许可、BulletRuntime 同步状态、BULLET_STICK 上下文及客户端运行时。

## 脱落设计
- 恢复碰撞前保存的速度，暂停期间不重置轨迹模块状态；脱落后运行同一飞行/碰撞/追踪管线，并向既有 TrajectoryRuntime 添加 Gravity spec。
- 以 DETACHED 表示重新飞行的来源状态（isFlying=true），可以再次进入 STUCK；取消独立 fallingMotion、200tick下坠计时和专用落地分支。
- 沿用剩余射程而非黏着期间增长的总 age；飞行轨迹已有射程模式可复用。
- 同步恢复飞行需携带轨迹 runtime、age/traveled/acceleration，并引入附着转换 revision，防止同tick或迟到的上次黏着状态覆盖新周期。
- 异常扫描发现剩余负数 throw 在 BulletState.Builder、BulletStickState 数值快照；AttackContext 方向无效也可回退到默认方向。配置解析错误、未知注册、必需对象等不可恢复错误保留。

## 复核补充
- 首次GameTest 10/11：原索引排除STUCK，恢复首步漏实体。已修复。
- 接触结算保留 currentMotion；分段无显式成本时使用真实段长比例，保存所在接触段的速度。接触段末尾当次伤害杀死宿主时，以revision变化中断旧轨迹段。
- 追踪中的DETACHED继续经过轨迹评估，重力不再被steering路径跳过。
- 晚观察者完整快照先恢复age/traveled再安装stream，并建立防旧修正水位。碰撞回滚保留回调中新加的spec和origin平移。

## 对问题的代码依据
- BulletClientRenderer 遍历客户端轻量子弹流，选择visual/纹理、绘制泪弹billboard或胎儿模型，处理缩放/颜色/光照；黏着用目标同帧插值位置+偏移，目标未解析则保留最后权威位置。
- BulletDespawnBatch 新serverTick为隐藏快照的时序水位；visibilityExit=true表示暂时离开可见范围，false表示真正终止，不允许旧消息复活。
- BulletRuntime新增hidden按世界/玩家保存已离开视野但尚未终止的身份；stickRevisions按世界/子弹保存已同步的转换序号，确保同tick重新黏着即使阶段相同也能立即同步，STUCK另每4tick刷新。
- ClientBulletRuntime新增临时隐藏/终止分流、黏着位置解析、spawn先恢复阶段和计数再校验速度、applyStick批量状态入口；脱落重建完整轨迹继续客户端预测，拒绝旧状态。无客户端玩法效果执行。
- ContextKeys.BULLET_STICK是只读类型化事件数据，包含目标UUID、位置、计时、时长、结束原因，供现有效果读取；不是另一份可变计时器。鼻窦炎周期效果用elapsedTicks取模。
- GravityTrajectoryModule.fallingMotion已删除，恢复原applyTyped半隐式0.05/tick算法；正常轨迹合成和射程处理共同生效。
- 生产及测试所有本功能改动Java文件的非import完整类型名扫描为0。三份docs文件均不存在。AGENTS两条规则已写入。
- 多段接触只有在最后一段与authored velocity一致时才推断中间接触速度；范围裁短等无法可靠还原时保留authored velocity。DETACHED低速允许，NaN/Infinity仍清理。

## 最终行为
普通飞行 → STUCK → EXPIRED移除；宿主失效 → DETACHED（isFlying=true）→普通轨迹+重力+碰撞→可再次STUCK。
黏着期间不耗飞行射程，恢复后使用原剩余射程；每次新的黏着重置该次elapsed并发布独立生命周期。
旧独立FALLING、200tick下坠限制和fallingMotion已移除。同步协议4携带revision及恢复飞行所需轨迹/计数器。
最后一次真实验证14项全部通过，包括二次黏着、镜像、追踪重力、非等长路径、近射程裁短速度回退、致命分段边界碰撞。
