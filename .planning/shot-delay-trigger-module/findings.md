# 本轮发现

- 当前 `ItemEvents.onGetShotDelay` 负责创建 `ExecutableEffectContext` 并 dispatch，需迁移到 `TriggerModuleEvents`。
- `TriggerModuleEvents.onAttackPlan` 使用 `EVENT` 与 `TARGET_POSITION`；射速事件应沿用同一桥接边界，并以 `ServerPlayer` 作为 dispatch entity。
- `FiringModifierRules` 只承载两个 Effect 的公式，应删除并将 helper 放回对应 Effect，收束为 `private static`。
- `AttackType.getBulletCount` 的事件边界已存在，但当前局部变量仍是 `int[]`。
