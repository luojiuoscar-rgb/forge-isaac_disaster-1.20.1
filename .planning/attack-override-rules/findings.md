# 调查与决定

- AttackSelector 先按 tier，再按 priority、组合需要数量排序；组合结果同时参与 main/base 选择。
- 硫磺火 (100,200) 已高于海王星普通候选和激光组合，但低于诅咒眼 (200,0) 与海王星诅咒眼组合 (200,1)。
- 剖腹产 (0,200) 低于诅咒眼；现有激光组合 (100,100) 同样不足。
- 新增 BRIMSTONE_CURSED_EYE_COMBO (200,200)、C_SECTION_CURSED_EYE_COMBO (200,100)，仅在对应两种攻击拥有且 active 时产生候选。
- 两条规则保留硫磺火与剖腹产同时存在时硫磺火更高；原血泪剖腹产组合 (300,0) 保持优先。
- 不改任何攻击的充能/发射逻辑，不实现剖腹产海王星自动攻击。
- AttackSelector 的排序键为 tier 降序、priority 降序、requiredAttackCount 降序；AttackPrio 现已按前两项从低到高排列，因此越靠后的枚举优先级越高，枚举 ordinal 不参与选择。
- 剖腹产/海王星协同的关键事实：PlayerAbility 当前只把 mainAttack 和 additionalAttacks 分发 tick；剖腹产成为 main 后海王星不会自动执行。CSection 仅在 holdingRightClick 时增加 charge，满充后自身发射；Neptunus 的非快速模式仅在未按住右键时增加自己的 charge。
- 已采用组合结果 `CSectionNeptunusAttack`：直接继承 CSection，仅将充能门槛从“按住右键”改为“满足资格即持续充能”，并让按下/释放不清空充能；满充后的事件、请求、声音、弹体属性和 CSection 特殊运行路径均复用父类。
- 组合攻击的注册键仍独立用于选择器候选，但攻击实例的 `getId()` 返回 `ModAttackTypes.C_SECTION`；因此弹体、TrackingProfile、碰撞冷却、客户端胎儿渲染和 LaserPlusFetus 无需新增 `isCSectionType` 分支即可自然复用。
- 开始时 git 仅有两份用户技能文件修改，必须保留。
- 2026-10-10：新增海王星＋血泪组合规则，结果为 Neptunus，优先级 `(100,400)`，高于血泪 `(100,300)`；其余更高 tier 的诅咒眼组合及血泪＋剖腹产组合仍按原规则竞争。
