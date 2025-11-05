package net.luojiuoscar.isaac_disaster.manager;

import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;

public class LootTableManager {
    // random drops
    public static final ResourceLocation RANDOM_COINS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/coins");
    public static final ResourceLocation RANDOM_PILLS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/pills");
    public static final ResourceLocation RANDOM_HEARTS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/hearts");
    public static final ResourceLocation RANDOM_BOMBS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/bombs");
    public static final ResourceLocation RANDOM_CARDS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/cards");
    public static final ResourceLocation RANDOM_KEYS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/keys");
    public static final ResourceLocation RANDOM_BATTERIES =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/batteries");
    public static final ResourceLocation RANDOM_SACKS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/sacks");
    public static final ResourceLocation RANDOM_TRINKETS =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/random/trinkets");
    // sacks
    public static final ResourceLocation GRAB_BAG =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/grab_bag");
    public static final ResourceLocation BLACK_SACK =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickups/black_sack");


    // item pools
    public static final ResourceLocation DEFAULT_ITEM_POOL =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pools/passive_items");


    // chests
    public static final ResourceLocation CHEST =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "chests/isaac/chest");
    public static final ResourceLocation LOCKED_CHEST =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "chests/isaac/locked_chest");
    public static final ResourceLocation GILDED_LOCKED_CHEST =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "chests/isaac/locked_chest_with_gilded_key");
    public static final ResourceLocation RED_CHEST =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "chests/isaac/red_chest");
    public static final ResourceLocation OLD_CHEST =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "chests/isaac/old_chest");


}
