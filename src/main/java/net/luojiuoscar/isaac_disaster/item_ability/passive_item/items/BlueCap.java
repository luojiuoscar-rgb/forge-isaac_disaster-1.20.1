package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueCap implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.BLUE_CAP.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.TEARS.apply(player, 1);
        StatManager.ATTACK_SPEED.apply(player, 0.1);
        StatManager.BLOCK_BREAKING.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), 1);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
        StatManager.TEARS.apply(player, -1);
        StatManager.ATTACK_SPEED.apply(player, -0.1);
        StatManager.BLOCK_BREAKING.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                StatManager.TEARS.description(1),
                StatManager.ATTACK_SPEED.description(0.1),
                StatManager.BLOCK_BREAKING.description(1),
                StatManager.BULLET_SPEED.description(-0.8)
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
