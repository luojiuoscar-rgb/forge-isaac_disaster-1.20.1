package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Steven implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.STEVEN.getId();
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        if(!player.level().isClientSide()){
            StatManager.modifyDamageAdder(player, 1);
        }
    }

    @Override
    public void onRemove(Player player) {
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
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", StatManager.getDamageBonus())
        );
    }
}
