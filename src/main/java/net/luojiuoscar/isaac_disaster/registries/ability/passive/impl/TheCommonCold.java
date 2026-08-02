package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.TheCommonCold.getTriggerChance;

public class TheCommonCold extends PassiveAbility {
    private static final double BASE_TRIGGER_CHANCE_PERCENT = 25.0;
    public TheCommonCold(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.THE_COMMON_COLD.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.THE_COMMON_COLD.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        double chancePercent = player == null
                ? BASE_TRIGGER_CHANCE_PERCENT
                : getTriggerChance(player) * 100.0;
        return List.of(
                Component.translatable("item.isaac_disaster.the_common_cold.lore.1",
                        DescriptionHelper.dynamicNumber(BASE_TRIGGER_CHANCE_PERCENT, chancePercent))
        );
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player){
        List<Component> description = new ArrayList<>();

        description.add(EffectManager.POISON.getExplainDesc());

        return description;
    }
}
