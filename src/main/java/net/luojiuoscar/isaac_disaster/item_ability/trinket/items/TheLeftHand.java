package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheLeftHand implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.THE_LEFT_HAND.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted) {

    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.THE_LEFT_HAND.getId(), isEnchanted ? 2 : 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.THE_LEFT_HAND.getId(), isEnchanted ? -2 : -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.the_left_hand.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.no_effect")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

}
