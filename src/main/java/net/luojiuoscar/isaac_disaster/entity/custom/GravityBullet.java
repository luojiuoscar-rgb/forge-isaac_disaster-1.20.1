package net.luojiuoscar.isaac_disaster.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GravityBullet extends TearBullet{
    private double gravity;

    public GravityBullet(EntityType<? extends TearBullet> type, Level level) {
        super(type, level);
        this.gravity = -1;
    }

    public GravityBullet(Level level, LivingEntity shooter, int lifeTick, double bulletSpeed, float scale, float damage, float xRot, float yRot) {
        super(level, shooter, lifeTick, bulletSpeed, scale, damage, xRot, yRot);
        this.gravity = -1;
    }

    public double getGravity(){
        return gravity;
    }

    public void setGravity(double gravity){
        this.gravity = gravity;
    }

    @Override
    public void tick(){
        Vec3 vel = getVelocity();
        double v = vel.length();
        vel = vel.add(0, gravity, 0).scale(v);
        setVelocity(new Vec3(0,0,0));

        super.tick();
    }
}
