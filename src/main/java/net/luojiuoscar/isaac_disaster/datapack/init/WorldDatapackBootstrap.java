package net.luojiuoscar.isaac_disaster.datapack.init;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datapack.DatapackDataType;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.ItemSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.function.Supplier;

/** Seeds once, before pack discovery. Neither reload nor pack deletion reimports TOML. */
public final class WorldDatapackBootstrap {
    public static final String PACK_ID = "file/" + IsaacDisaster.MOD_ID;
    private static final String MARKER = IsaacDisaster.MOD_ID + "_datapack.initialized";

    private WorldDatapackBootstrap() {}

    public static synchronized void ensure(Path world, Supplier<GameplaySettings> template) {
        Path marker = world.resolve(MARKER);
        if (Files.exists(marker)) return;
        Path pack = world.resolve("datapacks").resolve(IsaacDisaster.MOD_ID);
        try {
            if (!Files.exists(pack) && !Files.exists(pack.resolveSibling(IsaacDisaster.MOD_ID + ".zip"))) {
                // A completed folder is published only after both JSON and metadata are written.
                Path staging = world.resolve("." + IsaacDisaster.MOD_ID + "-datapack-" + UUID.randomUUID());
                Path settings = staging.resolve("data").resolve(IsaacDisaster.MOD_ID).resolve("gameplay/settings.json");
                Files.createDirectories(settings.getParent());
                Files.writeString(settings, new GsonBuilder().setPrettyPrinting().create().toJson(template.get().toJson()),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                Files.writeString(staging.resolve("pack.mcmeta"),
                        "{\"pack\":{\"pack_format\":15,\"description\":\"Isaac Disaster world settings\"}}\n",
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                for (var type : ItemSettings.types()) writeDefault(staging, type);
                Files.createDirectories(pack.getParent());
                Files.move(staging, pack);
            }
            Files.writeString(marker, "Initialized. This world uses datapack settings, not COMMON Config.\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException | RuntimeException error) {
            throw new IllegalArgumentException("Cannot initialize " + IsaacDisaster.MOD_ID + " datapack for "
                    + world + ": " + error.getMessage(), error);
        }
    }

    private static <T> void writeDefault(Path pack, DatapackDataType<T> type) throws IOException {
        Path file = pack.resolve("data").resolve(type.file().getNamespace()).resolve(type.file().getPath());
        if (Files.exists(file)) return;
        var json = type.codec().encodeStart(JsonOps.INSTANCE, type.defaults().get()).getOrThrow(false,
                error -> { throw new IllegalArgumentException(type.file() + ": " + error); });
        Files.createDirectories(file.getParent());
        Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }
}
