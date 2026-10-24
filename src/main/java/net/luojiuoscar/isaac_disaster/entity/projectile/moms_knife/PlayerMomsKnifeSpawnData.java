package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Initial render state is applied atomically instead of interpolating from a default heading. */
record PlayerMomsKnifeSpawnData(byte mode, int owner, Vec3 direction, float range, float speed,
                               long launchedAt, float yaw, float pitch) {
    PlayerMomsKnifeSpawnData {
        direction = PlayerMomsKnifeMotion.unit(direction);
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
    }

    static PlayerMomsKnifeSpawnData read(FriendlyByteBuf buffer) {
        return new PlayerMomsKnifeSpawnData(buffer.readByte(), buffer.readVarInt(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(), buffer.readFloat(), buffer.readLong(), buffer.readFloat(), buffer.readFloat());
    }
}
