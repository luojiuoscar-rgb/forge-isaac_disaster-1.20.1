package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.TinyPlanet;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipeline;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ReturningKnifeTrajectory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class TinyPlanetReturningMomsKnifeGameTests {
    private static final AtomicInteger NEXT_CASE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", timeoutTicks = 10)
    public static void movingAtAFractionalApexDoesNotCreateAFalseHighSpeedTrail(GameTestHelper helper) {
        Case c = new Case(helper, false);
        new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
        c.f.main.launch(0.7, 1, c.prepare(c.f.main.position(), 0.7, 1));
        c.after(1, () -> c.f.owner.setPos(c.f.owner.position().add(10, 0, 0)));
        c.after(2, () -> helper.assertTrue(!c.f.main.isFastMoving() && snapshot(c.f.main).trail().isEmpty(),
                "Exact sample times remove player translation even when the apex lies inside the tick"));
        c.after(3, c::succeed);
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 15)
    public static void widerOrbitAndHistoryHitGrazesButNotTheWholeDisk(GameTestHelper helper) {
        Case c = new Case(helper, false);
        var profile = (ReturningKnifeTrajectory) ModTrajectoryModules.TINY_PLANET_RETURNING_MOMS_KNIFE.get();
        var firstBlade = profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 4, 30, 1);
        Cow current = grazingCow(c, c.center().add(firstBlade.base().lerp(firstBlade.tip(), 0.5)), 0.2);
        c.f.main.launch(4, 1, c.prepare(c.f.main.position(), 4, 1));
        Cow[] historical = new Cow[1], outside = new Cow[1], interior = new Cow[1];
        c.after(1, () -> helper.assertTrue(current.getHealth() == 998,
                "The wider current blade hits a grazing enemy once, including its broad-phase search"));
        c.after(3, () -> {
            var trail = new PlayerMomsKnifeTrail();
            trail.append(snapshot(c.f.main).trail(), c.f.level.getGameTime(), 3);
            var blade = trail.sample(c.started + 1.5);
            Vec3 point = blade.base().lerp(blade.tip(), 0.5);
            historical[0] = grazingCow(c, point, 0.2);
            outside[0] = grazingCow(c, point, 0.32);
            interior[0] = grazingCow(c, c.center().add(0, 0, 0.5), 0.2);
            c.f.owner.setPos(c.f.owner.position().add(12, 0, 0));
        });
        c.after(4, () -> {
            helper.assertTrue(historical[0].getHealth() == 998,
                    "The same padding applies to old world-space paths without stacking sample damage");
            helper.assertTrue(outside[0].getHealth() == 1000, "Enemies beyond the new radius are still excluded");
            helper.assertTrue(interior[0].getHealth() == 1000, "The empty inside of the orbital ring is not a damage disk");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 15)
    public static void allKnifeModesShareTheWidthWhileDefaultSpeedHasNoTrail(GameTestHelper helper) {
        Case c = new Case(helper, false);
        Vec3 origin = c.f.main.position();
        Cow target = grazingCow(c, origin.add(0, 0, 0.4), 0.2);
        var oneWay = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
        helper.assertTrue(oneWay.initializeOneWay(c.f.owner, origin, origin.add(0, 0, 1), 0.1,
                c.prepare(origin, 24, 0.1)) && c.f.level.addFreshEntity(oneWay), "One-way control enters the world");
        c.drive(oneWay);
        AtomicInteger idleHits = new AtomicInteger(), oneWayHits = new AtomicInteger();
        c.listen((Consumer<LivingHurtEvent>) event -> {
            if (event.getEntity() != target) return;
            if (event.getSource().getDirectEntity() == c.f.main) idleHits.incrementAndGet();
            if (event.getSource().getDirectEntity() == oneWay) oneWayHits.incrementAndGet();
        });
        c.after(3, () -> {
            helper.assertTrue(idleHits.get() > 0 && oneWayHits.get() > 0,
                    "Idle and one-way knives both use the common quarter-block radius");
            helper.assertTrue(snapshot(c.f.main).trail().isEmpty() && snapshot(oneWay).trail().isEmpty(),
                    "Slow knives do not create a visual or damaging trail");
            new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
            c.f.main.launch(4, 1, c.prepare(origin, 4, 1));
        });
        c.after(4, () -> c.f.owner.setPos(c.f.owner.position().add(10, 0, 0)));
        c.after(5, () -> {
            helper.assertTrue(!c.f.main.isFastMoving() && !oneWay.isFastMoving()
                            && snapshot(c.f.main).trail().isEmpty() && snapshot(oneWay).trail().isEmpty(),
                    "Player translation does not turn a normal throw or an independent slow knife into a fast knife");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void aFastOneWayKnifeHasHistoryWithoutTinyPlanetAndFadesAfterSlowing(GameTestHelper helper) {
        Case c = new Case(helper, false);
        new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
        Vec3 origin = c.f.main.position();
        var fast = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
        helper.assertTrue(fast.initializeOneWay(c.f.owner, origin, origin.add(0, 0, 1), 2,
                c.prepare(origin, 24, 2)) && c.f.level.addFreshEntity(fast), "Fast knife enters the world");
        c.drive(fast);
        Cow[] target = new Cow[1];
        c.after(3, () -> {
            helper.assertTrue(fast.isFastMoving() && !snapshot(fast).trail().isEmpty(),
                    "Fast one-way motion creates history without any trajectory module");
            for (var sample : snapshot(fast).trail()) {
                helper.assertTrue(fast.getBoundingBoxForCulling().contains(sample.blade().base())
                                && fast.getBoundingBoxForCulling().contains(sample.blade().tip()),
                        "Visible historical blades must remain inside the one-way knife's culling bounds");
            }
            target[0] = grazingCow(c, origin.add(0, 0, 2.5), 0.2);
        });
        c.after(4, () -> {
            helper.assertTrue(target[0].getHealth() == 998, "A fast one-way knife's old path hits once at the common width");
            fast.setVelocity(new Vec3(0, 0, 1));
        });
        c.after(5, () -> helper.assertTrue(!fast.isFastMoving() && !snapshot(fast).trail().isEmpty(),
                "Old history survives the first slow tick"));
        c.after(8, () -> {
            helper.assertTrue(snapshot(fast).trail().isEmpty(), "History naturally expires three ticks after slowdown");
            helper.assertTrue(!fast.getBoundingBoxForCulling().contains(origin.add(0, 0, 2.5)),
                    "Expired history must not keep the culling bounds expanded");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 15)
    public static void aFastStraightReturningKnifeUsesTheSameHistoryAndCleansUpAtTheHand(GameTestHelper helper) {
        Case c = new Case(helper, false);
        new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
        c.f.main.launch(4, 3, c.prepare(c.f.main.position(), 4, 3));
        c.after(1, () -> helper.assertTrue(c.f.main.isFastMoving() && !snapshot(c.f.main).trail().isEmpty(),
                "Fast straight throws can create the same trail as orbital or one-way motion"));
        c.after(2, () -> helper.assertTrue(!c.f.main.isFastMoving() && !snapshot(c.f.main).trail().isEmpty(),
                "Deceleration stops new history but lets existing poses fade"));
        c.after(6, () -> {
            helper.assertTrue(!c.f.main.isInFlight() && !c.f.main.isFastMoving() && snapshot(c.f.main).trail().isEmpty(),
                    "Returning to the hand clears both the high-speed state and its damaging history");
            c.succeed();
        });
    }

    private static Cow grazingCow(Case c, Vec3 point, double gap) {
        Cow target = c.f.cow(point);
        target.setPos(target.getX(), point.y - gap - target.getBbHeight(), target.getZ());
        return target;
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 15)
    public static void separateKnivesStackHistoryDamageAndTeleportClearsBoth(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
        PlayerMomsKnifeEntity second = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
        second.initialize(c.f.owner, 1, Vec3.ZERO, new Vec3(0, 0, 1));
        helper.assertTrue(c.f.level.addFreshEntity(second), "The second real knife exists");
        c.f.ability.getRuntimeEntities().register(ModAttackTypes.MOMS_KNIFE.getId(), 1, second.getUUID());
        c.f.main.launch(4, 1, c.prepare(c.f.main.position(), 4, 1));
        second.launch(4, 1, c.prepare(second.position(), 4, 1));
        Cow[] target = new Cow[1];
        c.after(3, () -> {
            var trail = new PlayerMomsKnifeTrail();
            trail.append(snapshot(c.f.main).trail(), c.f.level.getGameTime(), 3);
            var blade = trail.sample(c.started + 1.5);
            target[0] = c.f.cow(blade.base().lerp(blade.tip(), 0.5));
            c.f.owner.setPos(c.f.owner.position().add(12, 0, 0));
        });
        c.after(4, () -> {
            helper.assertTrue(target[0].getHealth() == 996, "Each real knife independently hits once through its own history");
            c.f.owner.setPos(c.f.owner.position().add(40, 0, 0));
        });
        c.after(5, () -> {
            helper.assertTrue(target[0].getHealth() == 996, "Teleport cleanup prevents more historical damage");
            for (var knife : HeldMomsKnifeGroup.knives(c.f.owner)) {
                helper.assertTrue(snapshot(knife).trail().stream().allMatch(sample -> sample.time() >= c.started + 5),
                        "Teleporting drops all old poses without a bridge across the world");
            }
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void historicalWorldPathHurtsNewEntrantsThenExpires(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.main.launch(4, 1, c.prepare(c.f.main.position(), 4, 1));
        Cow[] target = new Cow[1];
        float[] expiredHealth = new float[1];
        c.after(3, () -> {
            var trail = new PlayerMomsKnifeTrail();
            trail.append(snapshot(c.f.main).trail(), c.f.level.getGameTime(), 3);
            var blade = trail.sample(c.started + 1.5);
            helper.assertTrue(blade != null, "Sub-tick world poses are retained in the spawn history");
            target[0] = c.f.cow(blade.base().lerp(blade.tip(), 0.5));
            c.f.owner.setPos(c.f.owner.position().add(12, 0, 0));
        });
        c.after(4, () -> helper.assertTrue(target[0].getHealth() == 998,
                "An enemy entering a historical path takes exactly one two-damage hit despite many sweep samples"));
        c.after(8, () -> {
            expiredHealth[0] = target[0].getHealth();
            helper.assertTrue(snapshot(c.f.main).trail().stream().allMatch(sample -> sample.time() >= c.started + 5),
                    "Only the most recent three ticks remain");
            helper.assertTrue(snapshot(c.f.main).trail().stream().allMatch(sample -> sample.blade().anchor().x > c.center().x - 9),
                    "Old world-space history is trimmed rather than dragged with the player");
        });
        c.after(11, () -> {
            helper.assertTrue(target[0].getHealth() == expiredHealth[0], "Expired history cannot keep damaging its old position");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 45)
    public static void trailNeverReplaysBlocksOrAddsTravelAndIsClearedOnReturn(GameTestHelper helper) {
        Case c = new Case(helper, true);
        BlockPos wall = BlockPos.containing(c.center().add(1, 0, 0));
        c.blocks.put(wall, c.f.level.getBlockState(wall));
        c.f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        AtomicInteger hits = new AtomicInteger();
        c.listen((Consumer<IsaacAttackHitBlockEvent>) event -> {
            if (event.getBulletObject() == c.f.main && event.getHitResult().getBlockPos().equals(wall)) hits.incrementAndGet();
        });
        c.f.main.launch(2, 1, c.prepare(c.f.main.position(), 2, 1));
        c.after(10, () -> {
            double expectedTravel = 0;
            var profile = (ReturningKnifeTrajectory) ModTrajectoryModules.TINY_PLANET_RETURNING_MOMS_KNIFE.get();
            for (int tick = 1; tick <= c.f.level.getGameTime() - c.started; tick++) {
                int age = tick;
                var path = PlayerMomsKnifeTrajectory.returningSamples(profile, age - 1, age, 15, 1, 0,
                        fraction -> profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 2, 15, age - 1 + fraction))
                        .stream().map(PlayerMomsKnifeTrail.Sample::blade).toList();
                expectedTravel += PlayerMomsKnifeTrajectory.distance(path);
            }
            helper.assertTrue(hits.get() == 1, "The active orbit and three ticks of history still publish each block once");
            helper.assertTrue(Math.abs(c.f.main.getTraveled() - expectedTravel) < 0.001,
                    "Damage history is not added to traveled distance: " + c.f.main.getTraveled() + " / " + expectedTravel);
        });
        c.after(20, () -> {
            helper.assertTrue(!c.f.main.isInFlight() && snapshot(c.f.main).trail().isEmpty(),
                    "Returning to the hand clears every historical damage pose");
            helper.assertTrue(c.f.main.launch(2, 1, c.prepare(c.f.main.position(), 2, 1))
                    && snapshot(c.f.main).trail().isEmpty(), "A new throw never inherits old history");
            c.f.main.discard();
            helper.assertTrue(snapshot(c.f.main).trail().isEmpty(), "Removal also clears history");
            c.succeed();
        });
    }

    private static PlayerMomsKnifeSpawnData snapshot(PlayerMomsKnifeEntity knife) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try { knife.writeSpawnData(buffer); return PlayerMomsKnifeSpawnData.read(buffer); }
        finally { buffer.release(); }
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 40)
    public static void realPreparationAffectsOnlyTheThrownReturningKnife(GameTestHelper helper) {
        Case c = new Case(helper, false);
        AttackContext prepared = c.prepare(c.f.main.position(), 2, 1);
        helper.assertTrue(prepared.getTrajectorySpecs().stream().anyMatch(spec ->
                spec.id().equals(ModTrajectoryModules.TINY_PLANET_RETURNING_MOMS_KNIFE.getId())),
                "Tiny Planet attaches through ordinary preparation");
        helper.assertTrue(!ModTrajectoryModules.TINY_PLANET_RETURNING_MOMS_KNIFE.get().appliesTo(c.f.main),
                "Idle and charging knives do not execute the orbit");
        var detached = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
        Vec3 start = c.f.main.position();
        helper.assertTrue(detached.initializeOneWay(c.f.owner, start, start.add(0, 0, 1), 1, prepared)
                && c.f.level.addFreshEntity(detached), "The same prepared specs can create a one-way knife");
        c.drive(detached);
        helper.assertTrue(c.f.main.launch(2, 1, prepared) && c.f.main.getFlightDuration() == 15,
                "The returning knife advances at the original radial progress per turn");
        c.after(3, () -> helper.assertTrue(detached.position().distanceTo(start.add(0, 0, 3)) < 1e-5
                        && detached.getTrajectoryRuntime().states().isEmpty(),
                "One-way flight remains straight and its returning module is never initialized"));
        c.after(9, () -> helper.assertTrue(c.f.main.isInFlight(), "The ordinary return tick no longer ends the orbit"));
        c.after(20, () -> {
            helper.assertTrue(!c.f.main.isInFlight(), "The orbit finishes after its fractional return clock");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 70)
    public static void expansionReachesTheOldRangeWhileHomingCannotDeflectIt(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.ability.setHoming(1);
        c.f.cow(c.center().add(2, 0, 2));
        c.f.main.launch(4, 1, c.prepare(c.f.main.position(), 4, 1));
        long launchedAt = c.f.level.getGameTime();
        c.after(9, () -> {
            helper.assertTrue(Math.abs(c.f.main.position().distanceTo(c.center()) - 3.96) < 1e-5,
                    "Quarter-flight expansion follows the slowed original curve");
            helper.assertTrue(c.f.main.getRenderAnchor(c.f.owner, 0).distanceTo(c.f.main.position()) < 1e-5,
                    "The analytic visual pose and collision pose agree with homing present");
        });
        c.after(15, () -> {
            var samples = snapshot(c.f.main).trail();
            double apex = launchedAt + 15;
            var before = samples.stream().filter(sample -> sample.time() < apex).reduce((a, b) -> b).orElseThrow().blade();
            var at = samples.stream().filter(sample -> sample.time() == apex).findFirst().orElseThrow().blade();
            helper.assertTrue(Math.abs(at.anchor().distanceTo(c.center()) - 4.6) < 1e-5,
                    "The apex reaches the original maximum radial extension");
            helper.assertTrue(before.tip().subtract(before.base()).normalize()
                            .dot(at.tip().subtract(at.base()).normalize()) < -0.99999,
                    "The server sweep records both sides of an instantaneous reversal");
        });
        c.after(31, () -> {
            helper.assertTrue(!c.f.main.isInFlight() && c.f.level.getGameTime() - launchedAt == 31,
                    "Homing does not change the thirty-tick flight duration");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 25)
    public static void movementVerticalViewAndLateSpawnUseTheSameAnalyticBlade(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.main.launch(4, 1, c.prepare(c.f.main.position(), 4, 1));
        c.after(4, () -> {
            c.f.owner.setPos(c.f.owner.position().add(3, 1, -2));
            c.f.owner.setYRot(75);
            c.f.owner.setXRot(-90);
        });
        c.after(5, () -> {
            var profile = (ReturningKnifeTrajectory) ModTrajectoryModules.TINY_PLANET_RETURNING_MOMS_KNIFE.get();
            var expected = profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 4, 30, 5);
            Vec3 axis = PlayerMomsKnifeMotion.toWorld(expected.tip().subtract(expected.base()), 75, -90);
            helper.assertTrue(c.f.main.getRenderDirection(c.f.owner, 0).dot(axis) > 0.99999
                            && c.f.main.getCenter().subtract(c.f.main.position()).normalize().dot(axis) > 0.99999,
                    "Player movement and turning do not override either the render or collision orbit direction");
        });
        c.after(7, () -> {
            Vec3 local = PlayerMomsKnifeMotion.offsetToLocal(c.f.main.position().subtract(c.center()), 75, -90);
            helper.assertTrue(Math.abs(local.y) < 1e-5, "The circle tilts into the player's current view plane");
            helper.assertTrue(c.f.main.getRenderAnchor(c.f.owner, 0).distanceTo(c.f.main.position()) < 1e-5,
                    "Moving and looking vertically do not leave the knife behind");
            var copy = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                copy.setPos(c.f.main.position());
                c.f.main.writeSpawnData(buffer);
                copy.readSpawnData(buffer);
                helper.assertTrue(copy.getFlightDuration() == 30
                                && copy.getRenderAnchor(c.f.owner, 0).distanceTo(c.f.main.position()) < 1e-5
                                && copy.getRenderDirection(c.f.owner, 0).distanceTo(c.f.main.getRenderDirection(c.f.owner, 0)) < 1e-5,
                        "A knife entering view halfway through an orbit carries its actual phase and profile");
            } finally { buffer.release(); copy.discard(); }
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void aShortThrowKeepsItsCadenceAndReportsWallsOnce(GameTestHelper helper) {
        Case c = new Case(helper, true);
        var target = c.f.cow(c.center().add(0, 0, -1));
        BlockPos wall = BlockPos.containing(c.center().add(0.9, 0, 0));
        c.blocks.put(wall, c.f.level.getBlockState(wall));
        c.f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        AtomicInteger hits = new AtomicInteger();
        Consumer<IsaacAttackHitBlockEvent> listener = event -> {
            if (event.getBulletObject() == c.f.main && event.getHitResult().getBlockPos().equals(wall)) hits.incrementAndGet();
        };
        c.listen(listener);
        Vec3 held = c.f.main.position();
        c.f.main.launch(0.05, 100, c.prepare(held, 0.05, 100));
        helper.assertTrue(c.f.main.getFlightDuration() == 5, "A short throw keeps the fixed four-turns-per-second cadence");
        c.after(8, () -> {
            helper.assertTrue(!c.f.main.isInFlight() && c.f.main.position().distanceTo(held) < 1e-5,
                    "A short orbit docks at its exact original slot");
            helper.assertTrue(target.getHealth() < 1000 && target.getHealth() >= 990,
                    "The complete orbit hits behind the player at most once per tick");
            helper.assertTrue(hits.get() == 1 && c.f.level.getBlockState(wall).is(Blocks.STONE),
                    "The outward and inward orbit shares one wall-contact record without breaking it");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 40)
    public static void removingTheItemAndHavingTwoCopiesDoNotChangeTheFrozenOrbit(GameTestHelper helper) {
        Case c = new Case(helper, false);
        new TinyPlanet(0, 0).handleObtain(c.f.owner, null);
        c.f.main.launch(2, 1, c.prepare(c.f.main.position(), 2, 1));
        helper.assertTrue(c.f.main.getFlightDuration() == 15, "Copies do not multiply orbit duration again");
        c.after(3, () -> {
            new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
            new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
            c.f.ability.setHoming(1);
        });
        c.after(9, () -> helper.assertTrue(c.f.main.isInFlight() && c.f.main.getFlightDuration() == 15,
                "Removing the source does not detach an already prepared orbit"));
        c.after(20, () -> {
            helper.assertTrue(!c.f.main.isInFlight(), "The frozen orbit still returns normally");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 40)
    public static void twoParallelKnivesOrbitTogetherThenAllowChargingAfterBothReturn(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
        c.f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(4);
        c.f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        c.f.ability.setChargeAmount(c.attack().getChargeBarId(), c.attack().getTotalCharge(c.f.owner));
        var contexts = c.attack().getAttackContexts(c.f.owner, 2).stream()
                .map(context -> context.toBuilder().range(4).speed(1).build()).toList();
        helper.assertTrue(AttackPipeline.executeRequest(AttackRequest.withContexts(c.f.owner, c.attack(),
                AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, false)),
                "Both parallel contexts pass real preparation and launch");
        c.after(5, () -> {
            var knives = HeldMomsKnifeGroup.knives(c.f.owner);
            helper.assertTrue(knives.size() == 2 && knives.stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "Both confirmed held knives start their orbit together");
            helper.assertTrue(Math.abs(knives.get(0).position().distanceTo(knives.get(1).position()) - 0.5) < 1e-5
                            && knives.stream().allMatch(knife -> Double.isFinite(knife.getRenderDirection(c.f.owner, 0).lengthSqr())),
                    "The rotating pair keeps its half-block separation and each knife faces its own motion tangent");
            helper.assertTrue(!c.attack().isChargeEligible(c.f.owner), "Any orbiting knife prevents charging");
        });
        c.after(20, () -> {
            c.f.ability.setHoldRightClick(true);
            c.attack().onTick(c.f.owner);
            helper.assertTrue(c.attack().isChargeEligible(c.f.owner)
                            && c.f.ability.getChargeAmount(c.attack().getChargeBarId()) > 0,
                    "Holding the button resumes charging after the whole group docks");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 95)
    public static void sixteenKnivesAndLokiProduceOnlyOneWaitingTimeBarrage(GameTestHelper helper) {
        Case c = new Case(helper, false);
        c.f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
        c.f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(16);
        c.f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        c.f.owner.getAttribute(Attributes.LUCK).setBaseValue(15);
        ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(c.f.owner, null);
        c.f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, c.f.owner);
        List<Long> shots = c.captureShots();
        long launched = c.f.level.getGameTime();
        c.fire();
        helper.assertTrue(HeldMomsKnifeGroup.knives(c.f.owner).size() == 19,
                "Loki's three additional slots remain outside the sixteen-knife cap");
        c.after(27, () -> helper.assertTrue(shots.isEmpty(), "The barrage waits for the expanding orbit to turn"));
        c.after(29, () -> helper.assertTrue(!shots.isEmpty() && shots.get(0) == launched + 28,
                "Exactly one first shot is emitted on the first server tick after the fractional apex: " + shots));
        c.after(45, () -> {
            helper.assertTrue(shots.size() == 16 && shots.get(15) == launched + 43,
                    "Nineteen held knives still produce one sixteen-shot burst, one knife per tick");
            helper.assertTrue(HeldMomsKnifeGroup.knives(c.f.owner).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "The burst does not shorten the fifty-five-tick group flight");
            c.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 25)
    public static void aNonIntegerOrdinaryTurnUsesTheActualRoundedWaitingTick(GameTestHelper helper) {
        Case c = new Case(helper, false);
        new TinyPlanet(0, 0).handleRemove(c.f.owner, null);
        List<Long> shots = c.captureShots();
        long launched = c.f.level.getGameTime();
        var plain = AttackContext.builder(c.f.owner, c.f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get()).speed(0.3).build();
        c.f.main.launch(1, 0.3, plain, true);
        c.after(6, () -> helper.assertTrue(shots.isEmpty(), "No burst before the fractional turnaround"));
        c.after(14, () -> {
            helper.assertTrue(shots.equals(List.of(launched + 7, launched + 8, launched + 9, launched + 10, launched + 11)),
                    "A seven-tick wait emits five shots without needing Tiny Planet: " + shots);
            c.succeed();
        });
    }

    private static final class Case implements AutoCloseable {
        final GameTestHelper helper;
        final PlayerMomsKnifeGameTests.Fixture f;
        final List<Object> listeners = new ArrayList<>();
        final Map<BlockPos, BlockState> blocks = new HashMap<>();
        final long started;

        Case(GameTestHelper helper, boolean needsBlocks) {
            this.helper = helper;
            f = new PlayerMomsKnifeGameTests.Fixture(helper, 0);
            f.owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(0,
                    needsBlocks ? 8 : 512 + NEXT_CASE.getAndIncrement() * 48, 0));
            f.main.initialize(f.owner);
            started = f.level.getGameTime();
            new TinyPlanet(0, 0).handleObtain(f.owner, null);
            helper.onEachTick(() -> {
                if (!f.closed) for (var knife : HeldMomsKnifeGroup.knives(f.owner)) tickIfNeeded(knife, started);
            });
        }

        Vec3 center() { return f.owner.getEyePosition().add(0, -0.15 * f.owner.getBbHeight(), 0); }

        MomsKnifeAttack attack() { return (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get(); }

        void fire() {
            f.ability.setChargeAmount(attack().getChargeBarId(), attack().getTotalCharge(f.owner));
            attack().onReleased(f.owner);
        }

        AttackContext prepare(Vec3 origin, double range, double speed) {
            var context = AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get())
                    .position(origin).mainAxis(f.owner.getLookAngle()).range(range).speed(speed).build();
            var capture = new CaptureKnife();
            helper.assertTrue(AttackPipeline.executeRequest(AttackRequest.withContexts(f.owner, capture,
                    AttackOrigin.ABILITY_EXTRA, AttackPipelineMode.PREPARE_AND_EXECUTE, List.of(context), false)),
                    "The ordinary preparation pipeline succeeds");
            return capture.context;
        }

        void tickIfNeeded(PlayerMomsKnifeEntity knife, long start) {
            if (!knife.isRemoved() && knife.getTrajectoryAge() < f.level.getGameTime() - start) knife.tick();
        }

        void drive(PlayerMomsKnifeEntity knife) {
            long start = f.level.getGameTime();
            helper.onEachTick(() -> { if (!f.closed) tickIfNeeded(knife, start); });
        }

        <E extends Event> void listen(Consumer<E> listener) {
            MinecraftForge.EVENT_BUS.addListener(listener);
            listeners.add(listener);
        }

        List<Long> captureShots() {
            List<Long> shots = new ArrayList<>();
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                        && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) shots.add(f.level.getGameTime());
            };
            listen(listener);
            return shots;
        }

        void after(int ticks, Runnable action) {
            helper.runAfterDelay(ticks, () -> {
                try { action.run(); }
                catch (RuntimeException | Error failure) { close(); throw failure; }
            });
        }

        void succeed() { close(); helper.succeed(); }

        public void close() {
            if (f.closed) return;
            listeners.forEach(MinecraftForge.EVENT_BUS::unregister);
            blocks.forEach(f.level::setBlockAndUpdate);
            f.close();
        }
    }

    private static final class CaptureKnife extends AttackType {
        AttackContext context;
        CaptureKnife() { super(0); }
        public ResourceLocation getId() { return ModAttackTypes.MOMS_KNIFE.getId(); }
        public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(); }
        public void performAttack(List<AttackContext> contexts, Consumer<AttackContext> fired) { context = contexts.get(0); }
        public void shoot(AttackContext context) {}
        public void makeSound(LivingEntity entity) {}
    }
}
