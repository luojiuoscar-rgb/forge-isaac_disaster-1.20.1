package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Brimstone;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.LongStream;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeBulletGameTests {
    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void theRearSpawnUsesTheCurrentViewEvenWhenHeadYawDiffers(GameTestHelper helper) {
        try (Case c = new Case(helper)) {
            for (float yaw : new float[]{0, 53, -90}) {
                for (float pitch : new float[]{-90, -23, 0, 90}) {
                    c.f.owner.setYRot(yaw);
                    c.f.owner.setYHeadRot(yaw + 60);
                    c.f.owner.setXRot(pitch);
                    Vec3 back = PlayerMomsKnifeMotion.barragePosition(c.f.owner).subtract(c.f.owner.getEyePosition());
                    helper.assertTrue(Math.abs(back.length() - 1) < 1e-9
                                    && back.distanceTo(c.f.owner.getLookAngle().normalize().scale(-1)) < 1e-9,
                            "Rear spawn remains one block behind the current view, independent of head interpolation");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void theFullBarrageBeginsOnTheExactIntegerTurnaroundTick(GameTestHelper helper) {
        Case c = new Case(helper);
        long start = c.f.level.getGameTime();
        List<Long> times = c.watchShots();
        c.launchAttack(12, 1);
        helper.runAfterDelay(5, () -> helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(c.f.owner.getUUID()).isEmpty(),
                "The burst effect creates no task until the main knife actually returns"));
        helper.runAfterDelay(11, () -> helper.assertTrue(times.isEmpty(), "No shot during the outward phase"));
        helper.runAfterDelay(25, () -> {
            try {
                helper.assertTrue(times.equals(LongStream.rangeClosed(start + 12, start + 21).boxed().toList()),
                        "Ten shots begin exactly when the main knife turns: " + times);
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void fractionalAndSubTickTurnaroundsEachCreateOneCorrectlyTimedBarrage(GameTestHelper helper) {
        Case fractional = new Case(helper);
        Case shortFlight = new Case(helper);
        long start = fractional.f.level.getGameTime();
        List<Long> first = fractional.watchShots();
        List<Long> second = shortFlight.watchShots();
        fractional.launchAttack(12, 1.1);
        shortFlight.launchAttack(12, 100);
        helper.runAfterDelay(24, () -> {
            try {
                helper.assertTrue(first.equals(LongStream.rangeClosed(start + 11, start + 20).boxed().toList()),
                        "The fractional apex is rounded up: " + first);
                helper.assertTrue(second.equals(LongStream.rangeClosed(start + 1, start + 10).boxed().toList()),
                        "A flight shorter than a tick creates one burst: " + second);
                helper.succeed();
            } finally { fractional.close(); shortFlight.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void aDirectBarrageKeepsItsCountAndSpeedSnapshot(GameTestHelper helper) {
        Case c = new Case(helper);
        List<Long> times = c.watchShots();
        long start = c.f.level.getGameTime();
        List<Double> speeds = new ArrayList<>();
        c.watchSpawns(event -> {
            if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == c.f.owner
                    && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) speeds.add(knife.getVelocity().length());
        });
        ExecutableEffectContext context = new ExecutableEffectContext(c.f.owner);
        context.set(ContextKeys.AMPLIFIER, 3.0);
        context.set(ContextKeys.ATTACK_CONTEXT, c.context(0.25));
        ModExecutableEffects.MOMS_KNIFE_BARRAGE.get().apply(context);
        context.set(ContextKeys.AMPLIFIER, 10.0);
        context.set(ContextKeys.ATTACK_CONTEXT, c.context(2));
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(times.equals(List.of(start, start + 1, start + 2)), "Direct execution starts in the current tick: " + times);
                helper.assertTrue(speeds.size() == 3 && speeds.stream().allMatch(speed -> Math.abs(speed - 0.25) < 1e-6),
                        "Later context changes do not alter the scheduled speed");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void removingBrimstoneBeforeTurnaroundKeepsTheLaunchSnapshot(GameTestHelper helper) {
        Case c = new Case(helper);
        long start = c.f.level.getGameTime();
        List<Long> times = c.watchShots();
        c.launchAttack(12, 1);
        helper.runAfterDelay(3, () -> {
            new Brimstone(0, 4).handleRemove(c.f.owner, null);
            c.f.ability.setChargeAmount(((MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get()).getChargeBarId(), 0);
        });
        helper.runAfterDelay(25, () -> {
            try {
                helper.assertTrue(times.equals(LongStream.rangeClosed(start + 12, start + 21).boxed().toList()),
                        "The launch snapshot still creates ten shots on return after Brimstone is removed: " + times);
                helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(c.f.owner.getUUID()).isEmpty(),
                        "The completed burst leaves no scheduled tasks");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 40)
    public static void controlCanBeGainedAndRemovedWithoutChangingSpeedOrTwentyFourBlockTravel(GameTestHelper helper) {
        Case c = new Case(helper);
        PlayerMomsKnifeEntity knife = c.f.oneWay(24, 1);
        Vec3 start = knife.position();
        Vec3[] locked = new Vec3[1];
        helper.runAfterDelay(2, () -> {
            c.f.owner.setYRot(90);
            c.f.ability.setControllable(1);
        });
        helper.runAfterDelay(7, () -> {
            helper.assertTrue(knife.getX() < start.x - 0.1, "Control turns the detached knife toward the new view");
            helper.assertTrue(Math.abs(knife.getVelocity().length() - 1) < 1e-6, "Control preserves speed");
            c.f.ability.setControllable(0);
            locked[0] = knife.getRenderDirection(c.f.owner, 1);
        });
        helper.runAfterDelay(10, () -> helper.assertTrue(knife.getRenderDirection(c.f.owner, 1).distanceTo(locked[0]) < 1e-6,
                "Removing control preserves the current heading"));
        helper.runAfterDelay(28, () -> {
            try {
                helper.assertTrue(knife.isRemoved() && Math.abs(knife.getTraveled() - 24) < 1e-6,
                        "The curved flight still consumes exactly 24 blocks");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void aValidHomingTargetWinsOverOwnerControl(GameTestHelper helper) {
        Case c = new Case(helper);
        Vec3 anchor = PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1);
        c.f.cow(anchor.add(2, 0, 2));
        c.f.ability.setHoming(1);
        c.f.ability.setControllable(1);
        PlayerMomsKnifeEntity knife = c.f.oneWay(24, 0.25);
        c.f.owner.setYRot(90);
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(knife.getX() > anchor.x + 0.1, "Homing turns right even though owner control points left");
                helper.assertTrue(Math.abs(knife.getVelocity().length() - 0.25) < 1e-6, "Homing also retains speed");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void aFastFinalSweepReportsAllWallsWithoutDestroyingOrSplitting(GameTestHelper helper) {
        Case c = new Case(helper);
        Vec3 origin = PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1);
        BlockPos base = BlockPos.containing(origin);
        List<BlockPos> walls = List.of(base.offset(0, 0, 1), base.offset(0, 0, 3), base.offset(0, 0, 6));
        for (BlockPos wall : walls) c.place(wall, Blocks.STONE.defaultBlockState());
        PlayerMomsKnifeEntity knife = c.f.oneWay(6, 10);
        List<BlockPos> reported = new ArrayList<>();
        c.watchBlocks(event -> { if (event.getBulletObject() == knife) reported.add(event.getHitResult().getBlockPos()); });
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(reported.equals(walls), "Every wall is reported in path order, including the final tip: " + reported);
                helper.assertTrue(knife.isRemoved() && knife.getHitBlockPositions().equals(Set.copyOf(walls)), "Hits survive range expiry");
                helper.assertTrue(knife.getLastBlockHit().getBlockPos().equals(walls.get(2)), "The last contact is recorded");
                helper.assertTrue(knife.getSplitTriggerCounts().getBlockHits() == 0 && knife.getSplitSequence().isEmpty(),
                        "Block contacts are not split trigger points");
                for (BlockPos wall : walls) helper.assertTrue(c.f.level.getBlockState(wall).is(Blocks.STONE), "No default block destruction");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 45)
    public static void aMainThrowReportsEachBlockOnceAndResetsOnlyOnTheNextThrow(GameTestHelper helper) {
        Case c = new Case(helper);
        BlockPos wall = BlockPos.containing(PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1)).offset(0, 0, 2);
        c.place(wall, Blocks.STONE.defaultBlockState());
        AtomicInteger hits = new AtomicInteger();
        c.watchBlocks(event -> { if (event.getBulletObject() == c.f.main) hits.incrementAndGet(); });
        helper.assertTrue(c.f.main.launch(4, 1), "The first main throw launches");
        helper.runAfterDelay(18, () -> {
            helper.assertTrue(hits.get() == 1, "The return does not report the same block again: " + hits);
            helper.assertTrue(c.f.main.launch(4, 1), "The next throw launches");
        });
        helper.runAfterDelay(36, () -> {
            try {
                helper.assertTrue(hits.get() == 2, "A new throw resets the contact memory: " + hits);
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void anIdleOrChargingBladeDoesNotDispatchBlockContacts(GameTestHelper helper) {
        Case c = new Case(helper);
        BlockPos wall = BlockPos.containing(PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1).add(0, 0, 0.4));
        c.place(wall, Blocks.STONE.defaultBlockState());
        AtomicInteger hits = new AtomicInteger();
        c.watchBlocks(event -> { if (event.getBulletObject() == c.f.main) hits.incrementAndGet(); });
        helper.runAfterDelay(2, () -> c.f.ability.setChargeAmount(ModAttackTypes.MOMS_KNIFE.getId(), 5));
        helper.runAfterDelay(6, () -> {
            try {
                helper.assertTrue(!c.f.main.isInFlight() && hits.get() == 0, "Idle and charging knives never emit block events");
                helper.assertTrue(c.f.main.getHitBlockPositions().isEmpty(), "Idle contact does not consume the next throw's memory");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void terraWorksWhileBounceSplitAndTrajectoryModulesAreExcluded(GameTestHelper helper) {
        Case c = new Case(helper);
        Vec3 origin = PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1);
        BlockPos wall = BlockPos.containing(origin).offset(0, 0, 2);
        c.place(wall, Blocks.STONE.defaultBlockState());
        CompositeTrigger triggers = new CompositeTrigger(List.of(
                new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.BREAK_BLOCK_AND_DROP),
                new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.BULLET_BOUNCE_ON_BLOCK),
                new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModExecutableEffects.BULLET_BOUNCE_ON_ENTITY)));
        AttackContext context = AttackContext.builder(c.f.owner, c.f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get())
                .trigger(triggers).speed(10).build();
        context.addSplitModule(ModSplitModules.CRICKETS_BODY.getId(), 1);
        context.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(), 1);
        PlayerMomsKnifeEntity knife = c.oneWay(origin, 6, 10, context);
        AtomicInteger splits = new AtomicInteger();
        Consumer<BulletSplitEvent> listener = event -> { if (event.getParent() == knife) splits.incrementAndGet(); };
        MinecraftForge.EVENT_BUS.addListener(listener);
        c.listeners.add(listener);
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(c.f.level.getBlockState(wall).isAir(), "The inherited Terra block trigger executes");
                helper.assertTrue(knife.isRemoved() && Math.abs(knife.getZ() - origin.z - 6) < 1e-6,
                        "Rubber Cement does not reflect the knife");
                helper.assertTrue(!knife.getTriggers().contains(ModExecutableEffects.BULLET_BOUNCE_ON_BLOCK.get())
                                && !knife.getTriggers().contains(ModExecutableEffects.BULLET_BOUNCE_ON_ENTITY.get()), "Both bounce effects are absent");
                helper.assertTrue(knife.getSplitSequence().isEmpty() && knife.getTrajectorySpecs().isEmpty() && splits.get() == 0,
                        "Neither split nor ordinary trajectory modules execute");
                helper.assertTrue(context.getTrigger().contains(ModExecutableEffects.BULLET_BOUNCE_ON_BLOCK.get())
                                && !context.getSplitSequence().isEmpty(), "The caller's context was not stripped");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void partialShapesAndWaterDoNotCreateFalseWallContacts(GameTestHelper helper) {
        Case c = new Case(helper);
        Vec3 idle = PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1);
        BlockPos base = BlockPos.containing(idle);
        c.place(base.offset(0, 0, 2), Blocks.OAK_SLAB.defaultBlockState());
        c.place(base.offset(0, 0, 3), Blocks.WATER.defaultBlockState());
        Vec3 origin = new Vec3(idle.x, base.getY() + 0.8, idle.z);
        PlayerMomsKnifeEntity knife = c.oneWay(origin, 5, 10, c.context(10));
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(knife.isRemoved() && knife.getHitBlockPositions().isEmpty(),
                        "The blade passes above the slab and through the empty water collision shape");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 15)
    public static void discardingDuringABlockCallbackStopsLaterCallbacksAndDamage(GameTestHelper helper) {
        Case c = new Case(helper);
        Vec3 origin = PlayerMomsKnifeMotion.idlePosition(c.f.owner, 1);
        BlockPos base = BlockPos.containing(origin);
        c.place(base.offset(0, 0, 1), Blocks.STONE.defaultBlockState());
        c.place(base.offset(0, 0, 3), Blocks.STONE.defaultBlockState());
        var target = c.f.cow(origin.add(0, 0, 4));
        PlayerMomsKnifeEntity knife = c.f.oneWay(6, 10);
        AtomicInteger hits = new AtomicInteger();
        c.watchBlocks(event -> {
            if (event.getBulletObject() == knife) {
                hits.incrementAndGet();
                knife.discard();
            }
        });
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(hits.get() == 1 && target.getHealth() == 1000, "No work continues after callback removal");
                helper.succeed();
            } finally { c.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void spawnStateAndBulletMetadataKeepTheRealOwnerDirectionAndDamage(GameTestHelper helper) {
        try (Case c = new Case(helper)) {
            c.f.owner.setYRot(135);
            c.f.owner.setXRot(-45);
            PlayerMomsKnifeEntity original = c.f.oneWay(24, 0.7);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            PlayerMomsKnifeEntity copy = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), c.f.level);
            try {
                copy.setPos(original.position());
                original.writeSpawnData(buffer);
                c.f.owner.setYRot(0);
                original.getOwner().getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3);
                copy.readSpawnData(buffer);
                IBulletObject bullet = original;
                helper.assertTrue(bullet.getDamage() == 6 && bullet.getTypeId().equals(ModAttackTypes.MOMS_KNIFE.getId())
                                && bullet.getRootTypeId().equals(bullet.getTypeId()), "The interface keeps knife identity and live panel damage");
                helper.assertTrue(copy.getOwner() == c.f.owner && copy.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY,
                        "The initial payload carries owner and mode");
                helper.assertTrue(copy.getRenderDirection(c.f.owner, 0).distanceTo(original.getRenderDirection(c.f.owner, 0)) < 1e-6
                                && Math.abs(copy.getRange() - 24) < 1e-6, "Initial direction and range are independent of the new owner view");
                helper.assertTrue(copy.yRotO == copy.getYRot() && copy.xRotO == copy.getXRot(), "The initial pose has no default-angle interpolation");
                for (SplitTriggerType type : SplitTriggerType.values()) original.recordSplitTrigger(type);
                helper.assertTrue(original.getSplitTriggerCounts().getBlockHits() == 0, "Explicit split recording is also disabled");
                var runtime = original.getTrajectoryRuntime();
                helper.assertTrue(runtime == original.getTrajectoryRuntime(), "The runtime getter never creates a transient copy");
                helper.succeed();
            } finally { buffer.release(); copy.discard(); }
        }
    }

    private static final class Case implements AutoCloseable {
        final PlayerMomsKnifeGameTests.Fixture f;
        final List<Object> listeners = new ArrayList<>();
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        Case(GameTestHelper helper) { f = new PlayerMomsKnifeGameTests.Fixture(helper); }

        AttackContext context(double speed) {
            return AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get()).speed(speed).build();
        }

        void launchAttack(double range, double speed) {
            f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
            MomsKnifeAttack attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.shoot(context(speed).toBuilder().range(range).build());
        }

        List<Long> watchShots() {
            List<Long> result = new ArrayList<>();
            watchSpawns(event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                        && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) result.add(f.level.getGameTime());
            });
            return result;
        }

        void watchSpawns(Consumer<EntityJoinLevelEvent> listener) {
            MinecraftForge.EVENT_BUS.addListener(listener);
            listeners.add(listener);
        }

        void watchBlocks(Consumer<IsaacAttackHitBlockEvent> listener) {
            MinecraftForge.EVENT_BUS.addListener(listener);
            listeners.add(listener);
        }

        void place(BlockPos pos, BlockState state) {
            blocks.putIfAbsent(pos, f.level.getBlockState(pos));
            f.level.setBlockAndUpdate(pos, state);
        }

        PlayerMomsKnifeEntity oneWay(Vec3 origin, double range, double speed, AttackContext context) {
            PlayerMomsKnifeEntity knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
            if (!knife.initializeOneWay(f.owner, origin, origin.add(0, 0, 1), range, speed, context)
                    || !f.level.addFreshEntity(knife)) throw new IllegalStateException("Failed to spawn a knife fixture");
            return knife;
        }

        @Override
        public void close() {
            for (Object listener : listeners) MinecraftForge.EVENT_BUS.unregister(listener);
            blocks.forEach(f.level::setBlockAndUpdate);
            f.close();
        }
    }
}
