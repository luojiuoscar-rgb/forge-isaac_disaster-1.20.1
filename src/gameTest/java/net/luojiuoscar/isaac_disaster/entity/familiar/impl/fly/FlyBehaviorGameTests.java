package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Real server fixtures for shared orbit state and the fly's single contact lifecycle. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FlyBehaviorGameTests {
    private static final ResourceLocation BLUE_FLY =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "fly_behavior", timeoutTicks = 60)
    public static void sixtyFourRealFliesKeepDistinctOrbitSlots(GameTestHelper helper) {
        checkRealOrbit(helper, 64);
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior", timeoutTicks = 60)
    public static void requestingOneHundredTwentyEightKeepsOnlySixtyFourFlies(GameTestHelper helper) {
        checkRealOrbit(helper, 128);
    }

    private static void checkRealOrbit(GameTestHelper helper, int requested) {
        int count = Math.min(requested, 64);
        Fixture fixture = new Fixture(helper);
        try {
            fixture.data.setCount(BLUE_FLY, requested);
            helper.assertTrue(fixture.data.getCount(BLUE_FLY) == count, "Blue fly requests honor the per-type limit");
            List<BlueFlyEntity> flies = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                        "The real familiar scheduler accepts fly " + i);
                UUID id = fixture.data.getEntry(BLUE_FLY).orElseThrow().getEntityId(i);
                BlueFlyEntity fly = (BlueFlyEntity) helper.getLevel().getEntity(id);
                helper.assertTrue(fly != null, "Each registered fly exists in the server level");
                fixture.entities.add(fly);
                flies.add(fly);
            }
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                    "The scheduler stops at the accepted number of flies");
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            frame.refresh(fixture.owner, helper.getLevel().getGameTime());
            flies.forEach(fly -> fly.setPos(frame.position(fly)));
            helper.runAfterDelay(6, () -> {
                try {
                    helper.assertTrue(fixture.data.getCount(BLUE_FLY) == count
                                    && fixture.data.getEntry(BLUE_FLY).orElseThrow().getEntityCount() == count,
                            "Idle orbit preserves all requested flies and UUIDs");
                    for (int i = 0; i < flies.size(); i++) {
                        BlueFlyEntity fly = flies.get(i);
                        helper.assertTrue(!fly.isRemoved() && fly.isValidFamiliar()
                                        && fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ORBIT,
                                "Real server ticks keep every fly in orbit, including slot " + i + fixture.describe(fly));
                        near(helper, fly.getBoundingBox().getCenter().y,
                                fixture.owner.getBoundingBox().getCenter().y, 0.001,
                                "Fly bodies stay at the owner's waist");
                        near(helper, fly.getDeltaMovement().length(), 0.3, 0.00001,
                                "A settled orbit uses the lower idle speed");
                        for (int j = 0; j < i; j++) {
                            helper.assertTrue(fly.position().distanceToSqr(flies.get(j).position()) > 0.02,
                                    "No high-count fly shares another fly's slot");
                        }
                    }
                    helper.succeed();
                } finally {
                    fixture.close();
                }
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void orbitTracksRealScaledAndCrouchingOwnerBounds(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlueFlyEntity fly = fixture.fly();
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            long tick = helper.getLevel().getGameTime();
            frame.refresh(fixture.owner, tick);
            double normalRadius = horizontalDistance(frame.position(fly), fixture.owner.position());
            ScaleUtils.setScale(fixture.owner, 2);
            fixture.owner.setPose(Pose.CROUCHING);
            fixture.owner.refreshDimensions();
            helper.assertTrue(fixture.owner.getBbWidth() > 1.1F && fixture.owner.getBbHeight() > 2.9F,
                    "The fixture uses actual scaled crouching player dimensions");
            frame.refresh(fixture.owner, tick + 1);
            Vec3 slot = frame.position(fly);
            near(helper, slot.y + fly.getBbHeight() * 0.5,
                    fixture.owner.getBoundingBox().getCenter().y, 0.00001,
                    "Orbit body center follows the scaled crouching waist");
            helper.assertTrue(horizontalDistance(slot, fixture.owner.position()) > normalRadius,
                    "Larger owners get a larger ring");
            fixture.owner.setYRot(137);
            frame.refresh(fixture.owner, tick + 2);
            near(helper, frame.position(fly).distanceTo(slot), 0.3, 0.00001,
                    "Turning the owner does not rotate the ring; it only advances its normal 0.3 step");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void oneOwnerSharesCandidateScans(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            List<BlueFlyEntity> flies = new ArrayList<>();
            for (int i = 0; i < 64; i++) flies.add(fixture.fly());
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            long tick = helper.getLevel().getGameTime();
            int before = frame.scans();
            frame.refresh(fixture.owner, tick);
            for (BlueFlyEntity fly : flies) {
                frame.refresh(fixture.owner, tick);
            }
            for (int i = 1; i < 4; i++) frame.refresh(fixture.owner, tick + i);
            helper.assertTrue(frame.scans() == before + 1, "64 flies reuse one candidate scan for four ticks");
            frame.refresh(fixture.owner, tick + 4);
            helper.assertTrue(frame.scans() == before + 2, "The next four-tick window refreshes candidates once");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void flyAcquiresHostileWithoutBulletThenReturnsWhenTargetLeavesOwnerRange(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlueFlyEntity fly = fixture.fly();
            Vec3 ownerCenter = fixture.owner.getBoundingBox().getCenter();
            fly.setPos(ownerCenter.add(0, -fly.getBbHeight() * 0.5, 0));
            var cow = fixture.mob(EntityType.COW, ownerCenter.add(0.6, -0.7, 0));
            var hostile = fixture.mob(EntityType.ZOMBIE, ownerCenter.add(5, -0.975, 0));
            float cowHealth = cow.getHealth();
            fly.tickFamiliar();
            helper.assertTrue(fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.CHASE && fly.getX() > ownerCenter.x,
                    "A fly acquires an idle hostile automatically without a bullet or owner attack" + fixture.describe(fly));
            helper.assertTrue(cow.getHealth() == cowHealth && !fly.isRemoved(),
                    "A closer animal is neither selected nor struck along the chase");
            near(helper, fly.getDeltaMovement().length(), 0.5, 0.00001, "Chase uses the fixed travel speed");
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            frame.refresh(fixture.owner, helper.getLevel().getGameTime() + 1);
            helper.assertTrue(frame.contains(fly), "A chasing fly retains its place in the owner's orbit roster");
            hostile.setPos(fixture.owner.position().add(12, 0, 0));
            fly.tickFamiliar();
            helper.assertTrue(fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.RETURN,
                    "A cached hostile leaving eight blocks of the owner immediately ends the chase");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void movingOwnerDoesNotTeleportOrAccelerateReturningFly(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlueFlyEntity fly = fixture.fly();
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            long tick = helper.getLevel().getGameTime();
            frame.refresh(fixture.owner, tick - 1);
            fly.setPos(frame.position(fly));
            Vec3 previous = fly.position();
            fixture.owner.setPos(fixture.owner.position().add(5, 0, 0));
            fly.tickFamiliar();
            near(helper, fly.position().distanceTo(previous), 0.6, 0.00001,
                    "Owner movement leaves the fly travelling exactly 0.6 blocks per tick" + fixture.describe(fly));
            helper.assertTrue(fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.RETURN,
                    "The displaced ring is recovered through the return state");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void contactUsesCurrentAttackAndConsumesBeforeSubclassHook(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2);
            ObservedFly fly = fixture.observedFly();
            Zombie victim = fixture.contactVictim(fly);
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
            fly.tickFamiliar();
            helper.assertTrue(fly.hooks == 1 && fly.hitTarget == victim && fly.accepted,
                    "A real contact delivers one accepted attempt to the subclass hook" + fixture.describe(fly));
            near(helper, fly.attemptedDamage, 14, 0.00001, "Blue fly deals twice the owner's current attack at contact time");
            helper.assertTrue(victim.getHealth() < victim.getMaxHealth()
                            && victim.getLastDamageSource() != null
                            && victim.getLastDamageSource().is(ModDamageType.FAMILIAR)
                            && victim.getLastDamageSource().getDirectEntity() == fly
                            && victim.getLastDamageSource().getEntity() == fixture.owner,
                    "Contact uses familiar damage with the fly direct source and owner attribution");
            assertConsumed(helper, fixture, fly);
            helper.assertTrue(fly.consumedBeforeHook, "Removal, requested count and runtime UUID are updated before the hook");
            helper.assertTrue(fly.hitPosition != null && fly.hitPosition.distanceTo(victim.getBoundingBox().getCenter()) < 1,
                    "The hook retains a world-space contact position after removal");
            fly.tickFamiliar();
            helper.assertTrue(fly.hooks == 1, "A consumed fly cannot dispatch its effect again");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void alreadyOverlappingTargetStillConsumesExactlyOneFly(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            ObservedFly fly = fixture.observedFly();
            Zombie victim = fixture.contactVictim(fly);
            fly.setPos(victim.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0));
            fly.tickFamiliar();
            helper.assertTrue(fly.hooks == 1 && fly.accepted && fly.hitTarget == victim,
                    "Contact at the initial position does not require a movement segment" + fixture.describe(fly));
            assertConsumed(helper, fixture, fly);
            fly.tickFamiliar();
            helper.assertTrue(fly.hooks == 1, "Initial overlap cannot consume or dispatch twice");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior", timeoutTicks = 140)
    public static void realFlyDetoursAroundSolidWallBeforeContact(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            ObservedFly fly = fixture.observedFly();
            Zombie victim = fixture.mob(EntityType.ZOMBIE, fixture.owner.position().add(6, 0, 0));
            fly.setPos(victim.getBoundingBox().getCenter().add(-5, -fly.getBbHeight() * 0.5, 0));
            BlockPos base = fixture.owner.blockPosition().offset(3, 0, -1);
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 3; z++) fixture.stone(base.offset(0, y, z));
            }
            AABB wall = new AABB(base.getX(), base.getY(), base.getZ(),
                    base.getX() + 1, base.getY() + 4, base.getZ() + 3);
            helper.assertTrue(!FlyNavigation.clearPath(fly, fly.position(),
                            victim.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0)),
                    "The actual stone wall blocks the direct chase path");
            new WallJourney(helper, fixture, fly, victim, wall).observeEveryTick();
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void cancelledContactStillConsumesAndReportsRejectedAttempt(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            ObservedFly fly = fixture.observedFly();
            Zombie victim = fixture.contactVictim(fly);
            float initial = victim.getHealth();
            Consumer<LivingAttackEvent> cancel = event -> {
                if (event.getEntity() == victim && event.getSource().getDirectEntity() == fly) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, cancel);
            try {
                fly.tickFamiliar();
                helper.assertTrue(victim.getHealth() == initial && fly.hooks == 1 && !fly.accepted,
                        "A Forge-cancelled hurt attempt is reported once as rejected" + fixture.describe(fly));
                helper.assertTrue(fly.consumedBeforeHook, "Rejected contact is consumed before the subclass hook too");
                assertConsumed(helper, fixture, fly);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior", timeoutTicks = 60)
    public static void shieldRejectedContactStillConsumesFly(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            Zombie victim = fixture.mob(EntityType.ZOMBIE, fixture.owner.position().add(3, 0, 0));
            victim.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
            victim.setYRot(90);
            victim.setYHeadRot(90);
            victim.setXRot(0);
            victim.startUsingItem(InteractionHand.MAIN_HAND);
            helper.runAfterDelay(6, () -> {
                try {
                    helper.assertTrue(victim.isBlocking(), "The fixture shield has completed its real use warmup");
                    ObservedFly fly = fixture.observedFly();
                    fly.setPos(victim.getBoundingBox().getCenter().add(-0.5, -fly.getBbHeight() * 0.5, 0));
                    victim.setYRot(90);
                    victim.setYHeadRot(90);
                    float initial = victim.getHealth();
                    fly.tickFamiliar();
                    helper.assertTrue(victim.getHealth() == initial && fly.hooks == 1 && !fly.accepted,
                            "A facing shield rejects the real contact attempt" + fixture.describe(fly));
                    helper.assertTrue(fly.consumedBeforeHook, "Shield rejection still consumes before dispatching the hook");
                    assertConsumed(helper, fixture, fly);
                    helper.succeed();
                } finally {
                    fixture.close();
                }
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void differentFlyVariantsShareLeadershipAndQueueAdmission(GameTestHelper helper) {
        for (boolean variantLeads : new boolean[]{true, false}) {
            try (var f = new Fixture(helper)) {
                var other = f.register(new OtherColorFly(helper));
                other.setPos(f.owner.position().add(variantLeads ? 1 : -1, 0, 0));
                var blue = f.fly();
                blue.setPos(f.owner.position().add(variantLeads ? -1 : 1, 0, 0));
                var target = f.mob(EntityType.ZOMBIE, f.owner.position().add(2, 0, 0));
                AbstractFlyFamiliarEntity leader = variantLeads ? other : blue;
                AbstractFlyFamiliarEntity follower = variantLeads ? blue : other;
                var frame = FlyOwnerFrame.get(f.owner);
                long tick = helper.getLevel().getGameTime();
                frame.refresh(f.owner, tick);
                helper.assertTrue(frame.attackTarget(f.owner, leader, tick) == target,
                        "The nearest fly can initiate and lead regardless of variant");
                helper.assertTrue(frame.attackTarget(f.owner, follower, tick) == null,
                        "The second fly still obeys one admission per tick");
                for (int delta = 1; delta <= 4; delta++) {
                    frame.refresh(f.owner, tick + delta);
                    helper.assertTrue(frame.attackTarget(f.owner, leader, tick + delta) == target,
                            "Either variant stays in the shared queue as leader");
                    helper.assertTrue(frame.attackTarget(f.owner, follower, tick + delta) == target,
                            "Either variant joins the same queue as follower");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_behavior")
    public static void otherFlyVariantsUseTheirOwnContactDamageAndEffect(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            var fly = f.register(new OtherColorFly(helper));
            var target = f.contactVictim(fly);
            target.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            float before = target.getHealth();
            fly.tickFamiliar();
            helper.assertTrue(target.getHealth() == before - 4 && fly.hooks == 1 && fly.damage == 4,
                    "A direct abstract-base subclass applies its own damage and effect");
            helper.assertTrue(fly.isRemoved() && f.data.getCount(BLUE_FLY) == 0
                            && !f.data.containsEntity(BLUE_FLY, fly.getUUID()),
                    "The shared contact behavior consumes the variant once");
            fly.tickFamiliar();
            helper.assertTrue(fly.hooks == 1, "A consumed variant cannot apply its effect again");
        }
        helper.succeed();
    }

    private static final class OtherColorFly extends AbstractFlyFamiliarEntity {
        int hooks;
        float damage;

        OtherColorFly(GameTestHelper helper) { super(ModEntities.BLUE_FLY.get(), helper.getLevel()); }
        @Override public int getBodyColor() { return 0xD04030; }
        @Override protected float getContactDamage(ServerPlayer owner, LivingEntity target) { return 4; }
        @Override protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hit,
                                             float damage, boolean accepted) {
            hooks++;
            this.damage = damage;
        }
    }

    private static void assertConsumed(GameTestHelper helper, Fixture fixture, ObservedFly fly) {
        helper.assertTrue(fly.isRemoved() && fixture.data.getCount(BLUE_FLY) == 0
                        && !fixture.data.containsEntity(BLUE_FLY, fly.getUUID()),
                "Contact removes both the live fly and its persistent requirement and UUID");
        FamiliarHelper.maintainExistingFamiliars(fixture.owner);
        helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                "The reconciler cannot respawn a consumed fly");
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.z - b.z) * (a.z - b.z));
    }

    private static void near(GameTestHelper helper, double actual, double expected, double tolerance, String message) {
        helper.assertTrue(Math.abs(actual - expected) <= tolerance,
                message + ": expected " + expected + ", got " + actual);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final List<Entity> entities = new ArrayList<>();
        private final Map<BlockPos, BlockState> replacedBlocks = new LinkedHashMap<>();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fly_behavior"));
            owner.setGameMode(GameType.SURVIVAL);
            owner.setInvulnerable(false);
            owner.getAbilities().invulnerable = false;
            owner.setHealth(owner.getMaxHealth());
            // A batch runs nearby templates concurrently. Give every owner a separate eight-block sphere.
            int isolatedHeight = 100 + NEXT_FIXTURE.getAndIncrement() * 14;
            Vec3 horizontalAnchor = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1)));
            // Keep the initial orbit inside one chunk. Cross-boundary sections may still be HIDDEN
            // during GameTest setup, so UUID lookups would temporarily shrink a 64-fly roster.
            owner.setPos(Math.floor(horizontalAnchor.x / 16) * 16 + 8.5, isolatedHeight,
                    Math.floor(horizontalAnchor.z / 16) * 16 + 8.5);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            // The GameTest world can contain mountains above the tiny template. Prepare actual air.
            try {
                BlockPos center = owner.blockPosition();
                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-12, -5, -12), center.offset(12, 6, 12))) {
                    replaceBlock(pos, Blocks.AIR.defaultBlockState());
                }
                // The GameTest save survives failed runs, including any fixture mobs left by a timeout.
                AABB arena = new AABB(center.offset(-12, -5, -12), center.offset(13, 7, 13));
                for (Entity stale : helper.getLevel().getEntities((Entity) null, arena,
                        entity -> !(entity instanceof Player))) {
                    stale.discard();
                }
            } catch (RuntimeException | Error failure) {
                close();
                throw failure;
            }
        }

        private String describe(BlueFlyEntity fly) {
            FlyOwnerFrame frame = FlyOwnerFrame.get(owner);
            return " [owner=" + owner.position() + ", alive=" + owner.isAlive() + ", removed=" + owner.isRemoved()
                    + ", health=" + owner.getHealth() + ", fly=" + fly.position() + ", state=" + fly.getFlyState()
                    + ", removed=" + fly.isRemoved() + ", valid=" + fly.isValidFamiliar()
                    + ", sameOwner=" + (fly.getOwner() == owner) + ", roster=" + frame.contains(fly)
                    + ", candidates=" + frame.candidates().size() + ", free="
                    + FlyNavigation.clearPath(fly, fly.position(), fly.position())
                    + ", block=" + helper.getLevel().getBlockState(fly.blockPosition())
                    + ", damage=" + (fly instanceof ObservedFly observed ? observed.hooks + "/" + observed.accepted : "n/a") + "]";
        }

        private BlueFlyEntity fly() {
            return register(new BlueFlyEntity(ModEntities.BLUE_FLY.get(), helper.getLevel()));
        }

        private ObservedFly observedFly() {
            return register(new ObservedFly(helper));
        }

        private <T extends AbstractFlyFamiliarEntity> T register(T fly) {
            fly.setOwner(owner);
            fly.setPos(owner.position());
            data.addCount(BLUE_FLY, 1);
            fly.setFormationIndex(data.getEntry(BLUE_FLY).orElseThrow().getEntityCount());
            entities.add(fly);
            helper.assertTrue(helper.getLevel().addFreshEntity(fly), "The real server accepts the fly fixture");
            data.registerEntity(BLUE_FLY, fly.getUUID());
            return fly;
        }

        private Zombie contactVictim(AbstractFlyFamiliarEntity fly) {
            Zombie victim = mob(EntityType.ZOMBIE, owner.position().add(3, 0, 0));
            fly.setPos(victim.getBoundingBox().getCenter().add(-0.5, -fly.getBbHeight() * 0.5, 0));
            return victim;
        }

        private <T extends Mob> T mob(EntityType<T> type, Vec3 position) {
            T mob = type.create(helper.getLevel());
            if (mob == null) throw new AssertionError("Fixture entity type creates a mob");
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.setInvulnerable(false);
            mob.setHealth(mob.getMaxHealth());
            mob.setPos(position);
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            entities.add(mob);
            helper.assertTrue(helper.getLevel().addFreshEntity(mob), "The real server accepts the mob fixture");
            return mob;
        }

        private void stone(BlockPos pos) {
            replaceBlock(pos, Blocks.STONE.defaultBlockState());
        }

        private void replaceBlock(BlockPos pos, BlockState replacement) {
            BlockState original = helper.getLevel().getBlockState(pos);
            if (original.equals(replacement)) return;
            replacedBlocks.putIfAbsent(pos.immutable(), original);
            helper.getLevel().setBlock(pos, replacement, 2);
        }

        @Override
        public void close() {
            for (Entity entity : entities) if (!entity.isRemoved()) entity.discard();
            replacedBlocks.forEach((pos, state) -> helper.getLevel().setBlock(pos, state, 2));
            data.clearRequirements();
            owner.discard();
        }
    }

    /** Checks each actual server tick using a separate vanilla segment/box intersection. */
    private static final class WallJourney implements Runnable {
        private final GameTestHelper helper;
        private final Fixture fixture;
        private final ObservedFly fly;
        private final Zombie victim;
        private final AABB wall;
        private final float initialHealth;
        private Vec3 previous;
        private boolean detoured;
        private double maxLateral;
        private double minBottom = Double.POSITIVE_INFINITY;
        private double maxBottom = Double.NEGATIVE_INFINITY;
        private int elapsed;

        private WallJourney(GameTestHelper helper, Fixture fixture, ObservedFly fly, Zombie victim, AABB wall) {
            this.helper = helper;
            this.fixture = fixture;
            this.fly = fly;
            this.victim = victim;
            this.wall = wall;
            previous = fly.position();
            initialHealth = victim.getHealth();
        }

        private void observeEveryTick() {
            // GameTestInfo removes a due Runnable after invoking it. Rescheduling the same
            // instance from inside run() would replace its map entry and then delete it.
            helper.onEachTick(this);
        }

        @Override
        public void run() {
            try {
                Vec3 current = fly.position();
                double halfWidth = fly.getBbWidth() * 0.5;
                AABB forbidden = new AABB(wall.minX - halfWidth, wall.minY - fly.getBbHeight(),
                        wall.minZ - halfWidth, wall.maxX + halfWidth, wall.maxY, wall.maxZ + halfWidth)
                        .deflate(0.00001);
                helper.assertTrue(!forbidden.contains(previous) && !forbidden.contains(current)
                                && forbidden.clip(previous, current).isEmpty(),
                        "Every real movement segment keeps the fly body outside the solid wall");
                helper.assertTrue(current.distanceTo(previous) <= 0.50001,
                        "Wall navigation never teleports or exceeds its travel speed");
                detoured |= Math.abs(current.z - fixture.owner.getZ()) > 1.5
                        || current.y < wall.minY - fly.getBbHeight() || current.y > wall.maxY;
                maxLateral = Math.max(maxLateral, Math.abs(current.z - fixture.owner.getZ()));
                minBottom = Math.min(minBottom, current.y);
                maxBottom = Math.max(maxBottom, current.y);
                if (current.x < wall.maxX) {
                    helper.assertTrue(victim.getHealth() == initialHealth,
                            "The fly cannot damage the victim from the blocked side of the wall; health="
                                    + victim.getHealth() + ", initial=" + initialHealth + ", source="
                                    + victim.getLastDamageSource() + fixture.describe(fly));
                }
                if (fly.isRemoved()) {
                    helper.assertTrue(detoured && fly.hooks == 1 && fly.hitTarget == victim && fly.accepted,
                            "A real bounded detour reaches the hostile and consumes one contact; elapsed=" + elapsed
                                    + ", detoured=" + detoured + ", maxLateral=" + maxLateral
                                    + ", minBottom=" + minBottom + ", maxBottom=" + maxBottom + ", wall=" + wall
                                    + ", hooks=" + fly.hooks + ", accepted=" + fly.accepted
                                    + ", correctTarget=" + (fly.hitTarget == victim) + ", hitTarget=" + fly.hitTarget
                                    + ", victim=" + victim + ", health=" + victim.getHealth()
                                    + ", damage=" + fly.attemptedDamage + fixture.describe(fly));
                    assertConsumed(helper, fixture, fly);
                    fixture.close();
                    helper.succeed();
                    return;
                }
                helper.assertTrue(++elapsed < 110,
                        "A finite stone wall has a reachable route within 110 ticks; elapsed=" + elapsed
                                + ", detoured=" + detoured + ", victim=" + victim.position()
                                + ", health=" + victim.getHealth() + ", targetValid="
                                + FlyOwnerFrame.validTarget(fixture.owner, victim) + fixture.describe(fly));
                previous = current;
            } catch (RuntimeException | Error failure) {
                fixture.close();
                throw failure;
            }
        }
    }

    /** Captures the public extension contract without replacing targeting, movement or damage. */
    private static final class ObservedFly extends BlueFlyEntity {
        private int hooks;
        private boolean accepted;
        private boolean consumedBeforeHook;
        private float attemptedDamage;
        private LivingEntity hitTarget;
        private Vec3 hitPosition;

        private ObservedFly(GameTestHelper helper) {
            super(ModEntities.BLUE_FLY.get(), helper.getLevel());
        }

        @Override
        protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                    float attemptedDamage, boolean damageAccepted) {
            hooks++;
            accepted = damageAccepted;
            this.attemptedDamage = attemptedDamage;
            hitTarget = target;
            this.hitPosition = hitPosition;
            PlayerFamiliarData data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                    .resolve().orElseThrow();
            consumedBeforeHook = isRemoved() && data.getCount(BLUE_FLY) == 0
                    && !data.containsEntity(BLUE_FLY, getUUID());
        }
    }
}
