package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
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
    public void onFirstUse(Player player){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1);
    }

    @Override
    public void onTriggeredEffect(Player player) {
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.FLY_TIME.getBonus() * 2),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.FLY_TIME.getBonus() * 4),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("attribute.isaac_disaster.fly_time")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getSynergyDescription());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());
        description.add(EffectManager.TRANSCENDENCE.getExplainDesc());


        return description;
    }
}
