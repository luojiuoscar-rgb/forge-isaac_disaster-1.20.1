package net.luojiuoscar.isaac_disaster.bullet.server;

import net.luojiuoscar.isaac_disaster.bullet.collision.EntityGrid;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.FetusMomsKnifeState;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Sweeps the scaled blade independently of the fetus body's hit cooldown and split lifecycle. */
final class FetusMomsKnifeDamage {
    private FetusMomsKnifeDamage() {}

    static void sweep(ServerLevel level, BulletState bullet, Vec3 start, Vec3 end, EntityGrid targets) {
        FetusMomsKnifeState knife = bullet.fetusKnife();
        LivingEntity owner = bullet.getOwner();
        if (knife == null || !bullet.isAlive() || owner == null || !owner.isAlive() || owner.level() != level
                || FetusMomsKnifeState.scale(bullet.renderScale()) <= 0) return;
        var path = knife.sweep(start, end, bullet.velocity(), bullet.renderScale(), bullet.collisionWidth());
        AABB bounds = path.get(0).bounds();
        for (var blade : path) bounds = bounds.minmax(blade.bounds());
        float damage = 2 * bullet.getDamage();
        if (!Float.isFinite(damage) || damage <= 0) return;
        for (LivingEntity target : targets.query(bounds)) {
            if (target == owner || !target.isAlive() || target.isRemoved() || target.isSpectator()
                    || EntityHelper.isFriendly(target, owner)) continue;
            for (int i = 1; i < path.size(); i++) {
                if (PlayerMomsKnifeMotion.contacts(path.get(i - 1), path.get(i), target.getBoundingBox())) {
                    if (knife.markHit(level.getGameTime(), target.getUUID())) {
                        target.hurt(ModDamageSources.momsKnife(level, owner, owner), damage);
                    }
                    if (!bullet.isAlive()) return;
                    break;
                }
            }
        }
    }
}
