package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Adds an explosion impulse to the receiving player's independently simulated movement. */
public record ExplosionKnockbackS2CPacket(Vec3 impulse) {
    public ExplosionKnockbackS2CPacket(FriendlyByteBuf buffer) {
        this(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(impulse.x);
        buffer.writeDouble(impulse.y);
        buffer.writeDouble(impulse.z);
    }

    public Vec3 applyTo(Vec3 currentVelocity) {
        return currentVelocity.add(impulse);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleExplosionKnockback(this)));
        context.setPacketHandled(true);
        return true;
    }
}
