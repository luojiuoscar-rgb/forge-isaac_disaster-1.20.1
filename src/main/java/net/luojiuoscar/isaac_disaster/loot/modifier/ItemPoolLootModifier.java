package net.luojiuoscar.isaac_disaster.loot.modifier;

import com.mojang.serialization.Codec;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.loot.LootContextHelper;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.loot.TempPoolManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class ItemPoolLootModifier extends LootModifier {
    private static final Gson LOOT_GSON = Deserializers.createLootTableSerializer().create();
    private static final EnumSet<LootGenerationMode> SUPPORTED_MODES =
            EnumSet.of(LootGenerationMode.NATURAL_DROP, LootGenerationMode.SPAWN_DROP);

    public static final Codec<ItemPoolLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, ItemPoolLootModifier::new));

    public ItemPoolLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (!SUPPORTED_MODES.contains(LootGenerationContext.currentMode())) return objectArrayList;
        if (objectArrayList.isEmpty()) return objectArrayList;

        ServerPlayer player = LootContextHelper.findResponsiblePlayer(lootContext);
        if (player == null) return objectArrayList;

        ResourceLocation tableId = lootContext.getQueriedLootTableId();
        ItemStack stack = objectArrayList.get(0);

        if (!(stack.getItem() instanceof IsaacItem) || objectArrayList.size() > 1 ||
                !tableId.getNamespace().equals(IsaacDisaster.MOD_ID) || !tableId.getPath().startsWith("pools/item/"))
            return objectArrayList;

        // 获取原表
        LootTable originalTable = lootContext.getLevel().getServer().getLootData().getLootTable(tableId);

        if (originalTable.pools.isEmpty()) return objectArrayList;
        LootPool pool = originalTable.pools.get(0); // Item pools currently contain one pool.
        List<LootPoolEntryContainer> newEntries = new ArrayList<>();
        try {
            for (LootPoolEntryContainer entry : pool.entries) {
                JsonObject data = LOOT_GSON.toJsonTree(entry, LootPoolEntryContainer.class).getAsJsonObject();
                if (entry instanceof LootItem) {
                    ResourceLocation itemId = ResourceLocation.parse(data.get("name").getAsString());
                    if (isAvailable(player, tableId, ForgeRegistries.ITEMS.getValue(itemId))) {
                        newEntries.add(entry); // Retain conditions, weight, quality, and functions.
                    }
                } else if (entry instanceof TagEntry) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM,
                            ResourceLocation.parse(data.get("name").getAsString()));
                    List<Item> items = new ArrayList<>();
                    lootContext.getLevel().registryAccess().registryOrThrow(Registries.ITEM)
                            .getTag(tag).ifPresent(holders -> {
                                for (Holder<Item> holder : holders) {
                                    if (isAvailable(player, tableId, holder.value())) items.add(holder.value());
                                }
                            });
                    if (data.get("expand").getAsBoolean()) {
                        for (Item item : items) {
                            JsonObject itemData = data.deepCopy();
                            itemData.addProperty("type", "minecraft:item");
                            itemData.addProperty("name", ForgeRegistries.ITEMS.getKey(item).toString());
                            itemData.remove("expand");
                            newEntries.add(LOOT_GSON.fromJson(itemData, LootPoolEntryContainer.class));
                        }
                    } else if (!items.isEmpty()) {
                        newEntries.add(new FilteredTagEntry(data, items));
                    }
                } else {
                    // Do not reinterpret other entry types as tags inferred from the table name.
                    newEntries.add(entry);
                }
            }
        } catch (RuntimeException e) {
            IsaacDisaster.LOGGER.warn("Could not rebuild item pool {}; retaining original loot", tableId, e);
            return objectArrayList;
        }

        // 加入玩家 addition
        for (ResourceLocation addItemId : PoolHelper.getAddition(player, tableId)) {
            Item addItem = ForgeRegistries.ITEMS.getValue(addItemId);
            if (addItem == null) continue;
            newEntries.add(LootItem.lootTableItem(addItem).build());
        }

        // 构建临时 pool
        LootPool.Builder poolBuilder = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        if (newEntries.isEmpty()) {
            poolBuilder.add(LootItem.lootTableItem(ModPassiveItems.BREAKFAST.get()));
        }else{
            for (LootPoolEntryContainer entry : newEntries) {
                poolBuilder.add(entryBuilder(entry));
            }
        }
        // Preserve the original pool settings as well as the entry settings.
        JsonObject poolData = LOOT_GSON.toJsonTree(pool, LootPool.class).getAsJsonObject();
        poolBuilder.setRolls(pool.getRolls()).setBonusRolls(pool.getBonusRolls());
        if (poolData.has("conditions")) {
            for (LootItemCondition condition : LOOT_GSON.fromJson(poolData.get("conditions"), LootItemCondition[].class)) {
                poolBuilder.when(() -> condition);
            }
        }
        if (poolData.has("functions")) {
            for (LootItemFunction function : LOOT_GSON.fromJson(poolData.get("functions"), LootItemFunction[].class)) {
                poolBuilder.apply(() -> function);
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

    private static boolean isAvailable(ServerPlayer player, ResourceLocation tableId, Item item) {
        ResourceLocation id = item == null ? null : ForgeRegistries.ITEMS.getKey(item);
        return item instanceof IsaacItem && id != null && !PoolHelper.isRemoved(player, tableId, id);
    }

    private static LootPoolEntryContainer.Builder<?> entryBuilder(LootPoolEntryContainer entry) {
        return new ExistingEntryBuilder(entry);
    }

    private static final class ExistingEntryBuilder extends LootPoolEntryContainer.Builder<ExistingEntryBuilder> {
        private final LootPoolEntryContainer entry;

        private ExistingEntryBuilder(LootPoolEntryContainer entry) { this.entry = entry; }

        @Override
        protected ExistingEntryBuilder getThis() { return this; }

        @Override
        public LootPoolEntryContainer build() { return entry; }
    }

    /** A non-expanded tag selects once and emits every remaining member. */
    private static final class FilteredTagEntry extends LootPoolSingletonContainer {
        private final List<Item> items;

        private FilteredTagEntry(JsonObject data, List<Item> items) {
            super(data.has("weight") ? data.get("weight").getAsInt() : 1,
                    data.has("quality") ? data.get("quality").getAsInt() : 0,
                    data.has("conditions") ? LOOT_GSON.fromJson(data.get("conditions"), LootItemCondition[].class)
                            : new LootItemCondition[0],
                    data.has("functions") ? LOOT_GSON.fromJson(data.get("functions"), LootItemFunction[].class)
                            : new LootItemFunction[0]);
            this.items = List.copyOf(items);
        }

        @Override
        public LootPoolEntryType getType() { return LootPoolEntries.TAG; }

        @Override
        protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
            for (Item item : items) consumer.accept(new ItemStack(item));
        }
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
