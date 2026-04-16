package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MomsKey extends PassiveAbility {
    public MomsKey(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_KEYS, 2);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.MOMS_KEY.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.MOMS_KEY.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_key", 2)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return ModSetAbility.MOM.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return ModSetAbility.MOM.get().getExtraDesc();
    }
}
