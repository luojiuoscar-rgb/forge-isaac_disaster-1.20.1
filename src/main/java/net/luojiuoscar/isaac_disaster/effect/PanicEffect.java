package net.luojiuoscar.isaac_disaster.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;

public class PanicEffect extends MobEffect {
    // 用来存储每个生物对应的 AvoidEntityGoal
    private final Map<PathfinderMob, AvoidEntityGoal<Player>> goalMap = new IdentityHashMap<>();

    public PanicEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (!(entity instanceof PathfinderMob mob)) {
            entity.removeEffect(this);
            return;
        };

        // 已经添加过就不重复添加
        if (goalMap.containsKey(mob)) return;

        AvoidEntityGoal<Player> goal = new AvoidEntityGoal<>(
                mob,
                Player.class,
                12.0F, // 感知距离
                1.2D,  // 逃跑速度
                1.5D   // 惊慌速度
        );

        mob.goalSelector.addGoal(1, goal);
        goalMap.put(mob, goal);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (!(entity instanceof PathfinderMob mob)) return;

        // 如果之前有保存对应 Goal，则移除
        AvoidEntityGoal<Player> goal = goalMap.remove(mob);
        if (goal != null) {
            mob.goalSelector.removeGoal(goal);
        }
    }
}
