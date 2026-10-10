执行时继续使用 isaac-disaster-item-creation 和 planning-with-files；按已确认覆盖关系实施，仅运行 runData 验证。

# 攻击覆盖关系修复

## 范围
- 硫磺火覆盖诅咒之眼和海王星，包括同时持有两种委托攻击。
- 剖腹产覆盖诅咒之眼。
- 剖腹产和海王星的自动攻击组合暂不实现。
- 复用组合候选机制，不更改普通攻击优先级、攻击行为、资源、技能文件或历史规划。

## 阶段
1. 核对选择器、优先级与组合规则：complete。
2. 增加两条覆盖组合规则和选择测试源码：complete。
3. 静态核对及一次 runData --offline --no-daemon：complete。

## 验证
- 测试源码覆盖成对、三者/四者并存、海王星诅咒眼组合、现有激光组合及基础候选。
- Microsoft Java 17，仅执行 runData；不单独 compile/build，不运行 JUnit/GameTest/客户端。
- 检查日志、生成资源和 git diff --check；分别报告未执行的游戏与测试验收。

## 后续调整（2026-10-10）
4. 按选择器的 tier/priority 升序重排 AttackPrio 枚举，使越靠后的优先级越高：complete。
5. 通过组合注册剖腹产＋海王星攻击，继承 CSection，仅覆盖自动充能/自动发射：complete。

## 重排验证结果
- `runData --offline --no-daemon` 成功，Microsoft Java17.0.11，BUILD SUCCESSFUL in 55s；数据生成 providers285ms，written0。
- 生成日志无 ERROR/Exception，生成资源无差异；`git diff --check` 与暂存区检查通过。
- 本轮未运行 JUnit、GameTest、客户端或游戏验收。

## 当前实现验证
- 组合攻击继承 CSection，使用独立注册 ID；静态检查和本轮唯一一次 runData 已完成。
- `runData --offline --no-daemon`：Microsoft Java17.0.11，BUILD SUCCESSFUL in 1m33s，providers346ms，written0。
- 最新日志无 ERROR/Exception，生成资源无差异，工作区/暂存区 `git diff --check` 通过。
- 未运行 JUnit、GameTest、客户端或游戏验收。

## 身份复核
- 组合攻击的注册键与运行时攻击身份分离：`CSectionNeptunusAttack.getId()` 返回 C_SECTION，移除所有 `isCSectionType` 兼容判断。

## 身份复核验证结果
- 已确认源码中不存在 `isCSectionType`；五处 C Section 运行判断均恢复为原有 `C_SECTION.getId()`。
- `runData --offline --no-daemon` 成功，Microsoft Java17.0.11，BUILD SUCCESSFUL in 43s，providers277ms，written0。
- 最新日志无 ERROR/Exception，生成资源无差异，工作区/暂存区 `git diff --check` 通过。
- 未运行 JUnit、GameTest、客户端或游戏验收。

## 错误记录补充
- 首次 AttackPrio 静态核对脚本使用 PowerShell 数组 `-join` 表达式比较，因运算优先级误报失败；随后打印并确认实际顺序与降序结果完全一致。

## 错误记录
- Gradle wrapper 首次启动：用户缓存 zip.lck 写入访问被沙箱拒绝；任务未执行，改用授权缓存访问继续。

## 验证结果
- 从源码解析全部 6 条注册组合规则及 AttackPrio，对 10 组拥有集合静态核对，胜出结果全部符合预期。
- Microsoft Java17.0.11，runData --offline --no-daemon 退出码0，BUILD SUCCESSFUL in 55s；providers248ms，written0。
- 最新生成日志无 ERROR/Exception，生成资源无差异，工作区/暂存区 git diff --check 通过。
- 测试源码已补充但未执行 JUnit/GameTest；未执行客户端/游戏/视觉验收。
