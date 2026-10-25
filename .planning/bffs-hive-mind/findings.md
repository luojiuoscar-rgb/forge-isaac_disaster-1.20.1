# 调查记录

工作区开始时干净，分支 feature/feature-b。本轮执行紧接已批准规划。
跟班继承普通Entity，视觉倍率必须独立于LivingEntity属性以及BombEffectSource.getScale。
现有持有缓存涵盖能力背包和Curios；无需引入事件或额外能力强度。
中文Wiki C247/C248和英文Wiki BFFS!已在规划阶段读取。BFFS!视觉+25%，蓝苍蝇/蜘蛛由Hive Mind负责。

图标均由英文Wiki BFFS!页面实际图片下载，已检查PNG签名及32×32尺寸：
- https://bindingofisaacrebirth.wiki.gg/images/Collectible_BFFS%21_icon.png?870a7f
- https://bindingofisaacrebirth.wiki.gg/images/Collectible_Hive_Mind_icon.png?7a84ab

使用Microsoft Java 17.0.11，显式指定现有GRADLE_USER_HOME。一次实际runData已成功（2m26s）。
Minecraft MCP当前未连接客户端，客户端画面和多人实机同步不声称通过；元数据传输将由GameTest验证。

## 本轮条目容器重构

- Forge注册表提供不可变类别/数值/Predicate定义，PlayerAbility只持有ID及真实份数；不同ID加算，同ID由独立配置控制份数。
- Curios权威槽位和持有缓存恢复后仅设置四个内置ID的数量；额外来源保留，不重放道具回调或套装效果。
- 纯JUnit构造Entity依赖尚未建立的Forge FluidType注册表，因此纯测试使用可组合Predicate验证聚合，真实类/接口过滤放入GameTest。
- 蜘蛛接触会检查世界建造高度；共享GameTest夹具不能无限增加Y，统一在源头限制高度。
- 本轮最终证据：一次runData成功（2m13s）、216项JUnit零失败、57项Forge GameTest全部通过。
