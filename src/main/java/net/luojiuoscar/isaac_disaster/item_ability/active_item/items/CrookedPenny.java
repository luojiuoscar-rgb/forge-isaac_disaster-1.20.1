package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class CrookedPenny implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.CROOKED_PENNY.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        RandomSource random = player.getRandom();

        if (random.nextDouble() < 0.5){
            ActiveItemManager.getInstance().getItemFromId(ItemId.DIPLOPIA.getId()).onTriggeredEffect(player);
        }else{
            // 清空背包并给予1块钱，返还弯币
            Inventory inv = player.getInventory();
            inv.items.clear();
            inv.offhand.clear();
            inv.armor.clear();
            PlayerHelper.giveItem(player, ModItems.CROOKED_PENNY.get(), 1);
            PlayerHelper.giveMoney(player, 1);
        }
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.crooked_penny.lore.1"),
                Component.translatable("item.isaac_disaster.crooked_penny.lore.2"),
                Component.translatable("item.isaac_disaster.special.cannot_duplicate")
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
