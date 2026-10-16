package net.luojiuoscar.isaac_disaster.datapack.gameplay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Complete immutable values for a single resource reload, independent of live TOML. */
public final class GameplaySettings {
    public static final Codec<GameplaySettings> CODEC = Codec.PASSTHROUGH.comapFlatMap(input -> {
        try {
            return DataResult.success(parse(input.convert(JsonOps.INSTANCE).getValue()));
        } catch (IllegalArgumentException error) {
            return DataResult.error(error::getMessage);
        }
    }, value -> new Dynamic<>(JsonOps.INSTANCE, value.toJson()));

    private final Map<String, Object> values;

    private GameplaySettings(Map<String, Object> values) {
        Map<String, Object> copy = new LinkedHashMap<>();
        values.forEach((id, value) -> copy.put(id, value instanceof List<?> list ? List.copyOf(list) : value));
        this.values = Map.copyOf(copy);
    }

    public static GameplaySettings defaults() {
        Map<String, Object> values = new LinkedHashMap<>();
        GameplayConfig.keys().forEach((id, key) -> values.put(id, key.defaultValue()));
        return new GameplaySettings(values);
    }

    /** Called only while creating the world's pack for the first time. */
    public static GameplaySettings fromTemplate() {
        JsonObject values = new JsonObject();
        GameplayConfig.keys().forEach((id, key) -> values.add(id, key.toJson(key.templateValue())));
        return parse(values);
    }

    public static GameplaySettings parse(JsonElement json) {
        if (!json.isJsonObject()) throw new IllegalArgumentException("Gameplay settings must be an object");
        Map<String, Object> values = new LinkedHashMap<>(defaults().values);
        json.getAsJsonObject().entrySet().forEach(entry -> {
            ConfigKey<?> key = GameplayConfig.keys().get(entry.getKey());
            if (key == null) throw new IllegalArgumentException("Unknown gameplay setting: " + entry.getKey());
            values.put(entry.getKey(), key.parse(entry.getValue()));
        });
        return new GameplaySettings(values);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) { return (T) values.get(key.id()); }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        GameplayConfig.keys().forEach((id, key) -> json.add(id, key.toJson(values.get(id))));
        return json;
    }
}
