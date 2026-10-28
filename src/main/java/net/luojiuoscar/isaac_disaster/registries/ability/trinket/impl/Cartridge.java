package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Cartridge.getChance;

public class Cartridge extends TrinketAbility {

    public Cartridge(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (ctx.isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModules.CARTRIDGE.getId(), 2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModules.CARTRIDGE.getId(), 1);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (ctx.isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModules.CARTRIDGE.getId(), -2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModules.CARTRIDGE.getId(), -1);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        double luck = player == null ? 0 : player.getAttributeValue(Attributes.LUCK);
        return List.of(Component.translatable("item.isaac_disaster.cartridge.lore.1",
                        DescriptionHelper.dynamicNumber(100 * getChance(stack != null && Trinket.isEnchanted(stack) ? 2 : 1, luck))),
                Component.translatable("item.isaac_disaster.cartridge.lore.2"),
                Component.translatable("item.isaac_disaster.special.stackable"));
    }

}
