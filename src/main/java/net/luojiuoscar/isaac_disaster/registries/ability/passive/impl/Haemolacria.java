package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class Haemolacria extends PassiveAbility {
    private static final UUID RANGE_MODIFIER = UUID.nameUUIDFromBytes(
            "isaac_disaster:haemolacria_range".getBytes(StandardCharsets.UTF_8));

    public Haemolacria(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            if (items.getItemCountFromAll(ModPassiveItems.HAEMOLACRIA.getId()) != 0) return;
            StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.5);
            StatManager.setModifier(player, RANGE_MODIFIER, ModAttributes.BULLET_RANGE.get(), -0.2,
                    null, null, 2);
            StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), 1);
            StatManager.addTriggerModule(player, ModTriggerModules.HAEMOLACRIA.getId(), 1);
            StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), 1);
        });
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(items -> {
            if (items.getItemCountFromAll(ModPassiveItems.HAEMOLACRIA.getId()) != 0) return;
            StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.5);
            StatManager.removeModifier(player, player.getAttribute(ModAttributes.BULLET_RANGE.get()), RANGE_MODIFIER);
            StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), -1);
            StatManager.addTriggerModule(player, ModTriggerModules.HAEMOLACRIA.getId(), -1);
            StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), -1);
        });
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.DAMAGE.description(1),
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.5),
                Component.translatable("item.isaac_disaster.haemolacria.lore.1"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.2"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.3"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.4"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.5")
        );
    }
}
