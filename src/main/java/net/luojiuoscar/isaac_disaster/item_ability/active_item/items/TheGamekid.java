package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class TheGamekid implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_GAMEKID.getId();
    }

    @Override
    public void onTriggeredEffect(Player player) {
        int duration = 200;
        MobEffectInstance invincible = new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration,0);
        MobEffectInstance no_bullet = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration,0);
        MobEffectInstance pac_man = new MobEffectInstance(ModEffects.PAC_MAN.get(), duration,0);

        player.addEffect(invincible);
        player.addEffect(no_bullet);
        player.addEffect(pac_man);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        int duration = 400;
        MobEffectInstance invincible = new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration,0);
        MobEffectInstance no_bullet = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration,0);
        MobEffectInstance pac_man = new MobEffectInstance(ModEffects.PAC_MAN.get(), duration,0);

        player.addEffect(invincible);
        player.addEffect(no_bullet);
        player.addEffect(pac_man);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_gamekid.lore.1")
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
        description.add(EffectManager.PAC_MAN.getExplainDesc());

        return description;
    }
}
