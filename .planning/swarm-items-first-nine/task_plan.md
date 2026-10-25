执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和对应道具分支规则。

# 第一批九件虫群道具：反馈修订

继续worktree-b / feature/feature-b，不切换分支、不提交。当前任务为前轮实现的继续修订；保留用户对假发及复击措辞的手工调整。

- [x] 清理过期候选数据及重复记录，保留可行性报告和图标来源
- [x] 蜘蛛盒、嗝屁猫的头、卫生巾本体文本使用单次数量；车载电池仅在协同说明“使用两次”
- [x] hasCarBattery集中为ActiveAbility辅助方法，服务端读取真实持有缓存、客户端读取同步缓存
- [x] 害虫横行、蜘蛛宝宝直接使用ON_HURT，不再自行筛选自伤或伤害量
- [x] 二代排除蓝蜘蛛等事件条件移入模块，删除专用效果器；复用SPAWN_BLUE_SPIDER/SpawnFamiliars指定位置生成
- [x] 假发本体描述“最多5只”，Hive Mind协同描述“最多10只”
- [x] SpiderBaby / spider_baby统一类、注册名、枚举、纹理、池、语言、测试；原版Wiki名称及来源URL保留
- [x] 复击追加HIT_ENTITY_RESTRICTED；泪弹/激光继续原命中入口，不重复抽取；仍为1/6概率、需求上限30、份数不放大
- [x] 完成一次runData、相关JUnit/GameTest、资源和暂存差异核查

原九件的等级、枚举顺序、数量/概率、首次奖励、充能及套装保持批准数值；本轮按上述用户反馈更新描述和触发边界。无旧档兼容或迁移。

本轮验证：runData执行一次成功，相关JUnit 18/18，Forge GameTest 87/87（本轮16项、相关回归71项）；生成/复制标签、九个模型、图标格式/尺寸/SHA256及Spider Baby命名检查通过。客户端/多人显示与手动退出重进未实测，玩家NBT保存加载和Curios装卸由GameTest验证。


## 已确认的第二轮调整

- [x] 删除assets.json等非Markdown规划文件；本目录只保留规划Markdown
- [x] Hive Mind两个套装成员关系保持现有对称代码，补全双套装协同/额外描述
- [x] Infestation与Spider Baby：ON_HURT独立概率clamp(0.10+0.02×Luck,0,0.25)，成功1–2只，不随份数放大；负幸运降低，动态描述与效果共用公式
- [x] 猫套获得FLY_TIME比例1及Cat模块，失去时对称移除；使用LivingDamageEvent→通用DEAL_DAMAGE，不新增Forge事件类
- [x] 猫套最终正伤害、未取消、归属玩家、非自身/友方、非所有跟班直接来源；固定2/3生成1只蓝苍蝇于玩家位置，共享64名额
- [x] 验证概率边界、动态文本、伤害入口/来源排除、套装阈值/真实Curios/保存加载、一次runData及相关JUnit/GameTest

继续原分支，不提交、不迁移旧档、不扩大跟班派生物溯源范围。已有其他手动修改保留。

## 去除受伤生成的共享概率抽象

- [x] 删除HurtSwarmGeneration；两件被动各自定义chance，自己的模块及描述分别引用
- [x] 删除公共帮助类测试，在既有概率测试中独立验证两件道具；GameTest不再依赖共享概率类
- [x] 一次runData、相关JUnit及虫群GameTest，核对手动Cat/语言修改保留
