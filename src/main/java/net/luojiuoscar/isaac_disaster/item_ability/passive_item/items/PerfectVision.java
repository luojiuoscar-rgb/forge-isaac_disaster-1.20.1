package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;


import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.UUIDManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PerfectVision implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.PERFECT_VISION.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        // 伤害修正
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        StatManager.setMultiplier(player, instance, -0.2, UUIDManager.PERFECT_VISION, "perfect_vision");
    }

    @Override
    public void onRemove(Player player) {
        // 伤害修正
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    int count = playerPassiveItem.getItemCount(ItemId.PERFECT_VISION.getId());
                    // 当最后一个已经被移除时；移除对应的modifier
                    if (count == 0){
                        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (instance == null) return;
                        StatManager.removeModifier(player, instance, UUIDManager.PERFECT_VISION);
                    }
                }
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.PERFECT_VISION.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.perfect_vision.lore.1"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage_multiplier_remove", 20)
        );
    }
}
