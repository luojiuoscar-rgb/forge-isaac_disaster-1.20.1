package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Placebo implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.PLACEBO.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        ItemStack stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof Pill item)) return;
        // 根据类型触发效果
        if (item.isHorsePill()){
            PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUseH(player, true);
        }else{
            PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUse(player, true);
        }
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        ItemStack stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof Pill item)) return;
        PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUseH(player, true);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.placebo.lore.1"),
                Component.translatable("item.isaac_disaster.placebo.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.placebo.synergy.car_battery.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
