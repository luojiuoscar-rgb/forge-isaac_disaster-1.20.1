package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagManager {
    public static final TagKey<Item> PASSIVE_ITEMS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "passive_items"));
    public static final TagKey<Item> ACTIVE_ITEMS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "active_items"));
    public static final TagKey<Item> PICKUPS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups"));
    public static final TagKey<Item> TRINKETS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trinkets"));

    public static final TagKey<Item> ITEM_CANNOT_BE_DUPLICATED =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "item_cannot_be_duplicated"));


}
