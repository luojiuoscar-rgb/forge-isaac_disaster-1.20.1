package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChargeBarUpdateS2CPacket {
    public enum Action { START, CORRECT, END }
    private final ResourceLocation id;
    private final Action action;
    private final boolean visible;
    private final float progress;
    private final float rate;

    public ChargeBarUpdateS2CPacket(ResourceLocation id, Action action, boolean visible, float progress, float rate) {
        this.id = id;
        this.action = action;
        this.visible = visible;
        this.progress = progress;
        this.rate = rate;
        if (id == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar packet with no registry ID");
        }
    }

    public ChargeBarUpdateS2CPacket(FriendlyByteBuf buf){
        ResourceLocation decodedId;
        Action decodedAction;
        boolean decodedVisible;
        float decodedProgress;
        float decodedRate;
        try {
            decodedId = buf.readResourceLocation();
            decodedAction = buf.readEnum(Action.class);
            decodedVisible = buf.readBoolean();
            decodedProgress = buf.readFloat();
            decodedRate = buf.readFloat();
        } catch (RuntimeException exception) {
            IsaacDisaster.LOGGER.warn("Skipping malformed charge bar packet", exception);
            decodedId = null;
            decodedAction = Action.END;
            decodedVisible = false;
            decodedProgress = 0f;
            decodedRate = 0f;
        }
        this.id = decodedId;
        this.action = decodedAction;
        this.visible = decodedVisible;
        this.progress = decodedProgress;
        this.rate = decodedRate;
    }

    public boolean isValid() {
        return id != null && action != null && Float.isFinite(progress) && Float.isFinite(rate) && rate >= 0f;
    }

    public void toBytes(FriendlyByteBuf buf){
        if (!isValid()) return;
        buf.writeResourceLocation(id);
        buf.writeEnum(action);
        buf.writeBoolean(visible);
        buf.writeFloat(progress);
        buf.writeFloat(rate);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        if (!isValid()) {
            context.setPacketHandled(true);
            return true;
        }
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleChargeUpdate(id, action, visible, progress, rate)));
        context.setPacketHandled(true);
        return true;
    }

}
