package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
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
        MobEffectInstance effect = player.getEffect(ModEffects.POWER_OF_BELIAL.get());
        int amplifier = 1;
        if (effect != null){
            amplifier += effect.getAmplifier();
        }
        amplifier = Math.min(amplifier, 255);

        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.POWER_OF_BELIAL.get(),
                240,
                amplifier
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggerEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_BOOK_OF_BELIAL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.1"),
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.2"),
                Component.translatable("item.isaac_disaster.special.stackable")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        if (ClientDataManager.getInstance().getCountFromId(ItemId.BLOOD_OF_THE_MARTYR.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.blood_of_the_martyr").append(": ")
                    .append(Component.translatable("item.isaac_disaster.blood_of_the_martyr.synergy.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("effect.isaac_disaster.power_of_belial").append(": ")
                .append(Component.translatable("effect.isaac_disaster.power_of_belial.explain.1", StatManager.getDamageBonus())));

        return description;
    }
}
