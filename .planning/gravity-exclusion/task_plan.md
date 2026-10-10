执行时继续使用 isaac-disaster-item-creation 和 planning-with-files；为重力轨迹模块增加追踪/可控子弹排除，仅运行 runData 验证。

# 重力模块显式排除

## 范围
- `GravityTrajectoryModule` 对 `isHoming()` 或 `isControllable()` 的子弹不生效。
- 在 `appliesTo` 和 `applyTyped` 两层显式防护，避免已有状态或组合调用重新施加重力。
- 补充重力模块测试源码；不运行 JUnit/GameTest。
- 保留当前工作区其他修改，不调整追踪、可控或轨迹调度模块。

## 阶段
1. 检查重力模块和轨迹评估入口：complete。
2. 增加显式排除及测试源码：complete。
3. 静态核对和一次 `runData --offline --no-daemon`：complete。

## 验证规则
- 仅执行 Microsoft Java17 环境下的 `runData --offline --no-daemon`。
- 检查最新日志、资源差异与工作区/暂存区 `git diff --check`。
- 未执行测试、客户端或游戏验收必须明确报告。

## 验证结果
- `appliesTo` 与 `applyTyped` 均包含 `isHoming()`、`isControllable()` 排除；测试源码覆盖追踪、可控和两者组合。
- `runData --offline --no-daemon`：Microsoft Java17.0.11，BUILD SUCCESSFUL in 43s，providers242ms，written0。
- 最新日志无 ERROR/Exception，资源无差异，工作区/暂存区 `git diff --check` 通过。
- 未运行 JUnit、GameTest、客户端或游戏验收。
