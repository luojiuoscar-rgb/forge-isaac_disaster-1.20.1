package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
    private final float damage;
    private long creationOrder;
    private final long shapeSeed;
    private final GroundEffectCoverage footprint;
    private int revision;
    private int age;
    private final int animationDurationTicks;
    private int animationElapsedTicks;
    private boolean settled;

    protected AbstractGroundEffect(GroundEffectType<?> type, int slot, int generation, @Nullable UUID ownerUuid, Vec3 position,
                                   double radius, int lifetime, float damage, long creationOrder, int animationDurationTicks) {
        this.animationDurationTicks = Math.max(0, animationDurationTicks);
        this.settled = this.animationDurationTicks == 0;
        this.type = type;
        this.slot = slot;
        this.generation = generation;
        this.ownerUuid = ownerUuid;
        this.position = position == null ? Vec3.ZERO : position;
        this.radius = Double.isFinite(radius) ? Math.max(0.0D, radius) : 0.0D;
        this.lifetime = Math.max(0, lifetime);
        this.damage = Float.isFinite(damage) ? Math.max(0.0F, damage) : 0.0F;
        this.creationOrder = creationOrder;
        this.shapeSeed = creationOrder;
        this.footprint = GroundEffectCoverage.puddle(this.radius, shapeSeed);
    }

    public final GroundEffectType<?> getType() { return type; }

    @Nullable public final ResourceLocation typeId() { return type == null ? null : type.id(); }

    /** Returns this instance's base RGB tint as {@code 0xRRGGBB}, before client brightness animation. */
    public abstract int getColor();

    public void onContactDamage(GroundEffectContact contact) { }

    public void onContactEffect(GroundEffectContact contact) { }

    public boolean affectsAllLivingEntities() { return false; }

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

    public final float damage() { return damage; }

    public final long creationOrder() {
        return creationOrder;
    }

    public final int animationDurationTicks() { return animationDurationTicks; }
    public final int animationElapsedTicks() { return animationElapsedTicks; }
    public final double animationScale() { return animationDurationTicks == 0 ? 1.0D : Math.min(1.0D, (double) animationElapsedTicks / animationDurationTicks); }
    public final double currentRadius() { return radius * animationScale(); }
    public final boolean isSettled() { return settled; }
    public final boolean isAnimationComplete() { return animationElapsedTicks >= animationDurationTicks; }

    public final int age() {
        return age;
    }

    public final long shapeSeed() { return shapeSeed; }
    public final int revision() { return revision; }
    public final GroundEffectCoverage footprint() { return footprint; }
    public final GroundEffectCoverage coverage() { return footprint; }

    protected final void markDirty() { revision++; }

    public final void repaint(long order) {
        age = 0;
        if (settled) {
            creationOrder = order;
        }
        revision++;
    }

    public final void settle(long order) {
        settled = true;
        creationOrder = order;
        revision++;
    }

    public final boolean tick() {
        if (age < lifetime) age++;
        if (!isAnimationComplete()) animationElapsedTicks++;
        return !isExpired();
    }

    public final boolean isExpired() {
        return age >= lifetime;
    }

    public final boolean contains(Vec3 point) {
        double dx = point.x - position.x;
        double dz = point.z - position.z;
        double scale = animationScale();
        double currentRadius = radius * scale;
        return scale > 0 && dx * dx + dz * dz <= currentRadius * currentRadius
                && footprint.contains(dx / scale, dz / scale);
    }
}
