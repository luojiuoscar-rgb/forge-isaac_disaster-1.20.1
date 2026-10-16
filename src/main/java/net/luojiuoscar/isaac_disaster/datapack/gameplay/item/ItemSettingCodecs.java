package net.luojiuoscar.isaac_disaster.datapack.gameplay.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

final class ItemSettingCodecs {
    private ItemSettingCodecs() {}

    static double validate(String field, double value) {
        if (!Double.isFinite(value) || value < 0 || value > 99999) {
            throw new IllegalArgumentException(field + " must be a finite number in [0, 99999]");
        }
        return value;
    }

    static <T> Codec<T> singleDouble(String field, Supplier<T> defaults,
                                     DoubleFunction<T> create, ToDoubleFunction<T> read) {
        return Codec.PASSTHROUGH.comapFlatMap(input -> {
            try {
                var json = input.convert(JsonOps.INSTANCE).getValue();
                if (!json.isJsonObject()) throw new IllegalArgumentException("Item settings must be an object");
                var object = json.getAsJsonObject();
                for (String name : object.keySet()) {
                    if (!name.equals(field)) throw new IllegalArgumentException("Unknown item setting: " + name);
                }
                if (!object.has(field)) return DataResult.success(defaults.get());
                var value = object.get(field);
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    throw new IllegalArgumentException(field + " must be a number");
                }
                return DataResult.success(create.apply(validate(field, value.getAsDouble())));
            } catch (IllegalArgumentException error) {
                return DataResult.error(error::getMessage);
            }
        }, value -> {
            var json = new JsonObject();
            json.addProperty(field, read.applyAsDouble(value));
            return new Dynamic<>(JsonOps.INSTANCE, json);
        });
    }
}
