package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ModReviveModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeadCat extends PassiveAbility {
    public static final int REVIVE_COUNT = 9;

    public DeadCat(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addReviveModuleConsumer(player, ModReviveModule.DEAD_CAT.getId(), REVIVE_COUNT);
        if (PlayerHelper.getItemCount(ItemId.DEAD_CAT.getId(), player) == 0) {
            StatManager.MAX_HEALTH.set(player, 1);
        }
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addReviveModuleProvider(player, ModReviveModule.DEAD_CAT.getId(), REVIVE_COUNT);
        StatManager.modifySetWithId(player, ModSetAbility.CAT.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addReviveModuleConsumer(player, ModReviveModule.DEAD_CAT.getId(), -REVIVE_COUNT);
        StatManager.addReviveModuleProvider(player, ModReviveModule.DEAD_CAT.getId(), -REVIVE_COUNT);
        StatManager.modifySetWithId(player, ModSetAbility.CAT.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.dead_cat.lore.1", StatManager.MAX_HEALTH.getBonus()),
                Component.translatable("item.isaac_disaster.dead_cat.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbility.CAT.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbility.CAT.get().getExtraDesc();
    }
}
