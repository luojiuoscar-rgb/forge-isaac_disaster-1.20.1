package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CricketsHead implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.CRICKETS_HEAD.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.5);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.5);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE.description(0.5),
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.5)
        );
    }
}
