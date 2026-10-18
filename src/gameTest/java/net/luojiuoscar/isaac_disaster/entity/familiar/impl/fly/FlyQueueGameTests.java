package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FlyQueueGameTests {
    private static final ResourceLocation BLUE_FLY = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "fly_queue")
    public static void targetQualificationIsSharedByAllSixtyFourFlies(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            List<QueuedFly> flies = new ArrayList<>();
            for (int i = 0; i < 64; i++) flies.add(f.fly(i, 0));
            CountingHusk target = new CountingHusk(helper);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(f.owner.position().add(6, 0, 0));
            f.entities.add(target);
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "The counting hostile enters the level");
            target.relationChecks = 0;
            FlyOwnerFrame frame = FlyOwnerFrame.get(f.owner);
            long tick = helper.getLevel().getGameTime();
            frame.refresh(f.owner, tick);
            int scanned = target.relationChecks;
            helper.assertTrue(scanned > 0, "The shared scan really checks hostility");
            for (QueuedFly fly : flies) frame.attackTarget(f.owner, fly, tick);
            helper.assertTrue(target.relationChecks == scanned,
                    "Sixty-four decisions reuse the scan's target qualification in the same tick");
            frame.refresh(f.owner, ++tick);
            for (QueuedFly fly : flies) frame.attackTarget(f.owner, fly, tick);
            helper.assertTrue(target.relationChecks == scanned + 1,
                    "The next tick refreshes hostility once for the entire owner, not once per fly");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_queue")
    public static void recalledAttackerLeavesItsOldTrailWithoutLosingItsRosterSlot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            QueuedFly fly = f.fly(0, 0);
            f.hostile(6);
            fly.tickFamiliar();
            helper.assertTrue(fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.CHASE, "The fly starts attacking");
            fly.setPos(f.owner.position().add(17, 0, 0));
            fly.tick();
            helper.assertTrue(fly.isRecallPending() && !fly.isRemoved(), "An attacker can wait for recall");
            FamiliarHelper.tickFamiliarAdmissions(f.owner);
            helper.assertTrue(!fly.isRecallPending() && fly.distanceToSqr(f.owner) < 4
                            && fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.RETURN,
                    "Recall resets the old attack before the next behavior update");
            helper.assertTrue(FlyOwnerFrame.get(f.owner).attackTarget(f.owner, fly,
                            helper.getLevel().getGameTime()) == null,
                    "The recalled fly no longer participates in its old attack trail");
            helper.assertTrue(f.data.containsEntity(BLUE_FLY, fly.getUUID()) && f.data.getCount(BLUE_FLY) == 1,
                    "Teleport retains the exact roster UUID and requirement");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_queue")
    public static void nearestFlyLeadsThenRecruitmentWalksTheRingOncePerTickAndSkipsRoaming(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            List<QueuedFly> flies = new ArrayList<>();
            for (int i = 0; i < 7; i++) flies.add(f.fly(i, i == 3 ? 1 : 0));
            long tick = helper.getLevel().getGameTime();
            FlyOwnerFrame frame = FlyOwnerFrame.get(f.owner);
            frame.refresh(f.owner, tick);
            QueuedFly roaming = flies.get(2);
            BlockPos blockedSlot = BlockPos.containing(frame.position(roaming));
            f.replace(blockedSlot, Blocks.STONE.defaultBlockState());
            roaming.tickFamiliar();
            helper.assertTrue(roaming.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ROAM,
                    "The skipped ring candidate is genuinely roaming");
            f.replace(blockedSlot, Blocks.AIR.defaultBlockState());
            Husk target = f.hostile(6);
            tick += 4;
            frame.refresh(f.owner, tick);
            helper.assertTrue(frame.attackTarget(f.owner, flies.get(0), tick) == null,
                    "The first ticking fly cannot lead when another fly is nearer the target");
            helper.assertTrue(frame.attackTarget(f.owner, flies.get(3), tick) == target,
                    "The nearest fly leads even when it is in the middle of the ring");
            List<Integer> admitted = new ArrayList<>(List.of(3));
            assertMembers(helper, f, frame, flies, admitted, tick);
            for (int next : new int[]{1, 0, 6, 5, 4}) {
                frame.refresh(f.owner, ++tick);
                admitted.add(next);
                assertMembers(helper, f, frame, flies, admitted, tick);
            }
            frame.refresh(f.owner, ++tick);
            assertMembers(helper, f, frame, flies, admitted, tick);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_queue")
    public static void recruitmentBudgetBelongsToEachOwnerAndSurvivesLeaderRemoval(GameTestHelper helper) {
        try (Fixture first = new Fixture(helper); Fixture second = new Fixture(helper)) {
            List<QueuedFly> a = List.of(first.fly(0, 0), first.fly(1, -1));
            List<QueuedFly> b = List.of(second.fly(0, 0), second.fly(1, -1));
            first.hostile(6);
            second.hostile(6);
            long tick = helper.getLevel().getGameTime();
            FlyOwnerFrame af = FlyOwnerFrame.get(first.owner);
            FlyOwnerFrame bf = FlyOwnerFrame.get(second.owner);
            af.refresh(first.owner, tick);
            bf.refresh(second.owner, tick);
            helper.assertTrue(af.attackTarget(first.owner, a.get(0), tick) != null
                            && bf.attackTarget(second.owner, b.get(0), tick) != null,
                    "Two owners independently admit a leader in the same server tick");
            assertMembers(helper, first, af, a, List.of(0), tick);
            assertMembers(helper, second, bf, b, List.of(0), tick);
            af.attack().remove(a.get(0));
            helper.assertTrue(af.attackTarget(first.owner, a.get(1), tick) == null,
                    "Removing the leader cannot grant a second admission in the same tick");
            af.refresh(first.owner, tick + 1);
            helper.assertTrue(af.attackTarget(first.owner, a.get(1), tick + 1) != null,
                    "The next tick can admit a new leader");
        }
        helper.succeed();
    }

    private static void assertMembers(GameTestHelper helper, Fixture f, FlyOwnerFrame frame,
                                      List<QueuedFly> flies, List<Integer> admitted, long tick) {
        // Repeated calls in an unusual order must not recruit additional members.
        for (int i = flies.size() - 1; i >= 0; i--) {
            helper.assertTrue((frame.attackTarget(f.owner, flies.get(i), tick) != null) == admitted.contains(i),
                    "Only the circularly selected members attack in this tick; slot=" + i);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_queue")
    public static void followersUseLeaderTargetAndHigherCatchUpSpeed(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        List<QueuedFly> flies = List.of(f.fly(0, 1), f.fly(1, -1), f.fly(2, -2));
        Husk chosen = f.hostile(6);
        Husk alternative = f.hostile(-6.5);
        helper.onEachTick(new Runnable() {
            private final List<Integer> joined = new ArrayList<>();
            private int elapsed;
            private boolean finished;

            @Override
            public void run() {
                if (finished) return;
                try {
                    int admitted = 0;
                    for (int i = 0; i < flies.size(); i++) {
                        QueuedFly fly = flies.get(i);
                        if (fly.getFlyState() != AbstractFlyFamiliarEntity.FlyState.CHASE || joined.contains(i)) continue;
                        joined.add(i);
                        admitted++;
                        double speed = i == 0 ? 0.5 : 0.6;
                        helper.assertTrue(Math.abs(fly.getDeltaMovement().length() - speed) < 0.00001,
                                "A new leader uses 0.5 and each newly admitted follower catches up at 0.6");
                        helper.assertTrue(fly.getDeltaMovement().x > 0,
                                "Followers join the leader instead of selecting their individually closer hostile");
                    }
                    helper.assertTrue(admitted <= 1, "At most one fly actually enters CHASE in each server tick");
                    if (joined.size() == 3) {
                        helper.assertTrue(joined.equals(List.of(0, 2, 1)), "Recruitment follows decreasing ring indices");
                        helper.assertTrue(f.data.getCount(BLUE_FLY) == 3, "Joining consumes no flies");
                        chosen.discard();
                        alternative.discard();
                        for (QueuedFly fly : flies) {
                            fly.tickFamiliar();
                            helper.assertTrue(fly.getFlyState() != AbstractFlyFamiliarEntity.FlyState.CHASE,
                                    "An invalid shared target releases the entire queue");
                        }
                        finished = true;
                        f.close();
                        helper.succeed();
                    } else {
                        helper.assertTrue(++elapsed < 12, "The three flies enter across successive ticks");
                    }
                } catch (RuntimeException | Error failure) {
                    finished = true;
                    f.close();
                    throw failure;
                }
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "fly_queue", timeoutTicks = 180)
    public static void followersRoundWallAndSucceedConsumedLeadersInOrder(GameTestHelper helper) {
        Fixture f = new Fixture(helper);
        List<QueuedFly> flies = List.of(f.fly(0, 0), f.fly(1, -0.4), f.fly(2, -0.8));
        Husk target = f.hostile(6);
        for (int y = 0; y < 4; y++) for (int z = -1; z <= 1; z++) {
            f.replace(f.owner.blockPosition().offset(3, y, z), Blocks.STONE.defaultBlockState());
        }
        helper.assertTrue(!FlyNavigation.clearPath(flies.get(0), flies.get(0).position(),
                target.getBoundingBox().getCenter()), "The attack starts behind a real wall");
        helper.onEachTick(new Runnable() {
            private final Map<UUID, Vec3> previous = new LinkedHashMap<>();
            private boolean finished;
            private int ticks;
            {
                flies.forEach(fly -> previous.put(fly.getUUID(), fly.position()));
            }

            @Override
            public void run() {
                if (finished) return;
                try {
                    for (QueuedFly fly : flies) {
                        Vec3 before = previous.put(fly.getUUID(), fly.position());
                        helper.assertTrue(fly.position().distanceTo(before) <= 0.60001,
                                "All queue movement stays within the 0.6 maximum");
                        helper.assertTrue(!fly.crossedBlock && fly.tickTravel <= 0.60001,
                                "Following a leader's trail never cuts through a wall or corner");
                    }
                    if (f.hits.size() == 3) {
                        helper.assertTrue(f.hits.equals(List.of(0, 2, 1)), "Each successor attacks in queue order");
                        helper.assertTrue(f.data.getCount(BLUE_FLY) == 0,
                                "All three contact attempts consume their own requirement");
                        FamiliarHelper.maintainExistingFamiliars(f.owner);
                        helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner),
                                "Queue handoff never respawns a consumed fly");
                        finished = true;
                        f.close();
                        helper.succeed();
                    } else {
                        helper.assertTrue(++ticks < 165, "The three-fly queue reaches the target and hands off its route");
                    }
                } catch (RuntimeException | Error failure) {
                    finished = true;
                    f.close();
                    throw failure;
                }
            }
        });
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final List<Entity> entities = new ArrayList<>();
        final List<Integer> hits = new ArrayList<>();
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fly_queue"));
            Vec3 origin = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1)));
            owner.setPos(origin.x, 60 + NEXT_FIXTURE.getAndIncrement() * 16, origin.z);
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            BlockPos center = owner.blockPosition();
            AABB arena = new AABB(center.offset(-10, -3, -10), center.offset(11, 8, 11));
            for (Entity stale : helper.getLevel().getEntities((Entity) null, arena, e -> !(e instanceof Player))) stale.discard();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-10, -3, -10), center.offset(10, 7, 10))) {
                replace(pos, Blocks.AIR.defaultBlockState());
            }
        }

        QueuedFly fly(int order, double x) {
            QueuedFly fly = new QueuedFly(this, order);
            fly.setOwner(owner);
            fly.setPos(owner.getBoundingBox().getCenter().add(x, -fly.getBbHeight() * 0.5, 0));
            data.addCount(BLUE_FLY, 1);
            fly.setFormationIndex(order);
            entities.add(fly);
            helper.assertTrue(helper.getLevel().addFreshEntity(fly), "The queue fly is added");
            data.registerEntity(BLUE_FLY, fly.getUUID());
            return fly;
        }

        Husk hostile(double x) {
            Husk target = EntityType.HUSK.create(helper.getLevel());
            if (target == null) throw new AssertionError("Missing husk");
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(owner.position().add(x, 0, 0));
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.setHealth(200);
            entities.add(target);
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "The hostile is added");
            return target;
        }

        void replace(BlockPos position, BlockState state) {
            BlockState before = helper.getLevel().getBlockState(position);
            if (before.equals(state)) return;
            blocks.putIfAbsent(position.immutable(), before);
            helper.getLevel().setBlock(position, state, 2);
        }

        @Override
        public void close() {
            entities.forEach(Entity::discard);
            blocks.forEach((position, state) -> helper.getLevel().setBlock(position, state, 2));
            data.clearRequirements();
            owner.discard();
        }
    }

    private static final class CountingHusk extends Husk {
        int relationChecks;

        CountingHusk(GameTestHelper helper) { super(EntityType.HUSK, helper.getLevel()); }

        @Override
        public boolean isAlliedTo(Entity entity) {
            relationChecks++;
            return super.isAlliedTo(entity);
        }
    }

    private static final class QueuedFly extends BlueFlyEntity {
        private final Fixture fixture;
        private final int order;
        private boolean recording;
        private boolean crossedBlock;
        private double tickTravel;

        QueuedFly(Fixture fixture, int order) {
            super(ModEntities.BLUE_FLY.get(), fixture.helper.getLevel());
            this.fixture = fixture;
            this.order = order;
        }

        @Override
        public void tick() {
            tickTravel = 0;
            recording = true;
            try { super.tick(); }
            finally { recording = false; }
        }

        @Override
        public void setPos(double x, double y, double z) {
            if (recording) {
                Vec3 next = new Vec3(x, y, z);
                tickTravel += position().distanceTo(next);
                crossedBlock |= !FlyNavigation.clearPath(this, position(), next);
            }
            super.setPos(x, y, z);
        }

        @Override
        protected void onContactHit(ServerPlayer owner, net.minecraft.world.entity.LivingEntity target,
                                    Vec3 position, float damage, boolean accepted) {
            fixture.hits.add(order);
        }
    }
}
