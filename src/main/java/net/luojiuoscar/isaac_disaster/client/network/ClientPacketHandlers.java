package net.luojiuoscar.isaac_disaster.client.network;

import java.util.List;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletShatterS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.screen.IsaacItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Client-only handlers for S2C packets whose common codecs must load on a dedicated server. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleFlyUpdate(int units) {
        if (Minecraft.getInstance().player != null) {
            ClientDataManager.getInstance().setFlyPercentage(units);
        }
    }

    public static void handleChargeUpdate(float progress) {
        if (Minecraft.getInstance().player != null) {
            ClientDataManager.getInstance().setChargeProgress(progress);
        }
    }

    public static void openIsaacItemScreen(
        List<ItemStack> passiveItems, List<ItemStack> trinketItems) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        IsaacDisaster.LOGGER.info("items: {}, trinkets: {}", passiveItems.size(), trinketItems.size());
        minecraft.setScreen(new IsaacItemScreen(passiveItems, trinketItems));
    }

    public static void refreshPlayerDimensions() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.refreshDimensions();
        }
    }

    public static void handleBulletSpawn(BulletSpawnS2CPacket packet) {
        ClientBulletRuntime.INSTANCE.spawn(packet);
    }

    public static void handleBulletSpawnBatch(BulletSpawnBatchS2CPacket packet) {
        packet.entries().forEach(ClientBulletRuntime.INSTANCE::spawn);
    }

    public static void handleBulletCorrection(BulletCorrectionS2CPacket packet) {
        ClientBulletRuntime.INSTANCE.correct(packet);
    }

    public static void handleBulletCorrectionBatch(BulletCorrectionBatchS2CPacket packet) {
        if (!ClientBulletRuntime.INSTANCE.acceptEpoch(packet.epoch())) {
            return;
        }
        packet.entries().forEach(ClientBulletRuntime.INSTANCE::correct);
    }

    public static void handleBulletDespawnBatch(BulletDespawnBatchS2CPacket packet) {
        if (!ClientBulletRuntime.INSTANCE.acceptEpoch(packet.epoch())) {
            return;
        }
        packet.identities().forEach(ClientBulletRuntime.INSTANCE::despawn);
    }

    public static void handleBulletShatter(BulletShatterS2CPacket packet) {
        ClientBulletRuntime.INSTANCE.applyShatter(packet);
    }

    /** Expands one server-side line segment into sparse local dust particles. */
    public static void handleLaserBeamBatch(LaserBeamBatchS2CPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        for (LaserBeamBatchS2CPacket.Beam beam : packet.entries()) {
            Vec3 delta = beam.end().subtract(beam.start());
            double distance = delta.length();
            int samples = Math.max(1, Math.min(160, (int) Math.ceil(distance / 0.45D)));
            Vector3f color = new Vector3f(
                    ((beam.color() >> 16) & 0xFF) / 255.0F,
                    ((beam.color() >> 8) & 0xFF) / 255.0F,
                    (beam.color() & 0xFF) / 255.0F);
            DustParticleOptions dust = new DustParticleOptions(color,
                    Math.max(0.01F, Math.min(4.0F, beam.width())));
            for (int i = 0; i <= samples; i++) {
                double fraction = (double) i / samples;
                Vec3 position = beam.start().lerp(beam.end(), fraction);
                minecraft.level.addParticle(dust, position.x, position.y, position.z, 0, 0, 0);
            }
        }
    }

    public static void handleBulletTracking(BulletTrackingBatchS2CPacket packet) {
        ClientBulletRuntime.INSTANCE.applyTracking(packet);
    }

    public static void handleReviveEvent(
        int entityId,
        double x,
        double y,
        double z,
        SoundEvent sound,
        ParticleOptions particle,
        ItemStack displayItem) {
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
            minecraft.level.playLocalSound(x, y, z, sound, entity.getSoundSource(), 1.0F, 1.0F, false);
        }
        if (minecraft.player == entity) {
            minecraft.gameRenderer.displayItemActivation(displayItem);
        }
    }
}
