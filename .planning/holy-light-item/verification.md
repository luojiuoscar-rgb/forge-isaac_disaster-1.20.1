# 圣光 Holy Light 实施与验证

## 已实现

- 目标分支`feature/feature-c`，开始时工作区干净；未同步其他分支、未提交代码。
- 品质3；Wiki原ID374仅作身份参考，`ItemId.HOLY_LIGHT`追加在末尾，使用ordinal，旧ID顺序不变。
- 普通泪弹逐攻击上下文按`1 / max(2, 10 - floor(luck * 0.9))`判定，多份不提高概率或倍率。浅蓝色`0x99DDFF`、颜色优先级20，同级及更高颜色保留。
- 圣光泪弹成功伤害生物后在目标脚下立即发射；撞方块使用真实碰撞点及向外0.01格偏移；自然到期不触发。分裂继承，穿透可逐目标触发。
- 激光保留原色，每次成功伤害后独立判定；其分裂产物继承已有判定规则。
- 光柱基础伤害为弹丸保存伤害的3倍，不读取当前面板、事件修改后的伤害或实际掉血量。
- 所有共享圣光对亡灵统一乘2；移除可变倍率接口与状态字段，撕裂苍穹无需再传入倍率。旧普通接口也应用该规则。
- 新增被动能力、触发模块、准备阶段附件、命中效果四个实现类；无专用Runtime/State/队列。复用已确认的Apple概率附加方式、妈妈的眼影染色方式和共享圣光柱。
- 获得/移除对称增减触发模块与现有天使套装计数；首次获得无奖励，无新道具NBT、常驻属性或音效。已经发射的弹丸不依赖继续持有道具。
- 已接入被动列表、能力、物品、模块、可执行效果、颜色；四个池为treasure、angel、greed_treasure、greed_angel，均不添加权重。
- 普通说明四行；Shift说明幸运上限、分裂继承、激光判定并复用套装说明。动态百分比通过Minecraft真实翻译解析器校验；未进行客户端字体宽度测量。

## 图标

- 英文Wiki官方原图：https://static.wikia.nocookie.net/bindingofisaacre_gamepedia/images/7/7c/Collectible_Holy_Light_icon.png/revision/latest?format=original
- 文件`src/main/resources/assets/isaac_disaster/textures/item/holy_light.png`，917字节，实际32×32 PNG，已解码打开核对外观；未转码或代画。
- SHA256：`37d75e98e02f8202221437d1c934caed659d56d3db646c1be8da06153eba8503`。
- 最初Wiki/API/CDN请求曾遇到超时、403和TLS错误，最终官方CDN请求成功，素材无缺失。

## 实际验证

- JDK：`C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot`；显式使用用户现有Gradle缓存。最初默认缓存错误地落到`C:\.gradle`，随后修正运行环境，未改项目配置。
- 先运行新增注册测试，旧实现因不存在`HOLY_LIGHT`枚举而按预期失败。日志`build/holy-light-item-red.log`。
- 新JUnit夹具首次出现注册颜色值相同及共享注册表缺少血泪颜色的问题；仅修正新增测试夹具，未修改铅笔等无关生产代码。
- 最终执行：

```powershell
.\gradlew.bat test --tests '*HolyLight*' --tests '*LeadPencilBurstTest' runGameTestServer '-PgameTestNamespaces=isaac_disaster_holy_light_item,isaac_disaster_holylight,isaac_disaster_crack_the_sky' runData --offline --no-daemon
```

- `BUILD SUCCESSFUL in 2m 49s`。日志`build/holy-light-item-validation.log`。
- JUnit：54项通过，失败/错误/跳过均为0；新增圣光道具7项，另包含圣光模块既有46项及铅笔1项。覆盖概率边界、独立判定、不按模块份数放大、颜色优先级、继承数据和真实百分比翻译解析。
- Forge GameTest：35项实际运行并全部通过，其中新增圣光道具9项、共享圣光22项、撕裂苍穹4项。覆盖真实穿透碰撞、伤害取消、自然到期、苹果倍率、科技2折扣、空中触发、墙面落点、失效上下文、直接能力获得/移除和共享亡灵增伤。
- `runData`仅实际执行一次，写入2个生成文件；模型与被动标签已核对。未手写或强制暂存忽略的生成目录，未额外执行独立编译或build。
- 静态检查：旧ItemId顺序、四池无权重、翻译位置、PNG签名/尺寸与差异空白检查通过。
- 未执行完整JUnit、其他GameTest命名空间、客户端画面、真实Curios穿脱、真实存档重载及联机验收；不将直接生命周期调用等同于装备槽测试。

## 手工验收入口

```text
/give @s isaac_disaster:holy_light
```

装备后检查浅蓝泪弹、命中与撞墙立即发射、激光触发、分裂与穿透效果，并复核卸下后的新旧弹丸区别。所有光柱继续采用既有0.75秒柱身及地面焦痕行为。
