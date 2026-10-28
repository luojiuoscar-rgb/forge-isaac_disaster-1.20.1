# 实现依据

以下初版依据仅保留为历史记录；当前规则以“第二轮依据”为准。
2026-10-28后续调整以末尾“当前修订”为准，替换第二轮的专用防火和粒子图标实现。

- 现有DeadlyPoisonEffect提供1+0.5*amplifier伤害及半点量化规则。
- 使用PotionProfile/POTIONS，触发模块采用类级CompositeTrigger；复用FIRE_RESISTANCE模块。
- Entity.baseTick自带每20 tick的onFire伤害；必须只在灼烧期间抑制此处，不能全局取消火焰伤害。
- Entity.setRemainingFireTicks是clearFire及负数灭火的共同入口；仅在服务端对带灼烧的活体处理。
- 烧伤计时必须独立于MobEffectInstance.duration，避免重复命中导致周期饥饿。
- 图标只能来自英文Wiki，需验证32×32；双Wiki正文先前403/超时，不声称完整核验。
- 道具图标已下载：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/f/f9/Collectible_Fire_Mind_icon.png/revision/latest?format=original 。原PNG，32×32，333字节，已查看外观。
- 初次图标地址404、wiki.gg跳转403、Fandom跳转超时；改用英文Wiki静态原图成功。灼烧图标从现有Minecraft client.jar提取fire_resistance.png。
- 生命周期使用BurningEffect本身的Forge事件和现有LivingEntityMixin清理；只新增EntityMixin，不新增Runtime/State/Manager。
- 首次附加补充duration+1的火焰计时，确保第80 tick的第二次伤害不会被原版先行灭火吞掉。
- 放置取消时回滚原方块；增加传送门边界测试后确认原版邻居更新会清除相关传送门块，18项实际GameTest通过，未引入额外捕获事务抽象。

## 第二轮依据

- 用户接受简化后表现降低和原版燃烧伤害，不再保证80 tick内恰好2次实际伤害；高火焰保护可使火焰早于第一次脉冲结束。
- 三处灼烧注入已全部删除，LivingEntityMixin恢复原状；新增ItemEntityMixin只负责掉落物防火返回值和合并继承，不参与灼烧效果。
- TerraAttachment -> BREAK_BLOCK_AND_DROP -> Level.destroyBlock(pos,true)。在破坏调用期间处理ItemEntity加入事件，可保留原掉落表、容器内容、经验及模组掉落流程。
- ItemEntity.fireImmune与Entity.isInvulnerableTo对IS_FIRE的检查可实现局部防火；不使用全伤害setInvulnerable。
- 当前未暂存修改：ModPassiveItems把FIRE_MIND移到HOLY_LIGHT之后；ModExecutableEffects抗火PotionProfile隐藏粒子。均属于用户已有改动，保持原样。
- 图像生成工具未返回可保存文件；图标最终复用Minecraft client.jar内textures/particle/flame.png（8×8），最近邻放大16×16并置于18×18透明画布，已检查透明通道和火焰外观；没有用CLI绘图回退。
- 在Terra破坏调用期间用临时线程作用域处理ItemEntity加入事件，保留原版容器内容、掉落表、经验、gamerule。溢出堆叠在作用域外生成，避免再次熔炼；不缓存熔炉配方。
- 2026-10-28修订：协同说明仅归地球，使用terra.synergy.fire_mind.*翻译，三条旧action键删除；火之意志详情沿用用户调整后的共享灼烧解释。
- 已直接核对当前Forge映射源码：ItemEntity.fireImmune读取物品类型防火属性，Entity.isInvulnerableTo结合IS_FIRE处理火免；实体persistentData标记本身不会自动赋予防火，现有Mixin将两者接通。setInvulnerable会扩大保护范围，重写自定义ItemEntity则需替换原掉落实体。

## 当前修订

- 用户明确接受原版无敌替代单独防火：直接item.setInvulnerable(true)，删除ItemEntityMixin及配置，删除标记工具，不保留合并继承注入或旧标记兼容。
- 原版合并只搬移物品、age和pickupDelay，不搬移invulnerable；接受原版合并规则，不承诺无敌状态一定传给未受保护的另一实体。
- 图标使用Minecraft client.jar内assets/minecraft/textures/block/fire_0.png的16×16首帧，周围加1像素透明边框供状态图标使用；不是textures/particle/flame.png。
- 全部实际吞饰品入口经PlayerHelper.swallowAllTrinkets进入CurioSlotManager.swallowTrinkets；每件成功转移后读取Foods.BREAD（nutrition=5、saturationMod=0.6）调用FoodData.eat，通常增加6点饱和度，遵守原版上限。不在capability加载或addToList路径进食。
- 用户已将地球协同文案精简为“自动熔炼掉落物”，保留该调整；翻译测试只检查实际仍使用的键。
- 后续按用户要求内联单次熔炼：仅BreakBlockAndDrop调用smeltOnce，因此移为该类private static方法并删除ItemDropHelper，函数内容保持不变。
