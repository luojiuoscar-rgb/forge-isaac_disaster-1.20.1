# ISAAC DISASTER
*made by LuojiuOscar*

### BLOCK ENTITIES
1. give pedestal with loots: ```/give @s isaac_disaster:pedestal{BlockEntityTag:{LootTable:"isaac_disaster:pools/item/passive_items",isDecoration:0b}}```
2. locked_chest: 20% to spawn an item; If it spawns any item then the original drop in it will be cleared. (tag: itemLootTable)
3. players(not creative & spectator) within **5** blocks from a pedestal will activate it and spawns item based on its lootTables.

### ITEM POOLS
1. all loot table should be located under **data/isaac_disaster/loot_tables/pools/** folder. All **item** pool should be under **data/isaac_disaster/loot_tables/pools/item/**
2. all Item pool under the **specified folder** should contain only 1 roll result. (also trinkets)
3. if you wants to use mixed loot tables, use loot table references.


