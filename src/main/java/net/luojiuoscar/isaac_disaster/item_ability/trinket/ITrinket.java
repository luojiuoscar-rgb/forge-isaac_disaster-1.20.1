package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface ITrinket {
    int getId();

    // effects
    default void onEquipped(LivingEntity entity){
        onEquipped(entity, false);
    };
    default void onEquipped(LivingEntity entity, boolean isEnchanted){};

    default void onUnequipped(LivingEntity entity){
        onUnequipped(entity, false);
    };
    default void onUnequipped(LivingEntity entity, boolean isEnchanted){};

    default void onTick(LivingEntity entity){
        onTick(entity, false);
    };
    default void onTick(LivingEntity entity, boolean isEnchanted){};


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
