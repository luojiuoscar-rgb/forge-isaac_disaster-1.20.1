package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;


public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id(){
        return packetId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // register ObtainPassiveItemS2CPacket
        net.messageBuilder(ObtainPassiveItemS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ObtainPassiveItemS2CPacket::new)
                .encoder(ObtainPassiveItemS2CPacket::toBytes)
                .consumerNetworkThread(ObtainPassiveItemS2CPacket::handle)
                .add();

        // register DirectObtainPassiveItemC2SPacket
        net.messageBuilder(DirectObtainPassiveItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DirectObtainPassiveItemC2SPacket::new)
                .encoder(DirectObtainPassiveItemC2SPacket::toBytes)
                .consumerNetworkThread(DirectObtainPassiveItemC2SPacket::handle)
                .add();

        // register DirectObtainPassiveItemS2CPacket
        net.messageBuilder(DirectObtainPassiveItemS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DirectObtainPassiveItemS2CPacket::new)
                .encoder(DirectObtainPassiveItemS2CPacket::toBytes)
                .consumerNetworkThread(DirectObtainPassiveItemS2CPacket::handle)
                .add();

        // register RemoveItemFromId
        net.messageBuilder(RemovePassiveItemFromIdC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RemovePassiveItemFromIdC2SPacket::new)
                .encoder(RemovePassiveItemFromIdC2SPacket::toBytes)
                .consumerNetworkThread(RemovePassiveItemFromIdC2SPacket::handle)
                .add();

        // register ClearPassiveItemC2SPacket
        net.messageBuilder(ClearPassiveItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClearPassiveItemC2SPacket::new)
                .encoder(ClearPassiveItemC2SPacket::toBytes)
                .consumerNetworkThread(ClearPassiveItemC2SPacket::handle)
                .add();

        // register UseActiveItemS2CPacket
        net.messageBuilder(UseActiveItemS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UseActiveItemS2CPacket::new)
                .encoder(UseActiveItemS2CPacket::toBytes)
                .consumerNetworkThread(UseActiveItemS2CPacket::handle)
                .add();

        // register PassiveItemSyncS2CPacket
        net.messageBuilder(PassiveItemSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PassiveItemSyncS2CPacket::new)
                .encoder(PassiveItemSyncS2CPacket::toBytes)
                .consumerNetworkThread(PassiveItemSyncS2CPacket::handle)
                .add();

        // register PickupOnUseS2CPacket
        net.messageBuilder(PickupOnUseS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PickupOnUseS2CPacket::new)
                .encoder(PickupOnUseS2CPacket::toBytes)
                .consumerNetworkThread(PickupOnUseS2CPacket::handle)
                .add();

        // register FlyUpdateS2CPacket
        net.messageBuilder(FlyUpdateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FlyUpdateS2CPacket::new)
                .encoder(FlyUpdateS2CPacket::toBytes)
                .consumerNetworkThread(FlyUpdateS2CPacket::handle)
                .add();

        // register UpdatePlayerScaleS2CPacket
        net.messageBuilder(UpdatePlayerScaleS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdatePlayerScaleS2CPacket::new)
                .encoder(UpdatePlayerScaleS2CPacket::toBytes)
                .consumerNetworkThread(UpdatePlayerScaleS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sentToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}
