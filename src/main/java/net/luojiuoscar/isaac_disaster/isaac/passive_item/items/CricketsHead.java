package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.PassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CricketsHead implements PassiveItem {
    public double damage_multiplier = 0.5;

    @Override
    public int getItemId() {
        return ItemId.CRICKETS_HEAD.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, 0.25);
            StatManager.modifyDamageMultiplier(player, damage_multiplier);
        }
    }

    @Override
    public void onRemove(Player player) {
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
                Component.translatable("item.isaac_disaster.attribute.damage", 0.25*StatManager.getDamageBonus()),
                Component.translatable("item.isaac_disaster.attribute.damage_multiplier", damage_multiplier*100)
        );
    }
}
