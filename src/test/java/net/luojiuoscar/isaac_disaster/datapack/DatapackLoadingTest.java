package net.luojiuoscar.isaac_disaster.datapack;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.datapack.init.WorldDatapackBootstrap;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatapackLoadingTest {
    @TempDir Path root;

    private Path settings() throws Exception {
        Path file = root.resolve("data/isaac_disaster/gameplay/settings.json");
        Files.createDirectories(file.getParent());
        return file;
    }

    @Test void malformedJsonIncludesResourceAndSourcePackInTheFailure() throws Exception {
        Files.writeString(settings(), "{broken");
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("test-map-pack", root, false)))) {
            var error = assertThrows(IllegalArgumentException.class, () -> IsaacDatapackTypes.load(resources));
            assertTrue(error.getMessage().contains("gameplay/settings.json"));
            assertTrue(error.getMessage().contains("test-map-pack"));
        }
    }

    @Test void initializationFailurePropagatesWithTheWorldPath() throws Exception {
        Files.writeString(root.resolve("datapacks"), "blocks directory creation");
        var config = CommentedConfig.inMemory();
        config.set("items", List.of());
        Config.spec().setConfig(config);
        try {
            var error = assertThrows(IllegalArgumentException.class,
                    () -> WorldDatapackBootstrap.ensure(root, GameplaySettings::fromTemplate));
            assertTrue(error.getMessage().contains(root.toString()));
        } finally {
            Config.spec().setConfig(null);
        }
        assertFalse(Files.exists(root.resolve("isaac_disaster_datapack.initialized")));
    }

    @Test void loadingUsesTheActualResourceManagerAndPropagatesInvalidData() throws Exception {
        Path file = settings();
        Files.writeString(file, "{\"health_bonus\": 21.0}");
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("test-map-pack", root, false)))) {
            var prepared = IsaacDatapackTypes.load(resources);
            Files.writeString(file, "{\"health_bonus\": false}");
            assertThrows(IllegalArgumentException.class, () -> IsaacDatapackTypes.load(resources));
            assertEquals(21.0, prepared.gameplay().get(
                    net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig.HEALTH_BONUS));
        }
    }
}
