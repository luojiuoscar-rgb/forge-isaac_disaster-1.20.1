package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SacredOrb implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.SACRED_ORB.getId();
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.SACRED_ORB.get());
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.sacred_orb.lore.1"),
                Component.translatable("item.isaac_disaster.sacred_orb.lore.2")
        );
    }

}
