package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Spider familiar lifecycle, contact effects, animation synchronization and client presentation. */
public abstract class AbstractSpiderFamiliarEntity extends AbstractIsaacFamiliarEntity {
    private static final EntityDataAccessor<Byte> MOVE_ANIMATION = SynchedEntityData.defineId(
            AbstractSpiderFamiliarEntity.class, EntityDataSerializers.BYTE);
    private final SpiderMotionController movement;
    private boolean consumed;
    private final ClientInterpolation interpolation = new ClientInterpolation();
    private float gaitTime, renderYaw, previousRenderYaw;
    private boolean renderYawInitialized;

    protected AbstractSpiderFamiliarEntity(EntityType<? extends AbstractSpiderFamiliarEntity> type, Level level) {
        super(type, level);
        configurePhysics();
        setMaxUpStep(0);
        movement = new SpiderMotionController(this, random);
    }

    /** Body RGB tint; legs and fangs retain their texture colors. */
    public abstract int getBodyColor();

    protected abstract float getContactDamage(ServerPlayer owner, LivingEntity target);
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 position,
                                float damage, boolean accepted) { }

    @Override protected void configurePhysics() { noPhysics = false; setNoGravity(false); }
    @Override public boolean canCollideWith(Entity entity) { return false; }
    @Override protected Vec3 ownerPlacement(Player owner) { return SpiderTerrain.nearOwner(this, owner); }
    @Override protected MovementEmission getMovementEmission() { return MovementEmission.NONE; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance <= 32 * 32; }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MOVE_ANIMATION, (byte) SpiderMovement.Gait.IDLE.ordinal());
    }

    public final SpiderMovement.Gait getMovementAnimation() {
        return SpiderMovement.Gait.fromId(entityData.get(MOVE_ANIMATION));
    }

    final void setMovementAnimation(SpiderMovement.Gait gait) {
        entityData.set(MOVE_ANIMATION, (byte) gait.ordinal());
    }

    final void updateMovementAnimation(Vec3 actualMovement) {
        setMovementAnimation(SpiderMovement.animation(actualMovement, getYRot()));
    }

    public final float getGaitAnimationTime(float partialTick) {
        return gaitTime + SpiderMovement.gaitTimeStep(getMovementAnimation(), partialTick);
    }

    public final float getRenderYaw(float partialTick) {
        return renderYawInitialized ? Mth.lerp(partialTick, previousRenderYaw, renderYaw) : getYRot();
    }

    @Override protected void onRecalled() { movement.recalled(); }

    @Override protected final void tickFamiliar() {
        if (!level().isClientSide) {
            if (consumed || isRemoved()) return;
            ServerPlayer owner = getOwner() instanceof ServerPlayer player && player.isAlive() ? player : null;
            movement.tick(owner);
            return;
        }
        if (!renderYawInitialized) {
            renderYaw = previousRenderYaw = getYRot();
            renderYawInitialized = true;
        } else {
            previousRenderYaw = renderYaw;
            renderYaw += Mth.wrapDegrees(getYRot() - renderYaw);
        }
        gaitTime += SpiderMovement.gaitTimeStep(getMovementAnimation(), 1);
        interpolate();
    }

    /** Contact consumes ownership before hurt callbacks, exactly like the fly familiar. */
    final boolean tryContact(ServerPlayer owner, Vec3 from, Vec3 to) {
        if (owner == null || consumed || isRemoved()) return false;
        SpiderOwnerFrame frame = SpiderOwnerFrame.get(owner);
        double halfWidth = getBbWidth() * 0.5;
        double halfHeight = getBbHeight() * 0.5;
        AABB bounds = new AABB(from, to).inflate(halfWidth, 0, halfWidth).expandTowards(0, getBbHeight(), 0);
        LivingEntity first = null;
        double parameter = Double.POSITIVE_INFINITY;
        for (LivingEntity candidate : frame.contactCandidates(bounds)) {
            if (!frame.validForTick(owner, candidate)) continue;
            var hit = SweptCollision.segmentAabb(from.add(0, halfHeight, 0), to.add(0, halfHeight, 0),
                    candidate.getBoundingBox().inflate(halfWidth, halfHeight, halfWidth));
            if (!hit.hit() || hit.parameter() >= parameter) continue;
            if (!SpiderTerrain.clearSweep(this, from, from.lerp(to, hit.parameter()))) continue;
            // Qualification may have changed since this tick's shared decision.
            if (!SpiderOwnerFrame.validTarget(owner, candidate)) continue;
            first = candidate;
            parameter = hit.parameter();
        }
        if (first == null) return false;
        var type = getFamiliarType();
        var data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        if (type == null || data == null || !data.containsEntity(type, getUUID())) return false;
        Vec3 hitPosition = from.lerp(to, parameter);
        setPos(hitPosition);
        consumed = true;
        float damage = getContactDamage(owner, first);
        if (!Float.isFinite(damage) || damage < 0) damage = 0;
        boolean accepted;
        try {
            data.removeEntity(type, getUUID());
            data.addCount(type, -1);
            accepted = first.hurt(ModDamageSources.familiar(level(), this, owner), damage);
        } finally {
            discardSilently();
        }
        onContactHit(owner, first, hitPosition.add(0, halfHeight, 0), damage, accepted);
        return true;
    }

    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (!level().isClientSide || interpolation.accept(position(), x, y, z, steps, teleport)) {
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            renderYaw = previousRenderYaw = getYRot();
            renderYawInitialized = true;
            return;
        }
        // Keep the logical yaw authoritative for later position-only packets.
        // Visual turning interpolates separately between client ticks in getRenderYaw.
        setYRot(getYRot() + Mth.wrapDegrees(yaw - getYRot()));
    }

    private void interpolate() {
        if (interpolation.tick()) setPos(interpolation.x, interpolation.y, interpolation.z);
    }

    /** Position interpolation state used for both relative and absolute tracking packets. */
    static final class ClientInterpolation {
        private static final double SNAP_DISTANCE_SQR = 8 * 8;
        double x, y, z;
        private double targetX, targetY, targetZ;
        private int remaining;

        boolean accept(Vec3 current, double x, double y, double z, int steps, boolean absolute) {
            targetX = x; targetY = y; targetZ = z;
            // Absolute packets also announce ground-state changes. Only a genuinely large
            // correction (such as a recall) bypasses the normal interpolation window.
            if (absolute && current.distanceToSqr(x, y, z) > SNAP_DISTANCE_SQR) {
                this.x = x; this.y = y; this.z = z;
                remaining = 0;
                return true;
            }
            this.x = current.x; this.y = current.y; this.z = current.z;
            remaining = Math.max(1, steps);
            return false;
        }

        boolean tick() {
            if (remaining <= 0) return false;
            x += (targetX - x) / remaining;
            y += (targetY - y) / remaining;
            z += (targetZ - z) / remaining;
            remaining--;
            return true;
        }
    }
}
