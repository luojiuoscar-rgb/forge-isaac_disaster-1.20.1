执行时继续使用 isaac-disaster-item-creation 技能；遵守已批准范围，仅运行 runData 验证。

# 通用蓄力与客户端显示

## 已确认范围
- AttackType 内管理蓄力，通过 IChargeableAttack 统一分发主攻击和独立攻击；移除 Revelation 临时 TODO 接入。
- START / CORRECT / END 同步，客户端推进与平滑修正；服务器决定实际蓄力及释放。
- 所有蓄力条的收缩白环采用像素轮廓，保持半透明、4px 初始距离、满蓄隐藏。
- 保持 Neptunus 空闲蓄力、CSection 满蓄自动发射、CursedEye 部分蓄力释放的行为。
- 已释放的终末天启光束不受蓄力清理影响；历史规划及现有修改保留。

## 阶段
1. 现有接口与消费者检查：complete
2. 通用蓄力接口与分发：complete
3. 同步协议及客户端预测：complete
4. 像素白环：complete
5. 静态核对、runData、交付：complete
6. 按审阅精简攻击职责与接口：complete

## 本轮已批准精简
- ModAttackTypes仅注册；主攻击复用现有缓存入口，独立攻击临时分发迁入AttackType。
- 删除chargesWhileHeld，统一addCharge只处理充能量；海王星通过override保留特殊行为。
- isIndependentCharge保留并注明临时TODO；不扩大至完整AttackType重构。
- 删除updateClientCharge及其调用；修复BrimstoneAttack缩进。
- 主攻击切换清理放到选择结果更新处；移除额外每tick查找与缓存字段。
- 保持客户端预测、像素白环和已释放Revelation光束行为；仅运行runData。

## 验证结果与人工验收
- runData --offline --no-daemon：成功，Microsoft Java17.0.11，36s；providers157ms，written0。
- latest.log：2026-10-09 21:04:43；未出现ERROR或Exception，终末天启生成模型正常。
- diff --check：通过（按仓库默认换行转换）；生成资源无变更。
- 新增/更新预测、双栏隔离、包动作/非法数据、像素轮廓测试源码；受技能要求限制，未运行测试任务。
- 游戏内待验收：47tick边界、两栏共存、海王星松开恢复/按下消耗、剖腹产自动重复、诅咒之眼部分释放、网络抖动/低TPS/暂停、死亡退出换维度、像素圆环观感。
- 本轮精简后重新runData：成功，Microsoft Java17.0.11，1m58s；providers765ms，written0；latest.log21:45:10，无ERROR/Exception。
- 本轮静态核对：每个主入口只取一次缓存攻击；独立分发排除该主攻击；注册类无运行时函数；删除符号无残留；切换清理不取消已发射光束。
- AttackChargeDispatchTest源码覆盖默认释放清理、Neptunus override及失去资格；未运行测试任务。游戏与视觉验收仍未执行。

## 错误记录
- 初次读取 ModAttackTypes 使用了错误目录；通过 rg --files 定位。
- session-catchup 的 python 命令不在 PATH；使用现有会话摘要和 Git 状态恢复，不影响实施。
- 强制关闭 autocrlf 的 diff --check 把现有CRLF误报为尾随空格；改用仓库默认换行转换核对。
- 本轮rg使用PowerShell字面量通配路径报错；改为搜索目录并用-g过滤。
