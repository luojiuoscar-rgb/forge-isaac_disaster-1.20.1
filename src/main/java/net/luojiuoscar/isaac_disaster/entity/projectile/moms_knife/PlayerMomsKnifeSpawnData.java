package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

/** Initial render state is applied atomically instead of interpolating from a default heading. */
record PlayerMomsKnifeSpawnData(byte mode, int owner, Vec3 direction, float range, float speed,
                               long launchedAt, float yaw, float pitch, int heldSlot, Vec3 localOffset, Vec3 baseDirection,
                               ResourceLocation heldSource, Vec3 trajectoryOffset, Vec3 trajectoryDirection,
                               String returningTrajectory, float flightDuration, List<PlayerMomsKnifeTrail.Sample> trail,
                               boolean fastMoving) {
    PlayerMomsKnifeSpawnData {
        trail = List.copyOf(trail);
        if (trail.size() > PlayerMomsKnifeTrail.MAX_SAMPLES) throw new IllegalArgumentException("Oversized knife trail");
        double previous = Double.NEGATIVE_INFINITY;
        for (var sample : trail) {
            if (!Double.isFinite(sample.time()) || sample.time() <= previous
                    || !Double.isFinite(sample.blade().anchor().lengthSqr())
                    || !Double.isFinite(sample.blade().tip().lengthSqr())) throw new IllegalArgumentException("Invalid knife trail");
            previous = sample.time();
        }
        direction = PlayerMomsKnifeMotion.unit(direction);
        baseDirection = PlayerMomsKnifeMotion.unit(baseDirection);
        flightDuration = Float.isFinite(flightDuration) ? Math.max(0, flightDuration) : 0;
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeByte(mode);
        buffer.writeVarInt(owner);
        buffer.writeDouble(direction.x);
        buffer.writeDouble(direction.y);
        buffer.writeDouble(direction.z);
        buffer.writeFloat(range);
        buffer.writeFloat(speed);
        buffer.writeLong(launchedAt);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeVarInt(heldSlot);
        buffer.writeResourceLocation(heldSource);
        buffer.writeDouble(localOffset.x);
        buffer.writeDouble(localOffset.y);
        buffer.writeDouble(localOffset.z);
        buffer.writeDouble(baseDirection.x);
        buffer.writeDouble(baseDirection.y);
        buffer.writeDouble(baseDirection.z);
        buffer.writeDouble(trajectoryOffset.x);
        buffer.writeDouble(trajectoryOffset.y);
        buffer.writeDouble(trajectoryOffset.z);
        buffer.writeDouble(trajectoryDirection.x);
        buffer.writeDouble(trajectoryDirection.y);
        buffer.writeDouble(trajectoryDirection.z);
        buffer.writeUtf(returningTrajectory);
        buffer.writeFloat(flightDuration);
        buffer.writeBoolean(fastMoving);
        buffer.writeVarInt(trail.size());
        for (var sample : trail) {
            buffer.writeDouble(sample.time());
            buffer.writeBoolean(sample.connected());
            Vec3 anchor = sample.blade().anchor(), facing = sample.blade().tip().subtract(sample.blade().base()).normalize();
            buffer.writeDouble(anchor.x); buffer.writeDouble(anchor.y); buffer.writeDouble(anchor.z);
            buffer.writeDouble(facing.x); buffer.writeDouble(facing.y); buffer.writeDouble(facing.z);
        }
    }

    static PlayerMomsKnifeSpawnData read(FriendlyByteBuf buffer) {
        byte mode = buffer.readByte();
        int owner = buffer.readVarInt();
        Vec3 direction = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        float range = buffer.readFloat();
        float speed = buffer.readFloat();
        long launchedAt = buffer.readLong();
        float yaw = buffer.readFloat();
        float pitch = buffer.readFloat();
        int slot = buffer.readVarInt();
        ResourceLocation source = buffer.readResourceLocation();
        Vec3 offset = readVector(buffer), base = readVector(buffer), trajectoryOffset = readVector(buffer), trajectoryDirection = readVector(buffer);
        String trajectory = buffer.readUtf();
        float duration = buffer.readFloat();
        boolean fast = buffer.readBoolean();
        int size = buffer.readVarInt();
        if (size < 0 || size > PlayerMomsKnifeTrail.MAX_SAMPLES) throw new IllegalArgumentException("Invalid knife trail size");
        List<PlayerMomsKnifeTrail.Sample> trail = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double time = buffer.readDouble();
            boolean connected = buffer.readBoolean();
            Vec3 anchor = readVector(buffer), facing = readVector(buffer);
            if (!Double.isFinite(facing.lengthSqr()) || facing.lengthSqr() < 1e-12) throw new IllegalArgumentException("Invalid trail direction");
            trail.add(new PlayerMomsKnifeTrail.Sample(time, PlayerMomsKnifeMotion.blade(anchor, facing), connected));
        }
        return new PlayerMomsKnifeSpawnData(mode, owner, direction, range, speed, launchedAt, yaw, pitch, slot,
                offset, base, source, trajectoryOffset, trajectoryDirection, trajectory, duration, trail, fast);
    }

    private static Vec3 readVector(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
