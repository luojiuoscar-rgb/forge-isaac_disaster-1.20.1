package net.luojiuoscar.isaac_disaster.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class TheWorldEffect extends MobEffect {
    public TheWorldEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }
}
