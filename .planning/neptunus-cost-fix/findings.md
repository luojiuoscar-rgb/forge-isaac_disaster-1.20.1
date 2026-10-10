# 调查记录

- `NeptunusAttack` 当前使用 15 个释放系数；`getCoolDownTicks` 每次从第一个区间开始累加，`getTotalCharge` 每次调用 `Arrays.stream(COEFFS).sum()`。
- `TrajectoryMotion` 是 record，字段访问器为 `rangeCost()`；同时提供 `chargedDistance(double)`，没有 `cost()`。
- `GravityTrajectoryModule` 对普通直线步进返回 `rangeCost = movement.length()`，测试期望值 1 应直接读取 `rangeCost()`。
- 本轮保持已有浮点转整数和释放系数语义，避免改变蓄力边界；只缓存总系数与累计系数，减少重复计算并让区间选择更直接。
