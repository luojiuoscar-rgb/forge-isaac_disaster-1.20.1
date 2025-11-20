package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Kamikaze implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.KAMIKAZE.getId();
    }

    @Override
    public SoundEvent getSound() {
        return null;
    }

    @Override
    public void onTriggeredEffect(Player player) {
        explode(player, 4, 35);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        explode(player, 7, 80);
    }

    private void explode(Player player, float power, float damage){
        LevelHelper.explodeCustom(player, player.position(), power, damage, true);
        player.hurt(player.damageSources().genericKill(), (float) StatManager.MAX_HEALTH.getBonus());
    }


    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.kamikaze.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.stronger"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
