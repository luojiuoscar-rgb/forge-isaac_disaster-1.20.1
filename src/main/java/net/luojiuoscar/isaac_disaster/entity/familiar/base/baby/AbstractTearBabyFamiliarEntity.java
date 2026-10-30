package net.luojiuoscar.isaac_disaster.entity.familiar.base.baby;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** Ordinary tear construction; concrete babies define projectile properties and directions. */
public abstract class AbstractTearBabyFamiliarEntity extends AbstractBabyFamiliarEntity {
    protected AbstractTearBabyFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected abstract double tearDamageCoefficient();

    protected abstract RegistryObject<BulletColor> tearColor();

    /** Applies the concrete baby's projectile properties after the ordinary tear defaults. */
    protected void configureTear(BulletState.Builder builder) { }

    /** Directions for one complete action; the default baby fires one tear along its aim. */
    protected List<Vec3> tearDirections(Vec3 aim) { return List.of(aim); }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        double configuredRange = GameplayConfig.DEFAULT_BULLET_RANGE.get();
        double range = Double.isFinite(configuredRange) ? Math.max(12, 1.5 * configuredRange) : 12;
        double damage = tearDamageCoefficient() * Math.max(0, StatManager.DAMAGE.getBonus()) * Math.max(0, 1 + getEnhancement());
        var color = tearColor();
        Vec3 origin = attackOrigin();
        List<BulletState> volley = new ArrayList<>();
        for (Vec3 axis : tearDirections(direction)) {
            double lengthSqr = axis.lengthSqr();
            if (!Double.isFinite(lengthSqr) || lengthSqr < 1e-12) return false;
            BulletState.Builder builder = BulletState.builder()
                    .owner(owner).shooter(this)
                    .position(origin).velocity(axis.normalize()).baseSpeed(1)
                    .range(range)
                    // Age expires before movement, so N movement ticks need lifetime N+1.
                    .lifetime((int) Math.min(Integer.MAX_VALUE, Math.ceil(range) + 1))
                    .damage(Double.isFinite(damage) ? damage : 0)
                    .colorId(color.getId()).color(color.get().color()).alpha(color.get().alpha())
                    .renderScale(1).collisionWidth(0.2).collisionHeight(0.2)
                    .noGravity(true);
            configureTear(builder);
            BulletState state = builder.build();
            if (!state.validateRuntimeVelocity()) return false;
            volley.add(state);
        }
        var runtime = BulletRuntime.INSTANCE;
        var level = owner.serverLevel();
        if (volley.isEmpty() || (long) runtime.activeCount(level.getServer()) + volley.size()
                > GameplayConfig.MAX_ACTIVE_BULLETS.get()) return false;
        for (BulletState state : volley) {
            if (runtime.spawn(level, state) == null) {
                // Visibility is published at the level tick boundary, so no partial volley has reached clients.
                for (BulletState admitted : volley) runtime.manager(level).remove(admitted);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onAttackSucceeded(ServerPlayer owner, Vec3 direction) {
        super.onAttackSucceeded(owner, direction);
        playAttackSound();
    }

    protected void playAttackSound() {
        level().playSound(null, getX(), getY(), getZ(), ModSounds.TEAR_BULLET_SHOT.get(),
                SoundSource.PLAYERS, 0.6F, 1F);
    }

}
