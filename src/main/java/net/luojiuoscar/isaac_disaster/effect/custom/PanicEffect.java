package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.EnumSet;

public class PanicEffect extends MobEffect {
    // 用来存储每个生物对应的 AvoidEntityGoal
    private final Map<PathfinderMob, AvoidEntityGoal<Player>> goalMap = new IdentityHashMap<>();

    public PanicEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof PathfinderMob mob) || entity.getType().is(TagManager.BOSSES)) {
            entity.removeEffect(this);
            return;
        }

        // 被魅惑覆盖
        if (entity.hasEffect(ModEffects.CHARM.get())) return;

        stopAttacking(mob);
        addFleeGoal(mob);
    }

    private void addFleeGoal(PathfinderMob mob) {

        // 已经添加过就不重复添加
        if (goalMap.containsKey(mob)) return;

        AvoidEntityGoal<Player> goal = new AvoidEntityGoal<>(
                mob,
                Player.class,
                12.0F, // 感知距离
                1.2D,  // 逃跑速度
                1.5D   // 惊慌速度
        );

        goal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        mob.goalSelector.addGoal(0, goal);
        goalMap.put(mob, goal);
    }

    public static boolean suppressesAttacks(Mob mob) {
        return mob instanceof PathfinderMob && !mob.getType().is(TagManager.BOSSES)
                && mob.hasEffect(ModEffects.PANIC.get()) && !mob.hasEffect(ModEffects.CHARM.get());
    }

    private static void stopAttacking(Mob mob) {
        mob.setTarget(null);
        mob.setAggressive(false);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !(entity instanceof PathfinderMob mob)) return;
        if (!suppressesAttacks(mob)) {
            removeFleeGoal(mob);
            return;
        }
        stopAttacking(mob);
        addFleeGoal(mob);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return true; }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (!(entity instanceof PathfinderMob mob)) return;

        removeFleeGoal(mob);
    }

    private void removeFleeGoal(PathfinderMob mob) {
        AvoidEntityGoal<Player> goal = goalMap.remove(mob);
        if (goal != null) {
            mob.goalSelector.removeGoal(goal);
        }
    }
}
