package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.RevelationAttack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_revelation")
@PrefixGameTestTemplate(false)
public final class RevelationVisualGameTests {
    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void fortySixTicksCannotFireAndClearsItsCharge(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.ability.setChargeAmount(f.attack.getChargeBarId(), 46);
            f.attack.handleChargeInput(f.owner, false);
            f.tick(35);
            helper.assertTrue(f.hits.isEmpty(), "A partial charge creates no damaging beam");
            helper.assertTrue(!f.ability.hasChargeAmount(f.attack.getChargeBarId()), "Release clears the actual charge");
            f.health(helper, 200);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void fullChargeRetainsFifteenHitsAtTwoTickIntervalsAndSavedDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.fire();
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
            f.tick(35);
            helper.assertTrue(f.hits.size() == 15, "The beam retains all fifteen damage pulses");
            for (int i = 1; i < f.hits.size(); i++) {
                helper.assertTrue(f.hits.get(i) - f.hits.get(i - 1) == 2, "The two-tick hit interval remains unchanged");
            }
            helper.assertTrue(f.last().followingPlayer(), "The registered player must remain attached throughout this test");
            f.health(helper, 170);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void startingAndCancelingAnotherChargeDoesNotInterruptAFiredBeam(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.fire();
            f.tick(5);
            f.ability.setHoldRightClick(true);
            f.attack.handleChargeInput(f.owner, true);
            f.attack.tickAttack(f.owner);
            helper.assertTrue(f.ability.getChargeAmount(f.attack.getChargeBarId()) == 1, "The next charge can advance while a beam continues");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.attack.tickAttack(f.owner);
            helper.assertTrue(!f.ability.hasChargeAmount(f.attack.getChargeBarId()), "Losing attack eligibility cancels the charge");
            f.tick(30);
            helper.assertTrue(f.hits.size() == 15, "Clearing the next charge cannot cancel the existing finite beam task");
            f.health(helper, 170);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void registeredPlayerMovementUpdatesTheOriginButNotAnUncontrolledDirection(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.fire();
            f.tick(1);
            RevelationBeamS2CPacket first = f.last();
            helper.assertTrue(first.followingPlayer(), "The fixture must exercise the attached-player path");
            f.owner.setPos(f.owner.position().add(0, 0, 4));
            f.owner.setYRot(0);
            f.tick(2);
            RevelationBeamS2CPacket moved = f.last();
            helper.assertTrue(moved.followingPlayer(), "Movement must not detach a live registered player");
            near(helper, RevelationAttack.getBeamOrigin(f.owner, 1), moved.start(), "The origin follows the current player position");
            near(helper, first.direction(), moved.direction(), "Without controllable, turning does not rotate an existing beam");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void controllableBeamTracksYawAndPitchWhileAttached(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.ability.setControllable(1);
            f.fire();
            f.tick(1);
            f.owner.setYRot(0);
            f.owner.setXRot(-30);
            f.tick(2);
            RevelationBeamS2CPacket turned = f.last();
            helper.assertTrue(turned.followingPlayer() && turned.controllable(), "Controllable is captured and remains attached");
            near(helper, GeometryHelper.mainAxisFromRotation(-30, 0), turned.direction(), "Both yaw and pitch update the server beam direction");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void deathFreezesTheLastAnchorAndStillCompletesAllPulses(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.assertDetach(helper, fixture -> fixture.owner.setHealth(0));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void changingWorldFreezesTheBeamInItsOriginalDimension(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ServerLevel nether = f.level.getServer().getLevel(Level.NETHER);
            helper.assertTrue(nether != null, "The dimension-change case needs a second actual server level");
            f.assertDetach(helper, fixture -> fixture.owner.setServerLevel(nether));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void logoutDetachesEvenIfTheOldPlayerObjectIsStillAlive(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.assertDetach(helper, fixture -> fixture.unregister(fixture.owner));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void replacingThePlayerWithTheSameUuidDoesNotReattachTheOldBeam(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.assertDetach(helper, fixture -> fixture.byId.put(fixture.owner.getUUID(),
                    new ServerPlayer(fixture.level.getServer(), fixture.level, fixture.owner.getGameProfile())));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void aViewerEnteringOnTheFinalPulseReceivesItsClosingSnapshot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Viewer late = f.viewer(f.owner.position().add(0, 0, 40));
            f.fire();
            f.tick(27);
            helper.assertTrue(late.connection.beams.isEmpty(), "An unseen out-of-range viewer receives no active snapshots");
            late.player.setPos(f.owner.position().add(0, 0, 2));
            f.tick(2);
            helper.assertTrue(late.connection.beams.size() == 1, "Entering on the last pulse still receives one visual snapshot");
            RevelationBeamS2CPacket closing = late.connection.beams.get(0);
            helper.assertTrue(closing.finished() && closing.ageTicks() == 28, "The late viewer sees the current closing phase, not a fresh firing animation");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void anUnseenOutOfRangeViewerNeverReceivesAFinalBeam(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Viewer far = f.viewer(f.owner.position().add(0, 0, 40));
            f.fire();
            f.tick(35);
            helper.assertTrue(far.connection.beams.isEmpty(), "The final pulse must not create a closing beam for unrelated players");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "revelation_visual")
    public static void aPreviousViewerStillReceivesTheEndAfterLeavingTheRange(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Viewer previous = f.viewer(f.owner.position().add(0, 0, 2));
            f.fire();
            f.tick(1);
            helper.assertTrue(previous.connection.beams.size() == 1, "The viewer first observes the active beam");
            previous.player.setPos(f.owner.position().add(0, 0, 40));
            f.tick(26);
            helper.assertTrue(previous.connection.beams.size() == 1, "No continuing snapshots are sent outside the range");
            f.tick(2);
            helper.assertTrue(previous.connection.beams.size() == 2 && previous.connection.beams.get(1).finished(),
                    "The final snapshot still clears a previously observed beam");
        }
        helper.succeed();
    }

    private static void near(GameTestHelper helper, Vec3 expected, Vec3 actual, String message) {
        helper.assertTrue(expected.distanceToSqr(actual) < .00000001, message + ": expected " + expected + ", got " + actual);
    }

    private record Viewer(ServerPlayer player, RecordingConnection connection) { }

    private static final class Fixture implements AutoCloseable {
        private static final ResourceLocation TASK = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "revelation_attack");
        private final ServerLevel level;
        private final ServerPlayer owner;
        private final RecordingConnection connection;
        private final PlayerAbility ability;
        private final RevelationAttack attack = ModAttackTypes.REVELATION.get();
        private final Cow target;
        private final List<Integer> hits = new ArrayList<>();
        private final List<Viewer> viewers = new ArrayList<>();
        private final List<ServerPlayer> online;
        private final Map<UUID, ServerPlayer> byId;
        private final Consumer<IsaacAttackAfterHitEvent> listener;
        private int tick;

        private Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            PlayerList playerList = level.getServer().getPlayerList();
            // Populate both real player lookups without starting a socket login or writing player saves.
            online = ObfuscationReflectionHelper.getPrivateValue(PlayerList.class, playerList, "f_11196_");
            byId = ObfuscationReflectionHelper.getPrivateValue(PlayerList.class, playerList, "f_11197_");
            Vec3 origin = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
            Viewer caster = viewer(origin);
            owner = caster.player;
            connection = caster.connection;
            owner.setYRot(-90);
            owner.setXRot(0);
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            ability = owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            helper.assertTrue(playerList.getPlayer(owner.getUUID()) == owner, "The fixture must register the actual player identity");
            target = new Cow(EntityType.COW, level);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(origin.add(3, 0, 0));
            level.addFreshEntity(target);
            listener = event -> {
                if (event.getSource() == owner && event.getHit().getEntity() == target) hits.add(tick);
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
        }

        private Viewer viewer(Vec3 position) {
            UUID id = UUID.randomUUID();
            ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(id, "rev_" + id.toString().substring(0, 8)));
            RecordingConnection recording = new RecordingConnection(level);
            player.connection = new ServerGamePacketListenerImpl(level.getServer(), recording, player);
            player.setPos(position);
            player.setNoGravity(true);
            online.add(player);
            byId.put(player.getUUID(), player);
            level.addNewPlayer(player);
            Viewer viewer = new Viewer(player, recording);
            viewers.add(viewer);
            return viewer;
        }

        private void unregister(ServerPlayer player) {
            online.remove(player);
            byId.remove(player.getUUID(), player);
        }

        private void fire() {
            ability.setChargeAmount(attack.getChargeBarId(), 47);
            attack.handleChargeInput(owner, false);
        }

        private RevelationBeamS2CPacket last() {
            return connection.beams.get(connection.beams.size() - 1);
        }

        private void assertDetach(GameTestHelper helper, Consumer<Fixture> invalidate) {
            ability.setControllable(1);
            fire();
            tick(1);
            RevelationBeamS2CPacket attached = last();
            helper.assertTrue(attached.followingPlayer(), "This test must begin with a genuinely attached beam");
            invalidate.accept(this);
            owner.setPos(owner.position().add(0, 0, 8));
            owner.setYRot(0);
            tick(2);
            RevelationBeamS2CPacket detached = last();
            helper.assertTrue(!detached.followingPlayer(), "Invalidating the player detaches the existing beam");
            near(helper, attached.start(), detached.start(), "Detachment freezes the last valid position");
            near(helper, attached.direction(), detached.direction(), "Detachment freezes the last valid direction");
            owner.setServerLevel(level);
            owner.setHealth(20);
            if (!online.contains(owner)) online.add(owner);
            byId.put(owner.getUUID(), owner);
            tick(30);
            helper.assertTrue(connection.beams.size() == 15 && hits.size() == 15, "All fifteen original-world pulses still complete");
            helper.assertTrue(last().finished() && !last().followingPlayer(), "A revived or reconnected owner cannot reattach the old beam");
            helper.assertTrue(last().dimension().equals(level.dimension().location()), "The beam stays in the release dimension");
            near(helper, attached.start(), last().start(), "The frozen position remains unchanged until completion");
            near(helper, attached.direction(), last().direction(), "The frozen direction remains unchanged until completion");
            health(helper, 170);
        }

        private void tick(int count) {
            for (int i = 0; i < count; i++) {
                tick++;
                ScheduledFuncHelper.tick(level.getServer());
            }
        }

        private void health(GameTestHelper helper, float expected) {
            helper.assertTrue(Math.abs(target.getHealth() - expected) < .001,
                    "Expected health " + expected + ", got " + target.getHealth());
        }

        @Override
        public void close() {
            MinecraftForge.EVENT_BUS.unregister(listener);
            ScheduledFuncHelper.clearByType(TASK, null);
            target.discard();
            byId.remove(owner.getUUID());
            for (Viewer viewer : viewers) {
                unregister(viewer.player);
                viewer.player.setServerLevel(level);
                if (!viewer.player.isRemoved()) level.removePlayerImmediately(viewer.player, Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private static final class RecordingConnection extends Connection {
        private static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "messages");
        private final List<RevelationBeamS2CPacket> beams = new ArrayList<>();
        private final int beamDiscriminator;

        private RecordingConnection(ServerLevel level) {
            super(PacketFlow.CLIENTBOUND);
            FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                Field field = ModMessages.class.getDeclaredField("INSTANCE");
                field.setAccessible(true);
                SimpleChannel channel = (SimpleChannel) field.get(null);
                channel.encodeMessage(new RevelationBeamS2CPacket(UUID.randomUUID(), UUID.randomUUID(), 1,
                        level.dimension().location(), Vec3.ZERO, new Vec3(1, 0, 0), 64, 1,
                        false, true, false, 0), encoded);
                beamDiscriminator = encoded.readUnsignedByte();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Unable to access the registered mod channel", exception);
            } finally {
                encoded.release();
            }
        }

        @Override
        public void send(Packet<?> packet) {
            send(packet, null);
        }

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
            if (!(packet instanceof ClientboundCustomPayloadPacket payload) || !CHANNEL.equals(payload.getIdentifier())) return;
            FriendlyByteBuf data = payload.getData();
            try {
                if (data.readUnsignedByte() == beamDiscriminator) beams.add(new RevelationBeamS2CPacket(data));
            } finally {
                data.release();
            }
        }
    }
}
