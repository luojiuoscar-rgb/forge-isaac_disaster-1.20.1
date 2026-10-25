执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和对应道具分支规则。

# BFFS! 与虫群之心

## 已批准的条目容器重构

- [x] 将硬编码profile替换为PlayerAbility持有的强化/视觉条目容器
- [x] 注册Entry定义，配置同ID份数的默认关闭叠加开关
- [x] 获取/移除维护份数，跟班自行消费强化值
- [x] NBT、独立复制与Curios恢复
- [x] 更新并执行JUnit/GameTest、一次runData，审查差异

强化默认0；视觉倍率=1+增幅总和。不同ID加算，同ID默认一份、开启配置后按真实份数。
Entry通过Forge注册，保存ID及份数；过滤Predicate不写入NBT。战争爆炸=(20+10×强化)×DamageBonus。
通用容器不得持有任何道具判断或具体跟班效果。

用户进一步明确：项目无需旧版本或旧存档兼容。移除FamiliarModifierMigration、pending字段及登录/重生/tick调用；保留普通NBT保存/读取与独立复制。
清理后验证：10项相关JUnit、29项相关Forge GameTest全部通过。历史记录中的旧档补齐验收不再属于当前实现。

- [x] 统一跟班伤害与同步视觉倍率，新增 SwarmFamiliar
- [x] 完成 BFFS! 注册、描述、图标及道具池
- [x] 完成 Hive Mind 注册、描述、图标及无效果苍蝇领主三件套
- [x] 验证真实伤害、持有更新、Curios、重载、翻译与同步元数据
- [x] 一次 runData、JUnit、相关 GameTest；审查 diff 并报告实际验证范围

## 上一轮原始实现记录（条目与叠加规则以本轮重构为准）

继续 feature/feature-b，不切换分支、不提交，不修改 docs。
BFFS!：非虫群跟班伤害×2，视觉×1.25。Hive Mind：虫群伤害×2，视觉×1.5。
同一道具不叠加；伤害即时读取持有缓存；视觉服务端更新并同步，实际尺寸不变。
战争蝗虫中心伤害由20变30个配置伤害单位；瘟疫剧毒随接触翻倍，50tick不变。
标记由蓝苍蝇、蓝蜘蛛实现，蝗虫继承。炸弹派生效果、循环掉落道具不额外强化。
原版ID247/248，项目均2级，追加枚举用ordinal。BFFS!加入商店/究极隐藏；Hive Mind加入商店。
图标为英文Wiki的Repentance 32×32 PNG；模型和标签由一次runData生成。

## 错误与限制

英文Hive Mind正文在规划阶段因审批连接失败未完成核对；项目数值已由用户确认。

## 本轮条目容器重构验收

Microsoft Java 17.0.11：本轮一次实际runData成功，216项相关JUnit全部通过，57项相关Forge GameTest全部通过。
覆盖真实Curios装卸、完整玩家保存/读取、死亡Clone事件、旧数据补齐、额外注册条目、非伤害强化消费、配置即时切换和元数据同步。
开启同ID叠加时两份Hive Mind：蓝苍蝇6D、视觉倍率2、战争中心伤害40×DamageBonus；默认关闭时保留上一轮数值。
纯JUnit负责聚合、Predicate组合与NBT；真实类/接口过滤在Forge GameTest中验证。
两次验证失败均是测试夹具问题：纯JUnit实体初始化缺Forge流体注册表；共享位置递增导致蜘蛛超过建造高度。已修复夹具并复测通过。
客户端实际渲染、脚部视觉锚点及多人画面尚未实测。未执行独立编译或build，未创建提交。
日志：build/reports/bffs-hive-mind/refactor-runData.log、refactor-verification-second.log、refactor-gametest-final.log。

## 上一轮原始实现验收

一次实际runData成功；175项相关JUnit与51项相关Forge GameTest全部通过。
服务端验证包含玩家存档重载、Curios真实装卸及新追踪者元数据。客户端没有连接，三类实际渲染、脚部视觉锚点和多人画面尚未实测。
生成模型和被动标签按仓库既有规则留在被忽略的src/generated/resources，不修改忽略规则。
原始测试日志在build/reports/bffs-hive-mind，不加入提交。
