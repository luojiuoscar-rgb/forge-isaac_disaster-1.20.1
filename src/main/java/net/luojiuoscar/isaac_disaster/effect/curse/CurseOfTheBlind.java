package net.luojiuoscar.isaac_disaster.effect.curse;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CurseOfTheBlind extends MobEffect {
    public CurseOfTheBlind(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of(new ItemStack(ModPassiveItems.BLACK_CANDLE.get()));
    }
}
