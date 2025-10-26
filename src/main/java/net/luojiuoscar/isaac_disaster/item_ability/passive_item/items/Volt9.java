package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Volt9 implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.VOLT_9.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        // 满电
        PlayerHelper.chargeAll((ServerPlayer) player, null);
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.VOLT_9.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.volt_9.lore.1"),
                Component.translatable("item.isaac_disaster.action.full_charge")
        );
    }
}
