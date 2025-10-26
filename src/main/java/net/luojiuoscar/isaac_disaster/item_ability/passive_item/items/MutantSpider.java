package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MutantSpider implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.MUTANT_SPIDER.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtain(Player player) {
        // 增加一层“双倍延迟”
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, 1)
        );
    }

    @Override
    public void onRemove(Player player) {
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> playerStatModifier.modifyDoubleShotDelay(player, -1)
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MUTANT_SPIDER.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.mutant_spider.lore.1"),
                Component.translatable("item.isaac_disaster.attribute.double_shot_delay")
        );
    }
}
