package net.luojiuoscar.isaac_disaster.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReviveEntityEventS2CPacket {
    private final int entityId;
    private final ItemStack displayItem;

    public ReviveEntityEventS2CPacket(Entity entity, ItemStack displayItem) {
        this(entity.getId(), displayItem);
    }

    public ReviveEntityEventS2CPacket(int entityId, ItemStack displayItem) {
        this.entityId = entityId;
        this.displayItem = displayItem.copy();
    }

    public ReviveEntityEventS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.displayItem = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeItem(displayItem);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }

            Entity entity = minecraft.level.getEntity(entityId);
            if (entity == null) {
                return;
            }

            minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
            minecraft.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);

            if (minecraft.player == entity) {
                minecraft.gameRenderer.displayItemActivation(displayItem);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
