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

public class TheSadOnion implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_SAD_ONION.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {

    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyTearsAdder(player, 1, isPermanent);
        StatManager.modifyBlockBreakingSpeedAdder(player, 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyTearsAdder(player, -1, isPermanent);
        StatManager.modifyBlockBreakingSpeedAdder(player, -1, isPermanent);

    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_SAD_ONION.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears", StatManager.getTearsBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_breaking_speed", StatManager.getBlockBreakingSpeed())
        );
    }
}
