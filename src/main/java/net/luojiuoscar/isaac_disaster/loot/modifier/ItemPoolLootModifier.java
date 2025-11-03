package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemPoolLootModifier extends LootModifier {
    public static final Codec<ItemPoolLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, ItemPoolLootModifier::new));

    public ItemPoolLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (objectArrayList.isEmpty() ||
                !(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player))
            return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/item/"))
            return objectArrayList;

        // 获取原表
        LootTable originalTable = lootContext.getLevel().getServer().getLootData().getLootTable(tableId);

        // 构建新的临时 entries
        List<LootPoolEntryContainer> newEntries = new ArrayList<>();

        LootPool pool = originalTable.pools.get(0); // first pool
        if (pool == null) return objectArrayList;

        for (LootPoolEntryContainer entry : pool.entries) {
            if (entry instanceof LootItem lootItem) {
                final Item[] result = new Item[1];
                lootItem.createItemStack(s -> result[0] = s.getItem(), lootContext);
                Item item = result[0];

                if (item instanceof IsaacItem isaacItem &&
                        !PoolHelper.isRemoved(player, tableId, isaacItem.getItemId())) {
                    newEntries.add(entry);
                }
            }
        }

//        // 加入玩家 addition
//        for (int addId : PoolHelper.getAddition(player, tableId)) {
//            Item addItem = ItemId.A_DOLLAR
//            if (addItem != null) {
//                newEntries.add(LootItem.lootTableItem(addItem).build());
//            }
//        }
//
//        // 构建单次 roll 的临时 LootPool
//        LootPool tempPool = new LootPool(
//                newEntries.toArray(LootPoolEntryContainer[]::new),
//                new LootItemCondition[0], // 没有额外条件
//                new LootItemFunction[0],  // 没有额外函数
//                1, 1, 1                   // min=1, max=1, rolls=1
//        );
//
//        // 临时 LootTable 包裹
//        LootTable tempTable = new LootTable(new LootPool[]{tempPool});

//        // roll 一次
//        ObjectArrayList<ItemStack> result = ObjectArrayList.of();
//        tempTable.getRandomItems(lootContext, result::add);

        return objectArrayList;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
