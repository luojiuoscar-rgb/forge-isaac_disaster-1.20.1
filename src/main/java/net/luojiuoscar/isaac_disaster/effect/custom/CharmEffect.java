package net.luojiuoscar.isaac_disaster.effect.custom;


import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;


public class CharmEffect extends MobEffect {
    public CharmEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (entity instanceof Player player) {
            player.removeEffect(this);
            return;
        }

        if (entity instanceof Mob mob) {
            // 跳过无攻击属性的被动生物
            if (mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == null)
                return;

            LivingEntity target = LevelHelper.findNearestLivingEntity(
                    mob.level(), mob.getX(), mob.getY(), mob.getZ(), StatManager.getNearbyRange(),
                    e -> e != mob && e.isAlive());

            if (target != null && target != mob) {
                mob.setTarget(target);
                mob.getBrain().setActiveActivityIfPossible(Activity.FIGHT);

                try {
                    mob.doHurtTarget(target);
                } catch (Exception e) {
                    // 兜底
                }
            }
        }
    }


    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }





}
