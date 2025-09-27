package net.luojiuoscar.isaac_disaster.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Steven implements PassiveItem {
    @Override
    public int getItemId() {
        return ItemIdManager.STEVEN;
    }

    @Override
    public void obtainEffect(Player player) {

    }

    @Override
    public void directObtainEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, 1);
        }
    }

    @Override
    public void removeEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, -1);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.STEVEN.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.steven.lore.1", StatManager.getBaseDamageBonus())
        );
    }
}
