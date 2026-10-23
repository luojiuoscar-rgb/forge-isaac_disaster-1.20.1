package net.luojiuoscar.isaac_disaster.registries.ground_effect;

import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/** Immutable Forge registry definition; mutable puddles are created separately for each spawn. */
public final class GroundEffectType<T extends AbstractGroundEffect> {
    private final ResourceLocation id;
    private final Factory<T> factory;

    public GroundEffectType(ResourceLocation id, Factory<T> factory) {
        this.id = Objects.requireNonNull(id, "id");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public ResourceLocation id() { return id; }

    public T create(int slot, int generation, @Nullable UUID owner, Vec3 position,
                    double radius, int lifetime, long creationOrder) {
        Objects.requireNonNull(position, "position");
        double safeRadius = Double.isFinite(radius) ? Math.max(0.0D, radius) : 0.0D;
        int safeLifetime = Math.max(0, lifetime);
        T effect = Objects.requireNonNull(factory.create(this, slot, generation, owner, position,
                safeRadius, safeLifetime, creationOrder), "Ground-effect factory returned null");
        if (effect.getType() != this) throw new IllegalStateException("Ground-effect factory returned a different type: " + id);
        if (effect.slot() != slot || effect.generation() != generation
                || !Objects.equals(effect.ownerUuid(), owner) || !position.equals(effect.position())
                || effect.radius() != safeRadius || effect.lifetime() != safeLifetime
                || effect.creationOrder() != creationOrder) {
            throw new IllegalStateException("Ground-effect factory changed requested spawn parameters: " + id);
        }
        return effect;
    }

    /** Creates a fresh instance preserving the supplied type, identity, owner and normalized spawn parameters. */
    @FunctionalInterface
    public interface Factory<T extends AbstractGroundEffect> {
        T create(GroundEffectType<T> type, int slot, int generation, @Nullable UUID owner,
                 Vec3 position, double radius, int lifetime, long creationOrder);
    }
}
