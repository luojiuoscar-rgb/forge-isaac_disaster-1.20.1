package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Set;
import java.util.function.Consumer;

public class ModItemModelProvider extends ItemModelProvider {


    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, IsaacDisaster.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Set<Item> skipped = Set.of(
                ModItems.GIGA_BOMB.get()
        );

        Consumer<Item> registerIfNotSkipped = (item) -> {
            if (!skipped.contains(item)) {
                basicItem(item);
            }
        };

        ItemListManager.PASSIVE_ITEM_LIST.forEach(r -> registerIfNotSkipped.accept(r.get()));
        ItemListManager.ACTIVE_ITEM_LIST.forEach(r -> registerIfNotSkipped.accept(r.get()));
        ItemListManager.TRINKET_LIST.forEach(r -> registerIfNotSkipped.accept(r.get()));
        ItemListManager.PICKUP_LIST.forEach(r -> registerIfNotSkipped.accept(r.get()));
    }
}
