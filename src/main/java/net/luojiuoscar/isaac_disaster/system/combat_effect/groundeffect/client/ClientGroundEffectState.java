package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectCoverage;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/** Client-side copy of one transient ground effect. */
public final class ClientGroundEffectState {
    private final int slot;
    private final int generation;
    private final Vec3 position;
    private final Vec3 previousRenderPosition;
    private final double movementStartTick;
    private final double radius;
    private final int lifetime;
    private final long creationOrder;
    @Nullable
    private final UUID ownerUuid;
    private final ResourceLocation typeId;
    private final int color;
    private final long shapeSeed;
    private final int revision;
    private final GroundEffectCoverage coverage;
    private final List<GroundEffectCoverage.Point> triangleVertices;
    private int age;
    private final int snapshotAge;
    private long snapshotTick;
    private final int animationDurationTicks;
    private final double animationStartTick;

    ClientGroundEffectState(GroundEffectSnapshotBatchS2CPacket.Entry entry, double localTick,
                            @Nullable ClientGroundEffectState previous) {
        this.animationDurationTicks = previous == null ? entry.animationDurationTicks() : previous.animationDurationTicks;
        this.animationStartTick = previous == null ? localTick - entry.animationElapsedTicks() : previous.animationStartTick;
        this.slot = entry.slot();
        this.generation = entry.generation();
        this.position = entry.position();
        this.previousRenderPosition = previous == null ? position : previous.renderPosition(localTick);
        this.movementStartTick = localTick;
        this.radius = entry.radius();
        this.age = Math.max(0, entry.age());
        this.snapshotAge = this.age;
        this.lifetime = Math.max(0, entry.lifetime());
        this.creationOrder = entry.creationOrder();
        this.ownerUuid = entry.ownerUuid();
        this.typeId = entry.typeId();
        this.color = entry.color();
        this.shapeSeed = entry.shapeSeed();
        this.revision = entry.revision();
        if (previous != null && previous.radius == radius && previous.shapeSeed == shapeSeed) {
            this.coverage = previous.coverage;
            this.triangleVertices = previous.triangleVertices;
            return;
        }
        this.coverage = GroundEffectCoverage.puddle(radius, shapeSeed);
        List<GroundEffectCoverage.Point> triangles = new ArrayList<>();
        for (GroundEffectCoverage.Polygon polygon : coverage.pieces()) {
            for (int i = 1; i < polygon.vertices().size() - 1; i++) {
                triangles.add(polygon.vertices().get(0));
                triangles.add(polygon.vertices().get(i));
                triangles.add(polygon.vertices().get(i + 1));
            }
        }
        this.triangleVertices = List.copyOf(triangles);
    }

    public long identity() { return ((long) generation << 32) | (slot & 0xFFFFFFFFL); }
    public int slot() { return slot; }
    public int generation() { return generation; }
    public Vec3 position() { return position; }
    public Vec3 renderPosition(double localTick) {
        double progress = Math.max(0, Math.min(1, (localTick - movementStartTick) / 2.0D));
        return previousRenderPosition.lerp(position, progress);
    }
    public double radius() { return radius; }
    public int lifetime() { return lifetime; }
    public long creationOrder() { return creationOrder; }
    @Nullable public UUID ownerUuid() { return ownerUuid; }
    public ResourceLocation typeId() { return typeId; }
    public int getColor() { return color; }
    public long shapeSeed() { return shapeSeed; }
    public int revision() { return revision; }
    public GroundEffectCoverage coverage() { return coverage; }
    public List<GroundEffectCoverage.Point> triangleVertices() { return triangleVertices; }
    public double animationScale(double localTick) {
        return animationDurationTicks == 0 ? 1.0D : Math.min(1.0D,
                Math.max(0.0D, localTick - animationStartTick) / animationDurationTicks);
    }
    public int age() { return age; }
    public boolean isExpired() { return age >= lifetime; }
    public void alignTime(long tick, double estimatedTick) {
        snapshotTick = tick;
        updateAge(estimatedTick);
    }
    public void updateAge(double estimatedTick) {
        double predicted = snapshotAge + Math.max(0.0D, estimatedTick - snapshotTick);
        // The server's despawn is authoritative; retain a visible last frame if prediction runs ahead.
        age = (int) Math.min(Math.max(0, lifetime - 1), predicted);
    }
}
