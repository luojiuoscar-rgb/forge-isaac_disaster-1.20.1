package net.luojiuoscar.isaac_disaster.isaac.active_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.isaac.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TheBookOfBelial implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_BOOK_OF_BELIAL.getId();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.THE_BOOK_OF_BELIAL_USE.get();
    }

    @Override
    public void onTriggeredEffect(Player player) {
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.POWER_OF_BELIAL.get(),
                240,
                1
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggerEffectStronger(Player player){
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.POWER_OF_BELIAL.get(),
                240,
                3
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_BOOK_OF_BELIAL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.1"),
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.2")
        );
    }

    @Override
    public List<Component> synergyDescriptionCarBattery() {
        return List.of(
                Component.translatable("item.isaac_disaster.car_battery").append(": ")
                        .append(Component.translatable("item.isaac_disaster.the_book_of_belial.synergy.car_battery.1"))
                        .withStyle(style -> style.withColor(ColorManager.SYNERGY))
        );
    }
}
