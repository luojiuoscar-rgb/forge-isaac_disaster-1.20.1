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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MiniMush implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.MINI_MUSH.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, -2);
        StatManager.MOVEMENT_SPEED.apply(player,  1.5);
        StatManager.RANGE.apply(player,  1);
        StatManager.BLOCK_BREAKING.apply(player,  1);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), 1);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.SCALE.apply(player, 2);
        StatManager.MOVEMENT_SPEED.apply(player,  -1.5);
        StatManager.RANGE.apply(player,  -1);
        StatManager.BLOCK_BREAKING.apply(player,  -1);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MINI_MUSH.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("attribute.isaac_disaster.scale_down"),
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.RANGE.description(1),
                StatManager.BLOCK_REACH.description(1)


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
