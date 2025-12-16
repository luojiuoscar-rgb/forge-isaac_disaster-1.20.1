package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrowthHormones extends PassiveAbility {
    public GrowthHormones(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, 1);
        StatManager.modifySetWithId(player, ModSetAbility.SPUN.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, -1);
        StatManager.modifySetWithId(player, ModSetAbility.SPUN.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1),
                StatManager.DAMAGE.description(1)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return ModSetAbility.SPUN.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return ModSetAbility.SPUN.get().getExtraDesc();
    }
}
