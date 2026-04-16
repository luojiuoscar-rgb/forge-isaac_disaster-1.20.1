package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.TransformLootTypesToAnother;
import net.minecraft.world.item.Item;

import java.util.List;

public class BogoBombs extends TransformLootTypesToAnother {
    @Override
    protected double getProbability(ExecutableEffectContext context) {
        return 1;
    }

    @Override
    protected List<Item> getOriginalItemType() {
        return List.of(ModItems.BOMB.get());
    }

    @Override
    protected List<Item> getTransformedItemType() {
        return List.of(ModItems.DOUBLE_BOMB.get());
    }
}
