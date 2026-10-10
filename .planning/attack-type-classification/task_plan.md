执行时继续使用 isaac-disaster-item-creation 和 planning-with-files 技能；仅运行 runData 验证。

# AttackType 分类、结果缓存与附加攻击重构

## 目标与决定
- 基础、委托、附加分别使用 BasicAttackType、DelegatingAttackType、AdditionalAttackType 标记。
- 抽出无分类标记的 AbstractBulletAttack 和 AbstractLaserAttack。
- AttackSelection 缓存主候选、基础候选与去重后的附加列表；基础/委托共用排序，附加不参与排序。
- 委托及直接生成弹体的衍生效果使用 baseAttack；身份判断使用 mainAttack。
- 玩家状态统一分发输入/tick；攻击自行管理充能与完整攻击，不增加 Controller。
- 删除 IndependentCharge 及 Technology2 旧专用触发路径。
- Revelation.performAttack 调度完整 15 次光柱，内部脉冲不重新进入流水线。
- 保留 47tick、64射程、2tick间隔、13段硫磺火、客户端预测和全部资源/数值。
- 已释放光柱不被玩家状态清理打断；保留历史计划、暂存区及其他现有修改。

## 阶段
1. 共用实现与分类接口：complete
2. 选择结果与消费者迁移：complete
3. 统一分发及获得/移除：complete
4. 终末天启完整攻击入口：complete
5. 测试源码、静态检查、runData：complete
6. 抽象类语义复核（2026-10-10）：complete
   - 抽象类只保留家族身份与单发/碰撞/轨迹/批处理机制。
   - 普通阵型、完整攻击策略、音效、泪弹状态参数、激光基础宽度移至具体类。
   - 保留用户已移回BulletAttack的音效实现及全部其他现有修改。
   - 逐项静态核对，再仅执行一次runData --offline --no-daemon。
7. CSection动态充能需求修复：complete
   - 移除阻止已满/超额充能处理的外层条件；每tick读取一次总需求。
   - 保留按住连续发射、重置、取消事件和充能规则。
   - 静态核对下降/相等/临界/未满边界，仅运行runData。
   - 验证：Microsoft Java17.0.11，runData --offline --no-daemon成功（2m7s），providers445ms，written0；日志无ERROR/Exception，diff --check通过。
   - 未运行JUnit/GameTest或客户端；游戏验收仍待执行。

## 残留清理与显式充能接口（2026-10-10）
- 状态：complete。继续使用isaac-disaster-item-creation/planning-with-files，保留其他现有修改。
- 删除仅测试使用的旧充能更新/包构造签名、无效复制赋值和无内容TODO。
- 将普通蓄力攻击及诅咒眼效果迁移到自身getChargeBarId，删除默认栏ID重载。
- 合并重复攻击表访问，getAttackTypes返回不可变快照；保留有独立含义的hasChargeAmount、调度查询与AttackContext接口。
- 删除旧弹跳坐标兼容方法和只验证旧契约的测试，不扩大生产可见性或新增测试便捷入口。
- 仅运行一次runData --offline --no-daemon，检查资源/日志/diff；不运行JUnit/GameTest或客户端。
- 验证完成：Microsoft Java17.0.11，runData退出码0、BUILD SUCCESSFUL in 38s；providers257ms、written0，日志无ERROR/Exception，资源无差异，两个diff --check通过。测试源码已迁移并增加不可变快照案例，未运行测试/游戏/视觉验收。

## 验证规则
- 只执行 runData --offline --no-daemon，使用 Microsoft Java17；检查生成日志及 git diff --check。
- 更新测试源码但不执行 JUnit/GameTest；不启动客户端，不运行独立 compile/build。
- 人工游戏验收：组合/委托、附加共存、46/47tick、15次判定、重复计数、中断、衍生攻击与各蓄力行为。

## 最终验证
- runData --offline --no-daemon：成功，Microsoft Java17.0.11，BUILD SUCCESSFUL in 2m；自动依赖compileJava完成，未单独运行编译任务。
- providers644ms，written0；latest.log无ERROR/Exception，模型/资源无新增错误。
- git diff --check及缓存区检查通过；旧缓存/IndependentCharge/Technology2旧效果符号无残留。
- 抽象父类与原实现进行去除身份/构造器差异后的静态文本比较，泪弹/激光共用逻辑一致。
- JUnit/GameTest测试源码已更新，但未编译或运行；游戏/视觉验收未执行。

## 抽象类语义复核验证（2026-10-10）
- 普通泪弹阵型/完整攻击/状态构建归BulletAttack；普通激光阵型/完整攻击/音效/基础宽度归LaserAttack；Technology2显式定义完整攻击/基础宽度，具体激光类型自行定义默认颜色。
- 抽象类无音效/阵型/完整攻击策略实现，移回的方法体静态比对一致，继承和分类保持不变。
- Gradle首次wrapper启动因C:\.gradle锁目录失败（任务未执行）；指定已有用户缓存后仅完成一次runData --offline --no-daemon，Microsoft Java17.0.11，BUILD SUCCESSFUL in 1m 34s。
- 数据生成providers316ms，written0；最新日志无ERROR/Exception，资源无差异；工作区及暂存区diff --check通过。
- 测试源码增加Technology2/Revelation宽度案例，未编译/执行；游戏/视觉验收未执行。
