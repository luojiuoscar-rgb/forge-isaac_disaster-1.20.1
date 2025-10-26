package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Pisces implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.PISCES.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.modifyPiercing(player, 1);
        StatManager.TEARS_CORRECTION.apply(player, 1);
        StatManager.BULLET_SCALE.apply(player, 2);
        StatManager.ATTACK_KNOCKBACK.apply(player, 1);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.modifyPiercing(player, -1);
        StatManager.TEARS_CORRECTION.apply(player, -1);
        StatManager.BULLET_SCALE.apply(player, -2);
        StatManager.ATTACK_KNOCKBACK.apply(player, -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.PISCES.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.piercing_bullet"),
                StatManager.TEARS_CORRECTION.description(1),
                Component.translatable("attribute.isaac_disaster.bullet_scale_up"),
                StatManager.ATTACK_KNOCKBACK.description(1)
        );
    }
}
