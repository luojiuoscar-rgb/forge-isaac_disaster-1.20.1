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

        // register PassiveItemModifyCountSyncS2CPacket
        net.messageBuilder(PassiveItemModifyCountSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PassiveItemModifyCountSyncS2CPacket::new)
                .encoder(PassiveItemModifyCountSyncS2CPacket::toBytes)
                .consumerNetworkThread(PassiveItemModifyCountSyncS2CPacket::handle)
                .add();

        // register PassiveItemMapSyncS2CPacket
        net.messageBuilder(PassiveItemMapSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PassiveItemMapSyncS2CPacket::new)
                .encoder(PassiveItemMapSyncS2CPacket::toBytes)
                .consumerNetworkThread(PassiveItemMapSyncS2CPacket::handle)
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

        // register RightClickUpC2SPacket
        net.messageBuilder(SetRightClickC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SetRightClickC2SPacket::new)
                .encoder(SetRightClickC2SPacket::toBytes)
                .consumerNetworkThread(SetRightClickC2SPacket::handle)
                .add();

        // register SetOnObtainS2CPacket
        net.messageBuilder(SetOnObtainS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SetOnObtainS2CPacket::new)
                .encoder(SetOnObtainS2CPacket::toBytes)
                .consumerNetworkThread(SetOnObtainS2CPacket::handle)
                .add();

        // register SetCountSyncS2CPacket
        net.messageBuilder(SetCountSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SetCountSyncS2CPacket::new)
                .encoder(SetCountSyncS2CPacket::toBytes)
                .consumerNetworkThread(SetCountSyncS2CPacket::handle)
                .add();

        // register PillRecordsSyncS2CPacket
        net.messageBuilder(PillRecordsSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PillRecordsSyncS2CPacket::new)
                .encoder(PillRecordsSyncS2CPacket::toBytes)
                .consumerNetworkThread(PillRecordsSyncS2CPacket::handle)
                .add();

        // register OpenItemScreenS2CPacket
        net.messageBuilder(OpenIsaacItemScreenS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenIsaacItemScreenS2CPacket::new)
                .encoder(OpenIsaacItemScreenS2CPacket::toBytes)
                .consumerNetworkThread(OpenIsaacItemScreenS2CPacket::handle)
                .add();

        // register OpenIsaacItemScreenC2SPacket
        net.messageBuilder(OpenIsaacItemScreenC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenIsaacItemScreenC2SPacket::new)
                .encoder(OpenIsaacItemScreenC2SPacket::toBytes)
                .consumerNetworkThread(OpenIsaacItemScreenC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sentToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}
