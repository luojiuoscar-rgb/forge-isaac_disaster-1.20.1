package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;


import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PerfectVision extends PassiveAbility {
    private static final UUID PERFECT_VISION_DAMAGE =
            UUID.nameUUIDFromBytes(("isaac_disaster:perfect_vision_damage").getBytes(StandardCharsets.UTF_8));

    public PerfectVision(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        // 伤害修正
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        StatManager.setModifier(player, PERFECT_VISION_DAMAGE, Attributes.ATTACK_DAMAGE, -0.2,
                0.1, null, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        // 伤害修正
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
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
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.perfect_vision.lore.1"),
                StatManager.DAMAGE_MULTIPLY_BASE.description(-0.2)
        );
    }
}
