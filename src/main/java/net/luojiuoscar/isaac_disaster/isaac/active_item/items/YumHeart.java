package net.luojiuoscar.isaac_disaster.isaac.active_item.items;

import net.luojiuoscar.isaac_disaster.isaac.active_item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class YumHeart implements ActiveItem {

    @Override
    public int getItemId() {
        return ItemId.YUM_HEART.getId();
    }


    @Override
    public void onTriggeredEffect(ServerPlayer player) {
        StatManager.healHealth(player, StatManager.getHealthBonus());
    }

    @Override
    public void onTriggerEffectStronger(ServerPlayer player){
        StatManager.healHealth(player, StatManager.getHealthBonus()*2);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.YUM_HEART.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.heal_health", StatManager.getHealthBonus())
        );
    }
}
