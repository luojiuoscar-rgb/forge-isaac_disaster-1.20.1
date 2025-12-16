package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class HookWorm extends TrinketAbility {
    public HookWorm(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof Player player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectory.HOOK_WORM.getId(), 1);
        if (ctx.isEnchanted){
            StatManager.TEARS.apply(player, 1);
            StatManager.RANGE.apply(player, 1);
        }else{
            StatManager.TEARS.apply(player, 0.5);
            StatManager.RANGE.apply(player, 0.5);
        }
        StatManager.addSpectral(player, 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof Player player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectory.HOOK_WORM.getId(), -1);
        if (ctx.isEnchanted){
            StatManager.TEARS.apply(player, -1);
            StatManager.RANGE.apply(player, -1);
        }else{
            StatManager.TEARS.apply(player, -0.5);
            StatManager.RANGE.apply(player, -0.5);
        }        StatManager.addSpectral(player, -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.hook_worm.lore.1"),
                StatManager.TEARS.description(0.5),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        return (stack != null && Trinket.isEnchanted(stack))
                ? List.of(
                StatManager.TEARS.description(0.5, Style.EMPTY.withColor(ColorManager.SYNERGY)),
                StatManager.RANGE.description(0.5, Style.EMPTY.withColor(ColorManager.SYNERGY)))
                : List.of();
    }
}
