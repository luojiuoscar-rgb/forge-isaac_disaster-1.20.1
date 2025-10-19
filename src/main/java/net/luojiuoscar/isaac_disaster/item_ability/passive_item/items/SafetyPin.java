package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SafetyPin implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.SAFETY_PIN.getId();
    }

    @Override
    public void onObtain(Player player) {
        PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyRangeAdder(player, 1);
        StatManager.modifyBlockReachAdder(player, 1);
        StatManager.modifyBulletSpeedAdder(player, 0.8);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyRangeAdder(player, -1);
        StatManager.modifyBlockReachAdder(player, -1);
        StatManager.modifyBulletSpeedAdder(player, -0.8);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.SAFETY_PIN.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_range", StatManager.getRangeBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_reach", StatManager.getBlockReachBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed", StatManager.getBulletSpeedBonus()),
                Component.translatable("item.isaac_disaster.action.give_black_heart", 1)


        );
    }
}
