package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TheBible implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_BIBLE.getId();
    }

    @Override
    public SoundEvent getSound() {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    public void onTriggeredEffect(Player player) {
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.getFlyTime() * 2),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggerEffectStronger(Player player){
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.getFlyTime() * 4),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_BIBLE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.fly")
        );
    }

    @Override
    public List<Component> synergyDescriptionCarBattery() {
        return List.of(
                Component.translatable("item.isaac_disaster.car_battery").append(": ")
                        .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                        .withStyle(style -> style.withColor(ColorManager.SYNERGY))
        );
    }
}
