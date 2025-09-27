package net.luojiuoscar.isaac_disaster.util;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items{
        public static final TagKey<Item> PASSIVE_ITEMS = tag("passive_items");

        private static TagKey<Item> tag(String name){
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name));
        }
    }
}
