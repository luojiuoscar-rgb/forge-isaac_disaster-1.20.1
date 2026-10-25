package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.SpawnFamiliars;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class BoxOfSpiders extends ActiveAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.BOX_OF_SPIDERS)));

    public BoxOfSpiders(int id, int level) { super(TRIGGER, id, level); }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand) {
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        var generation = (SpawnFamiliars) ModExecutableEffects.BOX_OF_SPIDERS.get();
        return List.of(
                Component.translatable("item.isaac_disaster.box_of_spiders.lore.1",
                        generation.minimumCount(),
                        generation.maximumCount()));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        List<Component> result = new ArrayList<>(ModSetAbilities.SPIDER_BABY.get().getSynergyDesc());
        if (hasCarBattery(player)) result.add(DescriptionHelper.getSynergyDesc(
                Component.translatable("item.isaac_disaster.car_battery"),
                Component.translatable("item.isaac_disaster.synergy.description.double")));
        return result;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return ModSetAbilities.SPIDER_BABY.get().getExtraDesc();
    }
}
