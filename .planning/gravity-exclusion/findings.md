# 调查结果

- 当前 `GravityTrajectoryModule.appliesTo` 只排除非 bullet 根类型和 `noGravity`，没有排除追踪/可控状态。
- `applyTyped` 只排除 laser 和 `noGravity`；增加同样的属性判断可防止状态已经挂载或组合路径调用时重力继续参与。
- `IBulletObject` 已提供 `isHoming()` 与 `isControllable()`，无需新增接口或访问 PlayerAbility。
- 重力由 `TrajectoryEvaluator` 根据模块 `appliesTo` 和轨迹状态执行；排除应放在模块自身，避免全局调度器添加特判。
