package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Polyphemus implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.POLYPHEMUS.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyDamageMultiplier(player, StatManager.getDamageMultiplier1());
        StatManager.modifyDamageAdder(player, 4 * StatManager.getDamageBonus());

        // 增加一层“双倍延迟”
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, 1)
        );
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyDamageMultiplier(player, -StatManager.getDamageMultiplier1());
        StatManager.modifyDamageAdder(player, -4 * StatManager.getDamageBonus());

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, -1)
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.POLYPHEMUS.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage_multiplier", StatManager.getDamageMultiplier1() * 100),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", StatManager.getDamageBonus() * 4),
                Component.translatable("item.isaac_disaster.polyphemus.lore.1"),
                Component.translatable("item.isaac_disaster.polyphemus.lore.2")
        );
    }
}
