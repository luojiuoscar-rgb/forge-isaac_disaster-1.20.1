package net.luojiuoscar.isaac_disaster.manager;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TagManager {
    public static final TagKey<Block> PENETRABLE_BLOCKS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("isaac_disaster", "penetrable_blocks"));

    public static final TagKey<Block> PASSIVE_ITEMS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("isaac_disaster", "passive_items"));

    public static final TagKey<Block> ACTIVE_ITEMS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("isaac_disaster", "active_items"));

    public static final TagKey<Block> PICKUPS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("isaac_disaster", "pickups"));


}
