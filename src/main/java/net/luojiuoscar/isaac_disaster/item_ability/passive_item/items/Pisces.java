package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Pisces implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.PISCES.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {

    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyPiercing(player, 1, isPermanent);
        StatManager.modifyTearsCorrectionAdder(player, 1, isPermanent);
        StatManager.modifyBulletScaleAdder(player, 2, isPermanent);
        StatManager.modifyAttackKnockBackAdder(player, 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyPiercing(player, -1, isPermanent);
        StatManager.modifyTearsCorrectionAdder(player, -1, isPermanent);
        StatManager.modifyBulletScaleAdder(player, -2, isPermanent);
        StatManager.modifyAttackKnockBackAdder(player, -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.PISCES.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.piercing_bullet"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.tears_correction", StatManager.getTearsCorrectionBonus()),
                Component.translatable("item.isaac_disaster.attribute.bullet_scale_up"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.attack_knockback", StatManager.getAttackKnockbackBonus())
                );
    }
}
