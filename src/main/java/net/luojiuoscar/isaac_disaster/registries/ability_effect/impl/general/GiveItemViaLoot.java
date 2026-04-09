package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * double: random count from a to b
 */
public class GiveItemViaLoot implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        Item item = context.get(ContextKeys.ITEM);
        if (item == null) return false;

        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        nums = new ArrayList<>(nums);
        if (nums.size() < 2){
            nums = List.of(1., 2.);
        }

        LootHelper.spawnItemViaLoot(entity, pos, item, entity.getRandom().nextInt(
                nums.get(0).intValue(), nums.get(1).intValue()));
        return true;
    }
}
