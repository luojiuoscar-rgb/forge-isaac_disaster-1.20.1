执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其实现与验证规则。

# 萌死戳的肺执行记录

用户已批准实施。Wiki ID 229；品质 2；追加 MONSTROS_LUNG，不调整已有 ordinal。

- [x] 核对当前委托攻击、ATTACK_PLAN、逐弹准备、充能清理与未提交改动。
- [x] 实现 MonstrosLung、MonstrosLungAttack、MonstrosLungAttackPattern 并注册。
- [x] 添加五行描述、Wiki 32×32 图标、treasure/greed_treasure 条目。
- [x] 静态核对批准行为，执行一次 runData，检查生成资源与 diff。

批准行为：tier 200 / priority 300；剖腹产覆盖；按住蓄力，满蓄力松开一次发射；ceil(4.3 * max(1, D))。
逻辑上下文同源同轴，ATTACK_PLAN 一次；按最终位置/方向去重后每方向展开 min(50, 14 + floor(2.4*(n-1)+0.5) + 12*(c-1))。
15° 半角均匀立体角散射；逐弹准备；委托 baseAttack，保留武器身份和效果；泪弹最终大小乘 0.90–1.33 与 GRAVITY 0.05；激光固定发射变换。
获取/移除对称管理攻击、BLOOD_TEAR；按用户后续纠正移除肺自身的 FIRING_MODIFIER 注册，相关道具自行管理它；无套装、奖励、额外持久数据、声音资源或新模块。
验证范围仅 runData 与静态核对，不运行独立编译、build、JUnit、GameTest 或客户端。

后续用户要求：剖腹产＋肺 Rule 返回 C_SECTION（200,301），硫磺火＋肺 Rule 返回 BRIMSTONE（200,302）；移除肺的 isActive 特例。肺 onReleased 拆为规划、方向分组、散射展开、弹丸配置四个私有方法。
新增 BRIMSTONE_MONSTROS_LUNG TriggerModule，由肺获取/移除对称管理；监听既有 ATTACK_PLAN，使用 BrimstonePlusMonstrosLung IExecutableEffect，仅向 PLAYER_PRIMARY 的硫磺火计划追加光柱，不增加 TriggerType。
用户已确认额外光柱每次发射共 3～5 道，均匀随机整数、平面内 360° 随机方向；平面随瞄准倾斜；整轮固定发射变换并沿用硫磺火充能、伤害、宽度、射程及 13 次调度。每道从未准备的新上下文创建，在逐弹准备时独立判定中毒、颜色等状态；不复制主光柱已判定结果。
