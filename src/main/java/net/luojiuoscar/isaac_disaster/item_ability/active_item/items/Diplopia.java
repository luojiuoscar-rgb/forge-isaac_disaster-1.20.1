package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Diplopia implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.DIPLOPIA.getId();
    }


    @Override
    public void onTriggeredEffect(Player player) {
        // 遍历背包并生成掉落
        Inventory inv = player.getInventory();
        List<ItemStack> invItems = new ArrayList<>();
        invItems.addAll(inv.items);
        invItems.addAll(inv.offhand);


        for (ItemStack stack : invItems) {
            if (!stack.isEmpty()) {
                // 跳过不可被复制的物品
                if (stack.is(TagManager.ITEM_CANNOT_BE_DUPLICATED)) continue;

                ItemStack copy = stack.copy();
                PlayerHelper.giveItem(player, copy);
            }
        }
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.diplopia.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
