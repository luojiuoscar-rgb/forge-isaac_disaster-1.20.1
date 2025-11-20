package net.luojiuoscar.isaac_disaster.effect.custom;


import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PoisonEffect extends MobEffect {
    public PoisonEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    private float getSourceDamage(LivingEntity entity){
        return entity.getCapability(EntityEffectProvider.ENTITY_EFFECT_CAP)
                .map(entityEffect -> (float) Math.max(1, entityEffect.getSourceDamageFromId(EffectManager.POISON.getId())))
                .orElse(1f);
    }


    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        float amount = getSourceDamage(pLivingEntity) * (0.30f + 0.05f * pAmplifier);
        // 无死亡保护
        pLivingEntity.hurt(pLivingEntity.damageSources().magic(), amount);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier){
        return pDuration % 20 == 0;
    }
}
