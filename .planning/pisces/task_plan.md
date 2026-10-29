# 双鱼座：泪弹击退

执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和被动道具规则。

分支feature/feature-c；获准跳过同步；保留四项原有加成，不提交、不加旧版本兼容。

1. [完成] 注册玩家bullet_knockback：默认0.2、单位增量0.1，双鱼座±3单位，动态说明。
2. [完成] AttackContext及BulletState发射快照、复制与分裂继承。
3. [完成] 替换固定击退为原版strength×快照；保留水平运动、Forge事件、抗性和作用域隔离，迁移所有tear调用。
4. [完成] 定向JUnit、实际GameTest及一次runData；差异审查与交付。

只保留三个关键Markdown；客户端和真实存档验收单独报告。
