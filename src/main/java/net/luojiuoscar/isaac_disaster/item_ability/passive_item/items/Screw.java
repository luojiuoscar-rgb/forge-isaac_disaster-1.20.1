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

public class Screw implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.SCREW.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyTearsAdder(player, 0.75, isPermanent);
        StatManager.modifyAttackSpeedAdder(player, 0.75, isPermanent);
        StatManager.modifyBulletSpeedAdder(player, 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyTearsAdder(player, -0.75, isPermanent);
        StatManager.modifyAttackSpeedAdder(player, -0.75, isPermanent);
        StatManager.modifyBulletSpeedAdder(player, -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.SCREW.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears", 0.75*StatManager.getTearsBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.attack_speed", 0.75*StatManager.getAttackSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed", StatManager.getBulletSpeedBonus())
        );
    }
}
