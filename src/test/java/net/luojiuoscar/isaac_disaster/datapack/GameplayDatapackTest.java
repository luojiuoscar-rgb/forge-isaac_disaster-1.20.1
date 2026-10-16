package net.luojiuoscar.isaac_disaster.datapack;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.config.IsaacClientConfig;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.luojiuoscar.isaac_disaster.datapack.init.WorldDatapackBootstrap;
import net.luojiuoscar.isaac_disaster.datapack.sync.GameplaySettingsS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class GameplayDatapackTest {
    @TempDir Path world;

    @AfterEach void unload() {
        Config.spec().setConfig(null);
        IsaacDatapackData.clearClient();
    }

    private void template(double health) {
        var config = CommentedConfig.inMemory();
        config.set("items", List.of());
        config.set(Config.HEALTH_BONUS.getPath(), health);
        Config.spec().setConfig(config);
    }

    @Test void seedsOnceAndNeverConsultsConfigAgainEvenIfPackIsDeleted() throws Exception {
        template(25.0);
        var calls = new AtomicInteger();
        WorldDatapackBootstrap.ensure(world, () -> { calls.incrementAndGet(); return GameplaySettings.fromTemplate(); });
        Path settings = world.resolve("datapacks/isaac_disaster/data/isaac_disaster/gameplay/settings.json");
        assertEquals(25.0, GameplaySettings.parse(JsonParser.parseString(Files.readString(settings))).get(GameplayConfig.HEALTH_BONUS));
        Files.writeString(settings, "{\"health_bonus\": 37.0}");
        template(90.0);
        WorldDatapackBootstrap.ensure(world, () -> { calls.incrementAndGet(); return GameplaySettings.fromTemplate(); });
        assertEquals(37.0, GameplaySettings.parse(JsonParser.parseString(Files.readString(settings))).get(GameplayConfig.HEALTH_BONUS));
        Files.delete(settings);
        WorldDatapackBootstrap.ensure(world, () -> { fail("must not reseed after initialization"); return null; });
        assertFalse(Files.exists(settings));
        assertEquals(1, calls.get());
    }

    @Test void preservesPreinstalledWorldPackWithoutReadingTemplate() throws Exception {
        Path pack = world.resolve("datapacks/isaac_disaster");
        Files.createDirectories(pack);
        Files.writeString(pack.resolve("pack.mcmeta"), "map-author-content");
        WorldDatapackBootstrap.ensure(world, () -> { fail("preinstalled pack must be preserved"); return null; });
        assertEquals("map-author-content", Files.readString(pack.resolve("pack.mcmeta")));
    }

    @Test void generatesFixedItemDefaultsAndDoesNotRestoreDeletedItemFiles() throws Exception {
        template(25.0);
        WorldDatapackBootstrap.ensure(world, GameplaySettings::fromTemplate);
        Path items = world.resolve("datapacks/isaac_disaster/data/isaac_disaster/gameplay/item");
        Path shield = items.resolve("holy_shield.json");
        assertTrue(Files.isRegularFile(shield));
        assertEquals(1.0, JsonParser.parseString(Files.readString(shield)).getAsJsonObject()
                .get("strength_per_stack").getAsDouble());
        assertEquals(0.007, JsonParser.parseString(Files.readString(items.resolve("money_is_power.json")))
                .getAsJsonObject().get("damage_per_coin").getAsDouble());
        Files.delete(shield);
        WorldDatapackBootstrap.ensure(world, () -> { fail("must not read TOML again"); return null; });
        assertFalse(Files.exists(shield));
    }

    @Test void rejectsRemovedConfigFields() {
        for (String key : List.of("holy_shield_strength", "money_is_power_strength", "coin_tier_1_weight",
                "coin_tier_2_weight", "coin_tier_3_weight")) {
            assertThrows(IllegalArgumentException.class,
                    () -> GameplaySettings.parse(JsonParser.parseString("{\"" + key + "\": 3}")), key);
        }
    }

    @Test void missingFieldsUseCanonicalDefaultsRegardlessOfTemplate() {
        template(91.0);
        var settings = GameplaySettings.parse(JsonParser.parseString("{\"damage_bonus\": 8.0}"));
        assertEquals(10.0, settings.get(GameplayConfig.HEALTH_BONUS));
        assertEquals(8.0, settings.get(GameplayConfig.DAMAGE_BONUS));
    }

    @Test void rejectsWrongTypesRangesUnknownFieldsAndNonfiniteValues() {
        for (String json : List.of("{\"health_bonus\":true}", "{\"health_bonus\":0}",
                "{\"passive_item_limit\":1.5}", "{\"active_item_auto_restore\":\"false\"}",
                "{\"health_bonus\":1e999}", "{\"typo_health_bonus\":8}", "[]", "{\"coin_tier_1_id\":\"INVALID ID\"}")) {
            assertThrows(IllegalArgumentException.class, () -> GameplaySettings.parse(JsonParser.parseString(json)), json);
        }
    }

    @Test void templateSnapshotContainsEveryRegisteredValueAndRemainsImmutable() {
        template(41.0);
        var settings = GameplaySettings.fromTemplate();
        Config.HEALTH_BONUS.set(77.0);
        assertEquals(41.0, settings.get(GameplayConfig.HEALTH_BONUS));
        assertEquals(GameplayConfig.keys().size(), settings.toJson().size());
        assertTrue(settings.toJson().has("default_passive_item_slots"));
        assertTrue(settings.toJson().has("max_active_bullets"));
        assertTrue(settings.toJson().has("items"));
    }

    @Test void snapshotPacketRoundTripsAllTypes() {
        template(32.0);
        var values = new LinkedHashMap<>(IsaacDatapackTypes.defaults().values());
        values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.fromTemplate());
        var source = new GameplaySettingsS2CPacket(new IsaacDatapackTypes.Snapshot(values));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            source.toBytes(buffer);
            var decoded = new GameplaySettingsS2CPacket(buffer);
            assertEquals(source.snapshot().gameplay().toJson(), decoded.snapshot().gameplay().toJson());
        } finally { buffer.release(); }
    }

    @Test void serverSnapshotNeverWritesLocalClientPreferencesOrLocalWorldTemplate() {
        template(41.0);
        var local = CommentedConfig.inMemory();
        local.set(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.getPath(), false);
        local.set(IsaacClientConfig.ATTRIBUTE_INDICATOR_LEFT_MARGIN.getPath(), 47);
        IsaacClientConfig.SPEC.setConfig(local);
        try {
            var values = new LinkedHashMap<>(IsaacDatapackTypes.defaults().values());
            values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.parse(JsonParser.parseString("{\"health_bonus\":12.0}")));
            IsaacDatapackData.acceptClient(new IsaacDatapackTypes.Snapshot(values));
            assertFalse(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
            assertEquals(47, IsaacClientConfig.ATTRIBUTE_INDICATOR_LEFT_MARGIN.get());
            assertEquals(41.0, Config.HEALTH_BONUS.get());
            assertEquals(12.0, GameplayConfig.HEALTH_BONUS.get());
            IsaacDatapackData.clearClient();
            assertFalse(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
            assertEquals(41.0, Config.HEALTH_BONUS.get());
        } finally {
            IsaacClientConfig.SPEC.setConfig(null);
        }
    }

}
