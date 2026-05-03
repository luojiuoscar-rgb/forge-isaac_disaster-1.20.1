# ISAAC DISASTER
*made by LuojiuOscar*

## INTRODUCTION
此模组目标在于还原以撒中的各类道具。[帮助文档](docs/documentation.md)

目前内容包括：
- 165个道具，包括主动和被动道具
- 40个饰品
- 25张卡牌
- 45+的药丸效果
- 还有更多！

模组的开发正在缓慢进行中，欢迎大家关注我的b站账号[洛玖Oscar](https://space.bilibili.com/250685960)！

---

## INTRODUCTION
The goal of this mod is to recreate various items from The Binding of Isaac.

Current content includes:
- 165 items, including active and passive items
- 40 trinkets
- 25 cards
- 45+ pill effects
- And more!

Development of the mod is progressing slowly. Welcome to follow my Bilibili account[洛玖Oscar](https://space.bilibili.com/250685960)!

---

## NOTES
<details>
<summary>click to expand</summary>

 - data/forge/loot_modifiers/global_loot_modifiers.json is overwritten by custom provider: ForgeGlobalLootModifiersProvider. 
This will ensure that all loot modifiers are in the correct order.
 - Removal of an **ITEM** from corresponding item pool is processed manually. 
Ensures in the future item could be generated from pool without removing it from pool.
**TRINKETS** will not be removed from any pool in any circumstance.
 - **ITEM** displayed on any blocks will only be grabbed when player's hand is empty.

#### BLOCK ENTITIES
 - Give pedestal with loots: ```/give @s isaac_disaster:pedestal{BlockEntityTag:{itemLootTable:"isaac_disaster:pools/item/passive_items",isDecoration:0b}}```
 - Debug stick could link multiple pedestals.
 - Players(not creative & spectator) within **5** blocks from a pedestal will activate it and spawns item based on its lootTables.
 - Locked_chest/old_chest: 20% to spawn an item; If it spawns any item then the original drop in it will be cleared. (tag: itemLootTable)
 - A chest without any itemLootTable will spawn item from the default pool, but they do not have default LootTable.
 - Positions of blocks containing items should be stored into IsaacItemBlockData. When the item disappears it should also be removed.

#### ITEM POOLS & LOOT TABLES
 - Generating more than 1 item from item pool may cause unpredictable error. Item display will always choose the first item in the container.
 - Only pools under  *data/isaac_disaster/loot_tables/pools/item/* folder will be affected by other items.
 - Items generated from pedestal & chests will be marked as removal.
 - Chest Loots are located under *resource/data/isaac_disaster/loot_tables/chests/isaac/* folder.

</details>

# CREDITS
I would like to thank the following contributors for their invaluable help and support:

- [@半只橘猫](https://github.com/cutvmh307): Provided preset loot tables for item pools.



