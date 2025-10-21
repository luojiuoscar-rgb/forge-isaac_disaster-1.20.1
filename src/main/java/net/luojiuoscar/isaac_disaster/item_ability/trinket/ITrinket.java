package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface ITrinket {
    int getId();

    // effects
    default void onEquipped(LivingEntity entity){};
    default void onUnequipped(LivingEntity entity){};
    default void onTick(LivingEntity entity){};


    ItemStack getItemStack();
    default Component getDisplayName(){
        return getItemStack().getDisplayName();
    }
    List<Component> getDescription();
    List<Component> getEnchantedDescription();
    default List<Component> getExplain(){
        return new ArrayList<>();
    };
}
