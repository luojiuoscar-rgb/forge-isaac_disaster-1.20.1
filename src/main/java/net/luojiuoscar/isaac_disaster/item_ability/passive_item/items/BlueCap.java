package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BlueCap implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.BLUE_CAP.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyMaxHealth(player, 1, isPermanent);
        StatManager.modifyTearsAdder(player, 1, isPermanent);
        StatManager.modifyAttackSpeedAdder(player, -0.1, isPermanent);
        StatManager.modifyBlockBreakingSpeedAdder(player, 1, isPermanent);
        StatManager.modifyBulletSpeedAdder(player, -0.8, isPermanent);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyMaxHealth(player, -1, isPermanent);
        StatManager.modifyTearsAdder(player, -1, isPermanent);
        StatManager.modifyAttackSpeedAdder(player, 0.1, isPermanent);
        StatManager.modifyBlockBreakingSpeedAdder(player, -1, isPermanent);
        StatManager.modifyBulletSpeedAdder(player, 0.8, isPermanent);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.BLUE_CAP.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.action.heal_health", StatManager.getHealthBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears", StatManager.getTearsBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_breaking_speed", StatManager.getBlockBreakingSpeed()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.attack_speed_remove",0.1*StatManager.getAttackSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_speed_remove", StatManager.getBulletSpeedBonus())
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getExplain();
    }
}
