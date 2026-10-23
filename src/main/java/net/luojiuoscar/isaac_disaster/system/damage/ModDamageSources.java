package net.luojiuoscar.isaac_disaster.system.damage;

import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
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

    public static DamageSource tear(Level level, @Nullable Entity owner, Vec3 velocity) {
        return new TearDamageSource(type(level, ModDamageType.TEAR), owner, velocity);
    }

    public static DamageSource laser(Level level, @Nullable Entity owner) {
        return new DamageSource(type(level, ModDamageType.LASER), owner, owner);
    }

    public static DamageSource familiar(Level level, Entity direct, @Nullable Entity owner) {
        return new DamageSource(type(level, ModDamageType.FAMILIAR), direct, owner);
    }

    public static DamageSource water(Level level, @Nullable Entity owner) {
        return new DamageSource(type(level, ModDamageType.WATER), null, owner);
    }

    private static Holder<DamageType> type(Level level, ResourceKey<DamageType> key) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    static final class TearDamageSource extends DamageSource {
        // Vec3 is immutable, so retaining this value cannot follow later bullet changes.
        final Vec3 velocity;

        private TearDamageSource(Holder<DamageType> type, @Nullable Entity owner, Vec3 velocity) {
            super(type, null, owner);
            this.velocity = velocity;
        }
    }
}
