package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.EffectDescriptionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UnicornStump implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.UNICORN_STUMP.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        int duration = 120;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        int duration = 240;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.UNICORN_STUMP.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.special.invincible", 6),
                Component.translatable("item.isaac_disaster.unicorn_stump.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.INVINCIBLE.getId()));
        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.LACRIMAL_HYPOSECRETION.getId()));

        return description;
    }
}
