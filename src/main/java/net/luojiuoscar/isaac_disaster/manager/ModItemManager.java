package net.luojiuoscar.isaac_disaster.manager;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModItemManager {
    // 统一存储所有被动道具（物品形式）
    public static final List<RegistryObject<Item>> PASSIVE_ITEM_LIST = new ArrayList<>();
    public static final List<RegistryObject<Item>> ACTIVE_ITEM_LIST = new ArrayList<>();


}
