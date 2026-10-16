package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.TransformLootTypesToAnother;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class HumbleingBundle extends TransformLootTypesToAnother {
    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWER.priority;
    }

    @Override
    protected double getProbability(ExecutableEffectContext context) {
        return 0.5;
    }

    @Override
    protected List<Item> getOriginalItemType() {
        var items = new ArrayList<>(List.of(ModItems.BOMB.get(), ModItems.KEY.get(), ModItems.RED_HEART.get()));
        Item coin = PlayerHelper.getItemFromConfig(GameplayConfig.COIN_TIER_1_ID.get());
        if (coin != null) items.add(coin);
        return items;
    }

    @Override
    protected List<Item> getTransformedItemType() {
        var items = new ArrayList<>(List.of(ModItems.DOUBLE_BOMB.get(), ModItems.KEY_RING.get(), ModItems.DOUBLE_RED_HEART.get()));
        Item coin = PlayerHelper.getItemFromConfig(GameplayConfig.COIN_TIER_1_ID.get());
        if (coin != null) items.add(coin);
        return items;
    }

    @Override
    protected ItemStack createTransformedStack(ItemStack original, Item replacement, int count) {
        if (replacement == original.getItem()) return original.copyWithCount(count * 2);
        return super.createTransformedStack(original, replacement, count);
    }
}
