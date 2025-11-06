package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INeedSyncToClient;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodOfTheMartyr implements IPassiveItem, INeedSyncToClient {
    @Override
    public int getItemId() {
        return ItemId.BLOOD_OF_THE_MARTYR.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);

        if (player instanceof ServerPlayer serverPlayer){
            sync(serverPlayer, getItemId());
        }
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);

        if (player instanceof ServerPlayer serverPlayer){
            sync(serverPlayer, getItemId());
        }
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE.description(1)
        );
    }

}
