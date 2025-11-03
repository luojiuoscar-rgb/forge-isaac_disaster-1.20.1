package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_,
                               CompletableFuture<TagLookup<Block>> p_275322_,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, IsaacDisaster.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addItemTag(TagManager.PASSIVE_ITEMS, ItemListManager.PASSIVE_ITEM_LIST);
        addItemTag(TagManager.ACTIVE_ITEMS, ItemListManager.ACTIVE_ITEM_LIST);
        addItemTag(TagManager.TRINKETS, ItemListManager.TRINKET_LIST);
        addItemTag(TagManager.PICKUPS, ItemListManager.PICKUP_LIST);
    }


    private void addItemTag(TagKey<Item> tagKey, List<RegistryObject<Item>> itemList) {
        var tagAppender = this.tag(tagKey);
        itemList.forEach(item -> tagAppender.add(item.get()));
    }

}