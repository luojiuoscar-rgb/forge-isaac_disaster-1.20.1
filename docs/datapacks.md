# Isaac Disaster 世界数据包

适用于 Minecraft Forge 1.20.1（`pack_format: 15`）。每个服务器的所有维度共享当前世界的数据包规则。

## 配置的归属

- CLIENT 配置用于玩家自己的 HUD 等本地显示设置，始终由玩家控制。
- 配置界面的服务端项目是新世界模板：仅在世界首次初始化时导出到数据包。
- 世界初始化后，玩法规则一律读取已启用的数据包。连接者本地的服务端模板不会影响远端服务器或已有地图。
- 服务器同步到客户端的是只读玩法数据缓存，不会修改玩家的 CLIENT 配置文件。

## 自动生成的文件

```text
<世界>/
├─ isaac_disaster_datapack.initialized
└─ datapacks/isaac_disaster/
   ├─ pack.mcmeta
   └─ data/isaac_disaster/gameplay/
      ├─ settings.json
      └─ item/
         ├─ holy_shield.json
         └─ money_is_power.json
```

首次载入未初始化的世界时，模组在数据包扫描前生成文件。目录名、命名空间、包 ID 与世界标记均由模组的 `MOD_ID` 派生，目前为 `isaac_disaster`。

初始化标记阻止再次导入 TOML。之后修改 Config，或者删除、禁用生成包，都不会重新从 Config 导入。已有同名文件夹或 ZIP 保留原内容；ZIP 不会被自动改写。

## 世界通用规则

`gameplay/settings.json` 是平铺 JSON 对象，字段名对应 COMMON Config 的叶子键。自动生成包含全部现行 64 个键；以下只是示例：

```json
{
  "default_max_health": 40.0,
  "health_bonus": 5.0,
  "active_item_auto_restore": false,
  "passive_item_limit": 100,
  "coin_tier_1_id": "isaac_disaster:penny",
  "coin_tier_1_value": 1
}
```

字段类型与范围沿用配置定义。缺失文件或字段使用代码默认值，不会读取当前 TOML。

## 道具规则

具体道具参数完全由 `gameplay/item/` 下的文件控制，不再出现在 Config 或配置界面中。首次生成使用固定默认值，修改文件后执行 `/reload`。

`holy_shield.json`：

```json
{ "strength_per_stack": 1.0 }
```

神圣屏障会免疫本次伤害；伤害超过 `(min(amplifier, 10) + 1) × strength_per_stack` 时额外消耗一层护盾。该参数是免消耗伤害阈值的系数，不是最大可阻挡伤害。神圣斗篷、神圣卡等提供的该效果共用此规则。

`money_is_power.json`：

```json
{ "damage_per_coin": 0.007 }
```

金钱就是力量的攻击力加成为钱币总价值乘以 `damage_per_coin`。两个参数均须为有限数字，范围 `[0, 99999]`。缺字段采用上述固定默认值，未知字段或错误类型会报错。

仅已登记支持配置的道具生成文件。给任意未支持的道具添加 JSON 不会自动改变其 Java 行为。

## 钱币掉落权重

钱币种类和价值仍由世界规则中的 `coin_tier_*_id`、`coin_tier_*_value` 决定。权重由各战利品表独立控制，使用原版 `weight` 和 `quality`，不会被全局钱币权重覆盖。

随机钱币表的位置是 `data/isaac_disaster/loot_tables/pickups/random/coins.json`，随模组提供，默认三档权重为 93、6、1。地图作者可以在自己的数据包中提供同路径文件覆盖它，例如：

```json
{
  "pools": [{
    "rolls": 1,
    "entries": [
      { "type": "isaac_disaster:coin_entry", "tier": 1, "weight": 93 },
      { "type": "isaac_disaster:coin_entry", "tier": 2, "weight": 6 },
      { "type": "isaac_disaster:coin_entry", "tier": 3, "weight": 1 }
    ]
  }]
}
```

## 地图包覆盖与重载

任意已启用的数据包都可以提供相同资源路径，不要求外层包名为 `isaac_disaster`。生成包首次发现时放在较低优先级，地图包可以覆盖它；之后管理员可以通过原版 `/datapack` 调整顺序和启用状态。

覆盖单位是单个完整 JSON 文件，不逐字段合并多个包。高优先级文件省略的字段采用代码默认值。道具各有文件，因此修改一个道具无需复制其他道具配置。

`/reload` 类型校验失败时，由原版命令报告重载失败；日志包含具体文件、来源包和错误原因，上一次有效玩法快照保持生效。配置内容过长、无法同步时，也会在准备阶段拒绝加载。成功后整体安装世界规则与道具规则，刷新在线玩家基础属性、槽位、倍率及飞行数据，再同步只读玩法快照。

首次加载沿用原版资源加载与 Forge 事件流程；新建世界复用预加载结果时，会在初始化阶段读取实际世界资源。数据校验或初始化文件写入失败会直接向调用链传播并中止加载，具体原因见日志；显示形式由原版对应入口决定。

刷新不会追溯重算已获得的永久道具加成或已存在的投射物。客户端与服务端需使用匹配的玩法同步协议。

## 扩展配置类型

模组开发者通过 `DatapackDataType<T>` 定义资源位置、Codec 和固定默认值，在 `ItemSettings.types()` 的清单中登记道具文档；生成、加载和网络快照共用该清单。游戏运行时读取不可变内存快照，不在道具发动时解析 JSON。

新增自定义文档也可在 `IsaacDatapackTypes` 登记。物品集合优先使用原版 `data/<namespace>/tags/items/*.json`。开发验证命令：`gradlew.bat test`；数据包专项集成验证：`gradlew.bat runGameTestServer -PgameTestNamespaces=isaac_disaster_datapack`。
