package net.luojiuoscar.isaac_disaster.entity.custom;


import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class LemonEffectCloud extends AreaEffectCloud {
    private float damage;

    public LemonEffectCloud(EntityType<? extends AreaEffectCloud> type, Level level) {
        super(type, level);
    }

    public LemonEffectCloud(Level level, double x, double y, double z, LivingEntity owner, float radius,
                            int duration, float radiusPerTick, int waitTime, float damage){
        super(level, x, y, z);
        this.setOwner(owner);

        this.setRadius(radius);
        this.setDuration(duration);
        this.setRadiusPerTick(radiusPerTick);
        this.setWaitTime(waitTime);
        this.setFixedColor(ColorManager.LEMON);
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        double radius = this.getRadius();
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox());

        for (LivingEntity entity : entities) {
            if (!shouldAffect(entity)) continue;
            // 伤害来源
            if (getOwner() != null){
                entity.hurt(getOwner().damageSources().magic(), this.damage);
            }else{
                entity.hurt(entity.damageSources().magic(), this.damage);
            }
        }
    }

    /** 过滤规则 */
    private boolean shouldAffect(LivingEntity entity) {
        // 排除自己
        if (entity == this.getOwner()) return false;

        // 当owner是玩家时
        if (this.getOwner() instanceof Player player &&
        entity instanceof Player otherPlayer){
            if (player.isAlliedTo(otherPlayer)) return false;
            if (!player.canHarmPlayer(otherPlayer)) return false;
        };
        return true;
    }
}
