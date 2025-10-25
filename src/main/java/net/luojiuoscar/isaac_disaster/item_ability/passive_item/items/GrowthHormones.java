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

public class GrowthHormones implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.GROWTH_HORMONES.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyDamageAdder(player, 1, isPermanent);
        StatManager.modifyMovementSpeedAdder(player, 1, isPermanent);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyDamageAdder(player, -1, isPermanent);
        StatManager.modifyMovementSpeedAdder(player, -1, isPermanent);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.GROWTH_HORMONES.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed", 1000*StatManager.getMovementSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", StatManager.getDamageBonus())
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
