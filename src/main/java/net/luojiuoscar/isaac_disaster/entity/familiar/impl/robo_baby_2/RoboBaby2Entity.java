package net.luojiuoscar.isaac_disaster.entity.familiar.impl.robo_baby_2;

import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractAutoAttackBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RoboBaby2Entity extends AbstractAutoAttackBabyFamiliarEntity {
    private static final LaserAttack LASER = new LaserAttack(0) {
        @Override protected boolean isSpectral(LivingEntity owner) { return true; }
        @Override protected boolean isHoming(LivingEntity owner) { return false; }
    };

    public RoboBaby2Entity(EntityType<? extends RoboBaby2Entity> type, Level level) { super(type, level); }

    @Override protected int attackIntervalTicks() { return 7; }

    @Override protected double targetDetectionRange() { return 12; }

    @Override protected boolean requiresTargetLineOfSight() { return false; }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        double configuredRange = GameplayConfig.DEFAULT_BULLET_RANGE.get();
        double range = Double.isFinite(configuredRange) ? Math.min(64, Math.max(24, 2 * configuredRange)) : 24;
        double damage = 2 * Math.max(0, StatManager.DAMAGE.getBonus()) * Math.max(0, 1 + getEnhancement());
        AttackContext context = AttackContext.builder(owner, this)
                .attackType(LASER).position(attackOrigin()).mainAxis(direction)
                .damage(Double.isFinite(damage) ? Math.min(Float.MAX_VALUE, damage) : 0)
                .range(range).speed(1).bulletScaleModifier(0.0D)
                .color(ModBulletColors.BASE.getId()).build();
        context.useFixedLaunchTransform();
        context.setBulletScale(1, true);
        context.freeze();
        LASER.performAttack(List.of(context));
        return true;
    }

    @Override
    protected Vec3 attackOrigin() {
        // Neutral head center: rig(24) + body(-6) + head(-3) + geometry center(-4) = 11 pixels.
        return position().add(0, (14.5 - 11) / 16 * getBaseModelScale() * getVisualScale(), 0);
    }

    @Override
    protected void onAttackSucceeded(ServerPlayer owner, Vec3 direction) {
        super.onAttackSucceeded(owner, direction);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.LASER_SHOT.get(), SoundSource.PLAYERS, 0.6F, 1F);
    }
}
