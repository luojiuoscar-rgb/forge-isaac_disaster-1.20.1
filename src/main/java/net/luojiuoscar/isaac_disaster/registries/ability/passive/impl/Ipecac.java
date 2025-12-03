package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Ipecac extends PassiveAbility {

    public Ipecac(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addBulletColor(player, ModBulletColor.IPECAC.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModule.IPECAC.getId(), 1);
        StatManager.addTrajectory(player, ModAttackTrajectory.GRAVITY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addBulletColor(player, ModBulletColor.IPECAC.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModule.IPECAC.getId(), -1);
        StatManager.addTrajectory(player, ModAttackTrajectory.GRAVITY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.ipecac.lore.1"),
                Component.translatable("item.isaac_disaster.ipecac.lore.2"),
                Component.translatable("item.isaac_disaster.ipecac.lore.3"),
                Component.translatable("item.isaac_disaster.ipecac.lore.4"),
                Component.translatable("item.isaac_disaster.ipecac.lore.5"),
                Component.translatable("item.isaac_disaster.ipecac.lore.6")

        );
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.POISON.getExplainDesc());

        return description;
    }

}
