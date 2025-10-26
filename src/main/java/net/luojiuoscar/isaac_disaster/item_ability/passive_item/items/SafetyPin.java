package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SafetyPin implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.SAFETY_PIN.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.SAFETY_PIN.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.RANGE.description(1),
                StatManager.BLOCK_REACH.description(1),
                StatManager.BULLET_SPEED.description(1),
                Component.translatable("item.isaac_disaster.action.give_black_heart", 1)


        );
    }
}
