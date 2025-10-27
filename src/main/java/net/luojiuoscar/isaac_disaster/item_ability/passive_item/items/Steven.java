package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Steven implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.STEVEN.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE.description(1)
        );
    }
}
