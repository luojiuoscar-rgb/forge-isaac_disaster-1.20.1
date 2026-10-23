package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.Objects;

/** Common transient state for a server-owned ground effect instance. */
public abstract class AbstractGroundEffect {
    private final GroundEffectType<?> type;
    private final int slot;
    private final int generation;
    @Nullable
    private final UUID ownerUuid;
    private final Vec3 position;
    private final double radius;
    private final int lifetime;
    private long creationOrder;
    private final long shapeSeed;
    private final GroundEffectCoverage footprint;
    private GroundEffectCoverage coverage;
    private int revision;
    private int age;

    protected AbstractGroundEffect(GroundEffectType<?> type, int slot, int generation, @Nullable UUID ownerUuid, Vec3 position,
                                   double radius, int lifetime, long creationOrder) {
        this.type = Objects.requireNonNull(type, "type");
        this.slot = slot;
        this.generation = generation;
        this.ownerUuid = ownerUuid;
        this.position = position;
        this.radius = Math.max(0.0D, radius);
        this.lifetime = Math.max(0, lifetime);
        this.creationOrder = creationOrder;
        this.shapeSeed = creationOrder;
        this.footprint = GroundEffectCoverage.puddle(this.radius, shapeSeed);
        this.coverage = footprint;
    }

    public final GroundEffectType<?> getType() { return type; }

    public final ResourceLocation typeId() { return type.id(); }

    /** Returns this instance's base RGB tint as {@code 0xRRGGBB}, before client brightness animation. */
    public abstract int getColor();

    public abstract void onContactEffect(GroundEffectContact contact);

    public final int slot() {
        return slot;
    }

    public final int generation() {
        return generation;
    }

    public final long identity() {
        return ((long) generation << 32) | (slot & 0xFFFFFFFFL);
    }

    @Nullable
    public final UUID ownerUuid() {
        return ownerUuid;
    }

    public final Vec3 position() {
        return position;
    }

    public final double radius() {
        return radius;
    }

    public final int lifetime() {
        return lifetime;
    }

    public final long creationOrder() {
        return creationOrder;
    }

    public final int age() {
        return age;
    }

    public final long shapeSeed() { return shapeSeed; }
    public final int revision() { return revision; }
    public final GroundEffectCoverage footprint() { return footprint; }
    public final GroundEffectCoverage coverage() { return coverage; }

    public final void setCoverage(GroundEffectCoverage value) {
        if (coverage.equals(value)) return;
        coverage = value;
        revision++;
    }

    public final void repaint(long order) {
        age = 0;
        creationOrder = order;
        coverage = footprint;
        revision++;
    }

    public final boolean tick() {
        if (age < lifetime) age++;
        return !isExpired();
    }

    public final boolean isExpired() {
        return age >= lifetime;
    }

    public final boolean contains(Vec3 point) {
        double dx = point.x - position.x;
        double dz = point.z - position.z;
        return dx * dx + dz * dz <= radius * radius && coverage.contains(dx, dz);
    }
}
