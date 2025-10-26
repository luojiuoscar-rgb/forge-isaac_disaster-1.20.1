package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ASnack implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.A_SNACK.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
        StatManager.healHealth(player, 1.0f);
    }

    @Override
    public void onObtain(Player player) {
        StatManager.modifyMaxHealth(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyMaxHealth(player, -1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.A_SNACK.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus())
        );
    }
}
