package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class Perfection extends TrinketAbility {
    public Perfection(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(Component.translatable("item.isaac_disaster.perfection.lore.1"),
                StatManager.LUCK.dynamicDescription(stack != null && Trinket.isEnchanted(stack) ? 20 : 10));
    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx){
        if (!(entity instanceof Player player)) return;
        if (ctx.isEnchanted) {
            StatManager.LUCK.apply(player, 20);
        } else {
            StatManager.LUCK.apply(player, 10);
        }

    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx){
        if (!(entity instanceof Player player)) return;
        if (ctx.isEnchanted) {
            StatManager.LUCK.apply(player, -20);
        } else {
            StatManager.LUCK.apply(player, -10);
        }
    }
}
