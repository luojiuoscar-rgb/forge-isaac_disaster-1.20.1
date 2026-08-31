package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public final class IsaacItemRegistryHelper {
    private static final Map<Integer, ResourceLocation> PASSIVE_ITEM_IDS = new HashMap<>();
    private static final Map<Integer, ResourceLocation> TRINKET_IDS = new HashMap<>();

    private IsaacItemRegistryHelper() {
    }

    public static void rebuildIdMappings() {
        PASSIVE_ITEM_IDS.clear();
        TRINKET_IDS.clear();

        ForgeRegistries.ITEMS.getEntries().forEach(entry -> {
            ResourceLocation id = entry.getKey().location();
            if (entry.getValue() instanceof PassiveItem passiveItem) {
                register(PASSIVE_ITEM_IDS, passiveItem.getId(), id, "passive item");
            } else if (entry.getValue() instanceof Trinket trinket) {
                register(TRINKET_IDS, trinket.getTrinketId(), id, "trinket");
            }
        });
    }

    public static ResourceLocation getPassiveItemResourceLocationFromId(int id) {
        return PASSIVE_ITEM_IDS.get(id);
    }

    public static ResourceLocation getTrinketResourceLocationFromId(int id) {
        return TRINKET_IDS.get(id);
    }

    private static void register(Map<Integer, ResourceLocation> mappings, int id,
                                  ResourceLocation resourceLocation, String category) {
        ResourceLocation previous = mappings.putIfAbsent(id, resourceLocation);
        if (previous != null) {
            throw new IllegalStateException("Duplicate " + category + " id " + id
                    + ": " + previous + " and " + resourceLocation);
        }
    }
}
