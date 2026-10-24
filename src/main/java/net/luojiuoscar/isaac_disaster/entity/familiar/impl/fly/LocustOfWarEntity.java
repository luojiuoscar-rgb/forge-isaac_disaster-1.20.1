package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeExecutableEffect;
import net.luojiuoscar.isaac_disaster.system.explosion.BombEffectSource;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Red locust: ordinary fly contact followed by one explosion. */
public final class LocustOfWarEntity extends BlueFlyEntity implements Locust, BombEffectSource {
    private final CompositeExecutableEffect cachedEffect = new CompositeExecutableEffect();
    private boolean effectsInitialized;

    public LocustOfWarEntity(EntityType<? extends LocustOfWarEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0xD04030;
    }

    @Override
    public LocustOfWarEntity getBombEffectEntity() {
        return this;
    }

    @Override
    public int getPower() {
        return 4;
    }

    @Override
    public float getScale() {
        return BombData.NORMAL.size();
    }

    @Override
    public boolean isOriginal() {
        return true;
    }

    @Override
    public CompositeExecutableEffect getCachedEffect() {
        return cachedEffect;
    }

    @Override
    public boolean markEffectsInitialized() {
        if (effectsInitialized) return false;
        effectsInitialized = true;
        return true;
    }

    @Override
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                float attemptedDamage, boolean damageAccepted) {
        float centerDamage = (float) (20 * StatManager.DAMAGE.getBonus());
        LevelHelper.explodeCustom(owner.serverLevel(), hitPosition,
                level().damageSources().explosion(this, owner),
                ExplosionParameters.standard(4, centerDamage));
    }
}
