package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WiggleWorm extends TrinketAbility {
    public WiggleWorm(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectory.WIGGLE_WORM.getId(), 1);
        if (ctx.isEnchanted){
            StatManager.TEARS.apply(player, 1);
        }else{
            StatManager.TEARS.apply(player, 0.5);
        }
        StatManager.addSpectral(player, 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;

        StatManager.addTrajectory(player, ModAttackTrajectory.WIGGLE_WORM.getId(), -1);
        if (ctx.isEnchanted){
            StatManager.TEARS.apply(player, -1);
        }else{
            StatManager.TEARS.apply(player, -0.5);
        }
        StatManager.addSpectral(player, -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.wiggle_worm.lore.1"),
                StatManager.TEARS.description(0.5),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> desc = new ArrayList<>();

        if (stack != null && Trinket.isEnchanted(stack)){
            desc.add(DescriptionHelper.getSynergyDesc(
                    Component.translatable("item.isaac_disaster.trinket.enchanted"),
                    StatManager.TEARS.description(0.5)
            ));
        }

        return desc;
    }
}
