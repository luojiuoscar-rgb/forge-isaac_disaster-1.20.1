package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.ClearPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.RemovePassiveItemFromIdC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.DirectObtainPassiveItemC2SPacket;
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

        // register ObtainPassiveItemC2SPacket
        net.messageBuilder(ObtainPassiveItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ObtainPassiveItemC2SPacket::new)
                .encoder(ObtainPassiveItemC2SPacket::toBytes)
                .consumerNetworkThread(ObtainPassiveItemC2SPacket::handle)
                .add();

        // register DirectObtainPassiveItemC2SPacket
        net.messageBuilder(DirectObtainPassiveItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DirectObtainPassiveItemC2SPacket::new)
                .encoder(DirectObtainPassiveItemC2SPacket::toBytes)
                .consumerNetworkThread(DirectObtainPassiveItemC2SPacket::handle)
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

    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sentToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}
