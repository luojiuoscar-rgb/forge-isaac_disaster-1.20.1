package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;


import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PerfectVision implements IPassiveItem {
    private static final UUID PERFECT_VISION_DAMAGE =
            UUID.nameUUIDFromBytes(("isaac_disaster:perfect_vision_damage").getBytes(StandardCharsets.UTF_8));

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.PERFECT_VISION.get());
    }

    @Override
    public int getItemId() {
        return ItemId.PERFECT_VISION.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        // 伤害修正
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        StatManager.setModifierMultiplyBase(player, instance, -0.2, PERFECT_VISION_DAMAGE, "perfect_vision");
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        // 伤害修正
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    int count = playerPassiveItem.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId());
                    // 当最后一个已经被移除时；移除对应的modifier
                    if (count == 0){
                        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (instance == null) return;
                        StatManager.removeModifier(player, instance, PERFECT_VISION_DAMAGE);
                    }
                }
        );
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.perfect_vision.lore.1"),
                StatManager.DAMAGE_MULTIPLY_BASE.description(-0.2)
        );
    }
}
