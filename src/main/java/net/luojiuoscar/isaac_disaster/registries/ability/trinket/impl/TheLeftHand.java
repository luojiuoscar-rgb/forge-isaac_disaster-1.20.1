package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TheLeftHand extends TrinketAbility {
    public TheLeftHand(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.THE_LEFT_HAND.getId(), ctx.isEnchanted ? 2 : 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.THE_LEFT_HAND.getId(), ctx.isEnchanted ? -2 : -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(Component.translatable("item.isaac_disaster.the_left_hand.lore.1"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> desc = new ArrayList<>();

        if (stack != null && Trinket.isEnchanted(stack)){
            desc.add(DescriptionHelper.getSynergyDesc(
                    Component.translatable("item.isaac_disaster.trinket.enchanted"),
                    Component.translatable("item.isaac_disaster.synergy.description.no_effect")
            ));
        }

        return desc;
    }

}
