package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BulletBounceOnEntity;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.MyReflectionBulletTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Real server collision and lifecycle tests with isolated managers and disposable living targets. */
@GameTestHolder("isaac_disaster_stick")
@PrefixGameTestTemplate(false)
public final class BulletStickGameTests {
    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void firstContactSticksDespiteRejectedDamagePiercingAndBounce(GameTestHelper h) {
        for (boolean rejected : List.of(true, false)) {
            try (Fixture f = new Fixture(h)) {
                Pig first = f.pig(f.origin.add(2, 0, 0));
                Pig second = f.pig(f.origin.add(3.5, 0, 0));
                first.setInvulnerable(rejected);
                float firstHealth = first.getHealth();
                float secondHealth = second.getHealth();
                BulletState bullet = f.bullet(100, 100).piercing(rejected).build();
                bullet.getTriggers().add(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER,
                        BulletBounceOnEntity::new));
                f.manager.spawn(bullet);
                f.manager.tick(h.getLevel());
                h.assertTrue(bullet.isAlive() && bullet.isStuck() && bullet.stickState().target() == first,
                        "Earliest contact attaches before piercing or bounce can continue the sweep");
                h.assertTrue(bullet.velocity().equals(Vec3.ZERO), "Attachment clears outgoing motion");
                h.assertTrue(rejected ? first.getHealth() == firstHealth : first.getHealth() < firstHealth,
                        "Attachment does not depend on acceptance of contact damage");
                h.assertTrue(second.getHealth() == secondHealth, "Later swept targets receive no damage");
                h.assertTrue(bullet.getSplitTriggerCounts().getEntityHits() == (rejected ? 0 : 1),
                        "Rejected contact cannot manufacture successful-hit splits");
            }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void canceledBeforeHitSuppressesDamageAndAttachment(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig target = f.pig(f.origin.add(2, 0, 0));
            float health = target.getHealth();
            BulletState bullet = f.manager.spawn(f.bullet(100, 100).build());
            Object listener = new Object() {
                @SubscribeEvent public void cancelStickyBeforeContact(IsaacAttackBeforeHitEntityEvent event) {
                    if (event.getBulletObject() == bullet) event.setCanceled(true);
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                f.manager.tick(h.getLevel());
                h.assertTrue(bullet.isAlive() && bullet.isFlying(), "Canceled contact stays in flight");
                h.assertTrue(target.getHealth() == health, "Canceled contact applies no damage");
                h.assertTrue(bullet.stickState().targetUuid() == null, "Canceled contact records no host");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void followsMovingAndTeleportingHostThenExpiresIndependentlyOfFlightLifetime(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            BulletState bullet = f.manager.spawn(f.bullet(3, 1).build());
            Vec3 offset = new Vec3(0.2, 0.7, -0.1);
            var ends = new ArrayList<BulletStickEndReason>();
            Object listener = new Object() {
                @SubscribeEvent public void recordExpiryStickEnd(BulletStickEndEvent event) {
                    if (event.getBulletObject() == bullet) ends.add(event.getReason());
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                bullet.stickTo(host, host.position().add(offset));
                host.setPos(host.position().add(0.5, 0.2, 0.3));
                f.manager.tick(h.getLevel());
                h.assertTrue(bullet.isAlive() && bullet.position().distanceToSqr(host.position().add(offset)) < 1e-12,
                        "Attachment outlives the original one-tick flight lifetime and keeps its offset");
                host.teleportTo(host.getX() + 5, host.getY() + 3, host.getZ() + 2);
                f.manager.tick(h.getLevel());
                h.assertTrue(bullet.isAlive() && bullet.position().distanceToSqr(host.position().add(offset)) < 1e-12,
                        "Same-dimension teleport keeps attachment instead of resuming flight");
                f.manager.tick(h.getLevel());
                h.assertTrue(!bullet.isAlive() && f.manager.activeCount() == 0
                        && ends.equals(List.of(BulletStickEndReason.EXPIRED)), "Duration expires once and removes the bullet");
                h.assertTrue(bullet.getSplitTriggerCounts().getEndOfLife() == 0,
                        "Sticky expiry does not invoke ordinary end-of-life splitting");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void hostDeathRestoresFlightAndCanAttachToAnotherHost(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig first = f.pig(f.origin.add(2, 0, 0));
            Vec3 contact = first.position().add(0, 0.7, 0);
            Pig second = f.pig(first.position().add(4, 0, 0));
            f.clearFlightPath(contact, second.position().add(1, 1, 0));
            BulletState bullet = f.manager.spawn(f.bullet(100, 1).position(contact).build());
            int[] starts = {0};
            var reasons = new ArrayList<BulletStickEndReason>();
            Object listener = new Object() {
                @SubscribeEvent public void start(BulletStickStartEvent event) {
                    if (event.getBulletObject() == bullet) starts[0]++;
                }
                @SubscribeEvent public void end(BulletStickEndEvent event) {
                    if (event.getBulletObject() == bullet) reasons.add(event.getReason());
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                bullet.stickTo(first, contact);
                for (int i = 0; i < 5; i++) f.manager.tick(h.getLevel());
                first.setHealth(0);
                f.manager.tick(h.getLevel());
                h.assertTrue(bullet.isAlive() && bullet.isStuck() && bullet.stickState().target() == second,
                        "Host loss resumes full flight and the same bullet collides with a second host: alive=" + bullet.isAlive()
                                + ", phase=" + bullet.stickState().phase() + ", position=" + bullet.position()
                                + ", velocity=" + bullet.velocity() + ", contact=" + contact + ", second=" + second.position()
                                + ", secondBox=" + second.getBoundingBox() + ", host=" + bullet.stickState().target()
                                + ", starts=" + starts[0] + ", reasons=" + reasons + ", traveled=" + bullet.traveled());
                h.assertTrue(starts[0] == 2 && reasons.equals(List.of(BulletStickEndReason.TARGET_DIED)),
                        "Each attachment has independent start/end notifications");
                h.assertTrue(bullet.traveled() > 0 && bullet.traveled() < 3.9
                                && Math.abs(bullet.traveled() - contact.distanceTo(bullet.position())) < 1e-6,
                        "Range is charged only up to the second host contact, not the full tick endpoint");
                h.assertTrue(second.getHealth() < second.getMaxHealth(), "Resumed flight retains normal first-contact damage");
                h.assertTrue(bullet.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId())),
                        "Ordinary gravity remains installed for subsequent detachments");
                h.assertTrue(bullet.stickState().elapsedTicks() == 0, "The new attachment starts a new duration");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void detachmentKeepsReflectionStateAndExpiresAtRemainingRange(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            Vec3 contact = host.position().add(0, 0.7, 0);
            BulletState bullet = f.manager.spawn(f.bullet(100, 1).position(contact)
                    .trajectorySpecs(List.of(new TrajectorySpec(ModTrajectoryModules.MY_REFLECTION_BULLET.getId(), 0))).build());
            TrajectoryEvaluator.initialize(bullet);
            var reflection = bullet.getTrajectoryRuntime().states().get(ModTrajectoryModules.MY_REFLECTION_BULLET.getId());
            bullet.restoreSnapshot(contact, 0, 63.8);
            bullet.stickTo(host, contact);
            for (int i = 0; i < 5; i++) f.manager.tick(h.getLevel());
            host.discard();
            f.manager.tick(h.getLevel());
            h.assertTrue(bullet.getTrajectoryRuntime().states().get(ModTrajectoryModules.MY_REFLECTION_BULLET.getId()) == reflection,
                    "Detachment preserves the existing reflection state instance");
            h.assertTrue(((MyReflectionBulletTrajectoryModule.State) reflection).distance() > 0,
                    "Reflection evaluates after detachment alongside gravity");
            h.assertTrue(bullet.traveled() >= 64 - 1e-6 && bullet.traveled() <= 64 + 1e-6,
                    "Only the original remaining range is consumed");
            f.manager.tick(h.getLevel());
            h.assertTrue(!bullet.isAlive(), "Range expiry resumes normally after host loss");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void detachedHomingStillEvaluatesGravity(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            Pig aim = f.pig(f.origin.add(12, 0, 4));
            Vec3 contact = host.position().add(0, 0.7, 0);
            f.clearFlightPath(contact, aim.position().add(0, 1, 0));
            BulletState bullet = f.manager.spawn(f.bullet(100, 1).position(contact)
                    .homing(true).homingRange(64).noGravity(true).build());
            bullet.stickTo(host, contact);
            host.discard();
            bullet.endStick(BulletStickEndReason.TARGET_REMOVED);
            bullet.setTrackingTarget(aim);
            f.manager.tick(h.getLevel());
            h.assertTrue(bullet.isAlive() && bullet.isFlying() && bullet.trackingPosition() != null,
                    "Detachment resumes active homing: alive=" + bullet.isAlive() + ", phase=" + bullet.stickState().phase()
                            + ", position=" + bullet.position() + ", velocity=" + bullet.velocity()
                            + ", target=" + bullet.trackingTarget() + ", tracking=" + bullet.trackingPosition()
                            + ", acceleration=" + bullet.acceleration() + ", aim=" + aim.position());
            h.assertTrue(bullet.acceleration().y < 0 && !bullet.noGravity(),
                    "Homing cannot bypass the additional gravity controller after detachment");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void discardFromEndListenerRemovesBulletWithoutRecursiveEndNotification(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            BulletState bullet = f.manager.spawn(f.bullet(100, 100).build());
            int[] ends = {0};
            Object listener = new Object() {
                @SubscribeEvent public void discardReentrantlyOnStickEnd(BulletStickEndEvent event) {
                    if (event.getBulletObject() != bullet) return;
                    ends[0]++;
                    event.getStickContext().bullet().discard();
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                bullet.stickTo(host, host.position().add(0, 0.5, 0));
                host.discard();
                f.manager.tick(h.getLevel());
                h.assertTrue(ends[0] == 1 && !bullet.isAlive() && f.manager.activeCount() == 0,
                        "Effect discard during END is idempotent and prevents falling");
                bullet.discard();
                h.assertTrue(ends[0] == 1, "Subsequent removal cannot post END again");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void ownerlessAttachmentPostsPairedServerLifecycleNotifications(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            Vec3 contact = host.position().add(0, 0.6, 0);
            BulletState bullet = f.manager.spawn(BulletState.builder().position(contact)
                    .velocity(new Vec3(1, 0, 0)).range(64).lifetime(1).stickDurationTicks(3).build());
            var phases = new ArrayList<String>();
            int[] ordinaryModuleCalls = {0};
            bullet.getTriggers().add(new SimpleTrigger(ModTriggerTypes.BULLET_TICK,
                    () -> context -> ordinaryModuleCalls[0]++));
            Object listener = new Object() {
                @SubscribeEvent public void recordOwnerlessStickStart(BulletStickStartEvent event) {
                    if (event.getBulletObject() == bullet) phases.add("start");
                }
                @SubscribeEvent public void recordOwnerlessStickTick(BulletStickTickEvent event) {
                    if (event.getBulletObject() == bullet) phases.add("tick");
                }
                @SubscribeEvent public void recordOwnerlessStickEnd(BulletStickEndEvent event) {
                    if (event.getBulletObject() == bullet) phases.add("end:" + event.getReason());
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                bullet.stickTo(host, contact);
                for (int tick = 0; tick < 3; tick++) f.manager.tick(h.getLevel());
                h.assertTrue(phases.equals(List.of("start", "tick", "tick", "end:EXPIRED")),
                        "An ownerless server attachment still publishes its complete lifecycle");
                h.assertTrue(!bullet.isAlive() && f.manager.activeCount() == 0,
                        "Ownerless expiry removes the bullet without module-owner assumptions");
                h.assertTrue(ordinaryModuleCalls[0] == 0, "Ownerless ordinary tick skips module dispatch");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void hostKilledByOrdinaryTickListenerOverridesSameTickExpiry(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            BulletState bullet = f.manager.spawn(f.bullet(1, 100).build());
            var ends = new ArrayList<BulletStickEndReason>();
            Object listener = new Object() {
                @SubscribeEvent public void killHostOnOrdinaryStickyTick(BulletTickEvent event) {
                    if (event.getBullet() == bullet) host.setHealth(0);
                }
                @SubscribeEvent public void recordHostTickDeathStickEnd(BulletStickEndEvent event) {
                    if (event.getBulletObject() == bullet) ends.add(event.getReason());
                }
            };
            MinecraftForge.EVENT_BUS.register(listener);
            try {
                bullet.stickTo(host, host.position().add(0, 0.6, 0));
                f.manager.tick(h.getLevel());
                h.assertTrue(ends.equals(List.of(BulletStickEndReason.TARGET_DIED)),
                        "A host lost during the ordinary tick callback forces END before expiry");
                h.assertTrue(bullet.isAlive() && bullet.isFlying(),
                        "Forced host loss resumes flight instead of naturally expiring on the same tick");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void registeredScorpioPoisonConsumesTheStickStartTargetContext(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Pig host = f.pig(f.origin.add(2, 0, 0));
            BulletState bullet = f.manager.spawn(f.bullet(100, 100).build());
            bullet.getTriggers().add(new SimpleTrigger(ModTriggerTypes.BULLET_STICK_START, ModExecutableEffects.SCORPIO));
            h.assertTrue(!host.hasEffect(ModEffects.DEADLY_POISON.get()), "Host starts without deadly poison");
            bullet.stickTo(host, host.position().add(0, 0.6, 0));
            h.assertTrue(host.hasEffect(ModEffects.DEADLY_POISON.get()),
                    "The registered Scorpio effect reads the living host from the stick trigger context");
            h.assertTrue(host.getEffect(ModEffects.DEADLY_POISON.get()).getDuration() == 40,
                    "Scorpio applies its real potion profile through the standard potion effect");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void removalAndDimensionChangePublishTheirActualForcedEndReasons(GameTestHelper h) {
        for (Entity.RemovalReason removal : List.of(Entity.RemovalReason.DISCARDED, Entity.RemovalReason.CHANGED_DIMENSION)) {
            try (Fixture f = new Fixture(h)) {
                Pig host = f.pig(f.origin.add(2, 0, 0));
                BulletState bullet = f.manager.spawn(f.bullet(100, 100).build());
                Vec3 contact = host.position().add(0, 0.6, 0);
                UUID hostId = host.getUUID();
                var ends = new ArrayList<BulletStickContext>();
                Object listener = new Object() {
                    @SubscribeEvent public void recordRemovedHostStickEnd(BulletStickEndEvent event) {
                        if (event.getBulletObject() == bullet) ends.add(event.getStickContext());
                    }
                };
                MinecraftForge.EVENT_BUS.register(listener);
                try {
                    bullet.stickTo(host, contact);
                    host.remove(removal);
                    f.manager.tick(h.getLevel());
                    BulletStickEndReason expected = removal == Entity.RemovalReason.CHANGED_DIMENSION
                            ? BulletStickEndReason.TARGET_CHANGED_DIMENSION : BulletStickEndReason.TARGET_REMOVED;
                    h.assertTrue(ends.size() == 1 && ends.get(0).endReason() == expected,
                            "The manager classifies actual host removal independently of natural expiry");
                    h.assertTrue(ends.get(0).targetUuid().equals(hostId) && ends.get(0).position().equals(contact),
                            "Forced end keeps host identity and the last attachment position");
                    h.assertTrue(bullet.isAlive() && bullet.isFlying(), "Removed hosts release surviving bullets into gravity");
                } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
            }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void sampledAttachmentRestoresTheContactVelocity(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Vec3 start = f.origin.add(0, 0.7, 0);
            Vec3 contact = start.add(2, 0, 0);
            Pig host = f.pig(contact.add(0, -0.7, 0));
            BulletState bullet = f.bullet(100, 100).position(start).build();
            bullet.advanceTrajectory(new TrajectoryMotion(start.add(3, 0, 0), new Vec3(2, 0, 0), Vec3.ZERO,
                    3, TrajectoryMotion.CompositionMode.ABSOLUTE, 3,
                    List.of(contact, start.add(3, 0, 0))), 1);
            bullet.stickTo(host, contact);
            bullet.retainTrajectoryFraction(0.5);
            host.discard();
            bullet.endStick(BulletStickEndReason.TARGET_REMOVED);
            h.assertTrue(Math.abs(bullet.velocity().x - 4) < 1e-9 && Math.abs(bullet.traveled() - 2) < 1e-9,
                    "Resumption uses contact-segment velocity and charged distance, not whole-tick endpoints");
            BulletState clipped = f.bullet(100, 100).position(start).build();
            Vec3 clippedContact = start.add(0.02, 0, 0);
            Pig next = f.pig(clippedContact.add(0, -0.7, 0));
            clipped.advanceTrajectory(new TrajectoryMotion(clippedContact, new Vec3(4, 0, 0), Vec3.ZERO,
                    0.02, TrajectoryMotion.CompositionMode.ABSOLUTE, 0.02, List.of(clippedContact)), 1);
            clipped.stickTo(next, clippedContact);
            next.discard();
            clipped.endStick(BulletStickEndReason.TARGET_REMOVED);
            h.assertTrue(Math.abs(clipped.velocity().x - 4) < 1e-9 && clipped.validateRuntimeVelocity(),
                    "A range-clipped substep retains its authored velocity instead of deriving speed from clipped distance");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "sticky")
    public static void lethalSampleBoundaryContactStopsTheOldPath(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Vec3 start = f.origin.add(0, 0.7, 0);
            BulletState bullet = f.manager.spawn(f.bullet(100, 100).position(start)
                    .trajectorySpecs(List.of(new TrajectorySpec(ModTrajectoryModules.MY_REFLECTION_BULLET.getId(), 0))).build());
            var runtime = bullet.getTrajectoryRuntime();
            runtime.beginStep();
            var predicted = TrajectoryEvaluator.evaluate(bullet, new Vec3(4, 0, 0), 0, 1);
            Vec3 boundary = predicted.path().get(0);
            runtime.rollbackStep();
            Pig host = f.pig(boundary.add(1, -0.7, 0));
            host.setPos(boundary.add(host.getBbWidth() * 0.5 + bullet.collisionWidth() * 0.5, -0.7, 0));
            host.setHealth(0.5F);
            f.manager.tick(h.getLevel());
            h.assertTrue(host.isDeadOrDying() && bullet.isAlive() && bullet.isDetached(),
                    "Lethal contact briefly attaches then resumes flight");
            h.assertTrue(bullet.position().distanceTo(boundary) < 1e-6,
                    "A transition at a segment boundary must stop all remaining old path segments");
            h.assertTrue(bullet.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId())),
                    "Collision rollback preserves the newly added gravity controller");
        }
        h.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final BulletManager manager = new BulletManager();
        final ServerPlayer owner;
        final Vec3 origin;
        final List<Pig> targets = new ArrayList<>();
        final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            origin = helper.absolutePos(new BlockPos(3, 20, 3)).getCenter();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "stick_runtime"));
            owner.setPos(origin.add(-3, 0, 0));
        }
        Pig pig(Vec3 position) {
            Pig target = EntityType.PIG.create(helper.getLevel());
            helper.assertTrue(target != null, "Test pig can be created");
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(position);
            helper.getLevel().addFreshEntity(target);
            targets.add(target);
            return target;
        }
        BulletState.Builder bullet(int duration, int lifetime) {
            AttackContext context = AttackContext.builder(owner, owner).attackType(ModAttackTypes.BULLET.get())
                    .position(origin.add(0, 0.5, 0)).direction(new Vec3(1, 0, 0)).damage(1.0)
                    .bulletScaleModifier(0.0).speed(4).range(64).stickDurationTicks(duration).build();
            return BulletState.from(context).lifetime(lifetime).collisionWidth(0.2).collisionHeight(0.2);
        }
        void clearFlightPath(Vec3 start, Vec3 end) {
            AABB bounds = new AABB(start, end).inflate(1);
            for (BlockPos mutable : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                    BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
                BlockPos position = mutable.immutable();
                originalBlocks.putIfAbsent(position, helper.getLevel().getBlockState(position));
                helper.getLevel().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            }
        }
        @Override public void close() {
            for (BulletState bullet : manager.activeStates()) manager.remove(bullet);
            for (Pig target : targets) target.discard();
            originalBlocks.forEach((position, state) -> helper.getLevel().setBlockAndUpdate(position, state));
        }
    }
}
