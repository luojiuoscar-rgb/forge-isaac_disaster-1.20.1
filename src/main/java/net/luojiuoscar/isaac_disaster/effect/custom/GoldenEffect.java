package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class GoldenEffect extends FrozenEffect {
    public GoldenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor, ModVisualLayers.GOLDEN.getId(), ModVisualLayers.GOLDEN.getId());
    }

    public static void reconcileVisualState(LivingEntity entity) {
        FrozenEffect.reconcileVisualState(entity, ModEffects.GOLDEN.get(),
                ModVisualLayers.GOLDEN.getId(), ModVisualLayers.GOLDEN.getId());
    }
}
