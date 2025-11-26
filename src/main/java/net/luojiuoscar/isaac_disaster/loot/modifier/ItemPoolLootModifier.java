package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.loot.TempPoolManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.RegistryObject;
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
        if (objectArrayList.isEmpty()) return objectArrayList;

        ServerPlayer player;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        } else {
            player = null;
            return objectArrayList;
        }

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals("isaac_disaster") || !tableId.getPath().startsWith("pools/item/"))
            return objectArrayList;

        // 获取原表
        LootTable originalTable = lootContext.getLevel().getServer().getLootData().getLootTable(tableId);

        // 新的临时 entries
        List<LootPoolEntryContainer.Builder<?>> newEntries = new ArrayList<>();

        LootPool pool = originalTable.pools.get(0);// 假设只有一个 pool
        if (pool == null) return objectArrayList;

        // 遍历原 pool entries
        for (LootPoolEntryContainer entry : pool.entries) {
            if (entry instanceof LootItem lootItem) {
                // LootItem 直接保留
                ItemStack[] tempStack = new ItemStack[1];
                lootItem.createItemStack(s -> tempStack[0] = s, lootContext);
                Item item = tempStack[0].getItem();
                if (item instanceof IsaacItem isaacItem && !PoolHelper.isRemoved(player, tableId, isaacItem.getId())) {
                    newEntries.add(LootItem.lootTableItem(item));
                }
            } else {
                try {
                    // 展开 tag，将 tag 中的所有物品加入
                    String path = tableId.getPath(); // e.g., pools/item/passive_items
                    String tagName = path.substring(path.lastIndexOf('/') + 1); // passive_items
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(tableId.getNamespace(), tagName));

                    lootContext.getLevel().registryAccess().registryOrThrow(Registries.ITEM).getTag(tagKey).ifPresent(tagItems -> {
                        for (Holder<Item> holder : tagItems) {
                            Item item = holder.value();
                            if (item instanceof IsaacItem isaacItem && !PoolHelper.isRemoved(player, tableId, isaacItem.getId())) {
                                newEntries.add(LootItem.lootTableItem(item));
                            }
                        }
                    });
                }catch (Exception e){}
            }
        }

        // 加入玩家 addition
        for (int addId : PoolHelper.getAddition(player, tableId)) {
            RegistryObject<Item> addItemReg = ItemId.getItemById(addId);
            if (addItemReg != null) {
                Item addItem = addItemReg.get();
                newEntries.add(LootItem.lootTableItem(addItem));
            }
        }

        // 构建临时 pool
        LootPool.Builder poolBuilder = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        if (newEntries.isEmpty()) {
            poolBuilder.add(LootItem.lootTableItem(ModPassiveItems.BREAKFAST.get()));
        }else{
            for (LootPoolEntryContainer.Builder<?> builder : newEntries) {
                poolBuilder.add(builder);
            }
        }

        LootPool tempPool = poolBuilder.build();

        // 保存到TempPoolManager
        TempPoolManager.put(player, tempPool);

        // 生成物品
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        tempPool.addRandomItems(result::add, lootContext);

        return result;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
