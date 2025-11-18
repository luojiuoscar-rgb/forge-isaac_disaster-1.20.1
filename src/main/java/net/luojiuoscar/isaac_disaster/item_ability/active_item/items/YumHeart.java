package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
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
    public void onTriggeredEffectStronger(Player player){
        StatManager.healHealth(player, 2);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.healHealthDescription(1)
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
        return description;
    }
}
