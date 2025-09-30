package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.isaac.passive_item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MidnightSnack implements PassiveItem {

    @Override
    public int getItemId() {
        return ItemId.MIDNIGHT_SNACK.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        //属性修改需要在服务端权威修改
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, 1);
            StatManager.healHealth(player, 1.0f);
        }
    }

    @Override
    public void onRemove(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, -1);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.MIDNIGHT_SNACK.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.health", StatManager.getHealthBonus())
        );
    }
}
