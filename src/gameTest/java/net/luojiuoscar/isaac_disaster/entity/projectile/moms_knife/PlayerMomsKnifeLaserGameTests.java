package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeLaserGameTests {
    @GameTest(template = "trajectory_empty", batch = "knife_laser")
    public static void aShortLinkStopsAtItsEndpointAfterHittingAnEnemy(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            Vec3 start = center(f);
            Vec3 end = start.add(0, 0, 0.65);
            Cow target = f.cow(start.add(0, 0, 0.3));
            SegmentProbe laser = new SegmentProbe();
            AttackContext context = AttackContext.builder(f.owner, f.main).attackType(ModAttackTypes.LASER.get())
                    .position(start).direction(end.subtract(start)).range(start.distanceTo(end)).damage(0.2).build();
            helper.assertTrue(Math.abs(context.getBulletRange() - 0.65) < 1e-9, "The context itself can describe a short connection");
            helper.assertTrue(laser.shootBetween(context), "The connection fires through the normal laser pipeline");
            helper.assertTrue(target.getHealth() < 1000, "The normal laser hits the enemy along the connection");
            helper.assertTrue(laser.lastPosition != null && laser.lastPosition.distanceTo(end) < 1e-5,
                    "Resuming after contact stops exactly at the knife endpoint: " + laser.lastPosition + " vs " + end);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser")
    public static void theLinkUsesTheSameVisualPacketsAsOrdinaryLaserAttack(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            PacketRecorder recorder = new PacketRecorder(f.owner);
            ModAttackTypes.LASER.get().shoot(AttackContext.builder(f.owner, f.owner)
                    .attackType(ModAttackTypes.LASER.get()).position(center(f)).direction(new Vec3(0, 0, 1))
                    .range(3).damage(0.2).build());
            helper.assertTrue(recorder.payloads.size() == 1, "An ordinary laser sends its existing visual packet: " + recorder.payloads.size());
            FriendlyByteBuf ordinary = recorder.payloads.get(0).getData();
            int discriminator;
            try {
                discriminator = ordinary.readVarInt();
                helper.assertTrue(!new LaserBeamBatchS2CPacket(ordinary).entries().isEmpty(),
                        "The reference uses the normal laser visual format");
            } finally { ordinary.release(); }

            ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.onReleased(f.owner);
            recorder.payloads.clear();
            f.main.tick();
            helper.assertTrue(recorder.payloads.size() == 1, "The knife tick sends the existing laser visual packet");
            FriendlyByteBuf linked = recorder.payloads.get(0).getData();
            try {
                helper.assertTrue(linked.readVarInt() == discriminator, "The link uses the ordinary laser packet type");
                var beams = new LaserBeamBatchS2CPacket(linked).entries();
                helper.assertTrue(beams.size() == 1 && beams.get(0).start().distanceTo(center(f)) < 1e-5
                                && beams.get(0).end().distanceTo(f.main.position()) < 1e-5,
                        "The ordinary laser visual runs exactly from the player to the knife");
                helper.assertTrue(beams.get(0).color() == 0xFF0000 && beams.get(0).width() > 0,
                        "The link uses the ordinary laser appearance");
            } finally { linked.release(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser", timeoutTicks = 30)
    public static void eachReturningKnifeLinksAtTwentyPercentEveryTick(GameTestHelper helper) {
        var f = fixture(helper);
        ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(16);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        Vec3 center = center(f);
        Cow target = f.cow(center.add(0, 0, 2));
        List<IsaacAttackAfterHitEvent> hits = new ArrayList<>();
        List<AttackFiredEvent> fired = new ArrayList<>();
        Map<Long, Set<Object>> perTick = new HashMap<>();
        Consumer<IsaacAttackAfterHitEvent> hit = event -> {
            if (event.getSource() == f.owner && event.getAttackType().equals(ModAttackTypes.LASER.getId())
                    && event.getHit().getEntity() == target) {
                helper.assertTrue(perTick.computeIfAbsent(f.level.getGameTime(), ignored -> new HashSet<>())
                        .add(event.getBulletObject().getShooter()), "The same knife hits this enemy only once in a tick");
                hits.add(event);
            }
        };
        Consumer<AttackFiredEvent> fire = event -> { if (event.getOwner() == f.owner) fired.add(event); };
        MinecraftForge.EVENT_BUS.addListener(hit);
        MinecraftForge.EVENT_BUS.addListener(fire);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        helper.onEachTick(() -> {
            if (!f.closed) HeldMomsKnifeGroup.knives(f.owner).forEach(knife -> { knife.tick(); knife.tick(); });
        });
        helper.runAfterDelay(6, () -> f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3));
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(hits.size() >= 12, "Both knives repeatedly hit along their connecting segments: " + hits.size());
                helper.assertTrue(hits.stream().allMatch(e -> Math.abs(e.getDamage() - 0.2) < 1e-6 || Math.abs(e.getDamage() - 0.6) < 1e-6)
                                && hits.stream().anyMatch(e -> Math.abs(e.getDamage() - 0.6) < 1e-6),
                        "Every beam hit reads twenty percent of the current panel, including mid-flight changes");
                helper.assertTrue(hits.stream().map(e -> e.getBulletObject().getShooter()).distinct().count() == 2,
                        "Each returning knife owns its own laser hit record");
                helper.assertTrue(fired.size() == 1, "Sustaining the links does not publish new attack releases");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(hit);
                MinecraftForge.EVENT_BUS.unregister(fire);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser", timeoutTicks = 15)
    public static void idleAndOneWayKnivesDoNotProduceLaserDamage(GameTestHelper helper) {
        var f = fixture(helper);
        ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
        Cow target = f.cow(center(f).add(0, 0, 1));
        List<IsaacAttackAfterHitEvent> hits = new ArrayList<>();
        Consumer<IsaacAttackAfterHitEvent> hit = event -> {
            if (event.getSource() == f.owner && event.getAttackType().equals(ModAttackTypes.LASER.getId())) hits.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(hit);
        AttackContext context = AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get())
                .position(center(f)).mainAxis(f.owner.getLookAngle()).range(24).speed(0.2).build();
        AttackExecutor.perform(AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.PREPARE_AND_EXECUTE, List.of(context), false));
        helper.onEachTick(() -> { if (!f.closed) f.knives().forEach(PlayerMomsKnifeEntity::tick); });
        helper.runAfterDelay(6, () -> {
            try {
                helper.assertTrue(target.getHealth() < 1000 && hits.isEmpty(), "Idle/one-way knife damage never creates a laser link");
                helper.succeed();
            } finally { MinecraftForge.EVENT_BUS.unregister(hit); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser", timeoutTicks = 30)
    public static void linksIgnoreWallsAndStayStraightWhenTheKnifeTurns(GameTestHelper helper) {
        var f = fixture(helper);
        f.owner.setPos(f.owner.getX(), 160, f.owner.getZ());
        f.main.initialize(f.owner);
        ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(16);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        f.ability.setHoming(1);
        Cow target = f.cow(center(f).add(0, 0, 2));
        Cow beyond = f.cow(center(f).add(0, 0, 30));
        BlockPos wall = BlockPos.containing(center(f).add(0, 0, 1));
        BlockState previous = f.level.getBlockState(wall);
        f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        List<Long> hitTimes = new ArrayList<>();
        List<Double> lengths = new ArrayList<>();
        List<Boolean> matchingRanges = new ArrayList<>();
        Consumer<IsaacAttackAfterHitEvent> hit = event -> {
            if (event.getSource() != f.owner || !event.getAttackType().equals(ModAttackTypes.LASER.getId())) return;
            var beam = event.getBulletObject();
            var attack = beam.getAttackContext();
            matchingRanges.add(Math.abs(attack.getBulletRange() - beam.getRange()) < 1e-9);
            Vec3 end = attack.getPos().add(beam.getVelocity().scale(beam.getRange()));
            helper.assertTrue(end.distanceTo(f.main.position()) < 1e-5 && attack.getPos().distanceTo(center(f)) < 1e-5
                            && beam.getTraveled() >= 0 && beam.getTraveled() <= beam.getRange(),
                    "The runtime segment connects the current player and knife positions, even below one block");
            helper.assertTrue(!beam.isHoming() && beam.isSpectral(), "The connecting laser never steers or stops at blocks");
            hitTimes.add(f.level.getGameTime());
            lengths.add(beam.getRange());
        };
        MinecraftForge.EVENT_BUS.addListener(hit);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        helper.onEachTick(() -> {
            if (!f.closed) {
                Vec3 midpoint = center(f).lerp(f.main.position(), 0.5);
                target.setPos(midpoint.add(0, -0.45, 0));
                target.setDeltaMovement(Vec3.ZERO);
                f.main.tick();
            }
        });
        helper.runAfterDelay(5, () -> f.owner.setYRot(35));
        helper.runAfterDelay(8, () -> f.owner.setPos(f.owner.position().add(0.4, 0, 0.2)));
        helper.runAfterDelay(13, () -> {
            try {
                helper.assertTrue(hitTimes.size() >= 6, "The link keeps hitting before and after the turn: " + hitTimes);
                helper.assertTrue(matchingRanges.stream().allMatch(Boolean::booleanValue),
                        "Laser runtime range is fully determined by its context");
                helper.assertTrue(lengths.stream().anyMatch(length -> length < 1),
                        "Short links retain their actual length in both context and runtime");
                helper.assertTrue(beyond.getHealth() == 1000, "The laser never extends beyond its knife endpoint");
                helper.assertTrue(f.level.getBlockState(wall).is(Blocks.STONE), "Piercing the wall does not break it");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(hit);
                f.level.setBlockAndUpdate(wall, previous);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser", timeoutTicks = 15)
    public static void aCompletedThrowStopsTheLink(GameTestHelper helper) {
        var f = fixture(helper);
        ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(2);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        helper.assertTrue(f.main.isInFlight()
                        && f.main.getTriggers().contains(ModExecutableEffects.LASER_PLUS_MOMS_KNIFE.get()),
                "The successful prepared throw enables its laser tick effect");
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(6, () -> {
            try {
                helper.assertTrue(!f.main.isInFlight(), "Returning to the player ends the laser tick effect");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_laser")
    public static void allSixteenMainKnivesAndApprovedLokiKnivesReceiveTheirOwnLinks(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.TECHNOLOGY.get().handleObtain(f.owner, null);
            ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(f.owner, null);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(100);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.onReleased(f.owner);
            var knives = HeldMomsKnifeGroup.knives(f.owner);
            helper.assertTrue(knives.size() == 19 && knives.stream().allMatch(knife -> knife.isInFlight()
                            && knife.getTriggers().contains(ModExecutableEffects.LASER_PLUS_MOMS_KNIFE.get())),
                    "Sixteen main knives and three approved Loki knives each carry a link: " + knives.size());
        }
        helper.succeed();
    }

    private static Vec3 center(PlayerMomsKnifeGameTests.Fixture f) {
        return f.owner.getEyePosition().add(0, -0.15 * f.owner.getBbHeight(), 0);
    }

    private static final class PacketRecorder extends Connection {
        final List<ClientboundCustomPayloadPacket> payloads = new ArrayList<>();

        PacketRecorder(ServerPlayer player) {
            super(PacketFlow.SERVERBOUND);
            new ServerGamePacketListenerImpl(player.server, this, player);
        }

        @Override public void send(Packet<?> packet) { record(packet); }
        @Override public void send(Packet<?> packet, PacketSendListener listener) { record(packet); }

        private void record(Packet<?> packet) {
            if (packet instanceof ClientboundCustomPayloadPacket payload
                    && payload.getIdentifier().getNamespace().equals("isaac_disaster")) payloads.add(payload);
        }
    }

    private static final class SegmentProbe extends LaserAttack {
        Vec3 lastPosition;

        SegmentProbe() { super(0); }

        @Override
        protected void stepLaser(LaserProjectile laser, ServerLevel level, AttackContext context) {
            super.stepLaser(laser, level, context);
            lastPosition = laser.position;
        }
    }

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper, 0);
        f.owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(0, 1024, 0));
        f.main.initialize(f.owner);
        return f;
    }
}
