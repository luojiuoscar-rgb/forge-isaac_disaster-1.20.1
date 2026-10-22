package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_blue_spider")
@PrefixGameTestTemplate(false)
public final class SpiderTargetingGameTests {
    private static final ResourceLocation TYPE = ResourceLocation.parse("isaac_disaster:blue_spider");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void sixtyFourSpidersShareScanAssignmentAndPriorityWork(GameTestHelper helper) {
        try (var f = new Fixture(helper, 64)) {
            f.zombie(f.start.add(2, 0, 0));
            f.zombie(f.start.add(-2, 0, 0));
            f.zombie(f.start.add(0, 0, 2));
            f.zombie(f.start.add(0, 0, -2));
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            helper.assertTrue(frame.scans() == 1 && frame.assignmentPasses() == 1, "First frame performs one shared scan and pass");
            helper.assertTrue(frame.priorityEvaluations() == 4, "Priority evaluated once per enemy rather than per spider");
            for (int delta = 0; delta < 4; delta++) {
                for (var spider : f.spiders) {
                    helper.assertTrue(frame.attackTarget(f.owner, spider, tick + delta) != null, "All 64 admitted spiders receive a group assignment");
                }
            }
            helper.assertTrue(frame.scans() == 1 && frame.assignmentPasses() == 1
                    && frame.priorityEvaluations() == 4, "256 lookups do not repeat scan, assignment, or priority work");
            frame.refresh(f.owner, tick + 4);
            helper.assertTrue(frame.scans() == 2 && frame.assignmentPasses() == 2
                    && frame.priorityEvaluations() == 8, "One more shared pass is due after four ticks");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void prioritizesOwnerThreatsAndRejectsPassiveAnimals(GameTestHelper helper) {
        try (var f = new Fixture(helper, 1)) {
            f.zombie(f.start.add(1, 0, 0));
            var threat = f.zombie(f.start.add(5, 0, 0));
            threat.setTarget(f.owner);
            var cow = EntityType.COW.create(f.level);
            cow.setPos(f.start);
            f.level.addFreshEntity(cow);
            f.targets.add(cow);
            var frame = SpiderOwnerFrame.get(f.owner);
            helper.assertTrue(!frame.contactCandidates(cow.getBoundingBox()).contains(cow), "Passive animals follow the blue fly eligibility rule");
            helper.assertTrue(frame.attackTarget(f.owner, f.spiders.get(0), f.level.getGameTime()) == threat,
                    "Enemy targeting owner outranks a closer idle hostile");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void deadTargetsAndRecallPendingSpidersInvalidateWithoutWaitingForScan(GameTestHelper helper) {
        try (var f = new Fixture(helper, 2)) {
            var target = f.zombie(f.start.add(2, 0, 0));
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            var first = f.spiders.get(0);
            var second = f.spiders.get(1);
            helper.assertTrue(frame.attackTarget(f.owner, first, tick) == target, "Target initially selected");
            first.setPos(f.start.add(17, 0, 0));
            first.tick();
            helper.assertTrue(first.isRecallPending() && frame.attackTarget(f.owner, first, tick) == null,
                    "Pending recall invalidates cached assignment in the same tick");
            target.setHealth(0);
            helper.assertTrue(frame.attackTarget(f.owner, second, tick) == null,
                    "Dead enemy invalidates cached assignment in the same tick");
            helper.assertTrue(frame.scans() == 1 && frame.assignmentPasses() == 1, "Invalidation needs no replacement world scan");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void contactIndexIncludesLargeSpanningTargetsExactlyOnce(GameTestHelper helper) {
        try (var f = new Fixture(helper, 1)) {
            var large = f.zombie(f.start);
            large.setBoundingBox(new AABB(f.start.add(-6, -2, -6), f.start.add(6, 4, 6)));
            var huge = f.zombie(f.start);
            huge.setBoundingBox(new AABB(f.start.add(-40, -40, -40), f.start.add(40, 40, 40)));
            var distant = f.zombie(f.start.add(-7, 0, -7));
            var frame = SpiderOwnerFrame.get(f.owner);
            var query = new AABB(f.start.add(5.5, 0, 5.5), f.start.add(6, 0.15, 6));
            var contacts = frame.contactCandidates(query);
            helper.assertTrue(contacts.size() == 2 && contacts.contains(large) && contacts.contains(huge),
                    "Contact index finds edge of multi-cell and huge targets once each; skips distant targets");
            helper.assertTrue(!contacts.contains(distant), "Distant target never leaks through broad phase");
            var manyCells = frame.contactCandidates(new AABB(f.start.add(-6, -2, -6), f.start.add(6, 4, 6)));
            helper.assertTrue(manyCells.stream().filter(entity -> entity == large).count() == 1,
                    "Crossing multiple indexed cells does not duplicate targets");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void enemyOnlyAssignsSpidersWithinEightBlocks(GameTestHelper helper) {
        try (var f = new Fixture(helper, 2)) {
            var target = f.zombie(f.start.add(2, 0, 0));
            var near = f.spiders.get(0);
            var far = f.spiders.get(1);
            near.setPos(f.start);
            far.setPos(f.start.add(-7, 0, 0));
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            frame.refresh(f.owner, tick);
            helper.assertTrue(frame.attackTarget(f.owner, near, tick) == target, "Nearby spider joins enemy group");
            helper.assertTrue(frame.attackTarget(f.owner, far, tick) == null, "Enemy cannot recruit a spider over eight blocks away");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void assignmentsStayStableUntilTheNextSharedRefresh(GameTestHelper helper) {
        try (var f = new Fixture(helper, 1)) {
            var left = f.zombie(f.start.add(-2, 0, 0));
            var right = f.zombie(f.start.add(3, 0, 0));
            var spider = f.spiders.get(0);
            spider.setPos(f.start.add(-1, 0, 0));
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick) == left, "Initial nearest enemy selected");
            spider.setPos(f.start.add(2, 0, 0));
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick + 1) == left, "Movement between shared refreshes preserves assignment");
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick + 4) == right, "Next shared refresh considers new position");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void capabilityRemovalInvalidatesAnAssignmentOnTheNextTick(GameTestHelper helper) {
        try (var f = new Fixture(helper, 1)) {
            var target = f.zombie(f.start.add(2, 0, 0));
            var spider = f.spiders.get(0);
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick) == target, "Linked spider receives assignment");
            f.data.removeEntity(TYPE, spider.getUUID());
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick + 1) == null, "Unlinked spider cannot keep attacking on the next tick");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_targeting")
    public static void differentSpiderVariantsShareTargetingAndUseTheirOwnContactEffects(GameTestHelper helper) {
        try (var f = new Fixture(helper, 1)) {
            f.prepareContactArena();
            f.data.setCount(TYPE, 2);
            var spider = new OtherColorSpider(f.level);
            spider.setOwner(f.owner);
            spider.setPos(f.start);
            f.level.addFreshEntity(spider);
            f.data.registerEntity(TYPE, spider.getUUID());
            f.targets.add(spider);
            var blue = f.spiders.get(0);
            var target = f.zombie(f.start.add(1, 0, 0));
            var frame = SpiderOwnerFrame.get(f.owner);
            long tick = f.level.getGameTime();
            helper.assertTrue(frame.attackTarget(f.owner, spider, tick) == target
                            && frame.attackTarget(f.owner, blue, tick) == target,
                    "Blue and other spider variants receive the same shared target assignment");
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);
            float before = target.getHealth();
            helper.assertTrue(SpiderTerrain.clearSweep(spider, f.start, target.position()),
                    "The contact fixture has an unobstructed corridor");
            helper.assertTrue(spider.tryContact(f.owner, f.start, target.position()),
                    "A direct abstract-base subclass inherits contact consumption");
            helper.assertTrue(target.getHealth() == before - 4 && spider.hooks == 1 && spider.damage == 4,
                    "The variant applies its own contact damage and effect");
            helper.assertTrue(f.data.getCount(TYPE) == 1 && spider.isRemoved()
                            && !f.data.containsEntity(TYPE, spider.getUUID())
                            && f.data.containsEntity(TYPE, blue.getUUID()) && !blue.isRemoved(),
                    "Consuming another variant preserves the blue spider and its requirement");
            helper.assertTrue(!spider.tryContact(f.owner, f.start, target.position()) && spider.hooks == 1,
                    "A consumed spider never damages or runs its effect twice");
        }
        helper.succeed();
    }

    private static final class OtherColorSpider extends AbstractSpiderFamiliarEntity {
        int hooks;
        float damage;

        OtherColorSpider(ServerLevel level) { super(ModEntities.BLUE_SPIDER.get(), level); }
        @Override public int getBodyColor() { return 0x308050; }
        @Override protected float getContactDamage(ServerPlayer owner, net.minecraft.world.entity.LivingEntity target) { return 4; }
        @Override protected void onContactHit(ServerPlayer owner, net.minecraft.world.entity.LivingEntity target,
                                             Vec3 hit, float damage, boolean accepted) {
            hooks++;
            this.damage = damage;
        }
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final Vec3 start;
        final List<BlueSpiderEntity> spiders = new ArrayList<>();
        final List<Entity> targets = new ArrayList<>();
        final Map<BlockPos, BlockState> replacedBlocks = new LinkedHashMap<>();

        Fixture(GameTestHelper helper, int count) {
            level = helper.getLevel();
            start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 16 + NEXT_FIXTURE.incrementAndGet() * 24, 3)));
            owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "spider_targets"));
            owner.setGameMode(GameType.SURVIVAL);
            owner.setHealth(owner.getMaxHealth());
            owner.setInvulnerable(false);
            owner.getAbilities().invulnerable = false;
            owner.setPos(start);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            data.setCount(TYPE, count);
            for (int i = 0; i < count; i++) {
                var spider = ModEntities.BLUE_SPIDER.get().create(level);
                spider.setOwner(owner);
                spider.setPos(start);
                level.addFreshEntity(spider);
                data.registerEntity(TYPE, spider.getUUID());
                spiders.add(spider);
            }
        }

        void prepareContactArena() {
            // Assignment-only fixtures can overlap terrain; real contact must use clear air.
            BlockPos center = BlockPos.containing(start);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(2, 2, 1))) {
                replacedBlocks.put(pos.immutable(), level.getBlockState(pos));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        Zombie zombie(Vec3 position) {
            var target = EntityType.ZOMBIE.create(level);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(position);
            level.addFreshEntity(target);
            targets.add(target);
            return target;
        }

        @Override
        public void close() {
            spiders.forEach(Entity::discard);
            targets.forEach(Entity::discard);
            replacedBlocks.forEach((pos, state) -> level.setBlock(pos, state, 2));
            data.clearRequirements();
            owner.discard();
        }
    }
}
