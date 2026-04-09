package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class LuckyToe extends TrinketAbility {

    public LuckyToe(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(StatManager.LUCK.description(1),
                Component.translatable("item.isaac_disaster.lucky_toe.lore.1"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        return (stack != null && Trinket.isEnchanted(stack))
                ? List.of(StatManager.LUCK.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY)))
                : List.of();
    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx){
        if (!(entity instanceof Player player)) return;
        StatManager.addTriggerModule(entity, ModTriggerModule.CHEST_LOOT_TRINKET.getId(), 1);
        if (ctx.isEnchanted) {
            StatManager.LUCK.apply(player, 2);
        } else {
            StatManager.LUCK.apply(player, 1);
        }

    };
    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx){
        if (!(entity instanceof Player player)) return;
        StatManager.addTriggerModule(entity, ModTriggerModule.CHEST_LOOT_TRINKET.getId(), -1);
        if (ctx.isEnchanted) {
            StatManager.LUCK.apply(player, -2);
        } else {
            StatManager.LUCK.apply(player, -1);
        }
    };

}
