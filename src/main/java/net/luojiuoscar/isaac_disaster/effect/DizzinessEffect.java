package net.luojiuoscar.isaac_disaster.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

public class DizzinessEffect extends MobEffect {
    protected DizzinessEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        //禁止移动
        Vec3 motion = pLivingEntity.getDeltaMovement();
        pLivingEntity.setDeltaMovement(0, motion.y, 0);

        //禁止飞行
        Player player = (Player) pLivingEntity;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier){
        return true;
    }
}
