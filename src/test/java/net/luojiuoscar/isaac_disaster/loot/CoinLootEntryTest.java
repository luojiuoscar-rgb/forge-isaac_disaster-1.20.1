package net.luojiuoscar.isaac_disaster.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.luojiuoscar.isaac_disaster.loot.type.CoinLootEntry;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CoinLootEntryTest {
    @Test
    void entriesForTheSameTierKeepTheirOwnLootTableWeights() {
        LootPoolEntry common = expand(new Coin(1, 17, 0));
        LootPoolEntry rare = expand(new Coin(1, 3, 0));

        assertAll(
                () -> assertEquals(17, common.getWeight(0)),
                () -> assertEquals(3, rare.getWeight(0)));
    }

    @Test
    void qualityAdjustsEachEntryWeightWithVanillaRoundingAndZeroFloor() {
        LootPoolEntry entry = expand(new Coin(2, 7, 3));

        assertAll(
                () -> assertEquals(11, entry.getWeight(1.5F)),
                () -> assertEquals(5, entry.getWeight(-0.5F)),
                () -> assertEquals(0, entry.getWeight(-3.0F)));
    }

    @Test
    void serializationPreservesLootTableWeightQualityAndFunctions() {
        var serializer = new CoinLootEntry.Serializer();
        var gson = Deserializers.createFunctionSerializer()
                .registerTypeAdapter(Coin.class, (JsonSerializer<Coin>) (entry, type, context) -> {
                    JsonObject json = new JsonObject();
                    serializer.serializeCustom(json, entry, context);
                    return json;
                }).create();
        Coin entry = new Coin(3, 19, -2,
                SetItemCountFunction.setCount(ConstantValue.exactly(4)).build());

        JsonObject json = gson.toJsonTree(entry).getAsJsonObject();

        assertAll(
                () -> assertEquals(3, json.get("tier").getAsInt()),
                () -> assertEquals(19, json.get("weight").getAsInt()),
                () -> assertEquals(-2, json.get("quality").getAsInt()),
                () -> assertEquals("minecraft:set_count", json.getAsJsonArray("functions")
                        .get(0).getAsJsonObject().get("function").getAsString()),
                () -> assertEquals(4, json.getAsJsonArray("functions")
                        .get(0).getAsJsonObject().get("count").getAsInt()));
    }

    private static LootPoolEntry expand(Coin entry) {
        var expanded = new ArrayList<LootPoolEntry>();
        assertTrue(entry.expand(null, expanded::add));
        assertEquals(1, expanded.size());
        return expanded.get(0);
    }

    private static final class Coin extends CoinLootEntry {
        private Coin(int tier, int weight, int quality, LootItemFunction... functions) {
            super(tier, weight, quality, new LootItemCondition[0], functions);
        }
    }
}
