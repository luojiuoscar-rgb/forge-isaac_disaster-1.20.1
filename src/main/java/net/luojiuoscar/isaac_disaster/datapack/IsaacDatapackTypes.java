package net.luojiuoscar.isaac_disaster.datapack;

import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.ItemSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

/** Add future parameter documents here; gameplay callers never parse JSON themselves. */
public final class IsaacDatapackTypes {
    public static final DatapackDataType<GameplaySettings> GAMEPLAY = new DatapackDataType<>(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "gameplay/settings.json"),
            GameplaySettings.CODEC, GameplaySettings::defaults);
    private static final List<DatapackDataType<?>> TYPES = registeredTypes();

    private IsaacDatapackTypes() {}

    private static List<DatapackDataType<?>> registeredTypes() {
        List<DatapackDataType<?>> types = new ArrayList<>();
        types.add(GAMEPLAY);
        types.addAll(ItemSettings.types());
        return List.copyOf(types);
    }

    public static List<DatapackDataType<?>> types() { return TYPES; }

    public static Snapshot defaults() {
        Map<DatapackDataType<?>, Object> values = new LinkedHashMap<>();
        TYPES.forEach(type -> values.put(type, type.defaults().get()));
        return new Snapshot(values);
    }

    public static Snapshot load(ResourceManager resources) {
        Map<DatapackDataType<?>, Object> values = new LinkedHashMap<>();
        TYPES.forEach(type -> values.put(type, type.load(resources)));
        return new Snapshot(values);
    }

    public record Snapshot(Map<DatapackDataType<?>, Object> values) {
        public Snapshot { values = Map.copyOf(values); }

        @SuppressWarnings("unchecked")
        public <T> T get(DatapackDataType<T> type) { return (T) values.get(type); }
        public GameplaySettings gameplay() { return get(GAMEPLAY); }
    }
}
