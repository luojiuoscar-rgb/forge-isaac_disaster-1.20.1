package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.CricketLeg.getChance;

public final class CricketLeg extends TrinketAbility {
    public CricketLeg(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext context) {
    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext context) {
        if (entity instanceof ServerPlayer) {
            StatManager.addTriggerModule(entity, ModTriggerModules.CRICKET_LEG.getId(), context.isEnchanted ? 2 : 1);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext context) {
        if (entity instanceof ServerPlayer) {
            StatManager.addTriggerModule(entity, ModTriggerModules.CRICKET_LEG.getId(), context.isEnchanted ? -2 : -1);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        int amplifier = stack != null && Trinket.isEnchanted(stack) ? 2 : 1;
        return List.of(Component.translatable("item.isaac_disaster.cricket_leg.lore.1",
                        DescriptionHelper.dynamicNumber(100 * getChance(amplifier))),
                Component.translatable("item.isaac_disaster.special.stackable"));
    }
}
