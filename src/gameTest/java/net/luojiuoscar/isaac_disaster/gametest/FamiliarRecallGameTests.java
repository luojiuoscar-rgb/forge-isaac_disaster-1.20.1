package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.AbstractFlyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.event.FamiliarEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Shared familiar lifecycle contracts, using real entities and the real spawn scheduler. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FamiliarRecallGameTests {
    private static final ResourceLocation BLUE_FLY =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
    private static final ResourceLocation MOM_KNIFE =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "mom_knife");

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void distantFlyWaitsWithoutMovingThenTeleportsWithItsOriginalUuid(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlueFlyEntity fly = (BlueFlyEntity) fixture.spawn(BLUE_FLY);
            Vec3 distant = fixture.owner.position().add(17, 0, 0);
            fly.setPos(distant);
            fly.tick();
            helper.assertTrue(!fly.isRemoved(), "A distant loaded fly waits for recall without being destroyed");
            helper.assertTrue(fly.position().equals(distant)
                            && fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ORBIT,
                    "Recall runs before orbit navigation or attack selection");
            fixture.assertLinked(BLUE_FLY, fly);
            FamiliarHelper.maintainExistingFamiliars(fixture.owner);
            fixture.assertLinked(BLUE_FLY, fly);
            fixture.admit();
            helper.assertTrue(!fly.isRemoved() && fly.distanceToSqr(fixture.owner) < 4,
                    "The admission scheduler teleports the same fly beside its owner");
            fixture.assertLinked(BLUE_FLY, fly);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void distantKnifePausesItsAttackAndResetsAfterTeleport(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            MomKnifeEntity knife = (MomKnifeEntity) fixture.spawn(MOM_KNIFE);
            Vec3 distant = fixture.owner.position().add(0, 17, 0);
            knife.setPos(distant);
            knife.setFamiliarState(MomKnifeEntity.MomKnifeState.ALIGN);
            knife.tick();
            helper.assertTrue(!knife.isRemoved(), "The shared recall preserves Mom's knife too");
            helper.assertTrue(knife.position().equals(distant)
                            && knife.getFamiliarState() == MomKnifeEntity.MomKnifeState.ALIGN,
                    "Recall prevents the old attack state from advancing or queuing movement");
            fixture.assertLinked(MOM_KNIFE, knife);
            fixture.admit();
            helper.assertTrue(knife.distanceToSqr(fixture.owner) < 4
                            && knife.getFamiliarState() == MomKnifeEntity.MomKnifeState.IDLE,
                    "Teleport resets the knife's old attack state before behavior resumes");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void sixteenBlocksIsInclusiveForEveryFamiliarType(GameTestHelper helper) {
        for (ResourceLocation type : List.of(BLUE_FLY, MOM_KNIFE)) {
            try (Fixture fixture = new Fixture(helper)) {
                AbstractIsaacFamiliarEntity familiar = fixture.spawn(type);
                familiar.setPos(fixture.owner.position().add(16, 0, 0));
                helper.assertTrue(familiar.isValidFamiliar(), "Exactly 16 blocks remains valid for " + type);
                familiar.tick();
                helper.assertTrue(!familiar.isRemoved(), "The boundary does not trigger recall for " + type);
                familiar.setPos(fixture.owner.position().add(16.00001, 0, 0));
                helper.assertTrue(familiar.isValidFamiliar(), "A distant registered familiar remains accepted for " + type);
                familiar.tick();
                helper.assertTrue(!familiar.isRemoved(), "Just beyond the boundary queues recall for " + type);
                fixture.admit();
                helper.assertTrue(familiar.distanceToSqr(fixture.owner) < 4,
                        "Just beyond sixteen blocks is teleported home for " + type);
                fixture.assertLinked(type, familiar);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void reconcilerUsesTheSameSixteenBlockBoundary(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            AbstractIsaacFamiliarEntity familiar = fixture.spawn(BLUE_FLY);
            familiar.setPos(fixture.owner.position().add(16.00001, 0, 0));
            FamiliarHelper.maintainExistingFamiliars(fixture.owner);
            helper.assertTrue(!familiar.isRemoved(), "Reconciliation preserves distant registered entities");
            familiar.tick();
            fixture.admit();
            helper.assertTrue(familiar.distanceToSqr(fixture.owner) < 4, "A reconciled familiar still teleports home");
            fixture.assertLinked(BLUE_FLY, familiar);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void mixedRosterRecallsReplenishOneAtATimeWithoutLosingRequirements(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            AbstractIsaacFamiliarEntity fly = fixture.spawn(BLUE_FLY);
            AbstractIsaacFamiliarEntity knife = fixture.spawn(MOM_KNIFE);
            fly.setPos(fixture.owner.position().add(17, 0, 0));
            knife.setPos(fixture.owner.position().add(0, 0, 17));
            fly.tick();
            knife.tick();
            fixture.assertLinked(BLUE_FLY, fly);
            fixture.assertLinked(MOM_KNIFE, knife);
            fixture.data.addCount(BLUE_FLY, 1);
            fixture.admit();
            helper.assertTrue(fly.distanceToSqr(fixture.owner) < 4 && knife.distanceToSqr(fixture.owner) > 256,
                    "The first tick recalls just the first queued entity across familiar types");
            fixture.admit();
            helper.assertTrue(knife.distanceToSqr(fixture.owner) > 256 && fixture.runtimeCount() == 2,
                    "Repeated scheduler calls cannot recall or spawn twice in one tick");
            helper.runAfterDelay(1, () -> {
                try {
                    fixture.admit();
                    helper.assertTrue(knife.distanceToSqr(fixture.owner) < 4 && fixture.runtimeCount() == 2,
                            "The next tick recalls the knife without also spawning the missing fly");
                    fixture.admit();
                    helper.assertTrue(fixture.runtimeCount() == 2, "Recall consumes the same admission budget as spawning");
                } catch (RuntimeException | Error failure) {
                    fixture.close();
                    throw failure;
                }
            });
            helper.runAfterDelay(2, () -> {
                try {
                    fixture.admit();
                    helper.assertTrue(fixture.runtimeCount() == 3, "Only after recalls does a later tick spawn the missing familiar");
                    helper.assertTrue(fixture.data.containsEntity(BLUE_FLY, fly.getUUID())
                                    && fixture.data.containsEntity(MOM_KNIFE, knife.getUUID()),
                            "Both original UUIDs survive the entire recall sequence");
                    helper.succeed();
                } finally { fixture.close(); }
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void lateUnregisteredEntityCannotReturnToAnOccupiedRoster(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            AbstractIsaacFamiliarEntity old = fixture.spawn(BLUE_FLY);
            fixture.data.removeEntity(BLUE_FLY, old.getUUID());
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(fixture.owner), "A missing slot is replenished");
            old.setPos(fixture.owner.position().add(17, 0, 0));
            old.tick();
            fixture.admit();
            helper.assertTrue(old.isRemoved() && fixture.runtimeCount() == 1,
                    "An old entity whose UUID is no longer registered is destroyed instead of recalled");
            helper.assertTrue(fixture.data.getCount(BLUE_FLY) == 1, "Stale cleanup preserves the requirement");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_recall")
    public static void ownerlessFamiliarKeepsPeriodicCleanup(GameTestHelper helper) {
        BlueFlyEntity fly = new BlueFlyEntity(ModEntities.BLUE_FLY.get(), helper.getLevel());
        fly.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getX() + 0.5, 100,
                helper.absolutePos(new BlockPos(1, 1, 1)).getZ() + 0.5);
        helper.assertTrue(helper.getLevel().addFreshEntity(fly), "Ownerless fixture enters the level");
        try {
            fly.tick();
            helper.assertTrue(!fly.isRemoved(), "Recall does not replace the periodic orphan grace period");
            for (int i = 1; i < 40 && !fly.isRemoved(); i++) fly.tick();
            helper.assertTrue(fly.isRemoved(), "Periodic ownership validation still removes an orphan within 40 ticks");
        } finally {
            fly.discardSilently();
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final List<AbstractIsaacFamiliarEntity> entities = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "recall-test"));
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
            owner.setPos(anchor.getX() + 0.5, 100, anchor.getZ() + 0.5);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                    .resolve().orElseThrow(() -> new AssertionError("Player familiar capability is attached"));
        }

        private AbstractIsaacFamiliarEntity spawn(ResourceLocation type) {
            data.addCount(type, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Scheduler creates fixture " + type);
            var entry = data.getEntry(type).orElseThrow();
            var familiar = (AbstractIsaacFamiliarEntity) helper.getLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
            helper.assertTrue(familiar != null, "Fixture is linked to an entity in the owner level");
            entities.add(familiar);
            return familiar;
        }

        private void assertLinked(ResourceLocation type, AbstractIsaacFamiliarEntity familiar) {
            helper.assertTrue(data.getCount(type) == 1, "Recall preserves the persistent requested count");
            helper.assertTrue(!familiar.isRemoved() && data.containsEntity(type, familiar.getUUID())
                            && data.getEntry(type).orElseThrow().getEntityCount() == 1,
                    "Recall preserves the original entity and its runtime UUID");
        }

        private void admit() {
            FamiliarEvents.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, owner));
        }

        private int runtimeCount() {
            return data.getEntries().stream().mapToInt(entry -> entry.getEntityCount()).sum();
        }

        @Override
        public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            entities.forEach(AbstractIsaacFamiliarEntity::discardSilently);
            owner.discard();
        }
    }
}
