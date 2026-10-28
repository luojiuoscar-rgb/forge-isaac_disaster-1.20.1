# 实施与验证进度

- 前轮九件实现已完成：一次runData、18项相关JUnit、84项相关GameTest通过；图标均原始32x32 PNG。
- 本轮按用户8点反馈继续修改，使用item-creation主动/被动规则及planning-with-files。
- 已保留用户对MomsWig及语言的手工简化。三件主动基础数量不随电池改写，电池识别移入ActiveAbility；假发本体5/Hive协同10。
- 二代死亡来源过滤已移入模块，删除专用Infestation2效果器；SPAWN_BLUE_SPIDER共享通用生成，假发也复用。
- 害虫横行/蜘蛛宝宝直接ON_HURT，复击支持既有HIT_ENTITY_RESTRICTED，蓝蜘蛛命名已统一SpiderBaby/spider_baby。
- 已删除5个过期文件：可行性目录候选数据库快照、findings/progress/task_plan，及本任务重复report；保留可行性report和本任务必要实施记录/图标来源。其他历史目录未清理。
- Windows仅大小写重命名时目标路径被视为已存在；通过经检查的中间文件名完成两处Java文件名大小写更新，原纹理字节不变。
- 已更新原测试并增加基础/协同描述、受限攻击及不重复抽取、无事件指定位置生成的覆盖。
- 本轮runData执行一次成功，JUnit 18/18通过。首次合并Gradle调用先processResources后runData，GameTest读到旧spiderbaby标签，导致两项Curios重载测试失败；通过比较生成目录/复制目录和TagLoader日志定位，不改生产代码。
- 单独重启GameTest以复制新资源后，87/87通过；日志build/reports/swarm-items-first-nine/revision-gameTests.log。新增16项及现有相关71项覆盖归属击杀、普通/零自伤路径、共享生成、文本协同、复击双入口、Curios装卸和完整玩家NBT保存加载。
- 新spider_baby模型、生成和已复制标签、九张32x32 PNG/SHA256及源码注册名称核对通过；既有Wiki原图名称和URL保留。
- 客户端游戏内显示、多人观察、手动世界退出重进未实测；不扩大为全项目测试通过，不执行提交。


## 第二轮效果调整结果

- 已删除assets.json；递归检查.planning仅有Markdown，未新增JSON快照。图标保持既有原始英文Wiki32x32 PNG，不更换图片、模型或池。
- Hive Mind保留用户已添加的双套装计数，补齐两个套装的协同和额外说明；额外说明带各自套装名。
- 害虫横行/蜘蛛宝宝继续ON_HURT独立抽取，共用HurtSwarmGeneration公式clamp(0.1+0.02×Luck,0,0.25)，成功1–2只，真实份数不放大。负幸运降低，显示动态百分比，概率与数量分行。
- 新增通用DEAL_DAMAGE触发，来自LOWEST阶段的LivingDamageEvent，携带归属伤害者、受害者、最终正伤害、位置及直接来源。没有新增自定义Forge事件类。
- 猫套通过既有三件阈值增减FLY_TIME比例1和Cat模块。固定2/3在玩家位置生成蓝苍蝇，忽略幸运；排除所有跟班直接来源、自己、友方、取消及完全吸收的伤害，复用共享名额和通用生成器。
- 本轮runData只执行一次且成功（cat-runData.log）；JUnit 24/24，Forge GameTest 92/92成功（cat-tests.log），包含本任务21项和71项相关既有回归。
- 新测试验证负幸运/小数/上限/非法输入、共享生成1–2端点、双套装和动态文本、真实伤害上下文、蓝苍蝇/蜘蛛/五蝗虫/菜刀排除、泪弹/激光/爆炸允许、完全吸收/取消/友方过滤、真实Curios套装阈值/恢复与完整玩家NBT重载。
- 本轮没有独立编译或build。客户端游戏内视觉、多人观察及手动退出重进未实测；玩家保存加载由GameTest验证。

## 移除共享概率抽象

- 按用户反馈删除HurtSwarmGeneration及其独立测试文件。Infestation、SpiderBaby各自保存chance计算，两个模块各自判定随机概率；描述使用各自道具公式。
- 已有概率JUnit分别验证两件道具的基础/负幸运/小数/上限/非法值，GameTest改为分别读取两件道具的概率。
- 开始时另有用户对Cat套装及语言文件的未暂存修改，本轮不编辑或暂存这两个文件。继续原分支，保留其他暂存内容。
- 验证：本轮一次runData成功，相关JUnit 10/10及虫群GameTest 21/21通过；src无已删除公共帮助类的引用。Cat与语言的用户手动修改保留，未运行独立编译/build。
