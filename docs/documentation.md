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
isDecoration [bool]
```

是否为装饰性底座（无实际功能效果）

```
itemLootTable [String]
```

绑定的道具池

```
locked [bool]
```

是否上锁（可使用钥匙解锁）

```
lifeCost [int]
```

生命消耗（单位数）
优先级高于金钱消耗

```
moneyCost [int]
```

金钱消耗

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
