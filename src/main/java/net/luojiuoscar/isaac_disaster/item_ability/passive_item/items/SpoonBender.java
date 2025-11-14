package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.BulletColor;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
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
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.applyHoming(player, 1);
        StatManager.addBulletColor(player, BulletColor.SPOON_BENDER.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.applyHoming(player, -1);
        StatManager.addBulletColor(player, BulletColor.SPOON_BENDER.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.homing_bullet")
        );
    }
}
