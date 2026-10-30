package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class MomsUnderwear extends PassiveAbility {
    private static final int MINIMUM_COUNT = 3;
    private static final int MAXIMUM_COUNT = 6;

    public MomsUnderwear(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        FamiliarHelper.spawnFamiliars(player, ModFamiliarEntities.BLUE_FLY.getId(), ModPassiveItems.MOMS_UNDERWEAR.getId(), player.position(),
                player.getRandom().nextInt(MAXIMUM_COUNT - MINIMUM_COUNT + 1) + MINIMUM_COUNT);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1);
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, -1);
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(StatManager.RANGE.description(1),
                Component.translatable("item.isaac_disaster.moms_underwear.lore.1", MINIMUM_COUNT, MAXIMUM_COUNT));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.MOM.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.MOM.get().getExtraDesc();
    }
}
