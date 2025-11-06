package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;


public class TheWorldEffect extends MobEffect {
    protected TheWorldEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }

    public static void onTriggered(LivingEvent.LivingTickEvent event){
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (PlayerHelper.countPlayer(p -> p.hasEffect(ModEffects.THE_WORLD.get())) > 0){
            boolean timeStop = theWorldEffect(entity);
            if (timeStop) {
                event.setCanceled(true);
                if (entity instanceof Mob mob) {
                    // 清空当前执行的 Goal
                    mob.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
                    mob.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
                }
                if (entity instanceof Creeper creeper){
                    creeper.setSwellDir(0);
                }
            }
        }
    }

    private static boolean theWorldEffect(LivingEntity entity){
        if (entity.isDeadOrDying() || entity.hasEffect(ModEffects.THE_WORLD.get())) return false;

        if (entity.invulnerableTime > 0) {
            entity.invulnerableTime = 0;
            entity.hurtDuration = 0;
            entity.hurtTime = 0;
            entity.hurtMarked = false;
        }
        return true;
    }
}
