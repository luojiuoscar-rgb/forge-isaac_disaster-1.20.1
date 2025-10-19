package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.logging.Logger;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TheInnerEye implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_INNER_EYE.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
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
        return new ItemStack(ModItems.THE_INNER_EYE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_inner_eye.lore.1"),
                Component.translatable("item.isaac_disaster.attribute.double_shot_delay")
        );
    }
}
