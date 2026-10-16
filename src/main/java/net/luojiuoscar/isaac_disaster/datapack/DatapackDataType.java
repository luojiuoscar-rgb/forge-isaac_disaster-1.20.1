package net.luojiuoscar.isaac_disaster.datapack;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Supplier;

/** Reusable contract for one named JSON document, decoded by its own Codec. */
public record DatapackDataType<T>(ResourceLocation file, Codec<T> codec, Supplier<T> defaults) {
    public T load(ResourceManager resources) {
        var resource = resources.getResource(file);
        if (resource.isEmpty()) return defaults.get();
        try (var reader = resource.get().openAsReader()) {
            return codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow(false,
                    error -> { throw new IllegalArgumentException(error); });
        } catch (IOException | RuntimeException error) {
            throw new IllegalArgumentException(file + " (" + resource.get().sourcePackId() + "): "
                    + error.getMessage(), error);
        }
    }
}
