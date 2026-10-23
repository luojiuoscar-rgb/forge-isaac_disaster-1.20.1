package net.luojiuoscar.isaac_disaster.groundeffect.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/** Client-side copy of one transient ground effect. */
public final class ClientGroundEffectState {
    private final int slot;
    private final int generation;
    private final Vec3 position;
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

    public ClientGroundEffectState(int slot, int generation, Vec3 position, double radius, int age,
                                   int lifetime, long creationOrder, @Nullable UUID ownerUuid,
                                   ResourceLocation typeId, int color, long shapeSeed, int revision,
                                   GroundEffectCoverage coverage) {
        this.slot = slot;
        this.generation = generation;
        this.position = position;
        this.radius = radius;
        this.age = Math.max(0, age);
        this.snapshotAge = this.age;
        this.lifetime = Math.max(0, lifetime);
        this.creationOrder = creationOrder;
        this.ownerUuid = ownerUuid;
        this.typeId = typeId;
        this.color = color;
        this.shapeSeed = shapeSeed;
        this.revision = revision;
        this.coverage = coverage;
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
