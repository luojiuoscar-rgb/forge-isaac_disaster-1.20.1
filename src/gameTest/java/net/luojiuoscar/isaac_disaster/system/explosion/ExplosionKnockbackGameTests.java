package net.luojiuoscar.isaac_disaster.system.explosion;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.networking.packet.ExplosionKnockbackS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_explosion")
@PrefixGameTestTemplate(false)
public final class ExplosionKnockbackGameTests {
    private static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "messages");

    @GameTest(template = "trajectory_empty", batch = "explosion_knockback")
    public static void pendingMobKnockbackPrecedesTwoExplosionImpulses(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, GameType.SURVIVAL)) {
            Husk attacker = EntityType.HUSK.create(helper.getLevel());
            attacker.setPos(fixture.player.position().add(-1, 0, 0));
            try {
                helper.assertTrue(attacker.doHurtTarget(fixture.player), "Mob attack actually damages the player");
                Vec3 pendingVelocity = fixture.player.getDeltaMovement();
                helper.assertTrue(fixture.player.hurtMarked && pendingVelocity.lengthSqr() > 0,
                        "Mob knockback is waiting for authoritative synchronization");
                fixture.connection.packets.clear();
                fixture.explode();
                fixture.explode();
                helper.assertTrue(fixture.motionPackets().size() == 1,
                        "The pending mob velocity is synchronized once");
                sameVelocity(helper, motion(fixture.motionPackets().get(0)), pendingVelocity,
                        "The absolute update contains the pre-explosion velocity");
                helper.assertTrue(fixture.impulses().size() == 2 && fixture.firstMotionIndex() < fixture.firstImpulseIndex(),
                        "One baseline precedes two independent explosion impulses");
                fixture.track();
                helper.assertTrue(fixture.motionPackets().size() == 1 && !fixture.player.hurtMarked,
                        "The tracker does not send the already synchronized explosion velocity again");
                sameVelocity(helper, fixture.replay(Vec3.ZERO), fixture.player.getDeltaMovement(),
                        "Client receives both the pending mob knockback and both explosion impulses");
            } finally {
                attacker.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_knockback")
    public static void ordinaryExplosionPreservesPredictedMovementWithoutAbsoluteReset(GameTestHelper helper) {
        for (GameType mode : List.of(GameType.SURVIVAL, GameType.CREATIVE)) {
            try (Fixture fixture = new Fixture(helper, mode)) {
                fixture.player.getAbilities().flying = false;
                Vec3 serverVelocity = new Vec3(0.25, -0.1, 0.15);
                Vec3 predictedVelocity = new Vec3(1.5, -0.75, -2);
                fixture.player.setDeltaMovement(serverVelocity);
                fixture.explode();
                fixture.track();
                helper.assertTrue(fixture.motionPackets().isEmpty() && fixture.impulses().size() == 1,
                        "An unmarked player receives only one additive impulse in " + mode);
                Vec3 impulse = fixture.impulses().get(0).impulse();
                sameVelocity(helper, fixture.player.getDeltaMovement(), serverVelocity.add(impulse),
                        "Server adds the impulse once");
                sameVelocity(helper, fixture.replay(predictedVelocity), predictedVelocity.add(impulse),
                        "Client retains its current movement");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_knockback")
    public static void spectatorsIgnoreExplosionEvenWhenDetonateReinsertsThem(GameTestHelper helper) {
        assertImmune(helper, GameType.SPECTATOR);
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_knockback")
    public static void flyingCreativePlayersKeepTheirExistingMotionUpdates(GameTestHelper helper) {
        assertImmune(helper, GameType.CREATIVE);
        helper.succeed();
    }

    private static void assertImmune(GameTestHelper helper, GameType mode) {
        for (boolean pending : List.of(false, true)) {
            try (Fixture fixture = new Fixture(helper, mode)) {
                fixture.player.getAbilities().flying = true;
                Vec3 velocity = new Vec3(0.125, 0.25, -0.375);
                fixture.player.setDeltaMovement(velocity);
                fixture.player.hurtMarked = pending;
                Consumer<ExplosionEvent.Detonate> listener = event -> {
                    if (!event.getAffectedEntities().contains(fixture.player)) event.getAffectedEntities().add(fixture.player);
                };
                MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, ExplosionEvent.Detonate.class, listener);
                try {
                    fixture.explode();
                    sameVelocity(helper, fixture.player.getDeltaMovement(), velocity, "Immune player's velocity is unchanged");
                    helper.assertTrue(fixture.player.hurtMarked == pending && fixture.impulses().isEmpty(),
                            "Explosion neither marks nor clears unrelated motion updates for " + mode);
                    fixture.track();
                    helper.assertTrue(fixture.motionPackets().size() == (pending ? 1 : 0),
                            "Only an already pending independent update is synchronized for " + mode);
                } finally {
                    MinecraftForge.EVENT_BUS.unregister(listener);
                }
            }
        }
    }

    private static Vec3 motion(ClientboundSetEntityMotionPacket packet) {
        return new Vec3(packet.getXa() / 8000.0, packet.getYa() / 8000.0, packet.getZa() / 8000.0);
    }

    private static void sameVelocity(GameTestHelper helper, Vec3 actual, Vec3 expected, String message) {
        helper.assertTrue(actual.distanceToSqr(expected) < 1.0e-6,
                message + ": actual=" + actual + ", expected=" + expected);
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final Vec3 center;
        final ServerPlayer player;
        final RecordingConnection connection = new RecordingConnection();

        Fixture(GameTestHelper helper, GameType mode) {
            this.helper = helper;
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            center = new Vec3(origin.getX() + 3.5, 240.5, origin.getZ() + 3.5);
            player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                    new GameProfile(UUID.randomUUID(), "explosion-sync"));
            new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player);
            player.setPos(center.add(1, 0, 0));
            player.setNoGravity(true);
            player.setGameMode(mode);
            try {
                var spawnProtection = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
                spawnProtection.setAccessible(true);
                spawnProtection.setInt(player, 0);
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
            helper.getLevel().addNewPlayer(player);
            player.hurtMarked = false;
            player.setDeltaMovement(Vec3.ZERO);
            connection.packets.clear();
        }

        void explode() {
            helper.assertTrue(CustomExplosion.explode(helper.getLevel(), center,
                    helper.getLevel().damageSources().explosion(null, null),
                    ExplosionParameters.standard(4, 20).withBlockInteraction(Explosion.BlockInteraction.KEEP)),
                    "Explosion executes");
        }

        void track() {
            new ServerEntity(helper.getLevel(), player, 1, true, packet -> { }).sendChanges();
        }

        List<ClientboundSetEntityMotionPacket> motionPackets() {
            return connection.packets.stream().filter(ClientboundSetEntityMotionPacket.class::isInstance)
                    .map(ClientboundSetEntityMotionPacket.class::cast).filter(packet -> packet.getId() == player.getId()).toList();
        }

        List<ExplosionKnockbackS2CPacket> impulses() {
            return connection.packets.stream().filter(this::isImpulse).map(this::decodeImpulse).toList();
        }

        boolean isImpulse(Packet<?> packet) {
            return packet instanceof ClientboundCustomPayloadPacket payload && CHANNEL.equals(payload.getIdentifier());
        }

        ExplosionKnockbackS2CPacket decodeImpulse(Packet<?> packet) {
            FriendlyByteBuf data = ((ClientboundCustomPayloadPacket) packet).getData();
            try {
                data.readVarInt(); // SimpleChannel message discriminator precedes the impulse codec.
                helper.assertTrue(data.readableBytes() == 3 * Double.BYTES, "Only an explosion impulse uses this payload");
                return new ExplosionKnockbackS2CPacket(data);
            } finally {
                data.release();
            }
        }

        int firstMotionIndex() {
            return connection.packets.indexOf(motionPackets().get(0));
        }

        int firstImpulseIndex() {
            for (int i = 0; i < connection.packets.size(); i++) if (isImpulse(connection.packets.get(i))) return i;
            return -1;
        }

        Vec3 replay(Vec3 velocity) {
            for (Packet<?> packet : connection.packets) {
                if (packet instanceof ClientboundSetEntityMotionPacket update && update.getId() == player.getId()) {
                    velocity = motion(update);
                } else if (isImpulse(packet)) {
                    velocity = decodeImpulse(packet).applyTo(velocity);
                }
            }
            return velocity;
        }

        @Override
        public void close() {
            helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    private static final class RecordingConnection extends Connection {
        final List<Packet<?>> packets = new ArrayList<>();

        RecordingConnection() {
            super(PacketFlow.SERVERBOUND);
        }

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
            packets.add(packet);
        }
    }
}
