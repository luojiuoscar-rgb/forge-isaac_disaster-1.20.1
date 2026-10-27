# 圣光受限攻击入口与 attachment 检查（2026-10-27）

## 圣光改动

- 继续在`feature/feature-c`实施，仅修改圣光的触发模块和命中效果；其他道具只检查。
- 类级`CompositeTrigger`增加`HIT_ENTITY_RESTRICTED -> HOLY_LIGHT_IMPACT`，沿用同一幸运概率，忽略道具份数对概率的放大。
- `HolyLightImpact`接受真实`LivingAttackEvent`：校验来源属于现有白名单、施法者归属、目标世界、事件未取消及有限伤害。读取该次事件的原始传入伤害，以其3倍作为圣光基础伤害，位置在触发时固定为目标脚下。
- 该入口没有`IBulletObject`，不能读取弹丸伤害快照；模组泪弹和激光仍按原有弹丸快照路径处理。
- 白名单包含player_attack、generic、mob_projectile、arrow、trident、magic、indirect_magic、mob_attack、mob_attack_no_aggro；需要来源拥有活体攻击者。模组tear、laser、holy_light等自定义伤害不在名单中，所以不会与弹丸入口重复或由圣光递归触发。
- `LivingAttackEvent`在原伤害结算之前派发。此时立即造成较大的圣光伤害会先设置目标无敌帧，可能让原攻击被拒绝，因此只将本次圣光交给既有`ScheduledFuncHelper`执行一次。任务在后续调度器执行阶段发射，无预警；伤害和位置已保存，移除或换世界的施法者不会继续发射。
- 不新增Runtime、State、持久化数据、触发类型或网络包。新分支返回值表示有效接收并安排一次效果；无效事件返回false，概率未命中由触发条件直接跳过。

## 检查范围与结论

检查`ability_effect/impl/attachment`下23个文件：1个抽象基类、21个具体道具附件及1个通用轨迹附件。结合对应触发模块、效果实现和受限事件上下文核对，不只搜索注册文字。

### 原本已接入的8项

| 道具/模块 | 受限入口执行内容 | 检查结果 |
| --- | --- | --- |
| DarkMatter | 同一概率施加恐惧 | 已注册，效果接收次级目标列表 |
| IronBar | 同一概率施加眩晕 | 已注册，药水入口可复用 |
| MomsEyeshadow | 同一概率施加魅惑 | 已注册，药水入口可复用 |
| MomsContacts | 同一概率施加石化 | 已注册，药水入口可复用 |
| MomsPerfume | 同一概率施加恐惧 | 已注册，药水入口可复用 |
| TheCommonCold | 同一概率施加剧毒 | 已注册，按已有面板伤害编码药水参数 |
| Scorpio | 施加剧毒 | 已注册，效果接收次级目标列表 |
| Ipecac | 在目标位置爆炸及施加剧毒 | 已注册，接收目标位置和本次伤害 |

圣光为本轮新增的第9项。上述其他8项仅作注册和上下文静态审查，不表示本轮进行了它们各自的游戏内联动验收。

### 仍缺失、值得后续补齐的2项

| 道具 | 当前证据 | 后续处理建议 |
| --- | --- | --- |
| 店主的头 / HeadOfTheKeeper | 模块只有准备阶段入口；附件在弹丸命中前按5%调用KeeperCoinDrop。掉钱效果只需攻击者和目标位置，均已由受限入口提供 | 可直接为受限入口补同样5%的条件并复用掉钱效果。本轮未改 |
| 苹果 / Apple | 模块只有准备阶段入口；AppleDamage要求IsaacAttackBeforeHitEntityEvent并修改该事件伤害为4倍 | 存在普通攻击覆盖缺口，但不能只加一条注册：LivingAttackEvent没有对应的可写伤害接口，需要适配可修改伤害的阶段，并保留概率和避免递归/重复结算。本轮未改 |

### 其余11项需要区分语义

| 附件 | 原因 |
| --- | --- |
| SinusInfectionAttachment | 黏附时长、目标和每秒伤害由真实BulletState/BulletStickContext管理；普通近战或原版箭没有这些状态。可以扩展，但需要定义对应黏附与持续伤害行为 |
| CricketsBodyAttachment、CompoundFractureAttachment、ParasiteAttachment、HaemolacriaAttachment | 依赖母弹上下文、分裂序列、方向及继承状态。受限命中事件没有这些对象，需要定义普通攻击如何产生子弹，不能只复用现有分裂模块 |
| BounceOnEntityAttachment、BounceOnBlockAttachment | 效果修改实际弹丸速度、碰撞历史及事件取消状态；普通命中没有可反弹的同类弹丸对象 |
| TerraAttachment | 当前附件处理岩石外观和撞方块破坏；目标活体命中入口不能代替方块事件。常驻伤害/破坏属性通过原能力应用 |
| LaserAttachment | 科技与硫磺火、剖腹产的特定弹丸联动，需要真实弹丸与攻击类型，不能泛化为普通攻击目标效果 |
| LeadPencilAttachment | 附件负责泪弹染色，额外齐射按ATTACK_PLAN统计发射轮次；直接接入每次命中会改变计数语义 |
| TrajectoryAttachmentEffect | 通用轨迹装配依赖AttackContext，用命中目标事件无法替代 |

这些项并非都应该排除普通攻击联动；结论仅为“没有可直接补上的同义入口”，需要逐项明确适配行为。本轮没有扩展其规则。

## 实际验证

- 使用Java17、用户既有Gradle缓存。先补1项JUnit与3项GameTest；旧实现中新增入口JUnit失败，近战/箭矢两项GameTest失败，其余10项GameTest通过。日志`build/holy-light-restricted-red.log`。
- 修复后执行：

```powershell
.\gradlew.bat test --tests '*HolyLight*' --tests '*RequiredTriggerTypeTest' --tests '*ScheduledFuncHelperTest' runGameTestServer '-PgameTestNamespaces=isaac_disaster_holy_light_item,isaac_disaster_holylight' runData --offline --no-daemon
```

- `BUILD SUCCESSFUL in 3m 16s`，日志`build/holy-light-restricted-validation.log`。
- JUnit：65项通过，失败/错误/跳过均为0；包含新增受限入口单次概率判定、既有圣光及调度器回归。
- Forge GameTest：34项实际运行并全部通过，其中圣光道具12项、共享圣光22项。新增3项确认近战保留原攻击2伤害并额外造成6圣光伤害；原版箭使用事件传入3伤害，面板改为100后仍额外造成9伤害；持有道具时自定义泪弹及圣光不重复触发。
- `runData`实际执行1次，写入0个生成文件。没有执行独立编译或build，没有新增素材需求。
- 未执行真实客户端画面、联机或存档验收；其他道具只作本表所列静态检查。
- 本轮开始时用户对被动HolyLight、TheWiz、ModBulletColors和语言文件的工作区内容已按SHA256核对保持不变；仅暂存本轮修改，不提交。
