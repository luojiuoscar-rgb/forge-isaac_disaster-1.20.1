package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class GoldenEffect extends EntityFreezeEffect {
    private static final ResourceLocation SOURCE = ModEffects.GOLDEN.getId();
    private static final ResourceLocation LAYER = ModVisualLayers.GOLDEN.getId();

    public GoldenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor, SOURCE, LAYER);
    }

    public static void reconcileVisualState(LivingEntity entity) {
        EntityFreezeEffect.reconcileVisualState(entity, ModEffects.GOLDEN.get(), SOURCE, LAYER);
    }
}
