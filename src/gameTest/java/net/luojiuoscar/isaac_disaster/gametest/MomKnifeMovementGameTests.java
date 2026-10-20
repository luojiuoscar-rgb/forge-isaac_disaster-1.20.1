package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.UUID;

/** Uses real game-time ticks for attack progression, and real ownership for movement/recall. */
@GameTestHolder("isaac_disaster_mom_knife")
@PrefixGameTestTemplate(false)
public final class MomKnifeMovementGameTests {
    private static final ResourceLocation MOM_KNIFE =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "mom_knife");

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void centeredFollowingMovesImmediatelyWithoutNetworkYawChanges(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 start = f.owner.position();
            f.knife.setPos(start);
            f.knife.setYRot(37);
            f.knife.tick();
            helper.assertTrue(f.knife.position().distanceTo(
                    start.add(0, f.owner.getBbHeight() * 0.7 * 0.35, -0.3325)) < 1e-6,
                    "The central rear slot follows immediately with no lateral displacement");
            helper.assertTrue(f.knife.getYRot() == 37, "Display heading does not change network entity yaw");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void targetLossStopsImmediatelyAndReturnMovesInTheCurrentTick(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.spawnTarget();
            f.knife.tick();
            helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.RISE, "Rear target starts rise");
            Vec3 stoppedAt = f.knife.position();
            f.target.discard();
            f.knife.tick();
            helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN
                            && f.knife.position().equals(stoppedAt) && f.knife.getDeltaMovement().equals(Vec3.ZERO),
                    "Target loss does not execute an old movement request");
            Vec3 returnStart = f.owner.position().add(4, 0, 0);
            f.knife.setPos(returnStart);
            f.knife.tick();
            helper.assertTrue(Math.abs(f.knife.position().distanceTo(returnStart) - 0.2) < 1e-6,
                    "Return starts in this tick at its configured speed");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void teleportResetsPreviousRenderPosition(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.knife.setOldPosAndRot();
            Vec3 destination = f.owner.position().add(1, 2, 3);
            f.knife.lerpTo(destination.x, destination.y, destination.z, -179, 15, 3, true);
            helper.assertTrue(f.knife.position().equals(destination) && f.knife.xOld == destination.x
                            && f.knife.yOld == destination.y && f.knife.zOld == destination.z,
                    "A teleport resets both ends of render interpolation");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void recallCancelsALockedKnifeAndKeepsTheOriginalUuid(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.spawnTarget();
            f.knife.tick();
            f.knife.tick();
            helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.RISE, "The target is locked during rise");
            UUID original = f.knife.getUUID();
            f.knife.setPos(f.owner.position().add(0, 17, 0));
            f.knife.tick();
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner), "Scheduler processes recall");
            f.knife.tick();
            helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW
                            && f.knife.distanceToSqr(f.owner) < 4 && f.knife.getUUID().equals(original)
                            && f.data.getCount(MOM_KNIFE) == 1 && f.data.containsEntity(MOM_KNIFE, original),
                    "Recall cancels attack and resumes following with the original UUID");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 100)
    public static void timedStabDamagesThenReturns(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 220);
        f.spawnTarget();
        float initial = f.target.getHealth();
        EnumSet<MomKnifeEntity.State> visited = EnumSet.noneOf(MomKnifeEntity.State.class);
        boolean[] attributed = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            visited.add(f.knife.getBehaviorState());
            if (!attributed[0] && f.target.getHealth() < initial) {
                // LivingEntity drops its last damage source after 40 ticks; inspect it at contact.
                helper.assertTrue(f.target.getLastDamageSource() != null
                                && f.target.getLastDamageSource().getDirectEntity() == f.knife
                                && f.target.getLastDamageSource().getEntity() == f.owner,
                        "Contact damage retains knife/owner attribution");
                attributed[0] = true;
            }
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN) f.target.setInvulnerable(true);
        });
        helper.runAfterDelay(50, () -> {
            try {
                helper.assertTrue(visited.containsAll(EnumSet.allOf(MomKnifeEntity.State.class)),
                        "Real ticks visit follow, rise, aim, thrust, retract and return");
                helper.assertTrue(f.target.getHealth() < initial, "A stab damages its locked victim during the forward or return sweep");
                helper.assertTrue(attributed[0], "The contact attribution was checked before expiry");
                helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW
                                && f.data.containsEntity(MOM_KNIFE, f.knife.getUUID())
                                && f.data.getCount(MOM_KNIFE) == 1, "Finishing preserves ownership and requirement");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 60)
    public static void launchedStabDoesNotChaseATargetThatMovesAway(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        float initial = f.target.getHealth();
        boolean[] moved = {false};
        helper.onEachTick(() -> {
            if (!f.closed && !moved[0] && f.knife.getBehaviorState() == MomKnifeEntity.State.THRUST) {
                f.target.setPos(f.target.position().add(4, 0, 0));
                moved[0] = true;
            }
        });
        helper.runAfterDelay(16, () -> {
            try {
                helper.assertTrue(moved[0] && f.knife.distanceToSqr(f.target) > 4, "Launched stab stays on its original path");
                helper.assertTrue(f.target.getHealth() == initial, "A victim that leaves the flight path is not hit remotely");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 60)
    public static void interruptedAimReturnsFromItsCurrentPose(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.AIM, "Fixture is aiming");
                MomKnifePose before = f.knife.getRenderPose(0);
                helper.assertTrue(before.pitch() < 0, "Blade has lifted backward from neutral");
                f.target.discard();
                f.knife.tick();
                MomKnifePose after = f.knife.getRenderPose(0);
                helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN
                                && Math.abs(after.pitch() - before.pitch()) < 1e-4
                                && Math.abs(after.twist() - before.twist()) < 1e-4,
                        "Cancelling starts recovery from the current pose");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 60)
    public static void riseIgnoresOwnerMovementAndSamplesTheRequestedHeight(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        f.knife.tick();
        Vec3 origin = f.knife.position();
        float health = f.target.getHealth();
        f.owner.setPos(f.owner.position().add(3, 0, 0));
        helper.runAfterDelay(4, () -> {
            try {
                Vec3 raised = f.knife.position();
                double height = raised.y - origin.y;
                helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.AIM, "Rise finishes quickly");
                helper.assertTrue(Math.abs(raised.x - origin.x) < 1e-6 && Math.abs(raised.z - origin.z) < 1e-6,
                        "The locked knife no longer follows its owner horizontally");
                helper.assertTrue(height >= 0.5 - 1e-6 && height <= 2 + 1e-6, "Rise height is 0.5-2 blocks");
                helper.assertTrue(f.target.getHealth() == health, "Rise and aim cannot damage");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 60)
    public static void aimTracksTheLatestTargetWithoutMovingTheHandle(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        Vec3[] anchor = {null};
        helper.onEachTick(() -> {
            if (f.closed) return;
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.AIM && anchor[0] == null) {
                anchor[0] = f.knife.position();
                f.target.setPos(f.target.position().add(2, 0, 0));
            }
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.THRUST) {
                try {
                    helper.assertTrue(anchor[0] != null && f.knife.position().equals(anchor[0]), "Handle stays still while aiming");
                    Vec3 desired = f.target.getBoundingBox().getCenter().subtract(anchor[0]).normalize();
                    helper.assertTrue(desired.distanceTo(f.knife.getRenderPose(0).bladePoint(Vec3.ZERO, 1)) < 1e-5,
                            "The launch pose faces the moved target");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 120)
    public static void cooldownOverlapsReturnAndDelaysTheNextLock(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        Vec3 victimPosition = f.target.position();
        long[] returnedAt = {-1};
        helper.onEachTick(() -> {
            if (f.closed) return;
            f.target.setDeltaMovement(Vec3.ZERO);
            f.target.setPos(victimPosition);
            f.target.setHealth(f.target.getMaxHealth());
            long now = f.knife.level().getGameTime();
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN && returnedAt[0] < 0) returnedAt[0] = now;
            if (returnedAt[0] >= 0 && f.knife.getBehaviorState() == MomKnifeEntity.State.RISE) {
                try {
                    long elapsed = now - returnedAt[0];
                    helper.assertTrue(elapsed >= 20 && elapsed <= 34,
                            "New lock waits 20-30 ticks plus at most a target-search interval");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 120)
    public static void missedStabReturnsFromItsLimitedRange(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        Vec3[] launch = {null};
        boolean[] returned = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.THRUST && launch[0] == null) {
                launch[0] = f.knife.position();
                f.target.setPos(f.target.position().add(4, 0, 0));
            }
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN) {
                returned[0] = true;
                f.target.setInvulnerable(true);
                helper.assertTrue(launch[0] != null && f.knife.distanceToSqr(launch[0]) <= 64.0001,
                        "A miss stops within its eight-block range");
            }
        });
        helper.runAfterDelay(90, () -> {
            try {
                helper.assertTrue(returned[0] && f.knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW,
                        "Miss returns to the queue without waiting indefinitely");
                helper.assertTrue(f.target.getHealth() == f.target.getMaxHealth(), "A missed target takes no damage");
                helper.succeed();
            } finally { f.close(); }
        });
    }
    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 70)
    public static void missedStabRetractsAtLaunchSpeedBeforeReturningToTheMovingOwner(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.spawnTarget();
        Vec3[] launch = {null};
        MomKnifePose[] pose = {null};
        Vec3[] previousRetraction = {null};
        boolean[] retracting = {false};
        boolean[] fullSpeed = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            MomKnifeEntity.State state = f.knife.getBehaviorState();
            if (state == MomKnifeEntity.State.THRUST && launch[0] == null) {
                launch[0] = f.knife.position();
                pose[0] = f.knife.getRenderPose(0);
                f.target.setPos(f.target.position().add(4, 0, 0));
            }
            if (state == MomKnifeEntity.State.RETRACT) {
                helper.assertTrue(pose[0].equals(f.knife.getRenderPose(0)), "Retraction keeps the launch orientation");
                if (!retracting[0]) {
                    helper.assertTrue(Math.abs(f.knife.position().distanceTo(launch[0]) - 6.5) < 1e-5,
                            "The missed shot completes its range before retracting");
                    helper.assertTrue(f.target.getHealth() == f.target.getMaxHealth(), "The missed target is unharmed");
                    f.target.discard();
                    f.owner.setPos(f.owner.position().add(3, 0, 0));
                    retracting[0] = true;
                } else {
                    Vec3 actual = f.knife.position().subtract(previousRetraction[0]);
                    Vec3 expected = pose[0].bladePoint(Vec3.ZERO, 1).scale(-1.25);
                    helper.assertTrue(actual.distanceTo(expected) < 1e-5, "Retraction reverses the full launch velocity");
                    fullSpeed[0] = true;
                }
                previousRetraction[0] = f.knife.position();
            }
            if (retracting[0] && state == MomKnifeEntity.State.RETURN) {
                try {
                    helper.assertTrue(fullSpeed[0], "Retraction advances at launch speed for multiple ticks");
                    helper.assertTrue(f.knife.position().equals(launch[0]),
                            "Slow return begins exactly at the original aim anchor, even if owner/target changed");
                    helper.assertTrue(f.knife.getRenderPose(0).equals(pose[0]), "Aim pose is restored before slow return");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
    }
    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void followReachesThirtyBlocksPerSecondAndVisibilityMatchesFlies(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 start = f.owner.position().add(8, 0, 0);
            f.knife.setPos(start);
            f.knife.tick();
            helper.assertTrue(Math.abs(f.knife.position().distanceTo(start) - 1.5) < 1e-6,
                    "Following caps at 1.5 blocks per tick, thirty blocks per second");
            helper.assertTrue(f.knife.shouldRenderAtSqrDistance(32 * 32)
                            && !f.knife.shouldRenderAtSqrDistance(32 * 32 + 0.01), "Render cutoff is exactly 32 blocks");
            helper.assertTrue(f.knife.getType().clientTrackingRange() == 3 && f.knife.getType().updateInterval() == 1,
                    "Tracking is three chunks and the network update interval remains one tick");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void followingPoseFacesTheOwnerFromEverySide(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (Vec3 offset : new Vec3[]{new Vec3(2, 1, 0), new Vec3(-2, 1, 0),
                    new Vec3(0, 1, 2), new Vec3(0, 1, -2)}) {
                f.knife.setPos(f.owner.position().add(offset));
                f.knife.tick();
                MomKnifePose pose = f.knife.getRenderPose(0);
                Vec3 desired = f.owner.position().subtract(f.knife.position()).multiply(1, 0, 1).normalize();
                helper.assertTrue(pose.pitch() == 0
                                && desired.distanceTo(Vec3.directionFromRotation(0, pose.yaw())) < 1e-4,
                        "Following keeps the tip down and heading toward the owner");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 30)
    public static void riseStartsAtTheFollowHeadingThenTurnsTowardTheLockedTarget(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.knife.setPos(f.owner.position().add(0.8, f.owner.getBbHeight() * 0.7, -0.95));
        f.spawnTarget();
        f.knife.tick();
        MomKnifePose start = f.knife.getRenderPose(0);
        Vec3 towardOwner = f.owner.position().subtract(f.knife.position()).multiply(1, 0, 1).normalize();
        helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.RISE
                        && towardOwner.distanceTo(Vec3.directionFromRotation(0, start.yaw())) < 1e-4,
                "Locking starts rise with the follow pose, without replacing it with the owner's yaw");
        helper.runAfterDelay(3, () -> {
            try {
                MomKnifePose raised = f.knife.getRenderPose(0);
                Vec3 ownerHeading = f.owner.position().subtract(f.knife.position())
                        .multiply(1, 0, 1).normalize();
                helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.AIM
                                && raised.equals(start)
                                && ownerHeading.distanceTo(Vec3.directionFromRotation(0, raised.yaw())) < 1e-4,
                        "Rise keeps the follow heading until the aiming phase starts");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 50)
    public static void repeatedContactDamagesOnlyOncePerFlightLeg(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 140);
        f.spawnTarget();
        f.target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        f.target.setHealth(200);
        float damage = (float) f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
        int[] forwardContacts = {0};
        int[] retractContacts = {0};
        boolean[] paused = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            MomKnifeEntity.State state = f.knife.getBehaviorState();
            if (state == MomKnifeEntity.State.THRUST || state == MomKnifeEntity.State.RETRACT) {
                Vec3 contact = f.knife.getRenderPose(0).bladePoint(f.knife.position(), 0.5);
                f.target.setPos(contact.add(0, -f.target.getBbHeight() * 0.5, 0));
                f.target.setDeltaMovement(Vec3.ZERO);
                if (state == MomKnifeEntity.State.THRUST) forwardContacts[0]++;
                else retractContacts[0]++;
            }
            if (state == MomKnifeEntity.State.PAUSE) {
                paused[0] = true;
                helper.assertTrue(forwardContacts[0] >= 3
                                && Math.abs(f.target.getHealth() - (200 - damage)) < 1e-5,
                        "Repeated forward contact causes exactly one successful damage application");
            }
            if (state == MomKnifeEntity.State.RETURN && paused[0]) {
                try {
                    helper.assertTrue(retractContacts[0] >= 3
                                    && Math.abs(f.target.getHealth() - (200 - damage * 2)) < 1e-5,
                            "Retraction gets one fresh hit, without repeating it on every contact tick");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
        helper.runAfterDelay(40, () -> {
            if (!f.closed) {
                try { helper.assertTrue(false, "The repeated-contact attack must complete"); }
                finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 50)
    public static void knivesKeepIndependentHitRecordsForTheSameVictim(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 180);
        f.data.addCount(MOM_KNIFE, 1);
        helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "The second knife is created");
        MomKnifeEntity second = (MomKnifeEntity) helper.getLevel().getEntity(
                f.data.getEntry(MOM_KNIFE).orElseThrow().getEntityId(1));
        helper.assertTrue(second != null, "The second knife is registered");
        f.spawnTarget();
        Vec3 victimPosition = f.target.position();
        f.target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        f.target.setHealth(200);
        float damage = (float) f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
        boolean[] firstReturned = {false};
        boolean[] secondReturned = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            f.target.setPos(victimPosition);
            f.target.setDeltaMovement(Vec3.ZERO);
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.RETURN) firstReturned[0] = true;
            if (second.getBehaviorState() == MomKnifeEntity.State.RETURN) secondReturned[0] = true;
            if (firstReturned[0] && secondReturned[0]) {
                try {
                    helper.assertTrue(Math.abs(f.target.getHealth() - (200 - damage * 4)) < 1e-5,
                            "Each of two knives may independently hit the same victim once per leg");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
        helper.runAfterDelay(40, () -> {
            if (!f.closed) {
                try { helper.assertTrue(false, "Both knives must complete their independent attacks"); }
                finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 40)
    public static void oneLayerFliesSixAndAHalfBlocksBeforePausing(GameTestHelper helper) {
        assertLayeredFlight(helper, 1, 6.5, 5.4);
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 40)
    public static void threeLayersFlySevenAndAHalfBlocksBeforePausing(GameTestHelper helper) {
        assertLayeredFlight(helper, 15, 7.5, 6.4);
    }

    private static void assertLayeredFlight(GameTestHelper helper, int knifeCount, double distance, double targetDistance) {
        Fixture f = new Fixture(helper);
        f.populateKnives(knifeCount);
        f.spawnTarget();
        f.target.setPos(f.owner.position().add(0, 0, -targetDistance));
        Vec3[] launch = {null};
        float health = f.target.getHealth();
        helper.onEachTick(() -> {
            if (f.closed) return;
            if (launch[0] == null && f.knife.getBehaviorState() == MomKnifeEntity.State.THRUST) {
                launch[0] = f.knife.position();
            }
            if (f.knife.getBehaviorState() == MomKnifeEntity.State.PAUSE) {
                try {
                    helper.assertTrue(launch[0] != null
                                    && Math.abs(f.knife.position().distanceTo(launch[0]) - distance) < 1e-5,
                            "The real flight completes the layer-dependent range before pausing");
                    helper.assertTrue(f.target.getHealth() < health,
                            "A stationary target near the detection boundary is reached during flight");
                    helper.succeed();
                } finally { f.close(); }
            }
        });
        helper.runAfterDelay(35, () -> {
            if (!f.closed) {
                try {
                    helper.assertTrue(false, "A detected boundary target must lead to a completed flight");
                } finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void queuedKnivesDoNotIncreaseThePopulatedFormationRange(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 300)) {
            f.data.setCount(MOM_KNIFE, 15);
            f.spawnTarget();
            f.target.setPos(f.owner.position().add(0, 0, -6.3));
            f.knife.tick();
            helper.assertTrue(f.knife.getBehaviorState() == MomKnifeEntity.State.FOLLOW,
                    "One live knife keeps one-layer targeting while fourteen knives are still queued");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement", timeoutTicks = 80)
    public static void thirdLayerKnifeCompletesFlightPauseAndRetraction(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 340);
        f.populateKnives(27);
        MomKnifeEntity outer = (MomKnifeEntity) helper.getLevel().getEntity(
                f.data.getEntry(MOM_KNIFE).orElseThrow().getEntityId(14));
        outer.setPos(f.owner.position().add(0, f.owner.getBbHeight() * 0.7, -2.05));
        f.spawnTarget();
        f.target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000000);
        f.target.setHealth(1000000);
        f.target.setPos(f.owner.position().add(0, (f.owner.getBbHeight() - f.target.getBbHeight()) * 0.5, -6.4));
        Vec3[] launch = {null};
        boolean[] paused = {false};
        boolean[] retracted = {false};
        helper.onEachTick(() -> {
            if (f.closed) return;
            try {
                helper.assertTrue(!outer.isRecallPending() && !outer.isRemoved(),
                        "A legitimate outer flight must not trigger distance recall");
                MomKnifeEntity.State state = outer.getBehaviorState();
                if (state == MomKnifeEntity.State.THRUST && launch[0] == null) launch[0] = outer.position();
                if (state == MomKnifeEntity.State.PAUSE) {
                    paused[0] = true;
                    helper.assertTrue(launch[0] != null && Math.abs(outer.position().distanceTo(launch[0]) - 7.5) < 1e-5,
                            "Three populated layers fly exactly seven and a half blocks");
                }
                if (state == MomKnifeEntity.State.RETRACT) retracted[0] = true;
                if (state == MomKnifeEntity.State.RETURN) {
                    helper.assertTrue(paused[0] && retracted[0]
                                    && launch[0] != null && outer.position().distanceToSqr(launch[0]) < 1e-8,
                            "The outer knife completes flight, pause and retraction back to its launch anchor");
                    helper.succeed();
                    f.close();
                }
            } catch (RuntimeException | Error failure) {
                f.close();
                throw failure;
            }
        });
        helper.runAfterDelay(70, () -> {
            if (!f.closed) {
                try {
                    helper.assertTrue(false, "The outer flight must complete; state=" + outer.getBehaviorState()
                            + ", target distance=" + f.target.getBoundingBox().getCenter()
                            .distanceTo(f.owner.getBoundingBox().getCenter()));
                }
                finally { f.close(); }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_movement")
    public static void thirdLayerSlotDoesNotRecallButTrueSeparationStillRecalls(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 380)) {
            f.populateKnives(27);
            MomKnifeEntity outer = (MomKnifeEntity) helper.getLevel().getEntity(
                    f.data.getEntry(MOM_KNIFE).orElseThrow().getEntityId(26));
            double angle = Math.toRadians(80);
            Vec3 slot = f.owner.position().add(Math.sin(angle) * 2.05, f.owner.getBbHeight() * 0.7,
                    -Math.cos(angle) * 2.05);
            outer.setPos(slot);
            outer.tick();
            helper.assertTrue(!outer.isRecallPending() && outer.position().distanceToSqr(slot) < 1e-8,
                    "The last slot of a full three-layer formation remains stable");
            outer.setPos(f.owner.position().add(80, 0, 0));
            outer.tick();
            helper.assertTrue(outer.isRecallPending(), "Real separation beyond the formation still queues recall");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final MomKnifeEntity knife;
        Cow target;
        boolean closed;

        Fixture(GameTestHelper helper) {
            this(helper, 100);
        }

        Fixture(GameTestHelper helper, double altitude) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "knife-test"));
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
            owner.setPos(anchor.getX() + 0.5, altitude, anchor.getZ() + 0.5);
            owner.setYRot(0);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            data.addCount(MOM_KNIFE, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Scheduler creates knife");
            knife = (MomKnifeEntity) helper.getLevel().getEntity(data.getEntry(MOM_KNIFE).orElseThrow().getEntityId(0));
            helper.assertTrue(knife != null, "Knife is registered");
            knife.setPos(owner.position().add(0, owner.getBbHeight() * 0.7, -0.95));
        }

        void populateKnives(int count) {
            data.setCount(MOM_KNIFE, count);
            while (data.getEntry(MOM_KNIFE).orElseThrow().getEntityCount() < count) {
                helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "A requested knife is spawned");
            }
        }

        void spawnTarget() {
            target = EntityType.COW.create(helper.getLevel());
            helper.assertTrue(target != null, "Target is created");
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(owner.position().add(0, 0, -2));
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "Target enters level");
        }

        @Override public void close() {
            closed = true;
            if (target != null) target.discard();
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            knife.discardSilently();
            owner.discard();
        }
    }
}
