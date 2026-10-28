package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PlayerMomsKnifeTrajectoryGameTests {
    private static final ResourceLocation SHIFT = ResourceLocation.parse("trajectory_test:knife_shift");
    private static final ResourceLocation OFFSET = ResourceLocation.parse("trajectory_test:knife_offset");

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ModTrajectoryModules.TRAJECTORY_MODULE_KEY, helper -> {
            helper.register(SHIFT, new Shift());
            helper.register(OFFSET, new Offset());
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void customReturningMotionStillDocksOnTheOriginalTickAndSynchronizesItsPose(GameTestHelper helper) {
        var f = fixture(helper);
        Vec3 origin = f.main.position();
        helper.assertTrue(f.main.launch(2, 1, context(f, SHIFT)), "A prepared knife trajectory launches");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(f.main.getX() > origin.x + 0.5, "The custom module changes the actual return knife position");
            helper.assertTrue(f.main.getRenderAnchor(f.owner, 0).distanceTo(f.main.position()) < 1e-5,
                    "Rendering reconstructs the actual trajectory endpoint");
            var copy = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                copy.setPos(f.main.position());
                f.main.writeSpawnData(buffer);
                copy.readSpawnData(buffer);
                helper.assertTrue(copy.getRenderAnchor(f.owner, 0).distanceTo(f.main.position()) < 1e-5,
                        "A late spawn carries the current correction atomically");
            } finally { buffer.release(); copy.discard(); }
        });
        helper.runAfterDelay(9, () -> {
            try {
                helper.assertTrue(!f.main.isInFlight() && f.main.position().distanceTo(origin) < 1e-5,
                        "The original eight-tick throw returns to its held anchor");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void compatibleOffsetsContinuePastTheApexInsteadOfExhaustingTheOutwardRange(GameTestHelper helper) {
        var f = fixture(helper);
        Vec3 origin = f.main.position();
        helper.assertTrue(f.main.launch(2, 1, context(f, OFFSET)), "A knife-specific offset launches");
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(f.main.isInFlight() && f.main.getTraveled() > 2,
                    "Return sampling continues after spending the nominal outward distance");
            helper.assertTrue(f.main.getZ() > origin.z + 1 && f.main.getY() > origin.y + 0.1,
                    "The offset remains on the returning blade");
        });
        helper.runAfterDelay(9, () -> {
            try {
                helper.assertTrue(!f.main.isInFlight() && f.main.position().distanceTo(origin) < 1e-5,
                        "The offset cannot prevent docking");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 30)
    public static void customDetachedMotionDamagesAlongItsActualSweepAndExpiresAtTwentyFourBlocks(GameTestHelper helper) {
        var f = fixture(helper);
        var knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
        Vec3 origin = f.main.position();
        var target = f.cow(origin.add(1, 0, 1.5));
        helper.assertTrue(knife.initializeOneWay(f.owner, origin, origin.add(0, 0, 1), 24, 1, context(f, SHIFT))
                && f.level.addFreshEntity(knife), "A detached knife retains its prepared trajectory");
        tickWhenNeeded(helper, f, knife);
        helper.runAfterDelay(3, () -> helper.assertTrue(target.getHealth() < 1000 && knife.getX() > origin.x + 1,
                "Entity contacts use the modified blade sweep"));
        helper.runAfterDelay(22, () -> {
            try {
                helper.assertTrue(knife.isRemoved() && Math.abs(knife.getTraveled() - 24) < 1e-6,
                        "The final transformed segment is clipped to exactly 24 blocks: " + knife.getTraveled());
                helper.succeed();
            } finally { knife.discard(); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", timeoutTicks = 20)
    public static void allExistingModulesKeepTheOrdinaryReturnCurveAndRemainUninitialized(GameTestHelper helper) {
        var f = fixture(helper);
        Vec3 origin = f.main.position();
        AttackContext context = context(f, null);
        for (String name : List.of("wiggle_worm", "ring_worm", "ouroboros_worm", "hook_worm", "gravity",
                "tiny_planet_bullet", "tiny_planet_laser", "my_reflection_bullet", "my_reflection_laser")) {
            context.addTrajectoryModule(ResourceLocation.parse("isaac_disaster:" + name), 1);
        }
        helper.assertTrue(f.main.launch(2, 1, context), "A knife can retain unrelated prepared specs safely");
        long launchedAt = f.level.getGameTime();
        helper.runAfterDelay(3, () -> {
            double expected = PlayerMomsKnifeMotion.distance(2, 1, f.level.getGameTime() - launchedAt);
            helper.assertTrue(f.main.position().distanceTo(origin.add(0, 0, expected)) < 1e-6,
                    "Existing trajectories do not alter the original curve");
            helper.assertTrue(f.main.getTrajectoryRuntime().states().isEmpty(), "No incompatible trajectory is initialized");
        });
        helper.runAfterDelay(9, () -> {
            try {
                helper.assertTrue(!f.main.isInFlight(), "Existing modules do not change the return tick");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty")
    public static void idleKnifeNeverEvaluatesEvenAnExplicitlyCompatibleModule(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.main.getTrajectoryRuntime().addSpec(new TrajectorySpec(SHIFT, 0));
            Vec3 origin = f.main.position();
            f.main.tick();
            helper.assertTrue(f.main.position().distanceTo(origin) < 1e-6
                            && f.main.getTrajectoryRuntime().states().isEmpty(),
                    "Idle and charging knives keep their held pose without trajectory execution");
        }
        helper.succeed();
    }

    private static AttackContext context(PlayerMomsKnifeGameTests.Fixture f, ResourceLocation module) {
        var result = AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get()).speed(1).build();
        if (module != null) result.addTrajectoryModule(module, 1);
        return result;
    }

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper) {
        // Keep these tests out of the existing fixture allocator and other tests' entity sections.
        var result = new PlayerMomsKnifeGameTests.Fixture(helper, 0);
        result.owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(0, 512, 0));
        result.main.initialize(result.owner);
        tickWhenNeeded(helper, result, result.main);
        return result;
    }

    private static void tickWhenNeeded(GameTestHelper helper, PlayerMomsKnifeGameTests.Fixture f, PlayerMomsKnifeEntity knife) {
        long start = f.level.getGameTime();
        helper.onEachTick(() -> {
            if (!f.closed && !knife.isRemoved() && knife.getTrajectoryAge() < f.level.getGameTime() - start) knife.tick();
        });
    }

    private static final class State implements TrajectoryState {
        public State copy() { return new State(); }
    }

    private static final class Shift extends TrajectoryModule<State> {
        public boolean appliesTo(IBulletObject bullet) { return ModAttackTypes.MOMS_KNIFE.getId().equals(bullet.getRootTypeId()); }
        public State createState() { return new State(); }
        protected Class<State> stateClass() { return State.class; }
        public void writeState(FriendlyByteBuf buffer, State state) {}
        public State readState(FriendlyByteBuf buffer) { return new State(); }
        protected TrajectoryMotion applyTyped(TrajectoryContext context, State state) {
            Vec3 velocity = context.input.baseVelocity().add(0.5, 0, 0);
            return TrajectoryMotion.relative(context.input.position().add(velocity), velocity, Vec3.ZERO, velocity.length());
        }
    }

    private static final class Offset extends OffsetTrajectoryModule<State> {
        public boolean appliesTo(IBulletObject bullet) { return ModAttackTypes.MOMS_KNIFE.getId().equals(bullet.getRootTypeId()); }
        public State createState() { return new State(); }
        protected Class<State> stateClass() { return State.class; }
        public void writeState(FriendlyByteBuf buffer, State state) {}
        public State readState(FriendlyByteBuf buffer) { return new State(); }
        protected Vec3 offsetTyped(TrajectoryKinematics frame, State state, int amplifier) { return new Vec3(0, 0.25, 0); }
    }
}
