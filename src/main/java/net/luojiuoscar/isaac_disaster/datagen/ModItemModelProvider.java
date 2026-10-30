package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
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
        ItemListManager.GENERIC_LIST.forEach(r -> registerIfNotSkipped.accept(r.get()));

        getBuilder("moms_knife_weapon").parent(new ModelFile.UncheckedModelFile("builtin/entity")).transforms()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 90, 25).scale(0.68F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, 25).scale(0.68F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -90, 10).scale(0.85F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 90, -10).scale(0.85F).end();
    }
}
