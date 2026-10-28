package net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;


/** Brother Bobby defines its own independent ordinary tear, damage, range and shot presentation. */
final class BrotherBobbyCombat {
    static final int SHOT_INTERVAL = 10;
    boolean fire(ServerPlayer owner, BrotherBobbyEntity bobby, Vec3 direction, long now) {
        if (direction.lengthSqr() < 1.0e-12) return false;
        Vec3 axis = direction.normalize();
        double shotRange = range(GameplayConfig.DEFAULT_BULLET_RANGE.get());
        BulletState state = BulletState.builder()
                .owner(owner).shooter(bobby)
                .position(bobby.muzzle(axis)).velocity(axis).baseSpeed(1)
                .range(shotRange)
                // Plain tears expire by age before moving: N movement ticks need lifetime N+1.
                .lifetime((int) Math.min(Integer.MAX_VALUE, Math.ceil(shotRange) + 1))
                .damage(damage(StatManager.DAMAGE.getBonus(), bobby.getEnhancement()))
                .colorId(ModBulletColors.BASE.getId()).color(ModBulletColors.BASE.get().color())
                .alpha(ModBulletColors.BASE.get().alpha())
                .renderScale(1).collisionWidth(0.2).collisionHeight(0.2)
                .noGravity(true).build();
        if (BulletRuntime.INSTANCE.spawn(owner.serverLevel(), state) == null) return false;
        bobby.recordShot(axis, now);
        return true;
    }

    static double damage(double bonus, double enhancement) {
        double result = 2 * Math.max(0, bonus) * Math.max(0, 1 + enhancement);
        return Double.isFinite(result) ? result : 0;
    }

    static double range(double configuredBase) {
        return Double.isFinite(configuredBase) ? Math.max(12, 1.5 * configuredBase) : 12;
    }
}
