package net.luojiuoscar.isaac_disaster.system.damage;

import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import org.jetbrains.annotations.Nullable;

/** A hurt-call scope owns exactly one matching Forge knockback dispatch. */
public final class HurtKnockback {
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();

    private HurtKnockback() {
    }

    public static void withSource(LivingEntity target, DamageSource source, double strength, Runnable knockback) {
        Frame previous = CURRENT.get();
        CURRENT.set(new Frame(target, response(source, strength)));
        try {
            knockback.run();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }

    /** Claim before dispatch so a listener's direct knockback cannot inherit its caller's source. */
    public static @Nullable Response claim(LivingEntity target) {
        Frame frame = CURRENT.get();
        if (frame == null || frame.target != target || frame.claimed) return null;
        frame.claimed = true;
        return frame.response;
    }

    /** Captures the owning entity's multiplier once; non-player sources use the configured default. */
    public static double captureMultiplier(@Nullable LivingEntity owner) {
        var attribute = owner == null ? null : owner.getAttribute(ModAttributes.BULLET_KNOCKBACK.get());
        return normalizeMultiplier(attribute == null ? GameplayConfig.DEFAULT_BULLET_KNOCKBACK.get() : attribute.getValue());
    }

    public static double normalizeMultiplier(double value) {
        return Double.isFinite(value) ? Math.max(0, Math.min(1024, value)) : 0;
    }

    private static @Nullable Response response(DamageSource source, double strength) {
        if (source.is(ModDamageType.TEAR)) {
            Vec3 velocity = source instanceof ModDamageSources.TearDamageSource tear ? tear.velocity : Vec3.ZERO;
            double multiplier = source instanceof ModDamageSources.TearDamageSource tear ? tear.knockbackMultiplier : 0;
            float scaled = Double.isFinite(strength) ? (float) (Math.max(0, strength) * multiplier) : 0;
            return new Response(true, scaled, -velocity.x, -velocity.z);
        }
        if (source.is(ModDamageType.LASER)
                || source.is(ModDamageType.FAMILIAR)
                || source.is(ModDamageType.WATER)) {
            return new Response(false, 0, 0, 0);
        }
        return null;
    }

    private static final class Frame {
        final LivingEntity target;
        final @Nullable Response response;
        boolean claimed;

        Frame(LivingEntity target, @Nullable Response response) {
            this.target = target;
            this.response = response;
        }
    }

    public record Response(boolean tear, float strength, double ratioX, double ratioZ) {
        public void apply(LivingEntity target, LivingKnockBackEvent event) {
            if (!tear || event.isCanceled()) return;
            Vec3 current = target.getDeltaMovement();
            Vec3 updated = applyTearImpulse(current, event.getStrength(), event.getRatioX(), event.getRatioZ(),
                    target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            if (!updated.equals(current)) {
                target.setDeltaMovement(updated);
                target.hasImpulse = true;
            }
        }
    }

    /** Forge ratios point toward the attacker; the applied impulse points in the opposite direction. */
    public static Vec3 applyTearImpulse(Vec3 current, double strength, double ratioX, double ratioZ,
                                       double resistance) {
        double adjustedStrength = strength * Math.max(0, 1 - resistance);
        Vec3 direction = new Vec3(ratioX, 0, ratioZ);
        if (!(adjustedStrength > 0) || direction.lengthSqr() < 1.0E-12) return current;
        return current.subtract(direction.normalize().scale(adjustedStrength));
    }
}
