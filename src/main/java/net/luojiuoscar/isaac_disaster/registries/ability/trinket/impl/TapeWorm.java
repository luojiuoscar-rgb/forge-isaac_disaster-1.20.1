package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class TapeWorm extends TrinketAbility {
    public TapeWorm(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof Player player)) return;

        if (ctx.isEnchanted){
            StatManager.RANGE.apply(player, 3);
        }else{
            StatManager.RANGE.apply(player, 1.5);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (!(entity instanceof Player player)) return;

        if (ctx.isEnchanted){
            StatManager.RANGE.apply(player, -3);
        }else{
            StatManager.RANGE.apply(player, -1.5);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.RANGE.description(1.5)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        return (stack != null && Trinket.isEnchanted(stack))
                ? List.of(StatManager.RANGE.description(1.5, Style.EMPTY.withColor(ColorManager.SYNERGY)))
                : List.of();
    }
}
