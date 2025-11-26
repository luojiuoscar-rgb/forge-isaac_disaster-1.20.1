package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Pisces extends PassiveAbility {
    public Pisces(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addPiercing(player, 1);
        StatManager.TEARS_CORRECTION.apply(player, 1);
        StatManager.BULLET_SCALE.apply(player, 2);
        StatManager.ATTACK_KNOCKBACK.apply(player, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addPiercing(player, -1);
        StatManager.TEARS_CORRECTION.apply(player, -1);
        StatManager.BULLET_SCALE.apply(player, -2);
        StatManager.ATTACK_KNOCKBACK.apply(player, -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.piercing_bullet"),
                StatManager.TEARS_CORRECTION.description(1),
                Component.translatable("attribute.isaac_disaster.bullet_scale_up"),
                StatManager.ATTACK_KNOCKBACK.description(1)
        );
    }
}
