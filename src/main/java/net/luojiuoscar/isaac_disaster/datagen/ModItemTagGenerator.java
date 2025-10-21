package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                               CompletableFuture<TagLookup<Block>> p_275322_,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, IsaacDisaster.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // 从统一列表中遍历所有被动物品，自动添加到标签中
        var passiveItemsTag = this.tag(TagManager.PASSIVE_ITEMS);
        ItemListManager.PASSIVE_ITEM_LIST.forEach(itemRegistry -> {
            passiveItemsTag.add(itemRegistry.get());
        });

        var activeItemsTag = this.tag(TagManager.ACTIVE_ITEMS);
        ItemListManager.ACTIVE_ITEM_LIST.forEach(itemRegistry -> {
            activeItemsTag.add(itemRegistry.get());
        });

        var trinketsTag = this.tag(TagManager.TRINKETS);
        ItemListManager.TRINKET_LIST.forEach(itemRegistry -> {
            trinketsTag.add(itemRegistry.get());
        });

        var pickupsTag = this.tag(TagManager.PICKUPS);
        ItemListManager.PICKUP_LIST.forEach(itemRegistry -> {
            pickupsTag.add(itemRegistry.get());
        });
    }

}