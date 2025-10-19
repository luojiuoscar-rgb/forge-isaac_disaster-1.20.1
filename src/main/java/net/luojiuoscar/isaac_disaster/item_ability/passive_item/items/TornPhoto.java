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

public class TornPhoto implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TORN_PHOTO.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyTearsAdder(player, 1);
        StatManager.modifyBlockBreakingSpeedAdder(player, 1);
        StatManager.modifyBulletSpeedAdder(player, 0.8);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyTearsAdder(player, -1);
        StatManager.modifyBlockBreakingSpeedAdder(player, -1);
        StatManager.modifyBulletSpeedAdder(player, -0.8);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.TORN_PHOTO.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears", StatManager.getTearsBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_breaking_speed", StatManager.getBlockBreakingSpeed()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed", StatManager.getBulletSpeedBonus())


        );
    }
}
