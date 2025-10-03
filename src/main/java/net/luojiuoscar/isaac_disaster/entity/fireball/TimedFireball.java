package net.luojiuoscar.isaac_disaster.entity.fireball;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;

public class TimedFireball extends LargeFireball {
    private int life;
    private final int max_life;

    public TimedFireball(Level level, LivingEntity shooter, double vx, double vy, double vz, int power) {
        this(level, shooter, vx, vy, vz, power, 120);
    }
    public TimedFireball(Level level, LivingEntity shooter, double vx, double vy, double vz, int power, int life) {
        super(level, shooter, vx, vy, vz, power);
        this.max_life = life;
        this.life = 0;
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (life > max_life) {
            this.discard();
        }
    }
}
