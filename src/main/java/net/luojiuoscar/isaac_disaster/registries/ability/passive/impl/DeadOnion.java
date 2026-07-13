package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeadOnion extends PassiveAbility {

    public DeadOnion(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, -2);
        StatManager.BULLET_SCALE.apply(player, 2);
        StatManager.addPiercing(player, 1);
        StatManager.addSpectral(player, 1);
        StatManager.addBulletColor(player, ModBulletColor.DEAD_ONION.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, 2);
        StatManager.BULLET_SCALE.apply(player, -2);
        StatManager.addPiercing(player, -1);
        StatManager.addSpectral(player, -1);
        StatManager.addBulletColor(player, ModBulletColor.DEAD_ONION.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.RANGE.description(-1),
                StatManager.BULLET_SPEED.description(-2),
                Component.translatable("attribute.isaac_disaster.bullet_scale_up"),
                Component.translatable("item.isaac_disaster.attribute.piercing_bullet"),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet")
        );
    }
}
