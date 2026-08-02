package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MomsPerfume extends PassiveAbility {
    public MomsPerfume(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS_CORRECTION.apply(player, 0.5);
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModule.MOMS_PERFUME.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS_CORRECTION.apply(player, -0.5);
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModule.MOMS_PERFUME.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();
        description.add(StatManager.TEARS_CORRECTION.description(0.5));
        description.add(Component.translatable("item.isaac_disaster.moms_perfume.lore.1"));
        return description;
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        return ModSetAbility.MOM.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack) {
        return ModSetAbility.MOM.get().getExtraDesc();
    }
}
