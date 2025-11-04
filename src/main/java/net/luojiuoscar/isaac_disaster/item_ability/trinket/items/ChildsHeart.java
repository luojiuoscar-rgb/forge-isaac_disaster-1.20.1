package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChildsHeart implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.CHILDS_HEART.getId();
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.CHILDS_HEART.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.childs_heart.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.higher_prob")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }
}
