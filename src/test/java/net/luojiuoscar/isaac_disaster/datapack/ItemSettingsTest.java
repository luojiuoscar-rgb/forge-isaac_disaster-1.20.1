package net.luojiuoscar.isaac_disaster.datapack;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.HolyShieldSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.ItemSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.MoneyIsPowerSettings;
import net.luojiuoscar.isaac_disaster.datapack.sync.GameplaySettingsS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemSettingsTest {
    @AfterEach void unload() { IsaacDatapackData.clearClient(); }

    @Test void omittedItemFieldsUseFixedDefaults() {
        var json = JsonParser.parseString("{}");
        assertEquals(1.0, HolyShieldSettings.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow().strengthPerStack());
        assertEquals(0.007, MoneyIsPowerSettings.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow().damagePerCoin());
    }

    @Test void itemCodecsRejectWrongTypesRangesUnknownFieldsAndNonfiniteValues() {
        for (String field : List.of("strength_per_stack", "damage_per_coin")) {
            var codec = field.equals("strength_per_stack") ? HolyShieldSettings.CODEC : MoneyIsPowerSettings.CODEC;
            for (String value : List.of("true", "\"2\"", "null", "[]", "{}", "-0.1", "100000", "1e999")) {
                var json = JsonParser.parseString("{\"" + field + "\":" + value + "}");
                assertTrue(codec.parse(JsonOps.INSTANCE, json).error().isPresent(), json.toString());
            }
            for (String json : List.of("[]", "null", "{\"typo\":2}")) {
                assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent(), json);
            }
            for (String value : List.of("0", "99999")) {
                assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"" + field + "\":" + value + "}")).result().isPresent());
            }
        }
    }

    @Test void everyItemDocumentIsRegisteredAndDefaultsAreUsable() {
        var snapshot = IsaacDatapackTypes.defaults();
        assertTrue(ItemSettings.types().contains(ItemSettings.HOLY_SHIELD));
        assertTrue(ItemSettings.types().contains(ItemSettings.MONEY_IS_POWER));
        for (var type : ItemSettings.types()) {
            assertNotNull(snapshot.values().get(type));
            assertTrue(type.file().getPath().startsWith("gameplay/item/"));
        }
        assertEquals(1.0, ItemSettings.holyShield().strengthPerStack());
        assertEquals(0.007, ItemSettings.moneyIsPower().damagePerCoin());
    }

    @Test void packetInstallsAllItemValuesAndDisconnectRestoresDefaults() {
        var values = new LinkedHashMap<>(IsaacDatapackTypes.defaults().values());
        values.put(ItemSettings.HOLY_SHIELD, new HolyShieldSettings(9.5));
        values.put(ItemSettings.MONEY_IS_POWER, new MoneyIsPowerSettings(0.25));
        var source = new GameplaySettingsS2CPacket(new IsaacDatapackTypes.Snapshot(values));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            source.toBytes(buffer);
            var decoded = new GameplaySettingsS2CPacket(buffer);
            IsaacDatapackData.acceptClient(decoded.snapshot());
            assertEquals(9.5, ItemSettings.holyShield().strengthPerStack());
            assertEquals(0.25, ItemSettings.moneyIsPower().damagePerCoin());
            assertEquals(source.snapshot().gameplay().toJson(), decoded.snapshot().gameplay().toJson());
            IsaacDatapackData.clearClient();
            assertEquals(1.0, ItemSettings.holyShield().strengthPerStack());
            assertEquals(0.007, ItemSettings.moneyIsPower().damagePerCoin());
        } finally { buffer.release(); }
    }
}
