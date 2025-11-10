package net.luojiuoscar.isaac_disaster.effect.custom;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;


public class VulnerableEffect extends MobEffect {
    public VulnerableEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }
}
