# 已确认信息

- 当前HEAD为a7780f02，开始时工作区干净，feature/feature-b。远程origin/master在f8c6458c，已获取最新远程信息且目标分支包含它，不需要合并。
- 基础时间默认10秒。旧接口只接受int倍率。炸弹袋旧4–5倍，硬币袋6–8倍，圣遗物12–18倍，均先用nextInt选倍率导致10秒分档。
- 用户明确不迁移灵魂；四种固定维护周期不纳入随机迁移。
- 用户确认五件等级1；后续统一stack模型，前四色普通1/金饰品2，征服蝗虫普通1–4/金饰品2–6，征服数量按累计n在[n,2+2n]闭区间随机一次。
- 现有PlayerFamiliarData.addCount已处理64名额与优先级；生成只需增加需求数量，现有调度器负责实体准入。
- 饰品正常/吞服均有既有存储与生命周期，不能另建装备事件或吞服存储。
- 初版调查发现PlayerHelper.getValueFromTrinket存在既有强化判定及返回分支语义问题，本轮不扩修这些旧辅助函数。最终版本使用既有模块stack贡献，不再依赖该函数或循环扫描装备/吞服列表。
- AddFamiliars复用RESOURCE_LOCATIONS及当前COUNT上下文，无新TriggerType或ContextKey。执行契约：缺少服务器玩家/类型/能力返回false，达到名额上限或数量0属于安全完成true。
- LocustTrinket普通贡献1、金饰品贡献2，卸下扣除对应强度，LocustGeneration读取AMPLIFIER。混合普通/强化份数直接累加，周期不随强度缩短。最后一份卸下会移除模块，重新装备等待完整40秒，吞服不会重置或重复贡献。ConquestLocustGeneration重写数量规则，LocustOfConquest复用其上限公式生成描述；移除conquest布尔开关和trinket查询参数。
- 描述模板由配置换算秒数，使用普通无特殊样式的数值文本；dynamicNumber仅用于单局内可变参数，不适用于此配置时间。正文显示普通数量，金饰品提示显示强化总数量。静态核对单句长度，无需拆分。
- 用户要求保留首次获取奖励：沿用Trinket.onFirstEquipped及consumed标记，每个物理饰品首次装备单独传1或2强度；与循环共用生成方法及ADD_FAMILIARS效果，不读取模块累计强度，因此不重复结算其他副本。额外奖励不改变计时器，循环初始冷却保持getTimeInterval(4)。按最新要求删除首次奖励描述及翻译键。
