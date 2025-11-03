package net.luojiuoscar.isaac_disaster.loot.type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.loot.ModLootTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class CoinLootEntry extends LootPoolSingletonContainer {
    private final int tier;

    protected CoinLootEntry(int tier, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.tier = tier;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        Item coin = getCoinFromTier(tier);
        if (coin != null) {
            consumer.accept(new ItemStack(coin));
        }
    }

    private static Item getCoinFromTier(int tier) {
        String id = switch (tier) {
            case 1 -> Config.COIN_TIER_1_ID.get();
            case 2 -> Config.COIN_TIER_2_ID.get();
            case 3 -> Config.COIN_TIER_3_ID.get();
            default -> "minecraft:air";
        };
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(id));
    }

    private static int getWeightFromTier(int tier) {
        return switch (tier) {
            case 1 -> Config.COIN_TIER_1_WEIGHT.get();
            case 2 -> Config.COIN_TIER_2_WEIGHT.get();
            case 3 -> Config.COIN_TIER_3_WEIGHT.get();
            default -> 0;
        };
    }

    // ---------------- 序列化器 ----------------
    public static class Serializer extends LootPoolSingletonContainer.Serializer<CoinLootEntry> {
        @Override
        public void serializeCustom(JsonObject json, CoinLootEntry entry, JsonSerializationContext context) {
            json.addProperty("tier", entry.tier);
        }

        @Override
        public CoinLootEntry deserialize(JsonObject json, JsonDeserializationContext context,
                                         int weight, int quality,
                                         LootItemCondition[] conditions,
                                         LootItemFunction[] functions) {
            int tier = json.get("tier").getAsInt();
            int w = getWeightFromTier(tier);
            return new CoinLootEntry(tier, w, quality, conditions, functions);
        }

    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootTypes.COIN_ENTRY.get();
    }
}
