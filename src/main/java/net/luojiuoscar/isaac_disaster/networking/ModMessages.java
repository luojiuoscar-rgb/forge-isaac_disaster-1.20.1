package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.*;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.*;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.ClearPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.EntityVisualStateS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.FlyUpdateS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

        net.messageBuilder(ClearPassiveItemC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClearPassiveItemC2SPacket::new)
                .encoder(ClearPassiveItemC2SPacket::toBytes)
                .consumerNetworkThread(ClearPassiveItemC2SPacket::handle)
                .add();

        net.messageBuilder(IsaacItemCountMapSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(IsaacItemCountMapSyncS2CPacket::new)
                .encoder(IsaacItemCountMapSyncS2CPacket::toBytes)
                .consumerNetworkThread(IsaacItemCountMapSyncS2CPacket::handle)
                .add();

        net.messageBuilder(PassiveItemCountSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PassiveItemCountSyncS2CPacket::new)
                .encoder(PassiveItemCountSyncS2CPacket::toBytes)
                .consumerNetworkThread(PassiveItemCountSyncS2CPacket::handle)
                .add();

        net.messageBuilder(TrinketCountSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TrinketCountSyncS2CPacket::new)
                .encoder(TrinketCountSyncS2CPacket::toBytes)
                .consumerNetworkThread(TrinketCountSyncS2CPacket::handle)
                .add();

        net.messageBuilder(FlyUpdateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FlyUpdateS2CPacket::new)
                .encoder(FlyUpdateS2CPacket::toBytes)
                .consumerNetworkThread(FlyUpdateS2CPacket::handle)
                .add();

        net.messageBuilder(SetRightClickC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SetRightClickC2SPacket::new)
                .encoder(SetRightClickC2SPacket::toBytes)
                .consumerNetworkThread(SetRightClickC2SPacket::handle)
                .add();

        net.messageBuilder(SetCountSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SetCountSyncS2CPacket::new)
                .encoder(SetCountSyncS2CPacket::toBytes)
                .consumerNetworkThread(SetCountSyncS2CPacket::handle)
                .add();

        net.messageBuilder(PillRecordsSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PillRecordsSyncS2CPacket::new)
                .encoder(PillRecordsSyncS2CPacket::toBytes)
                .consumerNetworkThread(PillRecordsSyncS2CPacket::handle)
                .add();

        net.messageBuilder(OpenIsaacItemScreenS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenIsaacItemScreenS2CPacket::new)
                .encoder(OpenIsaacItemScreenS2CPacket::toBytes)
                .consumerNetworkThread(OpenIsaacItemScreenS2CPacket::handle)
                .add();

        net.messageBuilder(OpenIsaacItemScreenC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenIsaacItemScreenC2SPacket::new)
                .encoder(OpenIsaacItemScreenC2SPacket::toBytes)
                .consumerNetworkThread(OpenIsaacItemScreenC2SPacket::handle)
                .add();

        net.messageBuilder(ChargeBarUpdateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ChargeBarUpdateS2CPacket::new)
                .encoder(ChargeBarUpdateS2CPacket::toBytes)
                .consumerNetworkThread(ChargeBarUpdateS2CPacket::handle)
                .add();

        net.messageBuilder(RefreshScaleS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RefreshScaleS2CPacket::new)
                .encoder(RefreshScaleS2CPacket::toBytes)
                .consumerNetworkThread(RefreshScaleS2CPacket::handle)
                .add();

        net.messageBuilder(IsaacFlightInputC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(IsaacFlightInputC2SPacket::new)
                .encoder(IsaacFlightInputC2SPacket::toBytes)
                .consumerNetworkThread(IsaacFlightInputC2SPacket::handle)
                .add();

        net.messageBuilder(SetIsaacFlightEnabledC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SetIsaacFlightEnabledC2SPacket::new)
                .encoder(SetIsaacFlightEnabledC2SPacket::toBytes)
                .consumerNetworkThread(SetIsaacFlightEnabledC2SPacket::handle)
                .add();

        net.messageBuilder(IsaacFlightStateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(IsaacFlightStateS2CPacket::new)
                .encoder(IsaacFlightStateS2CPacket::toBytes)
                .consumerNetworkThread(IsaacFlightStateS2CPacket::handle)
                .add();

        net.messageBuilder(EntityVisualStateS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EntityVisualStateS2CPacket::new)
                .encoder(EntityVisualStateS2CPacket::toBytes)
                .consumerNetworkThread(EntityVisualStateS2CPacket::handle)
                .add();

        net.messageBuilder(RockBottomHistorySyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RockBottomHistorySyncS2CPacket::new)
                .encoder(RockBottomHistorySyncS2CPacket::toBytes)
                .consumerNetworkThread(RockBottomHistorySyncS2CPacket::handle)
                .add();

        net.messageBuilder(ReviveHudSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ReviveHudSyncS2CPacket::new)
                .encoder(ReviveHudSyncS2CPacket::toBytes)
                .consumerNetworkThread(ReviveHudSyncS2CPacket::handle)
                .add();

        net.messageBuilder(ReviveEntityEventS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ReviveEntityEventS2CPacket::new)
                .encoder(ReviveEntityEventS2CPacket::toBytes)
                .consumerNetworkThread(ReviveEntityEventS2CPacket::handle)
                .add();

        net.messageBuilder(BulletSpawnS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletSpawnS2CPacket::new).encoder(BulletSpawnS2CPacket::toBytes)
                .consumerNetworkThread(BulletSpawnS2CPacket::handle).add();

        net.messageBuilder(BulletSpawnBatchS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletSpawnBatchS2CPacket::new).encoder(BulletSpawnBatchS2CPacket::toBytes)
                .consumerNetworkThread(BulletSpawnBatchS2CPacket::handle).add();

        net.messageBuilder(BulletCorrectionS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletCorrectionS2CPacket::new).encoder(BulletCorrectionS2CPacket::toBytes)
                .consumerNetworkThread(BulletCorrectionS2CPacket::handle).add();

        net.messageBuilder(BulletCorrectionBatchS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletCorrectionBatchS2CPacket::new).encoder(BulletCorrectionBatchS2CPacket::toBytes)
                .consumerNetworkThread(BulletCorrectionBatchS2CPacket::handle).add();

        net.messageBuilder(BulletDespawnBatchS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletDespawnBatchS2CPacket::new).encoder(BulletDespawnBatchS2CPacket::toBytes)
                .consumerNetworkThread(BulletDespawnBatchS2CPacket::handle).add();

        net.messageBuilder(BulletTrackingBatchS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletTrackingBatchS2CPacket::new).encoder(BulletTrackingBatchS2CPacket::toBytes)
                .consumerNetworkThread(BulletTrackingBatchS2CPacket::handle).add();

        net.messageBuilder(BulletShatterS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BulletShatterS2CPacket::new).encoder(BulletShatterS2CPacket::toBytes)
                .consumerNetworkThread(BulletShatterS2CPacket::handle).add();

        net.messageBuilder(LaserBeamBatchS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LaserBeamBatchS2CPacket::new).encoder(LaserBeamBatchS2CPacket::toBytes)
                .consumerNetworkThread(LaserBeamBatchS2CPacket::handle).add();

    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sentToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToTracking(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public static <MSG> void sendToTrackingAndSelf(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

}
