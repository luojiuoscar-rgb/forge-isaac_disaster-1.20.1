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

public class RoidRage extends PassiveAbility {
    public RoidRage(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.RANGE.description(1),
                StatManager.ENTITY_REACH.description(1),
                StatManager.BLOCK_REACH.description(1)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
