# D10 验证记录

使用Java 17：C:/Program Files/Microsoft/jdk-17.0.11.9-hotspot；离线Gradle缓存。

- TDD初次红灯：D10类尚未存在，定向测试编译失败。
- 首轮候选失败：先在JUnit、再在真实Forge确认getBaseClass为占位实现，改用原版/Forge属性供应表。候选断言改由真实GameTest执行。
- 测试修正：冻结注册表不允许临时构建EntityType，改用原版PLAYER的null工厂；按CONVERSION根实体计数，避免默认初始化附带鸡等造成随机误报。
- 第一次完整通过：2项JUnit、9项GameTest及runData一次，退出码0。日志build/d10-validation.log。
- 独立审查修正潜影贝定位、未注册乘客及直接解除骑乘；未发现剩余阻塞项。
- 最终回归：2项JUnit、11项真实GameTest全部通过，退出码0。日志build/d10-final-validation.log。未重复runData。
- 覆盖候选类型、球形边界、同类型、新UUID、初始化后生命比例、不复制状态/装备、创建及加入取消、无死亡掉落、友方/玩家/标签、单次快照及重复使用、空放6格、真实车载电池、未加载区块、潜影贝和骑乘取消。
- 真实reload覆盖直接ID、嵌套标签、移除排除及全部候选为空，包含失败仍消耗充能。
- 模型/主动标签、两道具池、PNG、语言JSON和git diff --check已核对。自动审批两次独立超时均按提示各重试一次成功；图标TLS凭据/吊销检查问题已解决。
- 未执行客户端、真实存档验收及全量JUnit；没有独立compile/build，没有提交。

Mob范围调整开始：用户已确认排除所有非Mob活体。保留用户已修改的两行说明和仅Boss排除标签；新增不依赖标签的盔甲架目标／结果拒绝GameTest，先验证红灯。

Mob红灯已确认：盔甲架会进入旧版结果池与目标查询。实现改为Mob查询，结果池通过一次未入世界实例检查确认Mob；实际创建再次约束Mob，移除Player分支。所有旧isCandidate签名已迁移，无兼容重载。

Mob范围最终验证：2项JUnit、12项真实GameTest及本轮一次runData全部通过，退出码0（build/d10-mob-validation.log）。盔甲架在无显式标签时仍被目标查询及结果创建拒绝，保留友方、Boss与动态标签排除。客户端／真实存档未额外验收。
