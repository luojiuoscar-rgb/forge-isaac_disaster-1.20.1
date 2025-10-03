package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;

public class YumHeart implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.YUM_HEART.getId();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.YUM_HEART_USE.get();
    }

    @Override
    public void onTriggeredEffect(Player player) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void onTriggerEffectStronger(Player player){
        StatManager.healHealth(player, 2);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.YUM_HEART.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.heal_health", StatManager.getHealthBonus())
        );
    }

    @Override
    public List<Component> synergyDescriptionCarBattery() {
        return List.of(
                Component.translatable("item.isaac_disaster.car_battery").append(": ")
                        .append(Component.translatable("item.isaac_disaster.yum_heart.synergy.car_battery.1")).withStyle(style -> style.withColor(ColorManager.SYNERGY))
        );
    }
}
