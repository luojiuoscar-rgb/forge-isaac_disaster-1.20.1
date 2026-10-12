# 核对结果

- 当前 master；保留既有 codex/skills/minecraft-datapack-1-20-1/scripts/test_validate_datapack.py 与 IsaacConfigDefaultsTest.java 改动。
- PREPARE_AND_EXECUTE 仅做逐弹准备与执行，不重新触发 ATTACK_PLAN。
- FiringModifierAttackPlan 使用规划攻击 getAttackContexts 重建逻辑数；Wiz 修改主方向；Loki 追加三个方向。
- AttackSelector 已支持 isActive；PlayerAbility 在主攻击变更时清除共享攻击蓄力。
- AttackContext.toBuilder 独立复制配置；setBulletScale(..., true) 不更改伤害；激光固定变换保留 Brimstone 调度方向。
- 已复用 BLOOD_TEAR：0xCC171F、优先级 5；GRAVITY：0.05 blocks/tick²。
- 图标已在规划阶段确认：https://bindingofisaacrebirth.wiki.gg/images/Collectible_Monstro%27s_Lung_icon.png?a46721。
- 五行描述静态审阅无需拆分，未做渲染宽度验证。
- 图标格式协商：Accept 含 image/webp 时返回 WebP；限定 image/png 后为真正 PNG32×32、224 bytes。无缺失素材。
- runData 成功且生成模型和被动道具标签；输出目录由现有配置管理，未手写模型 JSON。
