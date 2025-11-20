package net.luojiuoscar.isaac_disaster.manager.attack.type;

import net.luojiuoscar.isaac_disaster.entity.custom.GravityBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.minecraft.world.entity.LivingEntity;

public class IpecacAttack extends BulletAttack{
    @Override
    public TearBullet getBulletObject(LivingEntity shooter, float xRot, float yRot){
        return new GravityBullet(
                shooter.level(),
                shooter,
                getBulletLiftTime(shooter),
                getBulletSpeed(shooter),
                getBulletScale(shooter),
                getDamage(shooter),
                xRot,
                yRot
        );
    }

}
