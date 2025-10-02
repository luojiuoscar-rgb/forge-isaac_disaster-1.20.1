package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.IPassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Dessert implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.DESSERT.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, 1);
            StatManager.healHealth(player, 1.0f);
        }
    }

    @Override
    public void onRemove(Player player) {
        if(!player.level().isClientSide()) {
            StatManager.modifyMaxHealth(player, -1);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.DESSERT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.health", StatManager.getHealthBonus())
        );
    }
}
