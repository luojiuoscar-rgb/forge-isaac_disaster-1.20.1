# 发现

- 历史记录包含多轮修复与未执行客户端验收；应核对当前源码，不能把旧记录中的失败直接当作现存bug。
- 审查对象涵盖水迹、圣光、撕裂苍穹、圣光泪弹、终末天启视觉、火之意志、宝瓶座/室女座、双鱼座击退、D10与天王星。
- 实际全量JUnit：771项、2失败。AquariusSynergyTest:26期望持有伙伴后显示协同，当前Aquarius已无对应override；ZodiacLoreTest:20仍断言旧字句“每0.5秒造成330%面板伤害”，当前为“水迹每次造成330%面板伤害”。.planning/pisces/progress.md已记录用户修改描述后遗留这两项失败，因此先作为测试/需求不同步而非擅自认定生产逻辑应恢复。

## 审查时发现的问题

1. 分支测试债务：AquariusSynergyTest.java:26依赖已删除的协同文案，ZodiacLoreTest.java:20硬编码旧描述。两项从双鱼座记录延续至当前，阻止全量test通过。应按最新需求更新或移除过时用例，不能仅为旧测试恢复用户已精简的说明。
2. 共同祖先以前的运行问题：src/main/resources/data/isaac_disaster/loot_tables/chests/isaac/red_chest.json:8引用isaac_disaster:ref/soul_heart_2，当前资源中不存在对应表。红箱实体实际使用该表，选中此条目时无法生成预期的两个魂心。运行GameTest时LootDataManager重复报告Unknown loot table。git show确认a8555626已含相同引用；历史归于f1f76092，因此不是feature-c新增回归。

## 两轴审查

- Standards：独立规范审查未发现必须修复的硬性规范违规或确定运行时缺陷；未因个人抽象偏好增加建议。
- Spec：独立需求审查未发现确定的功能缺漏或范围扩张。遵循后续决定：CTS散布0.75×nearbyRange、自然调度和移除近期排除，终末天启无预警圈且使用圆柱，火之意志降低燃烧表现约束，D10使用Mob，天王星直接传冻结施加者且删除EFFECT_SOURCE。
- 客户端真实多人同步、视觉、性能压力及完整存档重载未在本轮验证；测试通过不能排除这些表面的缺陷。

## 本轮验证

- Microsoft Java 17.0.11，test --offline --no-daemon：160类771项，769通过、2失败、0错误及跳过。日志build/feature-c-history-review-junit.log。
- runGameTestServer --offline --no-daemon，11个相关命名空间：158项必需GameTest全部通过，Gradle退出0。日志build/feature-c-history-review-gametest.log。
- 日志中的Deliberate callback failure来自GroundEffectGameTests故意抛错的隔离回归，测试通过；不作为生产缺陷。d10_test_nested缺标签警告来自重载测试恢复，未作为正式数据错误。
- git diff --check及git diff --cached --check通过。未运行runData、独立compile或build；未修改生产代码及测试，也未提交。

## 用户授权后的修复

- red_chest.json中的缺失引用已改为直接物品条目，生成isaac_disaster:soul_heart ×2；保留原权重10及其他条目。JSON解析及字段检查通过。
- 已取消的协同说明对应AquariusSynergyTest已删除，不恢复旧需求。ZodiacLoreTest匹配当前水迹描述，室女座翻译示例的上限改用50%。
- 未新增测试类或修改道具机制；本轮使用现有全量JUnit和相关GameTest核验。
- 最终验证：Microsoft Java 17.0.11，test及runGameTestServer（isaac_disaster_zodiac、isaac_disaster_d10，含真实资源重载），BUILD SUCCESSFUL，退出0。159类770项JUnit全部通过，28项必需GameTest全部通过。日志build/feature-c-history-fix-validation.log中未再出现Unknown loot table或loot table validation problem。
- 验证范围：红箱JSON解析、直接条目数量/权重检查及真实服务器加载/多次资源重载通过；未实际手动开红箱抽样，未执行客户端、多人或完整存档验收。未运行runData、独立compile/build；不提交代码。
