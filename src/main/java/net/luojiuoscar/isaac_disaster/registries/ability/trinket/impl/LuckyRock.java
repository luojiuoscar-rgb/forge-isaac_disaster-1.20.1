package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class LuckyRock extends TrinketAbility {
    public LuckyRock(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx) {

    }

    @Override
    public void onEquipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (ctx.isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModule.LUCKY_ROCK.getId(), 2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModule.LUCKY_ROCK.getId(), 1);
        }
    }

    @Override
    public void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx) {
        if (ctx.isEnchanted){
            StatManager.addTriggerModule(entity, ModTriggerModule.LUCKY_ROCK.getId(), -2);
        }else{
            StatManager.addTriggerModule(entity, ModTriggerModule.LUCKY_ROCK.getId(), -1);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(Component.translatable("item.isaac_disaster.lucky_rock.lore.1"));
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        return (stack != null && Trinket.isEnchanted(stack))
                ? List.of(Component.translatable("item.isaac_disaster.lucky_rock.enchanted.lore.1").withStyle(style -> style.withColor(ColorManager.SYNERGY)))
                : List.of();
    }
}
