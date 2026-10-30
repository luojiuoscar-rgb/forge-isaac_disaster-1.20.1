package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SisterMaggy extends PassiveAbility {
    public SisterMaggy(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) { }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        var familiarId = ModFamiliarEntities.SISTER_MAGGY.getId();
        StatManager.modifySetWithId(player, ModSetAbilities.CONJOINED.getId(), 1);
        StatManager.addFamiliar(player, familiarId, familiarId, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        var familiarId = ModFamiliarEntities.SISTER_MAGGY.getId();
        StatManager.addFamiliar(player, familiarId, familiarId, -1);
        StatManager.modifySetWithId(player, ModSetAbilities.CONJOINED.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.sister_maggy.lore.1"),
                Component.translatable("item.isaac_disaster.sister_maggy.lore.2"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.CONJOINED.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.CONJOINED.get().getExtraDesc();
    }
}
