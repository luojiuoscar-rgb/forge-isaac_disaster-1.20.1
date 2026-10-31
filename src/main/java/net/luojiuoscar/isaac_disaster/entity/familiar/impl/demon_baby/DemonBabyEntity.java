package net.luojiuoscar.isaac_disaster.entity.familiar.impl.demon_baby;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractAutoAttackBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DemonBabyEntity extends AbstractAutoAttackBabyFamiliarEntity {
    public DemonBabyEntity(EntityType<? extends DemonBabyEntity> type, Level level) { super(type, level); }

    @Override protected int attackIntervalTicks() { return 7; }

    @Override public int getAttackDurationTicks() { return 7; }

    @Override protected double targetDetectionRange() { return 8; }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        double configuredRange = GameplayConfig.DEFAULT_BULLET_RANGE.get();
        double range = Double.isFinite(configuredRange) ? Math.max(8, configuredRange) : 8;
        double damage = 1.5 * Math.max(0, StatManager.DAMAGE.getBonus()) * Math.max(0, 1 + getEnhancement());
        var color = ModBulletColors.BLOOD_TEAR;
        BulletState tear = BulletState.builder()
                .owner(owner).shooter(this).position(attackOrigin()).velocity(direction).baseSpeed(1)
                .range(range).lifetime((int) Math.min(Integer.MAX_VALUE, Math.ceil(range) + 1))
                .damage(Double.isFinite(damage) ? damage : 0)
                .colorId(color.getId()).color(color.get().color()).alpha(color.get().alpha())
                .renderScale(1).collisionWidth(0.2).collisionHeight(0.2).noGravity(true).build();
        return BulletRuntime.INSTANCE.spawn(owner.serverLevel(), tear) != null;
    }

    @Override
    protected void onAttackSucceeded(ServerPlayer owner, Vec3 direction) {
        super.onAttackSucceeded(owner, direction);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.TEAR_BULLET_SHOT.get(), SoundSource.PLAYERS, 0.6F, 1F);
    }
}
