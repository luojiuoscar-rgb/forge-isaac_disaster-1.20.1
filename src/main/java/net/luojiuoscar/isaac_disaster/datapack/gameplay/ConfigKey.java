package net.luojiuoscar.isaac_disaster.datapack.gameplay;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** A typed setting. Forge supplies schema metadata and the initial template only. */
public final class ConfigKey<T> {
    private static final Gson GSON = new Gson();
    private final String id;
    private final ForgeConfigSpec.ConfigValue<T> template;
    private final ForgeConfigSpec.ValueSpec spec;

    ConfigKey(String id, ForgeConfigSpec.ConfigValue<T> template, ForgeConfigSpec.ValueSpec spec) {
        this.id = id;
        this.template = template;
        this.spec = spec;
    }

    public String id() { return id; }
    public T get() { return IsaacDatapackData.current().gameplay().get(this); }

    T defaultValue() { return template.getDefault(); }
    T templateValue() { return template.get(); }

    Object parse(JsonElement json) {
        Object fallback = defaultValue();
        Object value;
        try {
            if (fallback instanceof List<?>) {
                if (!json.isJsonArray()) throw new IllegalArgumentException("Expected a string array");
                value = json.getAsJsonArray().asList().stream().map(element -> {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                        throw new IllegalArgumentException("Expected a string array");
                    }
                    return element.getAsString();
                }).toList();
            } else {
                if (!json.isJsonPrimitive()) throw new IllegalArgumentException("Expected a primitive value");
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (fallback instanceof Boolean && primitive.isBoolean()) value = primitive.getAsBoolean();
                else if (fallback instanceof Integer && primitive.isNumber()) value = primitive.getAsBigDecimal().intValueExact();
                else if (fallback instanceof Double && primitive.isNumber()) {
                    double number = primitive.getAsDouble();
                    if (!Double.isFinite(number)) throw new IllegalArgumentException("Expected a finite number");
                    value = number;
                } else if (fallback instanceof String && primitive.isString()) value = primitive.getAsString();
                else throw new IllegalArgumentException("Wrong value type");
            }
            if (id.startsWith("coin_tier_") && id.endsWith("_id")
                    && ResourceLocation.tryParse((String) value) == null) {
                throw new IllegalArgumentException("Invalid item resource location");
            }
            if (!spec.test(value)) throw new IllegalArgumentException("Value outside the configured range");
            return value;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException(id + ": " + error.getMessage(), error);
        }
    }

    JsonElement toJson(Object value) { return GSON.toJsonTree(value); }
}
