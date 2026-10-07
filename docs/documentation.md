# ISD 模组简易文档（Early Access）

本文件用于说明当前版本中可用的基础指令与方块属性配置，供开发与测试阶段参考。

---

## 一、指令说明

所有指令均以 `/isd` 开头。

---

### 1. 与玩家相关

```
/isd player [玩家选择器] resetdata
```

重置玩家数据。

```
/isd player [玩家选择器] refreshpool
```

重置玩家的道具池记录（仅在启用道具池记录时生效）。

---

### 2. 与道具相关

```
/isd item clear
```

清空额外被动道具背包中的全部道具。

```
/isd item get [物品选择器]
```

获取当前玩家拥有的指定道具数量。

```
/isd item spawn [战利品表选择器] [坐标] [(可选)cost] [可选life/money] [数值]
```

在指定位置生成一个特殊道具底座。

参数说明：

* `cost`（可选）：是否启用消耗机制
* `life/money`（可选）：指定消耗类型（生命 / 金钱）
* `数值`：消耗数值

说明：

* 若使用生命消耗，其单位为“单位数”
* 示例：“早餐”提供 10 点生命值，则 1 单位生命 = 10 点生命值

---

### 3. 与饰品相关

```
/isd trinket clear swallowed
```

清理玩家已吞下的饰品集合。

```
/isd trinket enchantment set [bool]
```

设置玩家当前手持饰品是否为附魔状态。

---

### 4. 与胶囊相关

```
/isd pill shuffle
```

重置所有胶囊。

```
/isd pill use [效果选择器]
```

触发指定胶囊效果。

---

### 5. 游戏规则

```
disablePlaceholder: bool
```

是否禁用占位符刷新机制。

影响范围：

* 箱子占位符
* 房间占位符
* 特殊底座生成

说明：

* 当设置为 `true` 时，占位符不会被激活（无论玩家处于何种模式）

---

## 二、方块属性

---

### 1. 底座（Pedestal）

可设置属性：

```
contentSource [String: MANUAL / LOOT_TABLE]
```

内容来源。`MANUAL` 支持放入一个物品和空手拿取；`LOOT_TABLE` 根据道具池生成内容。手持物品点击已有内容的底座不会交换物品。

```
itemLootTable [String]
```

绑定的道具池。NBT 配置生成型底座时同时设置 `contentSource:"LOOT_TABLE"`。成功生成后 `generated` 为 true；失败保留配置并允许重试。领取后保持已生成状态，避免重新生成。

`isaac_disaster:pools/item/default` 从 `active_items` 与 `passive_items` 两个标签抽取。道具池过滤按各 entry 的实际标签执行，保留权重、条件及物品函数；移除后没有候选且没有额外添加物品时，兜底生成 Breakfast。

```
locked [bool]
```

是否上锁（可使用钥匙解锁）

```
priceType [String: FREE / LIFE / MONEY]
priceAmount [int >= 0]
```

领取条件：免费、消耗生命单位或消耗金钱。只能有一种价格；金额为 0 表示免费。创造模式免支付。

价格类型为空、金额为负数或免费价格包含非零金额时，会记录警告并回退到 `FREE/0`。无效的 NBT 枚举也记录警告并使用默认值。

```
breakPolicy [String: DROP_CONTENT / DISCARD_CONTENT]
```

破坏时掉落或丢弃展示物品，与内容来源、价格和联动独立。上锁时不掉落内容。

`autoUseOnAcquire [bool]` 控制领取后是否尝试自动使用，还需全局 `auto_use_passive_item` 配置开启。缺省为 false；`/isd item spawn` 显式开启。此设置与内容来源独立。

缺省配置为 `MANUAL`、`FREE/0`、`DROP_CONTENT`、未上锁、不自动使用。NBT 只读取当前格式；旧字段 `isDecoration`、`lifeCost`、`moneyCost` 不再生效。

可通过 `/give` 的 `BlockEntityTag` 获得配置好的方块物品，放置时由原版 `BlockItem` 加载配置。以下指令分别为免费生成型底座、生命商店、金钱商店和手动展示底座（枚举名称区分大小写）：

```mcfunction
/give @s isaac_disaster:pedestal{BlockEntityTag:{contentSource:"LOOT_TABLE",itemLootTable:"isaac_disaster:pools/item/default",priceType:"FREE",priceAmount:0,breakPolicy:"DISCARD_CONTENT",autoUseOnAcquire:1b}} 1

/give @s isaac_disaster:pedestal{BlockEntityTag:{contentSource:"LOOT_TABLE",itemLootTable:"isaac_disaster:pools/item/default",priceType:"LIFE",priceAmount:2,breakPolicy:"DISCARD_CONTENT",autoUseOnAcquire:1b}} 1

/give @s isaac_disaster:pedestal{BlockEntityTag:{contentSource:"LOOT_TABLE",itemLootTable:"isaac_disaster:pools/item/default",priceType:"MONEY",priceAmount:15,breakPolicy:"DISCARD_CONTENT",autoUseOnAcquire:1b}} 1

/give @s isaac_disaster:pedestal{BlockEntityTag:{contentSource:"MANUAL",priceType:"FREE",priceAmount:0,breakPolicy:"DROP_CONTENT",autoUseOnAcquire:0b}} 1
```

生成型底座的物品在放置后生成，需要附近有非创造、非旁观模式玩家，且 `disablePlaceholder` 游戏规则未开启。`/give` 得到的方块物品本身尚未进行抽取。生命价格的数字使用模组的生命单位。

也可以使用 `/isd item spawn <道具池> <坐标> [cost life|money <非负金额>]` 配置生成型底座。

---

### 2. 箱子方块（Chest）

可设置属性：

```
itemLootTable [String]
```

道具池

```
itemLootChance [double]
```

生成道具的概率

```
lootTable [String]
```

普通战利品池

```
locked [bool]
```

是否上锁

---

## 备注

* 当前文档为早期版本，后续可能发生变更
