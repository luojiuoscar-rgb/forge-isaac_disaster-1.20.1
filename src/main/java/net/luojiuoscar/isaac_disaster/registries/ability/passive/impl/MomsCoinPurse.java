package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MomsCoinPurse extends PassiveAbility {
    public MomsCoinPurse(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        LootHelper.spawnLootAtPos(player, player.position(), ModLootTables.RANDOM_PILLS, 4);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.modifySetWithId(player, ModSetAbility.MOM.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_pill", 4)
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
