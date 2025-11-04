# ISAAC DISASTER
*made by LuojiuOscar*

## NOTES
1. data/forge/loot_modifiers/global_loot_modifiers.json is overwritten by custom provider: ForgeGlobalLootModifiersProvider. 
This will ensure that all loot modifiers are in the correct order.
2. Removal of an **ITEM** from corresponding item pool is processed manually. 
Ensures in the future item could be generated from pool without removing it.
**TRINKETS** will not be removed in any circumstance.
3. **ITEM** displayed on any blocks will only be grabbed when player's hand is empty.

#### BLOCK ENTITIES
1. Give pedestal with loots: ```/give @s isaac_disaster:pedestal{BlockEntityTag:{itemLootTable:"isaac_disaster:pools/item/passive_items",isDecoration:0b}}```
2. Debug stick could link multiple pedestals.
3. Players(not creative & spectator) within **5** blocks from a pedestal will activate it and spawns item based on its lootTables.
4. Locked_chest/old_chest: 20% to spawn an item; If it spawns any item then the original drop in it will be cleared. (tag: itemLootTable)
5. A chest without any itemLootTable will spawn item from the default pool.

#### ITEM POOLS
1. All loot table should be located under *data/isaac_disaster/loot_tables/pools/* folder. All **item** pool should be under *data/isaac_disaster/loot_tables/pools/item/*
2. All Item pool under the **specified folder** should contain only 1 roll result. (also trinkets)
3. If you wants to use mixed loot tables, use loot table references.
4. Items generated from pedestal & chests will be marked as removal.

## MIXIN
 - LivingEntity getDimensions()

## ACCESS TRANSFORMER
 - PrimedTnt.owner
 - LivingEntity.getEyeHeight()
 - Creeper.explodeCreeper()
 - LootTable.pools
 - LootPool.entries




