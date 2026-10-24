package net.luojiuoscar.isaac_disaster.registries.ground_effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/** Forge factory definition; invalid addon output is rejected before paint is changed. */
public final class GroundEffectType<T extends AbstractGroundEffect> {
    private final Factory<T> factory;
    private boolean reportedFactoryFailure;
    private boolean reportedMissingId;

    public GroundEffectType(@Nullable Factory<T> factory) { this.factory = factory; }

    @Nullable
    public ResourceLocation id() {
        var registry = RegistryManager.ACTIVE.getRegistry(ModGroundEffects.GROUND_EFFECT_KEY);
        ResourceLocation id = registry == null ? null : registry.getKey(this);
        if (id == null && !reportedMissingId) {
            reportedMissingId = true;
            IsaacDisaster.LOGGER.warn("Skipping synchronization of an unregistered ground-effect type");
        }
        return id;
    }

    @Nullable
    public T create(int slot, int generation, @Nullable UUID owner, Vec3 position,
                    double radius, int lifetime, float damage, long creationOrder) {
        return create(slot, generation, owner, position, radius, lifetime, damage, creationOrder, 0);
    }

    @Nullable
    public T create(int slot, int generation, @Nullable UUID owner, Vec3 position,
                    double radius, int lifetime, float damage, long creationOrder, int animationDurationTicks) {
        if (factory == null || position == null || !Double.isFinite(position.x)
                || !Double.isFinite(position.y) || !Double.isFinite(position.z)) {
            reject("Missing factory or invalid position", null);
            return null;
        }
        double safeRadius = Double.isFinite(radius) ? Math.max(0.0D, radius) : 0.0D;
        int safeLifetime = Math.max(0, lifetime);
        float safeDamage = Float.isFinite(damage) ? Math.max(0.0F, damage) : 0.0F;
        int safeAnimation = Math.max(0, animationDurationTicks);
        try {
            T effect = factory.create(this, slot, generation, owner, position,
                    safeRadius, safeLifetime, safeDamage, creationOrder, safeAnimation);
            if (effect == null || effect.getType() != this || effect.slot() != slot || effect.generation() != generation
                    || !Objects.equals(effect.ownerUuid(), owner) || !position.equals(effect.position())
                    || effect.radius() != safeRadius || effect.lifetime() != safeLifetime || effect.damage() != safeDamage
                    || effect.creationOrder() != creationOrder || effect.animationDurationTicks() != safeAnimation
                    || effect.age() != 0 || effect.animationElapsedTicks() != 0) {
                reject("Factory changed the requested type, identity or spawn parameters", null);
                return null;
            }
            return effect;
        } catch (RuntimeException failure) {
            reject("Factory failed", failure);
            return null;
        }
    }

    private void reject(String reason, @Nullable RuntimeException failure) {
        if (reportedFactoryFailure) return;
        reportedFactoryFailure = true;
        IsaacDisaster.LOGGER.warn("Ground-effect creation rejected: {}", reason, failure);
    }

    @FunctionalInterface
    public interface Factory<T extends AbstractGroundEffect> {
        T create(GroundEffectType<T> type, int slot, int generation, @Nullable UUID owner,
                 Vec3 position, double radius, int lifetime, float damage, long creationOrder, int animationDurationTicks);
    }
}
