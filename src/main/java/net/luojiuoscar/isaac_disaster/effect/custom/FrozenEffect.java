package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Freezes ordinary mobs while letting external motion slide across the ground. */
public final class FrozenEffect extends EntityFreezeEffect {
    private static final ResourceLocation SOURCE = ModEffects.FROZEN.getId();
    private static final ResourceLocation LAYER = ModVisualLayers.FROZEN.getId();

    public FrozenEffect(MobEffectCategory category, int color) {
        super(category, color, SOURCE, LAYER);
    }

    public static void reconcileVisualState(LivingEntity entity) {
        EntityFreezeEffect.reconcileVisualState(entity, ModEffects.FROZEN.get(), SOURCE, LAYER);
    }
}
