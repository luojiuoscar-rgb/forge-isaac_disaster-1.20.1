package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.text2speech.Narrator.LOGGER;

public class IsaacPoisonEffect extends MobEffect {
    public IsaacPoisonEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    private float getSourceDamage(LivingEntity entity){
        AtomicReference<Double> defaultDamage = new AtomicReference<>();
        entity.getCapability(EntityEffectProvider.ENTITY_CAP).ifPresent(
                entityEffect -> defaultDamage.set(entityEffect.getSourceDamageFromName(EffectNameManager.ISAAC_POISON))
        );
        // 返回最终结果
        return (float) (double) defaultDamage.get();
    }


    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        float amount = getSourceDamage(pLivingEntity) * (0.25f + 0.05f * pAmplifier);
        // 无死亡保护
        pLivingEntity.hurt(pLivingEntity.damageSources().magic(), amount);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier){
        return pDuration % 20 == 0;
    }
}
