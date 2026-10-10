# 海王星释放计算与 GravityTrajectoryTest 修复计划

执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其实现与验证规则。

## 目标

- 优化 `NeptunusAttack` 的释放间隔计算，保持当前基于玩家原始射击延迟的释放语义。
- 修复 `GravityTrajectoryTest` 对已不存在的 `TrajectoryMotion.cost()` 的引用。
- 保留工作区中其他未提交修改，不扩展到无关攻击或轨迹行为。

## 阶段

- [x] 检查海王星当前计算、`TrajectoryMotion` API 与测试调用方。
- [ ] 预计算海王星释放区间和总系数，移除重复运行时求和。
- [ ] 将测试改用 `rangeCost()`，并静态检查无旧 `cost()` 调用。
- [ ] 按技能执行一次 `runData --offline --no-daemon`，检查差异格式和生成日志。

## 验收

- 释放系数序列、充能总量和低射速连续模式行为保持不变。
- 测试断言读取 `TrajectoryMotion.rangeCost()`。
- 不运行额外 compile、build、JUnit、GameTest 或客户端任务。
## 阶段记录

- 预计算阶段：完成。
- 测试 API 修复阶段：完成。

- [x] 预计算海王星释放区间和总系数，移除重复运行时求和。
- [x] 将测试改用 `rangeCost()`，并静态检查无旧 `cost()` 调用。
- [blocked] `runData --offline --no-daemon`：Wrapper/Gradle 用户目录权限与离线插件缓存限制，详见 progress.md。
- [x] 按用户要求停止，不再运行 `runData`。
