package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** One projectile's attachment memory; definitions and effect instances never own this state. */
public final class BulletStickState {
    public enum Phase { FLIGHT, STUCK, DETACHED }
    private Phase phase = Phase.FLIGHT;
    private int durationTicks, elapsedTicks, revision;
    private int targetEntityId = -1;
    private UUID targetUuid;
    private Vec3 offset = Vec3.ZERO;
    private LivingEntity target;
    private Level attachmentLevel;
    private boolean ended;
    private Vec3 flightVelocity = Vec3.ZERO;
    private Vec3 contactPosition = Vec3.ZERO;

    BulletStickState(int durationTicks) {
        this.durationTicks = Math.max(0, durationTicks);
    }

    void begin(LivingEntity target, Vec3 contact, Vec3 velocity) {
        flightVelocity = velocity;
        contactPosition = contact;
        revision++;
        this.target = target;
        this.attachmentLevel = target.level();
        targetEntityId = target.getId();
        targetUuid = target.getUUID();
        offset = contact.subtract(target.position());
        phase = Phase.STUCK;
        elapsedTicks = 0;
        ended = false;
    }

    boolean end(boolean detach) {
        if (phase != Phase.STUCK || ended) return false;
        ended = true;
        revision++;
        if (detach) phase = Phase.DETACHED;
        return true;
    }

    void releaseTarget() { target = null; attachmentLevel = null; }
    @Nullable Level attachmentLevel() { return attachmentLevel; }
    int advanceElapsed() { return ++elapsedTicks; }
    Vec3 flightVelocity() { return flightVelocity; }
    Vec3 contactPosition() { return contactPosition; }

    public Phase phase() { return phase; }
    public int durationTicks() { return durationTicks; }
    public int elapsedTicks() { return elapsedTicks; }
    public int revision() { return revision; }
    public int targetEntityId() { return targetEntityId; }
    public @Nullable UUID targetUuid() { return targetUuid; }
    public Vec3 offset() { return offset; }
    public @Nullable LivingEntity target() { return target; }
    public Snapshot snapshot() {
        return new Snapshot(phase, durationTicks, elapsedTicks, revision, targetEntityId, targetUuid, offset);
    }

    void restore(Snapshot snapshot) {
        phase = snapshot.phase();
        durationTicks = snapshot.durationTicks();
        elapsedTicks = snapshot.elapsedTicks();
        revision = snapshot.revision();
        targetEntityId = snapshot.targetEntityId();
        targetUuid = snapshot.targetUuid();
        offset = snapshot.offset();
        target = null;
        ended = phase != Phase.STUCK;
    }

    /** A complete phase snapshot for late observers; entity identity checks include UUID. */
    public record Snapshot(Phase phase, int durationTicks, int elapsedTicks, int revision,
                           int targetEntityId, @Nullable UUID targetUuid, Vec3 offset) {
        public Snapshot {
            Objects.requireNonNull(phase, "phase");
            Objects.requireNonNull(offset, "offset");
            durationTicks = Math.max(0, durationTicks);
            elapsedTicks = Math.max(0, elapsedTicks);
            revision = Math.max(0, revision);
            if (!Double.isFinite(offset.x) || !Double.isFinite(offset.y) || !Double.isFinite(offset.z))
                offset = Vec3.ZERO;
        }
        public static Snapshot flight() { return new Snapshot(Phase.FLIGHT, 0, 0, 0, -1, null, Vec3.ZERO); }
        public void write(FriendlyByteBuf buf) {
            buf.writeEnum(phase);
            buf.writeVarInt(durationTicks);
            buf.writeVarInt(elapsedTicks);
            buf.writeVarInt(revision);
            buf.writeInt(targetEntityId);
            buf.writeBoolean(targetUuid != null);
            if (targetUuid != null) buf.writeUUID(targetUuid);
            buf.writeDouble(offset.x); buf.writeDouble(offset.y); buf.writeDouble(offset.z);
        }
        public static Snapshot read(FriendlyByteBuf buf) {
            Phase phase = buf.readEnum(Phase.class);
            int duration = buf.readVarInt(), elapsed = buf.readVarInt(), revision = buf.readVarInt();
            int targetId = buf.readInt();
            UUID uuid = buf.readBoolean() ? buf.readUUID() : null;
            return new Snapshot(phase, duration, elapsed, revision, targetId, uuid,
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
    }
}
