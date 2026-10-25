# 当前实施依据

- 用户明确要求：普通道具文案展示其自身单次效果，车载电池改用单独“使用两次”；假发基础上限与Hive Mind上限同理分开说明。
- HIT_ENTITY_RESTRICTED来自LivingAttackEvent及PlayerHelper.isHitAllowedType。自定义TEAR/LASER/FAMILIAR伤害不在此白名单，故复击可同时接入after-hit及restricted而不重复抽取。
- ON_HURT自身已包含自伤；无需再区分SELF_DAMAGE或普通零伤害。Forge取消的事件不进入普通订阅回调。
- 二代模块保留LivingDeathEvent的取消、归属、蓝蜘蛛来源排除及同世界条件；效果器只处理玩家、目标位置和数量，允许脱离死亡事件调用。
- 生成蜘蛛的各数量/批次定义继续统一使用SpawnFamiliars。固定单只SPAWN_BLUE_SPIDER由二代、假发共享；盒子/受伤保留各自注册数量，避免把主动放大规则扩散到被动。
- 项目身份统一SpiderBaby/spider_baby/SPIDER_BABY；图标来源仍是原版英文Wiki的Spiderbaby，URL不改写，SHA256不变。
- 在同一Gradle调用中串接runData及GameTest时，processResources可能先复制旧生成文件，随后runData更新不会在本次任务图里再次复制。注册名变更后应单独调用GameTest刷新复制资源；本轮已验证旧标签导致的两项失败因此消失，不应为此加入存档迁移或修改Curios逻辑。


- 用户随后确认受伤生成改为10%+每幸运2个百分点，范围0–25%，成功1–2只；此前固定数量的记录为历史结果。
- 猫套只排除直接来源为AbstractIsaacFamiliarEntity的伤害，不新增跟班派生弹体/子炸弹的血统追踪；玩家直接、泪弹、激光及玩家爆炸遵循同一最终正伤害入口。
- 临时assets.json已删除，所有规划材料现仅保留Markdown；无新增素材，本轮无需联网重新下载。
