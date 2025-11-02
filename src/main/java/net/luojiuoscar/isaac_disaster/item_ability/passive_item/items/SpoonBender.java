package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpoonBender implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.SPOON_BENDER.getId();
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.SPOON_BENDER.get());
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.modifyHoming(player, 1);
        StatManager.addBulletFilter(player, ColorManager.SPOON_BENDER_FILTER);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.modifyHoming(player, -1);
        StatManager.removeBulletFilter(player, ColorManager.SPOON_BENDER_FILTER);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.homing_bullet")
        );
    }
}
