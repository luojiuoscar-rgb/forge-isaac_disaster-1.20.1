# 已确认规格与实现约束

- Virgo概率 `1/max(2,4-floor(luck*0.2))`；200 tick护盾叠时长；多份只叠胶囊品质。
- Aquarius生成时独立概率、固定颜色和状态种类；状态施加时读取当前面板；基础伤害生成时保存。
- 共用效果附加节点，水迹不伪造弹丸事件；吐根水迹使用天蝎40 tick/100%面板剧毒，火之意志只灼烧。
- 水迹移动时Y固定、速度0.25格/tick、固定8格水平搜敌；失去同高度支撑立即移除，不落向低层。
- 原水迹接触冷却10 tick，water已绕过伤害冷却；同施法者重叠不叠伤。
- 当前协议15，既有快照包含位置、颜色和revision，无需改线格式。
- 资料来自本地已归档Wiki/EID；在线双站正文核验未完成。

- 远端玩家的水平速度不可靠；宝瓶周期模块使用弱引用位置映射检测两次采样间的真实水平位移，不落盘；移除模块清理。
- 贴地投影原先读取整个台阶形状的最高面，已收敛为脚下实际XZ处的碰撞顶面；增加已加载检查。
- 图标来源：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/6/62/Collectible_Virgo_icon.png/revision/latest?format=original
- 图标来源：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/5/53/Collectible_Aquarius_icon.png/revision/latest?format=original

- 本轮调优：室女座0幸运25%、5幸运1/3、10幸运达到50%上限；共享水迹/宝瓶默认半径统一0.75；协同说明按对应持有道具显示。
- 旧实现曾读取nearby_range（默认12）；现已改为从每片水迹中心固定8格，包含边界，不受配置影响。

- 当前源码确认：旧宝瓶每片每tick独立getEntitiesOfClass，半径读nearby_range；子弹则按区域共享候选、复用目标并4 tick退避。水迹已在现有Manager内复用分组思想，保留逐片敌我筛选。

- 索敌实现选择：候选AABB向外按XZ 8格、Y 1格取整，共用原版实体分区查询；子弹额外建立的EntityGrid兼供碰撞使用，水迹不再重复建索引。缓存每tick与Manager.clear清理，不截断候选。
- 有效目标按每tick验证后继续追踪，失效立即停止；无目标时每4tick重查。移动仍每tick最多0.25格，不改变水迹伤害及同步节奏。
