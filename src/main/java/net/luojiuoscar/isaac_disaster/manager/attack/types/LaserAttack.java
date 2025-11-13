package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.luojiuoscar.isaac_disaster.manager.attack.AttackTypeId;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class LaserAttack implements IAttackType {
    private static final double RANGE = 20.0; // 激光长度
    private static final double WIDTH = 0.8;  // 激光“粗细”判定范围
    private static final float DAMAGE = 6.0f;

    @Override
    public int getId() {
        return AttackTypeId.LASER.getId();
    }

    @Override
    public int getPriority() {
        return AttackTypeId.LASER.getPriority();
    }

    @Override
    public void performAttack(LivingEntity living, int color, Set<Integer> hitEffects) {
        if (!(living instanceof Player player)) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Level level = player.level();

        // 终点
        Vec3 endPos = eyePos.add(lookVec.scale(RANGE));

        // 服务端负责伤害判定
        if (!level.isClientSide) {
            AABB laserBox = new AABB(eyePos, endPos).inflate(WIDTH);
            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class, laserBox,
                    e -> e != player && e.isAlive()
            );

            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().playerAttack(player), DAMAGE);
            }
        }

        // 客户端负责渲染光束（例如粒子/自定义渲染）
        if (level.isClientSide) {
           // LaserRenderer.spawnLaserBeam(level, eyePos, endPos);
        }
    }
}
