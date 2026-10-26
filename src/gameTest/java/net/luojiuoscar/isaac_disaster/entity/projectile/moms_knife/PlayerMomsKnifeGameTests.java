package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsKnifeBarrage;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Brimstone;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeGameTests {
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 45)
    public static void attackLaunchQueuesOneBurstAndRejectsAnotherReleaseDuringFlight(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        MomsKnifeAttack attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        AttackContext context = AttackContext.builder(f.owner, f.owner).attackType(attack)
                .position(PlayerMomsKnifeMotion.idlePosition(f.owner, 1)).mainAxis(f.owner.getLookAngle())
                .range(64).speed(1).build();
        attack.shoot(context);
        attack.shoot(context);
        helper.runAfterDelay(15, () -> helper.assertTrue(f.knives().stream()
                .noneMatch(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY), "No barrage before the main knife turns"));
        helper.runAfterDelay(28, () -> {
            try {
                long detached = f.knives().stream().filter(knife -> knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).count();
                helper.assertTrue(detached == 10, "Only the successful throw creates a full-charge burst: " + detached);
                helper.assertTrue(f.ability.getAttackSelection().mainAttack() == attack, "Brimstone does not replace the knife attack");
                helper.assertTrue(f.main.isInFlight() && !attack.isChargeEligible(f.owner), "The main knife still gates charging");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void overlappingBurstsKeepTheMainSlotAndAllowChargingAfterReturn(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        helper.assertTrue(f.main.launch(0.1, 1), "A short main throw launches");
        f.barrage(10, 0.25);
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(!f.main.isInFlight(), "The main knife has already returned while its burst continues");
            helper.assertTrue(((MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get()).isChargeEligible(f.owner), "Charging is available");
            helper.assertTrue(f.main.launch(0.1, 1), "The next release launches without cancelling the old burst");
            f.barrage(10, 0.25);
        });
        helper.runAfterDelay(16, () -> {
            try {
                long detached = f.knives().stream().filter(knife -> knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).count();
                helper.assertTrue(detached == 20, "Both full-charge bursts complete: " + detached);
                helper.assertTrue(f.main.getUUID().equals(f.ability.getRuntimeEntities().getId(ModAttackTypes.MOMS_KNIFE.getId())),
                        "Detached knives never replace or unregister the main slot");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 30)
    public static void detachedFlightIgnoresLaterPlayerTurnsAndTravelsPastEightBlocks(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        PlayerMomsKnifeEntity knife = f.oneWay(12, 1);
        Vec3 start = knife.position();
        helper.runAfterDelay(3, () -> {
            f.owner.setYRot(90);
            f.owner.setPos(f.owner.position().add(3, 0, 0));
        });
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(!knife.isRemoved() && knife.getZ() - start.z > 8, "Detached knife is not capped at eight blocks");
            helper.assertTrue(Math.abs(knife.getX() - start.x) < 1e-6, "Turning and moving do not drag a detached knife");
        });
        helper.runAfterDelay(16, () -> {
            try {
                helper.assertTrue(knife.isRemoved(), "The detached knife expires instead of returning");
                helper.assertTrue(Math.abs(knife.position().distanceTo(start) - 12) < 1e-5, "The final movement is clipped to range");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void twoKnivesStackDamageEveryTickThroughVanillaHurtCooldown(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.4));
        f.oneWay(16, 0.01);
        float[] before = new float[1];
        helper.runAfterDelay(3, () -> before[0] = f.targets.get(0).getHealth());
        helper.runAfterDelay(6, () -> {
            try {
                float lost = before[0] - f.targets.get(0).getHealth();
                helper.assertTrue(Math.abs(lost - 12) < 1e-4, "Two knives each deal two damage over each of three ticks: " + lost);
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void homingPreservesTheRadialCurveAndExactReturnTime(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.setHoming(1);
        f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(2, 0, 3));
        helper.assertTrue(f.main.launch(6, 1), "The main knife launches");
        long launched = f.level.getGameTime();
        AtomicBoolean deflected = new AtomicBoolean();
        helper.onEachTick(() -> {
            if (f.closed) return;
            try {
                f.main.tick();
                long age = f.level.getGameTime() - launched;
                double expected = PlayerMomsKnifeMotion.distance(6, 1, age);
                Vec3 idle = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
                helper.assertTrue(Math.abs(f.main.position().distanceTo(idle) - expected) < 1e-5,
                        "Tracking keeps the original radial curve at age " + age);
                if (age > 0 && Math.abs(f.main.getX() - idle.x) > 0.05) deflected.set(true);
                if (age >= 24) {
                    helper.assertTrue(deflected.get(), "The main knife actually turned toward the target");
                    helper.assertTrue(!f.main.isInFlight(), "Homing does not extend the 24-tick return time");
                    helper.succeed();
                    f.close();
                }
            } catch (RuntimeException failure) {
                f.close();
                throw failure;
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 25)
    public static void removingBrimstoneDuringABurstLetsAllRemainingKnivesFire(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        f.barrage(10, 0.25);
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        int[] emitted = new int[1];
        helper.runAfterDelay(3, () -> {
            emitted[0] = f.knives().size();
            new Brimstone(0, 4).handleRemove(f.owner, null);
        });
        helper.runAfterDelay(14, () -> {
            try {
                helper.assertTrue(emitted[0] > 1 && f.knives().size() == 11,
                        "Removing Brimstone leaves all ten scheduled shots intact");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void theFinalClippedSweepDamagesThroughAnUnbrokenWall(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        Vec3 anchor = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
        Cow target = f.cow(anchor.add(0, 0, 2.8));
        BlockPos wall = BlockPos.containing(anchor.add(0, 0, 1));
        BlockState original = f.level.getBlockState(wall);
        f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        PlayerMomsKnifeEntity knife = f.oneWay(2, 10);
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(knife.isRemoved(), "A sub-tick one-way flight ends");
                helper.assertTrue(Math.abs(knife.position().distanceTo(anchor) - 2) < 1e-5, "There is no endpoint overshoot");
                helper.assertTrue(target.getHealth() == 998, "The final blade sweep still damages once");
                helper.assertTrue(f.level.getBlockState(wall).is(Blocks.STONE), "The wall is not destroyed");
                helper.succeed();
            } finally {
                f.level.setBlockAndUpdate(wall, original);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void targetLossDistanceAndClearingTrackingDoNotRetainInvalidTargets(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            PlayerMomsKnifeTracking tracking = new PlayerMomsKnifeTracking();
            Vec3 anchor = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
            Cow first = f.cow(anchor.add(0, 0, 2));
            helper.assertTrue(tracking.targetPoint(f.owner, anchor, PlayerMomsKnifeMotion.FORWARD, 100) != null,
                    "A valid nearby target is selected");
            first.discard();
            helper.assertTrue(tracking.targetPoint(f.owner, anchor, PlayerMomsKnifeMotion.FORWARD, 101) == null,
                    "A removed target is immediately dropped");
            Cow second = f.cow(anchor.add(0, 0, 5));
            Vec3 unexpected = tracking.targetPoint(f.owner, anchor, PlayerMomsKnifeMotion.FORWARD, 104);
            helper.assertTrue(unexpected == null,
                    "A target beyond four blocks is excluded; anchor=" + anchor + ", unexpected=" + unexpected);
            second.setPos(anchor.add(0, -0.45, 1));
            helper.assertTrue(tracking.targetPoint(f.owner, anchor, PlayerMomsKnifeMotion.FORWARD, 108) != null,
                    "A target entering range is acquired on a later scan");
            tracking.clear();
            second.setInvulnerable(true);
            helper.assertTrue(tracking.targetPoint(f.owner, anchor, PlayerMomsKnifeMotion.FORWARD, 109) == null,
                    "Clearing tracking does not retain the old invulnerable target");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void switchingAwayRemovesBothModesWithoutLosingTheMainSlotEarly(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            PlayerMomsKnifeEntity detached = f.oneWay(32, 1);
            detached.discard();
            helper.assertTrue(f.main.getUUID().equals(f.ability.getRuntimeEntities().getId(ModAttackTypes.MOMS_KNIFE.getId())),
                    "Removing a detached knife leaves the main slot intact");
            detached = f.oneWay(32, 1);
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            f.main.tick();
            detached.tick();
            helper.assertTrue(f.main.isRemoved() && detached.isRemoved(), "Both modes enforce holding eligibility");
            helper.assertTrue(f.ability.getRuntimeEntities().getId(ModAttackTypes.MOMS_KNIFE.getId()) == null,
                    "Removing the main knife unregisters its slot");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 30)
    public static void detachedHomingAndLosingHomingKeepSpeedAndTotalTravel(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.setHoming(1);
        Vec3 start = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
        f.cow(start.add(2, 0, 2));
        PlayerMomsKnifeEntity knife = f.oneWay(6, 0.5);
        Vec3[] previous = {start};
        double[] traveled = {0};
        AtomicBoolean deflected = new AtomicBoolean();
        long launched = f.level.getGameTime();
        helper.onEachTick(() -> {
            if (f.closed) return;
            try {
                Vec3 current = knife.position();
                double step = current.distanceTo(previous[0]);
                if (step > 1e-8) {
                    helper.assertTrue(Math.abs(step - 0.5) < 1e-5, "Homing never slows a detached knife: " + step);
                    traveled[0] += step;
                    if (Math.abs(current.x - start.x) > 0.05) deflected.set(true);
                }
                previous[0] = current;
                long age = f.level.getGameTime() - launched;
                if (age >= 4) f.ability.setHoming(0);
                if (age >= 16) {
                    helper.assertTrue(deflected.get(), "The detached knife actually tracked before the ability was removed");
                    helper.assertTrue(knife.isRemoved() && Math.abs(traveled[0] - 6) < 1e-5,
                            "Losing homing neither resets range nor extends travel: " + traveled[0]);
                    helper.succeed();
                    f.close();
                }
            } catch (RuntimeException failure) {
                f.close();
                throw failure;
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void verticalDetachedKnivesKeepTheirOwnPoseWhenThePlayerTurns(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.setYRot(35);
            f.owner.setXRot(-90);
            PlayerMomsKnifeEntity knife = f.oneWay(8, 1);
            f.owner.setYRot(135);
            knife.tick();
            helper.assertTrue(Math.abs(knife.getYRot() - 35) < 1e-4, "A vertical detached knife keeps its launch heading");
            helper.assertTrue(knife.getXRot() == -90, "The knife still points straight up");
            helper.assertTrue(knife.shouldRenderAtSqrDistance(100 * 100), "The small hitbox does not impose an eight-block display limit");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void aimFindsTheNearestEligibleIntersectionThroughWalls(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 eye = f.owner.getEyePosition();
            Vec3 end = eye.add(0, 0, 24);
            Cow far = f.cow(eye.add(0, 0, 18));
            Cow near = f.cow(eye.add(0, 0, 6));
            Cow friendly = f.cow(eye.add(0, 0, 3));
            PlayerTeam team = f.level.getScoreboard().addPlayerTeam("knife-" + f.owner.getId());
            f.level.getScoreboard().addPlayerToTeam(f.owner.getScoreboardName(), team);
            f.level.getScoreboard().addPlayerToTeam(friendly.getScoreboardName(), team);
            BlockPos wall = BlockPos.containing(eye.add(0, 0, 2));
            BlockState original = f.level.getBlockState(wall);
            f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            try {
                Vec3 expected = near.getBoundingBox().clip(eye, end).orElseThrow();
                helper.assertTrue(PlayerMomsKnifeAim.point(f.owner).distanceTo(expected) < 1e-6,
                        "The nearest enemy surface wins despite a wall and a closer ally");
                near.discard();
                expected = far.getBoundingBox().clip(eye, end).orElseThrow();
                helper.assertTrue(PlayerMomsKnifeAim.point(f.owner).distanceTo(expected) < 1e-6,
                        "Removing the first enemy reveals the farther enemy");
                far.setInvulnerable(true);
                f.cow(eye.add(0, 0, 25));
                helper.assertTrue(PlayerMomsKnifeAim.point(f.owner).distanceTo(eye.add(0, 0, 12)) < 1e-6,
                        "Invulnerable, friendly and beyond-range mobs do not replace the fallback");
            } finally {
                f.level.setBlockAndUpdate(wall, original);
                f.level.getScoreboard().removePlayerTeam(team);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void aimFallbackRemainsTwelveBlocksFromTheEyeAtVerticalAngles(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (float pitch : new float[]{-90, -35, 0, 35, 90}) {
                f.owner.setYRot(37);
                f.owner.setXRot(pitch);
                Vec3 expected = f.owner.getEyePosition().add(f.owner.getLookAngle().normalize().scale(12));
                Vec3 aim = PlayerMomsKnifeAim.point(f.owner);
                helper.assertTrue(aim.distanceTo(expected) < 1e-6, "Fallback is eye-relative at pitch " + pitch);
                Vec3 origin = PlayerMomsKnifeMotion.scatterOrigin(PlayerMomsKnifeMotion.idlePosition(f.owner, 1),
                        37, pitch, 1, -1);
                PlayerMomsKnifeEntity knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
                helper.assertTrue(knife.initializeOneWay(f.owner, origin, aim, 24, 1), "A scattered knife initializes");
                helper.assertTrue(knife.getRenderDirection(f.owner, 1).distanceTo(aim.subtract(origin).normalize()) < 1e-6,
                        "The scattered knife converges onto the eye ray rather than its own parallel ray");
                knife.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 60)
    public static void barrageScattersEachOriginAndConvergesBeforeExpiringAtTwentyFourBlocks(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        List<ScatteredShot> shots = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                    && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) {
                shots.add(new ScatteredShot(knife, knife.position(), knife.getRenderDirection(f.owner, 1),
                        knife.getLookAngle(), PlayerMomsKnifeAim.point(f.owner),
                        f.owner.getEyePosition().subtract(f.owner.getLookAngle().normalize()),
                        f.owner.getYRot(), f.owner.getXRot()));
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        f.barrage(10, 1.7);
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(3, () -> {
            f.owner.setPos(f.owner.position().add(2, 0, 0));
            f.owner.setYRot(53);
            f.owner.setXRot(-23);
        });
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(shots.size() == 10, "A full burst was observed");
                boolean beforeTurn = false;
                boolean afterTurn = false;
                boolean offset = false;
                for (ScatteredShot shot : shots) {
                    Vec3 displacement = shot.origin().subtract(shot.center());
                    Vec3 right = PlayerMomsKnifeMotion.toWorld(new Vec3(1, 0, 0), shot.yaw(), shot.pitch());
                    Vec3 up = PlayerMomsKnifeMotion.toWorld(new Vec3(0, 1, 0), shot.yaw(), shot.pitch());
                    Vec3 forward = PlayerMomsKnifeMotion.toWorld(PlayerMomsKnifeMotion.FORWARD, shot.yaw(), shot.pitch());
                    helper.assertTrue(Math.abs(displacement.dot(right)) <= 1 + 1e-6
                                    && Math.abs(displacement.dot(up)) <= 1 + 1e-6
                                    && Math.abs(displacement.dot(forward)) < 1e-6,
                            "Every origin lies in the current perpendicular square; forward=" + displacement.dot(forward)
                                    + ", x=" + displacement.dot(right) + ", y=" + displacement.dot(up)
                                    + ", yaw=" + shot.yaw() + ", pitch=" + shot.pitch());
                    Vec3 expectedDirection = shot.aim().subtract(shot.origin()).normalize();
                    helper.assertTrue(shot.direction().distanceTo(expectedDirection) < 1e-6,
                            "Each shot aims from its own origin at the current eye-ray point");
                    helper.assertTrue(shot.facing().distanceTo(expectedDirection) < 1e-3,
                            "The model faces the actual launch direction");
                    helper.assertTrue(shot.knife().isRemoved()
                                    && Math.abs(shot.knife().position().distanceTo(shot.origin()) - 24) < 1e-5,
                            "Each knife travels exactly 24 blocks including its clipped final step");
                    beforeTurn |= shot.yaw() == 0;
                    afterTurn |= shot.yaw() == 53;
                    offset |= displacement.lengthSqr() > 1e-8;
                }
                helper.assertTrue(beforeTurn && afterTurn && offset, "Later shots use the moved, turned player's new anchor");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void aOneTickTapThrowsTheMainKnifeAndTwoExtraKnives(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        MomsKnifeAttack attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        helper.assertTrue(attack.getTotalCharge(f.owner) > 10, "The normal charge time exceeds ten ticks");
        f.ability.setChargeAmount(attack.getChargeBarId(), 1);
        attack.shoot(AttackContext.builder(f.owner, f.owner).attackType(attack)
                .position(PlayerMomsKnifeMotion.idlePosition(f.owner, 1)).mainAxis(f.owner.getLookAngle())
                .range(64).speed(0.25).build());
        helper.assertTrue(f.main.isInFlight(), "A tap still launches the main knife");
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).count() == 2,
                        "A tap has exactly two extra knives");
                helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "The two-shot barrage has finished scheduling");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void zeroCountsAreSafeNoOpsAndMissingSpeedSourcesAreRejected(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            MomsKnifeBarrage effect = (MomsKnifeBarrage) ModExecutableEffects.MOMS_KNIFE_BARRAGE.get();
            ExecutableEffectContext context = new ExecutableEffectContext(f.owner);
            for (double count : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
                context.set(ContextKeys.AMPLIFIER, count);
                helper.assertTrue(effect.applyEffect(context), "A nonpositive or nonfinite count is a safe no-op");
            }
            helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "Zero shots create no task");
            context.set(ContextKeys.AMPLIFIER, 3.0);
            helper.assertTrue(!effect.applyEffect(context), "Positive shots need a speed source");
            helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "Missing inputs create no task");
            AttackContext normalized = AttackContext.builder(f.owner, f.owner).speed(0).build();
            helper.assertTrue(normalized.getBulletSpeed() > 0, "AttackContext normalizes zero speed before effects read it");
            context.set(ContextKeys.ATTACK_CONTEXT, normalized);
            helper.assertTrue(effect.applyEffect(context), "An already normalized speed remains valid");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void anExplicitCountWorksWithoutBrimstoneAndStartsOnTheInvocationTick(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        long launchedAt = f.level.getGameTime();
        List<Long> emittedAt = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                    && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) emittedAt.add(f.level.getGameTime());
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        f.barrage(3, 0.25);
        helper.runAfterDelay(7, () -> {
            try {
                helper.assertTrue(emittedAt.equals(List.of(launchedAt, launchedAt + 1, launchedAt + 2)),
                        "A reusable effect emits exactly its explicit count on consecutive ticks: " + emittedAt);
                helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "Completed tasks leave no index");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void removingBrimstoneBeforeTheFirstShotStillCompletesTheBurst(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        f.barrage(10, 0.25);
        new Brimstone(0, 4).handleRemove(f.owner, null);
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(f.knives().size() == 11, "Removing Brimstone does not cancel pending shots");
                helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "A completed burst leaves no task index");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void switchingAwayStopsTheScheduledBurstAndRemovesItsIndex(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        f.barrage(10, 0.25);
        helper.runAfterDelay(3, () -> f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY));
        helper.runAfterDelay(8, () -> {
            try {
                helper.assertTrue(f.knives().isEmpty(), "Main and detached knives clean themselves up");
                helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(), "Invalid ownership stops the burst");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    private record ScatteredShot(PlayerMomsKnifeEntity knife, Vec3 origin, Vec3 direction, Vec3 facing,
                                 Vec3 aim, Vec3 center, float yaw, float pitch) {}

    static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final PlayerAbility ability;
        final PlayerMomsKnifeEntity main;
        final List<Cow> targets = new ArrayList<>();
        boolean closed;

        Fixture(GameTestHelper helper) {
            this(helper, NEXT_FIXTURE.getAndIncrement());
        }

        Fixture(GameTestHelper helper, int fixture) {
            level = helper.getLevel();
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "pk-" + id.toString().substring(0, 8)));
            BlockPos spawn = level.getSharedSpawnPos();
            owner.setPos(spawn.getX() + 0.5 + fixture / 10 * 32, 80 + fixture % 10 * 24, spawn.getZ() + 0.5);
            owner.setNoGravity(true);
            owner.setYRot(0);
            owner.setXRot(0);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
            level.addNewPlayer(owner);
            ability = owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
            ability.init();
            ability.addAttackType(ModAttackTypes.MOMS_KNIFE.getId(), 1, owner);
            main = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), level);
            main.initialize(owner);
            helper.assertTrue(level.addFreshEntity(main), "The main knife enters the world");
            ability.getRuntimeEntities().register(ModAttackTypes.MOMS_KNIFE.getId(), main.getUUID());
        }

        void barrage(int count, double speed) {
            ExecutableEffectContext context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.AMPLIFIER, (double) count);
            context.set(ContextKeys.ATTACK_CONTEXT, AttackContext.builder(owner, owner).speed(speed).build());
            if (!((MomsKnifeBarrage) ModExecutableEffects.MOMS_KNIFE_BARRAGE.get()).applyEffect(context)) {
                throw new IllegalStateException("Barrage fixture creation failed");
            }
        }

        PlayerMomsKnifeEntity oneWay(double range, double speed) {
            PlayerMomsKnifeEntity knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), level);
            if (!knife.initializeOneWay(owner, range, speed) || !level.addFreshEntity(knife)) {
                throw new IllegalStateException("One-way fixture creation failed");
            }
            return knife;
        }

        Cow cow(Vec3 anchor) {
            Cow cow = EntityType.COW.create(level);
            if (cow == null) throw new IllegalStateException("Cow fixture creation failed");
            cow.setNoAi(true);
            cow.setNoGravity(true);
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
            cow.setHealth(1000);
            cow.setPos(anchor.add(0, -0.45, 0));
            if (!level.addFreshEntity(cow)) throw new IllegalStateException("Cow fixture spawn failed");
            targets.add(cow);
            return cow;
        }

        List<PlayerMomsKnifeEntity> knives() {
            return level.getEntitiesOfClass(PlayerMomsKnifeEntity.class, new AABB(owner.position(), owner.position()).inflate(64),
                    knife -> knife.getOwner() == owner);
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            ScheduledFuncHelper.clearByType(MomsKnifeBarrage.SCHEDULE_TYPE, owner.getUUID());
            for (PlayerMomsKnifeEntity knife : knives()) knife.discard();
            for (Cow cow : targets) cow.discard();
            main.discard();
            owner.discard();
        }
    }
}
