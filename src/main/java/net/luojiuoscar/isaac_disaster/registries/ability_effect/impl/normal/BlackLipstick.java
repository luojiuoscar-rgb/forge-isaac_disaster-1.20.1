package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.TransformLootTypesToAnother;
import net.minecraft.world.item.Item;

import java.util.List;

public class BlackLipstick extends TransformLootTypesToAnother {

    @Override
    protected double getProbability(ExecutableEffectContext context) {
        double amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.);

        return amplifier * 0.1;
    }

    @Override
    protected List<Item> getOriginalItemType() {
        return List.of(ModItems.SOUL_HEART.get(), ModItems.HALF_SOUL_HEART.get());
    }

    @Override
    protected List<Item> getTransformedItemType() {
        return List.of(ModItems.BLACK_HEART.get());
    }
}
