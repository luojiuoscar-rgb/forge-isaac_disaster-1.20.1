package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueCap extends PassiveAbility {
    public BlueCap(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.TEARS.apply(player, 1);
        StatManager.ATTACK_SPEED.apply(player, 0.1);
        StatManager.BLOCK_BREAKING.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
        StatManager.TEARS.apply(player, -1);
        StatManager.ATTACK_SPEED.apply(player, -0.1);
        StatManager.BLOCK_BREAKING.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                StatManager.TEARS.description(1),
                StatManager.ATTACK_SPEED.description(0.1),
                StatManager.BLOCK_BREAKING.description(1),
                StatManager.BULLET_SPEED.description(-0.8)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getExplain();
    }
}
