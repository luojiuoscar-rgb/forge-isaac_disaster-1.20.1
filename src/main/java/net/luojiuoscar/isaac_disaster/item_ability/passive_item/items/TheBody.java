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

public class TheBody implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_BODY.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyMaxHealth(player, 3);
        StatManager.healHealth(player, 3);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyMaxHealth(player, -3);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.THE_BODY.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", 3*StatManager.getHealthBonus()),
                Component.translatable("item.isaac_disaster.action.heal_health", 3*StatManager.getHealthBonus())
        );
    }
}
