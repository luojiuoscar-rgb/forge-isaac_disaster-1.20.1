package net.luojiuoscar.isaac_disaster.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;



public class PanicEffect extends MobEffect {
    private static final String PANIC_GOAL_KEY = "panicFleeGoal";

    protected PanicEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof PathfinderMob mob)) {
            entity.removeEffect(this);
            return;
        }

        // 检查是否已添加过
        if (mob.getPersistentData().getBoolean(PANIC_GOAL_KEY)) return;

        // 添加通用逃离玩家的Goal
        mob.goalSelector.addGoal(1,
                new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(
                        mob,                      // 自己
                        Player.class,             // 要躲的类型
                        16.0F,                    // 触发距离
                        1.0D,                     // 远离速度
                        1.2D                      // 惊慌速度
                )
        );

        mob.getPersistentData().putBoolean(PANIC_GOAL_KEY, true);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (!(entity instanceof Mob mob)) return;

        // 清空该Goal
        mob.goalSelector.getAvailableGoals().removeIf(wrapped ->
                wrapped.getGoal() instanceof net.minecraft.world.entity.ai.goal.AvoidEntityGoal
        );

        mob.getPersistentData().remove(PANIC_GOAL_KEY);
    }
}