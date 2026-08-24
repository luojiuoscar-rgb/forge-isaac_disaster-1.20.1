package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Passive ability for Cricket's Body. */
public final class CricketsBody extends PassiveAbility {
    private static final UUID RANGE_MODIFIER = UUID.nameUUIDFromBytes(
            "isaac_disaster:crickets_body_range".getBytes(StandardCharsets.UTF_8));
    public CricketsBody(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SCALE.apply(player, 1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.CRICKETS_BODY.getId(), 1);
        StatManager.TEARS_CORRECTION.apply(player, 0.5);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            if (items.getItemCountFromAll(ItemId.CRICKETS_BODY.getId()) == 1) {
                StatManager.setModifier(player, RANGE_MODIFIER, ModAttributes.BULLET_RANGE.get(), -0.2,
                        null, null, 2);
            }
        });
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SCALE.apply(player, -1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.CRICKETS_BODY.getId(), -1);
        StatManager.TEARS_CORRECTION.apply(player, -0.5);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            if (items.getItemCountFromAll(ItemId.CRICKETS_BODY.getId()) == 0) {
                AttributeInstance range = player.getAttribute(ModAttributes.BULLET_RANGE.get());
                StatManager.removeModifier(player, range, RANGE_MODIFIER);
            }
        });
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.BULLET_SCALE.description(1.5),
                StatManager.TEARS_CORRECTION.description(0.5),
                Component.translatable("item.isaac_disaster.crickets_body.lore.1"),
                Component.translatable("item.isaac_disaster.crickets_body.lore.2"),
                Component.translatable("item.isaac_disaster.crickets_body.lore.3")
        );
    }
}
