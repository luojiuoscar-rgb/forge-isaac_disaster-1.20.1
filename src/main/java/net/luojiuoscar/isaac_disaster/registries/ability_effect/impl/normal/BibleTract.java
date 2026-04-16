package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.pickup.Heart;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.TransformLootTypesToAnother;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BibleTract extends TransformLootTypesToAnother {
    @Override
    protected double getProbability(ExecutableEffectContext context) {
        double amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.);

        return amplifier / 30;
    }

    @Override
    protected List<Item> getOriginalItemType() {
        return List.of();
    }

    @Override
    protected List<Item> getTransformedItemType() {
        return List.of(ModItems.ETERNAL_HEART.get());
    }

    @Override
    protected @Nullable Class<? extends Item> getOriginalInstanceType() {
        return Heart.class;
    }
}
