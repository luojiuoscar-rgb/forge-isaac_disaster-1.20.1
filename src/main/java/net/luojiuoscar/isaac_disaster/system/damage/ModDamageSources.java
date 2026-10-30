package net.luojiuoscar.isaac_disaster.system.damage;

import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Damage attribution shared by entity-free tears, lasers, and entity-backed familiars. */
public final class ModDamageSources {
    private ModDamageSources() {
    }

    public static DamageSource tear(Level level, @Nullable Entity owner, Vec3 velocity, double knockbackMultiplier) {
        return tear(level, owner, velocity, knockbackMultiplier, null);
    }

    public static DamageSource tear(Level level, @Nullable Entity owner, Vec3 velocity, double knockbackMultiplier,
                                    @Nullable IBulletObject projectile) {
        return new TearDamageSource(type(level, ModDamageType.TEAR), owner, velocity, knockbackMultiplier, projectile);
    }

    public static DamageSource laser(Level level, @Nullable Entity owner) {
        return laser(level, owner, null);
    }

    public static DamageSource laser(Level level, @Nullable Entity owner, @Nullable IBulletObject projectile) {
        return new ProjectileDamageSource(type(level, ModDamageType.LASER), owner, owner, projectile);
    }

    @Nullable
    public static IBulletObject projectile(DamageSource source) {
        return source instanceof ProjectileDamageSource projectileSource ? projectileSource.projectile : null;
    }

    public static DamageSource familiar(Level level, Entity direct, @Nullable Entity owner) {
        return new DamageSource(type(level, ModDamageType.FAMILIAR), direct, owner);
    }

    public static DamageSource water(Level level, @Nullable Entity owner) {
        return new DamageSource(type(level, ModDamageType.WATER), null, owner);
    }

    public static DamageSource holyLight(Level level, Entity caster) {
        return new DamageSource(type(level, ModDamageType.HOLY_LIGHT), null, caster);
    }

    private static Holder<DamageType> type(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    private static class ProjectileDamageSource extends DamageSource {
        @Nullable private final IBulletObject projectile;

        private ProjectileDamageSource(Holder<DamageType> type, @Nullable Entity direct, @Nullable Entity owner,
                                       @Nullable IBulletObject projectile) {
            super(type, direct, owner);
            this.projectile = projectile;
        }
    }

    static final class TearDamageSource extends ProjectileDamageSource {
        // Vec3 is immutable, so retaining this value cannot follow later bullet changes.
        final Vec3 velocity;
        final double knockbackMultiplier;

        private TearDamageSource(Holder<DamageType> type, @Nullable Entity owner, Vec3 velocity, double knockbackMultiplier,
                                 @Nullable IBulletObject projectile) {
            super(type, null, owner, projectile);
            this.velocity = velocity;
            this.knockbackMultiplier = HurtKnockback.normalizeMultiplier(knockbackMultiplier);
        }
    }
}
