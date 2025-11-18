package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoidRage implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.ROID_RAGE.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.RANGE.description(1),
                StatManager.ENTITY_REACH.description(1),
                StatManager.BLOCK_REACH.description(1)
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
