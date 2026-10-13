package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MomsBra extends ActiveAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.MOMS_BRA),
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.MOMS_BRA_BLINDNESS)
    ));

    public MomsBra(int id, int level) {
        super(TRIGGER, id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand) {
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), 1);
    }

    @Override
    public double getStrongerAmplifier() { return 1; }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.moms_bra.lore.1", 4),
                Component.translatable("item.isaac_disaster.moms_bra.lore.2", 4)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.CAR_BATTERY.getId()) == 0) return List.of();
        return List.of(DescriptionHelper.getSynergyDesc(
                Component.translatable("item.isaac_disaster.car_battery"),
                Component.translatable("item.isaac_disaster.synergy.description.no_effect")
        ));
    }
    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbilities.MOM.get().getExtraDesc();
    }

}
