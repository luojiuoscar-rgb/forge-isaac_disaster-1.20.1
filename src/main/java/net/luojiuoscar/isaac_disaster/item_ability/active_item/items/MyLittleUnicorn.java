package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MyLittleUnicorn implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.MY_LITTLE_UNICORN.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        int duration = 120;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.RAMPAGE.get(), duration, 3));
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        int duration = 240;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.RAMPAGE.get(), duration, 3));
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.special.invincible", 6),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.1"),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.2"),
                Component.translatable("item.isaac_disaster.my_little_unicorn.lore.3")
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

        description.add(EffectManager.INVINCIBLE.getExplainDesc());
        description.add(EffectManager.LACRIMAL_HYPOSECRETION.getExplainDesc());
        description.add(EffectManager.RAMPAGE.getExplainDesc());

        return description;
    }
}
