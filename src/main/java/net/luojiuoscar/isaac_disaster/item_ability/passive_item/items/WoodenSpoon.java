package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WoodenSpoon implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.WOODEN_SPOON.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMovementSpeedAdder(player, 1.5);
        }
    }

    @Override
    public void onRemove(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMovementSpeedAdder(player, -1.5);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.WOODEN_SPOON.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                //移动速度以百分比显示
                TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed", 1.5*StatManager.getMovementSpeedBonus()*1000)
        );
    }
}
