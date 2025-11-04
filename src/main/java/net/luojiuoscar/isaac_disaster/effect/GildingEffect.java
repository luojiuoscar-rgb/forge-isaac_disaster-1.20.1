package net.luojiuoscar.isaac_disaster.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

public class GildingEffect extends MobEffect {
    protected GildingEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }
}
