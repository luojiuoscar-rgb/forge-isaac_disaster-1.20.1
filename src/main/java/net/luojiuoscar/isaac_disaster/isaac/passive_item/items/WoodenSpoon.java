package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.PassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WoodenSpoon implements PassiveItem {
    @Override
    public int getItemId() {
        return ItemId.WOODEN_SPOON.getId();
    }

    @Override
    public void obtainEffect(Player player) {

    }

    @Override
    public void directObtainEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMovementSpeedAdder(player, 1.5);
        }
    }

    @Override
    public void removeEffect(Player player) {
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
                Component.translatable("item.isaac_disaster.attribute.movement_speed", 1.5*StatManager.getMovementSpeedBonus()*1000)
        );
    }
}
