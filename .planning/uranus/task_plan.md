# 天王星 Uranus

已批准方案；feature/feature-c，跳过主分支同步，不提交，不增加网络字段或旧版兼容层。

## 执行阶段
- [x] 弹丸伤害来源、死亡附件与药水施加者管线
- [x] 天王星附件、共享冻结来源、1血冰雕与碎裂冰弹
- [x] 水迹生成、无来源伤害及冻结目标排除
- [x] 能力注册、资源、星象池与条件协同说明
- [x] 定向JUnit、GameTest、一次runData与差异审查

## 固定规则
减速I 80tick；restricted/水迹仅减速。冰弹真实致死事件转换为1血无限冻结，普通Mob且非Boss。所有冻结Mob每2tick留下半径0.75、80tick水迹；水迹排除冻结目标。真死亡延迟1tick水平发射12发纯净冰弹。来源及冰雕标记保存在目标NBT。

## 验证边界
分别报告JUnit、实际GameTest、runData、客户端与存档验收；不独立运行compile/build。

最终结果：35项定向JUnit、15项真实GameTest通过；runData成功执行一次；独立审查两项问题已修复并复核。客户端画面、多人观察及完整退出重进未执行，实体NBT保存/回读已由GameTest验证。未提交。

## 来源接口对齐后续
- [x] 只读核对feature-b的来源绑定，将相同SOURCE(ResourceLocation)与SimpleTrigger接口对齐到本分支。
- [x] 保持EFFECT_SOURCE(Entity)用于药水施加者，补充两种来源独立且绑定不污染外部上下文的测试。
- [x] 运行来源绑定定向JUnit及天王星GameTest；不重复runData，不修改feature-b或提交。

## 冻结来源精简（用户确认的新方案）
- [x] 致死冻结直接使用addEffect(instance, owner)，删除EFFECT_SOURCE及通用POTIONS中的来源传递，减速使用普通POTIONS。
- [x] 验证直接冻结的归属、首片水迹与碎裂行为，以及普通药水/来源ID绑定回归；15项定向JUnit和16项GameTest通过，不重复runData或提交。
