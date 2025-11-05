package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public interface ITrinket {
    int getId();

    // effects
    default void onFirstEquipped(LivingEntity entity, boolean isEnchanted){};

    default void onEquipped(LivingEntity entity, boolean isEnchanted){};

    default void onUnequipped(LivingEntity entity, boolean isEnchanted){};


    List<Component> getDescription();
    List<Component> getEnchantedDescription();
    default List<Component> getExplain(){
        return new ArrayList<>();
    };
}
