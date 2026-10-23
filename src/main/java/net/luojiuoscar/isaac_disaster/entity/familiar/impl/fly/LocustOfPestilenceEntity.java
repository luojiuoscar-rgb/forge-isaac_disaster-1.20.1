package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Green locust: ordinary fly contact damage followed by two D-sized poison pulses. */
public final class LocustOfPestilenceEntity extends BlueFlyEntity {
    // Pulses occur at remaining durations 40 and 20, on ticks 20 and 40 after contact.
    // Starting at 40 would immediately lose the first pulse to vanilla hurt cooldown.
    private static final int POISON_DURATION_TICKS = 50;

    public LocustOfPestilenceEntity(EntityType<? extends LocustOfPestilenceEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0x4AA64F;
    }

    @Override
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                float attemptedDamage, boolean damageAccepted) {
        if (!damageAccepted || !target.isAlive()) return;
        int amplifier = DeadlyPoisonEffect.amplifierForDamage(attemptedDamage * 0.5);
        target.addEffect(new MobEffectInstance(ModEffects.DEADLY_POISON.get(), POISON_DURATION_TICKS, amplifier), owner);
    }
}
