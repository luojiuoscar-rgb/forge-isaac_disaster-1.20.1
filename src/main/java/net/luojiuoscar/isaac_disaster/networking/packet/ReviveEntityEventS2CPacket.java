package net.luojiuoscar.isaac_disaster.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ReviveEntityEventS2CPacket {
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final SoundEvent sound;
    private final ParticleOptions particle;
    private final ItemStack displayItem;

    public ReviveEntityEventS2CPacket(Entity entity, SoundEvent sound,
                                      ParticleOptions particle, ItemStack displayItem) {
        this(entity.getId(), entity.getX(), entity.getY(), entity.getZ(), sound, particle, displayItem);
    }

    public ReviveEntityEventS2CPacket(int entityId, double x, double y, double z,
                                      SoundEvent sound, ParticleOptions particle, ItemStack displayItem) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.sound = sound;
        this.particle = particle;
        this.displayItem = displayItem.copy();
    }

    public ReviveEntityEventS2CPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readVarInt();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        ResourceLocation soundId = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
        this.sound = soundId == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(soundId);

        ResourceLocation particleId = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
        if (particleId == null) {
            this.particle = null;
        } else {
            ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);
            this.particle = particleType == null
                    ? null
                    : readParticle(particleType, buffer);
        }
        this.displayItem = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeNullable(sound == null ? null : ForgeRegistries.SOUND_EVENTS.getKey(sound),
                FriendlyByteBuf::writeResourceLocation);
        buffer.writeNullable(particle == null ? null : ForgeRegistries.PARTICLE_TYPES.getKey(particle.getType()),
                FriendlyByteBuf::writeResourceLocation);
        if (particle != null) {
            particle.writeToNetwork(buffer);
        }
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

            if (particle != null) {
                minecraft.particleEngine.createTrackingEmitter(entity, particle, 30);
            }
            if (sound != null) {
                minecraft.level.playLocalSound(x, y, z,
                        sound, entity.getSoundSource(), 1.0F, 1.0F, false);
            }

            if (minecraft.player == entity) {
                minecraft.gameRenderer.displayItemActivation(displayItem);
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T extends ParticleOptions> T readParticle(ParticleType<T> particleType,
                                                               FriendlyByteBuf buffer) {
        return particleType.getDeserializer().fromNetwork(particleType, buffer);
    }
}
