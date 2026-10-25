package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Initial render state is applied atomically instead of interpolating from a default heading. */
record PlayerMomsKnifeSpawnData(byte mode, int owner, Vec3 direction, float range, float speed,
                               long launchedAt, float yaw, float pitch, int heldSlot, Vec3 localOffset, Vec3 baseDirection) {
    PlayerMomsKnifeSpawnData(byte mode, int owner, Vec3 direction, float range, float speed,
                            long launchedAt, float yaw, float pitch) {
        this(mode, owner, direction, range, speed, launchedAt, yaw, pitch, 0, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
    }

    PlayerMomsKnifeSpawnData {
        direction = PlayerMomsKnifeMotion.unit(direction);
        baseDirection = PlayerMomsKnifeMotion.unit(baseDirection);
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
        buffer.writeDouble(localOffset.x);
        buffer.writeDouble(localOffset.y);
        buffer.writeDouble(localOffset.z);
        buffer.writeDouble(baseDirection.x);
        buffer.writeDouble(baseDirection.y);
        buffer.writeDouble(baseDirection.z);
    }

    static PlayerMomsKnifeSpawnData read(FriendlyByteBuf buffer) {
        return new PlayerMomsKnifeSpawnData(buffer.readByte(), buffer.readVarInt(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(), buffer.readFloat(), buffer.readLong(), buffer.readFloat(), buffer.readFloat(),
                buffer.readVarInt(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }
}
