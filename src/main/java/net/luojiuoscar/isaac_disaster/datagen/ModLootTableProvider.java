package net.luojiuoscar.isaac_disaster.datagen;

import com.google.common.collect.ImmutableList;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Set;
import java.util.function.BiConsumer;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(), ImmutableList.of(
                new SubProviderEntry(PoolLootTables::new, LootContextParamSets.EMPTY)
        ));
    }

    public static class PoolLootTables implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> writer) {
            addItemPool(writer, "passive_items", TagManager.PASSIVE_ITEMS);
            addItemPool(writer, "active_items", TagManager.ACTIVE_ITEMS);
            addTrinketPool(writer, "trinkets", TagManager.TRINKETS);
        }

        private void addTrinketPool(BiConsumer<ResourceLocation, LootTable.Builder> writer, String name, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
            LootTable.Builder table = LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                            .add(TagEntry.expandTag(tag))
                    );

            writer.accept(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pools/trinket/" + name), table);
        }

        private void addItemPool(BiConsumer<ResourceLocation, LootTable.Builder> writer, String name, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
            LootTable.Builder table = LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                            .add(TagEntry.expandTag(tag))
                    );

            writer.accept(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pools/item/" + name), table);
        }
    }
}
