package net.luojiuoscar.isaac_disaster.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Dessert implements PassiveItem {

    @Override
    public int getItemId() {
        return ItemIdManager.DESSERT;
    }

    @Override
    public void obtainEffect(Player player) {
    }

    @Override
    public void directObtainEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyMaxHealth(player, 1, 1);
        }
    }

    @Override
    public void removeEffect(Player player) {
        if(!player.level().isClientSide()) {
            StatManager.modifyMaxHealth(player, -1, 0);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.DESSERT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.dessert.lore.1", StatManager.getBaseHealthBonus())
        );
    }
}
