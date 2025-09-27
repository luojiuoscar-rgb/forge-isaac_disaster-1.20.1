package net.luojiuoscar.isaac_disaster.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CricketsHead implements PassiveItem {
    public double damage_multiplier = 0.5;

    @Override
    public int getItemId() {
        return ItemIdManager.CRICKETS_HEAD;
    }

    @Override
    public void obtainEffect(Player player) {

    }

    @Override
    public void directObtainEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, 0.25);
            StatManager.modifyDamageMultiplier(player, damage_multiplier);
        }
    }

    @Override
    public void removeEffect(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, -0.25);
            StatManager.modifyDamageMultiplier(player, -damage_multiplier);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.CRICKETS_HEAD.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.crickets_head.lore.1", 0.25*StatManager.getBaseDamageBonus()),
                Component.translatable("item.isaac_disaster.crickets_head.lore.2", damage_multiplier*1000)
        );
    }
}
