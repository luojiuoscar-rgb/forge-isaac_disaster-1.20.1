# 源码与已确认规则
## 残留复核决策
- hasChargeAmount区分未记录与显式0值，是独立的运行时状态查询，不能仅因无人调用删除；保留。
- getBulletTypeMap和getAttackTypes重复提供拥有计数，统一后getAttackTypes返回Map.copyOf不可变快照，删除前者。
- ScheduledFuncHelper.getPlayerTasks提供任务观测价值，AttackContext的状态构建/复制查询也有领域含义；本轮保留。
- positionFromCenter是明确旧测试兼容方法，生产路径不调用，两个旧测试只验证已废弃的实体底部转换；与旧方法一并删除，不把私有实现公开给测试。
- 无参充能访问仍是生产依赖，应先迁移所有调用再删除；诅咒眼效果从已确定的CursedEyeAttack取得栏ID。
- 现有 PlayerAbility 将选择结果拆为四个字段，best_attack_type 另存档；拥有计数存于 bullet_types。
- DelegatingAttackType 当前已防止嵌套委托，但依赖低于自身优先级搜索。
- Revelation 持有状态目前通过每tick扫描注册表发现；获得/移除未注册攻击拥有计数。
- Technology2 通过 RIGHT_CLICK_TICK -> SHOOT_LASER 运行，该模块/效果无其他生产消费者。
- 用户确认 TammysHead、SadBomb、LokisHorns 独立发射应使用最高基础攻击。
- Revelation 当前 startBeam 在释放中调度，每个脉冲经 AttackExecutor；本轮移至完整 performAttack。
- SplitExecutor 与轨迹调试命令需要单次激光执行，保持该用途和现有公共入口。
- 游戏测试及反射测试包含 LaserAttack 内部实现引用，抽象拆分需要检查这些消费者。

## 实施后的职责
- AttackSelection包含mainCandidate/baseCandidate及不可变附加列表；选择沿用tier/priority/requiredCount/id/rule排序。
- PlayerAbility只存结果缓存，在初始化/加载/复制/登录/重生重建；复制传入新玩家进行isActive检查。
- 获得/移除统一增减拥有计数；更新缓存只清理被替换/移除的充能，不调用Schedule清理。
- Technology2保留原来持头按住右键每tick的PREPARE_AND_EXECUTE单束发射，无主攻击冷却门槛。
- Revelation用EXECUTE_ONLY发动完整光柱；内部脉冲freeze后performLaserBatch，保留旧维度作用域和通用伤害事件。
- 客户端协议、绘制、数值、资源及道具池均未在本轮修改。

## 抽象类语义复核（2026-10-10）
- 当前BulletAttack已包含makeSound实现，AbstractBulletAttack中已删除音效；保留该现有修改。
- getAttackContexts的普通多弹阵型与performAttack的立即完整发射策略属于具体攻击；移回BulletAttack/LaserAttack。Technology2保留单束阵型，显式实现立即批量执行；Revelation已有独立完整调度。
- createOptimizedState中的0.4前移、200tick寿命上限、0.2碰撞尺寸、64控制范围和0.8转向是普通泪弹定义，移回BulletAttack；AbstractBulletAttack只要求子类构建状态，再统一发射事件和运行时提交。CSection仍继承BulletAttack的状态基础并覆写。
- getWidth的0.25基础宽度属于普通激光/Technology2定义，移至两类；AbstractLaserAttack保留抽象宽度约定和laserWidth计算工具。Brimstone/Revelation已有独立宽度实现。
- 保留在抽象父类：根身份、单发执行、LaserProjectile、批处理/快扫/轨迹/碰撞/命中去重、通用激光伤害来源和伤害提交、世界获取、通用粒子/同步实现及对应扩展钩子。这些属于家族运行机制，不选择完整攻击阵型/持续调度/音效/平衡参数。
- 当前继承关系不变：Haemolacria/CSection沿用普通泪弹；Brimstone沿用LaserAttack阵型，Technology2/Revelation直接继承抽象激光类。
- 追加视觉语义检查：未指定颜色时的红色是具体激光定义，抽象类不应硬编码。新增抽象getDefaultLaserColor，由LaserAttack/Technology2返回原红色、Revelation返回自身白色；统一内部RGB解析，保留颜色注册和渲染/包机制。Revelation仍关闭粒子绘制，其显式白色上下文和光柱渲染不变。
- 跟踪算法原来复用了BASE_LASER_WIDTH作为转向标定分母；这不是各攻击实际基础宽度。保留其0.25算法标定并改名HOMING_REFERENCE_WIDTH，避免具体宽度迁移后混淆或改变现有转向行为。

## CSection动态充能需求
- onTick外层charge < getTotalCharge导致射速变化后需求降低至当前值或以下时，既不能发射也不再充能。
- 持头按住时应始终处理：charge+1 >= total则发射，否则增加充能；总需求本tick只读取一次，使用long加法避免溢出。
- 本项目无Mockito，直接单测onTick需要真实ServerPlayer/capability/Forge事件环境；不为两行条件迁移添加只复刻表达式的测试或扩大生产接口。改为静态边界核对，游戏验收记录需求20→15/20→20及正常循环。
