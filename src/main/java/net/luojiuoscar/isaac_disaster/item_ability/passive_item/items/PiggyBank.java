package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PiggyBank implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.PIGGY_BANK.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveMoney(player, 3);
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.PIGGY_BANK.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.PIGGY_BANK.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_money", 3),
                Component.translatable("item.isaac_disaster.piggy_bank.lore.1")
        );
    }
}
