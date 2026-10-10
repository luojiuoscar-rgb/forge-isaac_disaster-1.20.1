# 实施记录

- 2026-10-10：检查 `GravityTrajectoryModule`、`IBulletObject`、轨迹评估调用和现有重力测试。
- 确认现有 API 已有 `isHoming()`、`isControllable()`，计划在重力模块两层入口复用。
- 已在 `appliesTo` 与 `applyTyped` 同时排除追踪和可控子弹，并补充组合状态测试源码。
- 静态核对确认两层排除均存在；runData 成功（Java17.0.11，43s，providers242ms、written0），日志无 ERROR/Exception，资源无差异，diff --check 通过。
- 未运行 JUnit/GameTest、客户端或游戏验收；保留工作区其他已有修改。
