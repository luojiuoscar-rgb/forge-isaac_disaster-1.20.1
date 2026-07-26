package net.luojiuoscar.isaac_disaster.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 一个让玩家在药效期间可以飞行的效果。
 * Forge 1.20.1 适用
 */
public class TranscendenceEffect extends MobEffect {
    public TranscendenceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
