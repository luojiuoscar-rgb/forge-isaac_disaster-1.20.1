package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_mom_knife")
@PrefixGameTestTemplate(false)
public final class MomKnifeTimingGameTests {
    private static final ResourceLocation MOM_KNIFE =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "mom_knife");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void zeroWaitStartsRiseImmediatelyAndAimsForTwoTicks(GameTestHelper helper) {
        assertTiming(helper, 9, 0, 2);
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void sixTickWaitFollowsTheOwnerBeforeRisingAndAiming(GameTestHelper helper) {
        assertTiming(helper, 12, 6, 5);
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void sixTickAimFinishesTurningBeforeLaunch(GameTestHelper helper) {
        assertTiming(helper, 5, 2, 6);
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void waitingKeepsItsLockedTargetWhenAnotherCandidateAppears(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        SeededKnife knife = f.knife(12);
        Cow locked = f.target();
        locked.setPos(f.owner.position().add(0, 0, -4));
        knife.tick();
        f.target();
        f.owner.setYRot(180);
        helper.onEachTick(() -> {
            if (f.closed || knife.getBehaviorState() != MomKnifeEntity.State.THRUST) return;
            try {
                Vec3 expected = locked.getBoundingBox().getCenter().subtract(knife.position()).normalize();
                helper.assertTrue(expected.distanceTo(knife.getRenderPose(0).bladePoint(Vec3.ZERO, 1)) < 2e-5,
                        "Waiting preserves the locked target despite a closer candidate and an owner turn");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void targetLossDuringWaitingCancelsTheDeadlineAndAllowsANewLock(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        SeededKnife knife = f.knife(12);
        Cow lost = f.target();
        knife.tick();
        lost.setInvulnerable(true);
        knife.tick();
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW,
                    "An invalidated pending target cannot start a stale rise or force a return");
            f.target();
        });
        helper.onEachTick(() -> {
            if (f.closed || knife.getBehaviorState() != MomKnifeEntity.State.RISE) return;
            try {
                helper.assertTrue(knife.lastTick >= f.startedAt + 7,
                        "Only the replacement target can start the next rise");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 40)
    public static void knivesKeepIndependentRiseAndAimDeadlines(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        SeededKnife first = f.knife(12);
        SeededKnife second = f.knife(12);
        f.target();
        first.tick();
        second.tick();
        helper.assertTrue(first.getBehaviorState() == MomKnifeEntity.State.FOLLOW
                        && second.getBehaviorState() == MomKnifeEntity.State.RISE,
                "Each knife samples its own rise wait even when both select in the same tick");
        long[] firedAt = {-1, -1};
        helper.onEachTick(() -> {
            if (f.closed) return;
            if (first.getBehaviorState() == MomKnifeEntity.State.THRUST && firedAt[0] < 0) firedAt[0] = first.lastTick;
            if (second.getBehaviorState() == MomKnifeEntity.State.THRUST && firedAt[1] < 0) firedAt[1] = second.lastTick;
            if (firedAt[0] < 0 || firedAt[1] < 0) return;
            try {
                helper.assertTrue(firedAt[0] - f.startedAt == 14 && firedAt[1] - f.startedAt == 9,
                        "The knives preserve separate waits and aim durations through launch");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 80)
    public static void recallWhileWaitingClearsThePendingAttack(GameTestHelper helper) {
        assertRecall(helper, MomKnifeEntity.State.FOLLOW);
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 80)
    public static void recallWhileAimingRequiresANewAttackCycle(GameTestHelper helper) {
        assertRecall(helper, MomKnifeEntity.State.AIM);
    }

    private static void assertRecall(GameTestHelper helper, MomKnifeEntity.State recallState) {
        Fixture f = new Fixture(helper);
        SeededKnife knife = f.knife(12);
        f.target();
        knife.tick();
        long[] recalledAt = {-1};
        helper.onEachTick(() -> {
            if (f.closed) return;
            if (recalledAt[0] < 0 && knife.getBehaviorState() == recallState) {
                recalledAt[0] = helper.getLevel().getGameTime();
                UUID original = knife.getUUID();
                knife.setPos(f.owner.position().add(0, 17, 0));
                knife.tick();
                helper.assertTrue(knife.isRecallPending(), "Separation queues the locked knife for recall");
                helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner), "The owner processes recall");
                helper.assertTrue(knife.getUUID().equals(original)
                                && knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW,
                        "Recall reuses the knife and restores following");
            }
            if (recalledAt[0] < 0) return;
            long elapsed = helper.getLevel().getGameTime() - recalledAt[0];
            if (elapsed < 19) helper.assertTrue(knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW,
                    "Clearing the pending attack prevents stale deadlines from bypassing recall cooldown");
            if (knife.getBehaviorState() == MomKnifeEntity.State.RISE) {
                try {
                    helper.assertTrue(elapsed >= 19, "The next rise belongs to a fresh attack after cooldown");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_timing", timeoutTicks = 180)
    public static void eachAttackSamplesANewAimDuration(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        SeededKnife knife = f.knife(12);
        Cow target = f.target();
        Vec3 targetPosition = target.position();
        knife.aimSeed = 0L;
        long[] aimingAt = {-1};
        int[] launches = {0};
        boolean[] launched = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            target.setDeltaMovement(Vec3.ZERO);
            target.setPos(targetPosition);
            target.setHealth(target.getMaxHealth());
            MomKnifeEntity.State state = knife.getBehaviorState();
            if (state == MomKnifeEntity.State.AIM && aimingAt[0] < 0) aimingAt[0] = knife.lastTick;
            if (state == MomKnifeEntity.State.THRUST && !launched[0]) {
                launched[0] = true;
                helper.assertTrue(knife.lastTick - aimingAt[0] == (launches[0] == 0 ? 2 : 6),
                        "Each new aim uses a fresh random duration rather than the previous attack's sample");
                if (++launches[0] == 2) {
                    f.close();
                    helper.succeed();
                }
            }
            if (state == MomKnifeEntity.State.RETURN && launched[0]) {
                knife.aimSeed = 3L;
                launched[0] = false;
                aimingAt[0] = -1;
            }
        });
    }

    private static void assertTiming(GameTestHelper helper, long seed, int waitTicks, int aimTicks) {
        Fixture f = new Fixture(helper);
        SeededKnife knife = f.knife(seed);
        Cow target = f.target();
        long lockedAt = helper.getLevel().getGameTime();
        Vec3[] riseOrigin = {null};
        Vec3[] aimAnchor = {null};
        Runnable checkTiming = () -> {
            if (f.closed || knife.lastTick == Long.MIN_VALUE) return;
            long elapsed = knife.lastTick - lockedAt;
            MomKnifeEntity.State state = knife.getBehaviorState();
            if (elapsed < waitTicks) {
                helper.assertTrue(state == MomKnifeEntity.State.FOLLOW,
                        "The knife keeps following until its sampled rise deadline: " + elapsed);
                helper.assertTrue(knife.getRenderPose(0).pitch() == 0, "Waiting does not lift the blade");
            } else if (elapsed < waitTicks + 3) {
                helper.assertTrue(state == MomKnifeEntity.State.RISE, "Rise lasts exactly three ticks");
                if (riseOrigin[0] == null) {
                    riseOrigin[0] = knife.position();
                    if (waitTicks > 0) helper.assertTrue(riseOrigin[0].x > f.initialOwnerPosition.x + 1,
                            "The waiting knife follows the moving owner before choosing its rise origin");
                }
                helper.assertTrue(Math.abs(knife.position().x - riseOrigin[0].x) < 1e-6
                                && Math.abs(knife.position().z - riseOrigin[0].z) < 1e-6,
                        "Rise locks the position captured after waiting");
            } else if (elapsed < waitTicks + 3 + aimTicks) {
                helper.assertTrue(state == MomKnifeEntity.State.AIM, "Aim lasts the sampled duration");
                if (aimAnchor[0] == null) {
                    aimAnchor[0] = knife.position();
                    target.setPos(target.position().add(1, 0, 0));
                }
                helper.assertTrue(knife.position().equals(aimAnchor[0]), "Aim holds the handle still");
            } else {
                try {
                    helper.assertTrue(elapsed == waitTicks + 3 + aimTicks
                                    && state == MomKnifeEntity.State.THRUST,
                            "Launch occurs at the sampled deadline without an extra tick");
                    Vec3 desired = target.getBoundingBox().getCenter().subtract(knife.position()).normalize();
                    helper.assertTrue(desired.distanceTo(knife.getRenderPose(0).bladePoint(Vec3.ZERO, 1)) < 2e-5,
                            "Even the shortest aim fully faces the latest target before launch");
                    helper.succeed();
                } finally { f.close(); }
            }
        };
        helper.onEachTick(checkTiming);
        knife.tick();
        checkTiming.run();
        if (waitTicks > 0) f.owner.setPos(f.owner.position().add(3, 0, 0));
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final Vec3 initialOwnerPosition;
        final long startedAt;
        final List<Cow> targets = new ArrayList<>();
        boolean closed;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            startedAt = helper.getLevel().getGameTime();
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, "kt-" + id.toString().substring(0, 8)));
            BlockPos anchor = helper.getLevel().getSharedSpawnPos();
            initialOwnerPosition = new Vec3(anchor.getX() + 0.5, 1600 + NEXT_FIXTURE.getAndIncrement() * 32,
                    anchor.getZ() + 0.5);
            owner.setPos(initialOwnerPosition);
            owner.setYRot(0);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        SeededKnife knife(long seed) {
            data.addCount(MOM_KNIFE, 1);
            SeededKnife knife = new SeededKnife(helper, seed);
            knife.setOwner(owner);
            knife.refreshFormationIndex(owner, data.getEntry(MOM_KNIFE).orElseThrow().getEntityCount());
            knife.setPos(owner.position().add(0, owner.getBbHeight() * 0.7, -0.95));
            helper.assertTrue(helper.getLevel().addFreshEntity(knife), "Owned knife enters the level");
            data.registerEntity(MOM_KNIFE, knife.getUUID());
            return knife;
        }

        Cow target() {
            Cow target = EntityType.COW.create(helper.getLevel());
            helper.assertTrue(target != null, "Target is created");
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(owner.position().add(0, 0, -2));
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "Target enters the level");
            targets.add(target);
            return target;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            targets.forEach(Cow::discard);
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            owner.discard();
        }
    }

    private static final class SeededKnife extends MomKnifeEntity {
        long lastTick = Long.MIN_VALUE;
        Long aimSeed;

        SeededKnife(GameTestHelper helper, long seed) {
            super(ModEntities.MOM_KNIFE.get(), helper.getLevel());
            random.setSeed(seed);
        }

        @Override
        protected void tickFamiliar() {
            // Keep later attack samples deterministic despite the base entity's validity-check RNG.
            if (aimSeed != null && getBehaviorState() == State.RISE) random.setSeed(aimSeed);
            super.tickFamiliar();
            lastTick = level().getGameTime();
        }
    }
}
