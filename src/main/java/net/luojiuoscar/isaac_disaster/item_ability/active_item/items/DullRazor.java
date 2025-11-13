package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class DullRazor implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.DULL_RAZOR.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        // 触发受伤事件
        LivingHurtEvent event = new LivingHurtEvent(player, player.damageSources().genericKill(), 0);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.dull_razor.lore.1")
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
