package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SacredHeart extends PassiveAbility {
    public SacredHeart(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.DAMAGE.apply(player, 1);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 1.3);
        StatManager.BULLET_SPEED.apply(player, 1);
        StatManager.RANGE.apply(player, 1.5);
        StatManager.TEARS.apply(player, -0.6);
        StatManager.addHoming(player, 1);
        StatManager.BLOCK_BREAKING.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);

        StatManager.addBulletColor(player, ModBulletColor.SACRED_HEART.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
        StatManager.DAMAGE.apply(player, -1);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -1.3);
        StatManager.BULLET_SPEED.apply(player, -1);
        StatManager.RANGE.apply(player, -1.5);
        StatManager.TEARS.apply(player, 0.6);
        StatManager.addHoming(player, -1);
        StatManager.BLOCK_BREAKING.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);

        StatManager.addBulletColor(player, ModBulletColor.SACRED_HEART.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                StatManager.DAMAGE.description(1),
                StatManager.DAMAGE_MULTIPLY_BASE.description(1.3),
                StatManager.BULLET_SPEED.description(1),
                StatManager.RANGE.description(1.5),
                StatManager.TEARS.description(-0.6),
                StatManager.BLOCK_BREAKING.description(1),
                StatManager.ENTITY_REACH.description(1),
                StatManager.BLOCK_REACH.description(1),
                Component.translatable("item.isaac_disaster.attribute.homing_bullet"),
                Component.translatable("item.isaac_disaster.action.full_health")
        );
    }
}
