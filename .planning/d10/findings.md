# D10 关键发现

- 复用D12主动能力、升级音、车载电池条件说明；忽略amplifier，不改变充能共享流程。普通与贪婪宝箱房池均无权重。
- Forge 47.4.9的EntityType.getBaseClass实际返回Entity.class。候选先按可召唤、属性供应表和排除标签过滤，再创建未加入世界的实例确认属于Mob。每种类型每次使用只检查一次；不跨使用缓存，标签reload立即生效。目标查询、结果创建均限制Mob，移除专门的玩家分支。
- onFinalizeSpawn使用CONVERSION并尊重生成取消；加入失败保留原实体。初始化后恢复位置、碰撞箱及朝向，覆盖潜影贝网格吸附与朝向重置。
- 仅加入新根实体，直接removeVehicle解除初始化附带及旧实体的骑乘关系，避免下马位置扫描。解除被Forge取消时中止替换；旧实体清理失败则撤回新实体。外部监听器自行维持的引用不强改。
- 真实32×32 PNG已核验签名、IHDR与图像。来源：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/e/e5/Collectible_D10_icon.png/revision/latest?format=original 。Windows证书吊销检查服务离线，curl使用ssl-no-revoke后成功，仍验证服务器证书。
- 保留用户精简的两行文案及仅Boss默认排除标签，静态排版审查；实际Minecraft翻译解析通过。模型及主动标签由runData生成，src/generated按项目规则忽略。
