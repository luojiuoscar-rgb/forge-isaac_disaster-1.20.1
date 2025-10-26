package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

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

public class SpeedBall implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.SPEED_BALL.getId();
    }

    @Override
    public void onFirstObtain(Player player) {

    }

    @Override
    public void onObtain(Player player) {
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.BULLET_SPEED.apply(player, 1);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.BULLET_SPEED.apply(player, -1);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.SPEED_BALL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.BULLET_SPEED.description(1)
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
