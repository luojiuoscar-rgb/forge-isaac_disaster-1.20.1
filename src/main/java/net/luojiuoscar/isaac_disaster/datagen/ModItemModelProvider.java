package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

public class ModItemModelProvider extends ItemModelProvider {


    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, IsaacDisaster.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 直接从统一列表中获取所有被动物品，无需手动逐个添加
        ModPassiveItems.PASSIVE_ITEM_LIST.forEach(itemRegistry -> {
            // 对列表中的每个物品注册基础模型
            Item item = itemRegistry.get();
            basicItem(item);
        });
    }
}
