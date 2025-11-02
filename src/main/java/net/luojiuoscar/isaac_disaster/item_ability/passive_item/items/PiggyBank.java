package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PiggyBank implements IHurtTriggerPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.PIGGY_BANK.getId();
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.PIGGY_BANK.get());
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveMoney(player, 3);
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_money", 3),
                Component.translatable("item.isaac_disaster.piggy_bank.lore.1")
        );
    }

    @Override
    public void handleHurtEffect(Player player, Entity target) {
        RandomSource random = player.getRandom();

        if (random.nextDouble() < 0.5){
            PlayerHelper.giveMoney(player, 1);
        }else {
            PlayerHelper.giveMoney(player, 2);
        }

    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1;
    }
}
