package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfWarEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_familiar_position")
@PrefixGameTestTemplate(false)
public final class FamiliarPositionGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final ResourceLocation BLUE = id("blue_fly");
    private static final ResourceLocation WAR = id("locust_of_war");

    @GameTest(template = "trajectory_empty")
    public static void explicitPositionAddsNewOwnershipWithoutFillingOldDeficits(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 5);
            f.restore(3);
            Vec3 position = f.owner.position().add(3.25, 1.5, 2.75);
            var spawned = spawn(f.owner, BLUE, position, 1);
            helper.assertTrue(spawned.size() == 1, "One new familiar is returned immediately");
            var fly = spawned.get(0);
            helper.assertTrue(fly.getOwner() == f.owner && fly.position().equals(position)
                            && fly.isValidFamiliar() && helper.getLevel().getEntity(fly.getUUID()) == fly,
                    "The world entity starts at the explicit feet position with a valid player owner");
            helper.assertTrue(f.data.getCount(BLUE) == 6 && f.data.getEntry(BLUE).orElseThrow().getEntityCount() == 4,
                    "Five requirements and three entities become six requirements and four entities");
            var order = List.copyOf(f.data.getEntityIdsInAdmissionOrder());
            helper.assertTrue(order.get(order.size() - 1).equals(fly.getUUID()), "The new entity joins the tail");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void directSpawnsNeitherConsumeNorObeyTheTickAdmissionBudget(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 2);
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner), "The normal scheduler spends this tick's admission");
            helper.assertTrue(spawn(f.owner, BLUE, f.owner.position().add(2, 1, 0), 3).size() == 3,
                    "Direct spawning admits a whole batch after the tick budget is spent");
            helper.assertTrue(!FamiliarHelper.tickFamiliarAdmissions(f.owner), "A direct batch does not reset the spent budget");
        }
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 1);
            spawn(f.owner, BLUE, f.owner.position().add(2, 1, 0), 1);
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner), "Direct spawning leaves an unused scheduler budget available");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledReplacementPreservesOldCountsEntitiesAndRecallOrder(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 64);
            f.restore(64);
            var original = List.copyOf(f.data.getEntityIdsInAdmissionOrder());
            UUID tail = original.get(63);
            f.data.queueRecall(tail);
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == f.owner
                        && WAR.equals(familiar.getFamiliarType())) {
                    helper.assertTrue(familiar.isValidFamiliar(), "Join listeners can recognize the reserved owner membership");
                    event.setCanceled(true);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                helper.assertTrue(spawn(f.owner, WAR, f.owner.position().add(2, 1, 0), 1).isEmpty(), "A canceled join returns no familiar");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(f.data.getCount(BLUE) == 64 && f.data.getCount(WAR) == 0
                            && original.equals(List.copyOf(f.data.getEntityIdsInAdmissionOrder()))
                            && original.stream().allMatch(uuid -> helper.getLevel().getEntity(uuid) != null)
                            && tail.equals(f.data.pollRecall()),
                    "A failed replacement preserves all old requirements, entities, FIFO order and pending recalls");
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner), "No phantom requirement is left to spawn later");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void successfulReplacementKeepsTheEarliestBlueFlies(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 64);
            f.restore(64);
            var original = List.copyOf(f.data.getEntityIdsInAdmissionOrder());
            var spawned = spawn(f.owner, WAR, f.owner.position().add(2, 1, 0), 2);
            helper.assertTrue(spawned.size() == 2 && f.data.getCount(BLUE) == 62 && f.data.getCount(WAR) == 2,
                    "Two accepted locusts replace two blue flies within the shared cap");
            var roster = List.copyOf(f.data.getEntityIdsInAdmissionOrder());
            helper.assertTrue(roster.size() == 64 && roster.subList(0, 62).equals(original.subList(0, 62))
                            && helper.getLevel().getEntity(original.get(62)) == null
                            && helper.getLevel().getEntity(original.get(63)) == null,
                    "The oldest 62 flies survive and both displaced tails are removed immediately");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void fullEqualPriorityAndPartialBatchesDoNotCreateHiddenRequirements(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(WAR, FAMILIAR_SOURCE, 63);
            var spawned = spawn(f.owner, id("locust_of_conquest"), f.owner.position().add(2, 1, 0), 4);
            helper.assertTrue(spawned.size() == 1 && f.data.getCount(id("locust_of_conquest")) == 1,
                    "Only one free slot is accepted; an explicit count is never randomized");
            helper.assertTrue(spawn(f.owner, id("locust_of_famine"), f.owner.position(), 1).isEmpty()
                            && f.data.getCount(id("locust_of_famine")) == 0,
                    "Equal-priority locusts cannot displace one another or leave overflow");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void clearedRequirementsInsideJoinAreNotRestoredByFailureCleanup(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 3);
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == f.owner) {
                    f.data.clearRequirements();
                    event.setCanceled(true);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                helper.assertTrue(spawn(f.owner, WAR, f.owner.position(), 1).isEmpty(), "An invalidated join produces no entity");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(f.data.getCount(BLUE) == 0 && f.data.getCount(WAR) == 0,
                    "Failure cleanup never restores a snapshot over listener changes");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledBatchCommitsOnlySuccessfulEntities(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            AtomicInteger attempts = new AtomicInteger();
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == f.owner
                        && attempts.incrementAndGet() % 2 == 0) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                var spawned = spawn(f.owner, BLUE, f.owner.position().add(2, 1, 0), 4);
                helper.assertTrue(spawned.size() == 2 && f.data.getCount(BLUE) == 2
                                && f.data.getEntry(BLUE).orElseThrow().getEntityCount() == 2,
                        "A four-attempt batch records only its two successful joins");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void invalidPositionsTypesAndCountsAreSafeNoOps(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            helper.assertTrue(spawn(f.owner, BLUE, new Vec3(Double.NaN, 1, 0), 1).isEmpty()
                            && spawn(f.owner, BLUE, new Vec3(0, Double.POSITIVE_INFINITY, 0), 1).isEmpty()
                            && spawn(f.owner, id("missing_familiar"), f.owner.position(), 1).isEmpty()
                            && spawn(f.owner, BLUE, f.owner.position(), -1).isEmpty()
                            && spawn(f.owner, BLUE, f.owner.position(), 0).isEmpty()
                            && spawn(f.owner, BLUE, f.owner.position().add(10000, 0, 0), 1).isEmpty()
                            && f.data.getEntries().isEmpty(),
                    "Invalid, unloaded and zero-count requests do not create entities or requirements");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void conquestBatchUsesTheProvidedRandomWithoutChangingExplicitCounts(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Vec3 position = f.owner.position().add(2, 1, 0);
            for (int seed = 0; seed < 12; seed++) {
                FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
                f.data.clearRequirements();
                int expected = RandomSource.create(seed * 1_000_003L).nextInt(4) + 1;
                var spawned = ModFamiliarEntities.LOCUST_OF_CONQUEST.get().spawnEntry(f.owner,
                        ModFamiliarEntities.LOCUST_OF_CONQUEST.getId(), FAMILIAR_SOURCE, position, RandomSource.create(seed * 1_000_003L));
                helper.assertTrue(spawned.size() == expected && spawned.stream().allMatch(e -> e.position().equals(position)),
                        "The type-defined one-to-four batch draws from the supplied random source once");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void explicitPositionAppliesToGroundedFamiliarsAndOnlyTheFirstSpawn(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ResourceLocation spider = id("blue_spider");
            Vec3 position = f.owner.position().add(4, 2, 0);
            var spawned = FamiliarHelper.spawnFamiliars(f.owner, spider, FAMILIAR_SOURCE, position);
            helper.assertTrue(spawned.size() == 1 && spawned.get(0).position().equals(position),
                    "The explicit feet position is honored even for grounded familiars");
            FamiliarHelper.discardAllRuntimeFamiliars(f.owner);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner), "The persistent requirement remains restorable");
            var replacement = helper.getLevel().getEntity(f.data.getEntry(spider).orElseThrow().getEntityId(0));
            helper.assertTrue(replacement != null && !replacement.position().equals(position),
                    "Restoration uses the type's ordinary owner placement rather than replaying the old initial position");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void nestedJoinRequestsCannotStealTheOuterReservation(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            AtomicInteger callbacks = new AtomicInteger();
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == f.owner) {
                    callbacks.incrementAndGet();
                    helper.assertTrue(spawn(f.owner, WAR, f.owner.position(), 1).isEmpty(),
                            "A recursive join for the same owner cannot reserve a conflicting slot");
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                helper.assertTrue(spawn(f.owner, BLUE, f.owner.position(), 1).size() == 1 && callbacks.get() == 1,
                        "The original request still commits once without recursive event dispatch");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(f.data.getCount(BLUE) == 1 && f.data.getCount(WAR) == 0,
                    "The rejected nested request leaves no hidden ownership");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void successfulWorldJoinStillRequiresAValidFinalAllocation(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (event.getEntity() instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == f.owner) {
                    f.data.setCount(WAR, FAMILIAR_SOURCE, 64);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                helper.assertTrue(spawn(f.owner, BLUE, f.owner.position(), 1).isEmpty(),
                        "An accepted world join cannot claim a slot taken by a listener's higher-priority requirement");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(f.data.getCount(BLUE) == 0 && f.data.getCount(WAR) == 64
                            && f.data.getEntityIdsInAdmissionOrder().isEmpty(),
                    "The rejected entity is removed, and the listener's independent requirement remains intact");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void alreadyReducedRequirementsAreReconciledBeforeImmediateAdmission(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 5);
            f.restore(5);
            UUID first = f.data.getEntry(BLUE).orElseThrow().getEntityId(0);
            f.data.setCount(BLUE, FAMILIAR_SOURCE, 1);
            helper.assertTrue(spawn(f.owner, BLUE, f.owner.position().add(2, 1, 0), 1).size() == 1
                            && f.data.getCount(BLUE) == 2 && f.data.getEntry(BLUE).orElseThrow().getEntityCount() == 2
                            && f.data.getEntry(BLUE).orElseThrow().getEntityId(0).equals(first),
                    "Only the already-excess tails are discarded before one new requirement and entity are admitted");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void immediateWarLocustCapturesTheOwnersBombEffectsAtWorldJoin(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow()
                    .addItem(f.owner, new ItemStack(ModPassiveItems.HOT_BOMB.get()));
            StatManager.addTriggerModule(f.owner, ModTriggerModules.HOT_BOMB.getId(), 1);
            var spawned = spawn(f.owner, WAR, f.owner.position().add(2, 1, 0), 1);
            helper.assertTrue(spawned.size() == 1
                            && ((LocustOfWarEntity) spawned.get(0)).getCachedEffect().contains(ModExecutableEffects.HOT_BOMB.get()),
                    "The owner is already assigned when the real join event captures Hot Bomb");
        }
        helper.succeed();
    }

    private static List<AbstractIsaacFamiliarEntity> spawn(ServerPlayer owner, ResourceLocation type, Vec3 pos, int count) {
        return FamiliarHelper.spawnFamiliars(owner, type, FAMILIAR_SOURCE, pos, count);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("isaac_disaster", name);
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;

        private Fixture(GameTestHelper helper) {
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "familiar-position"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(3, 1, 3));
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        private void restore(int count) {
            for (int i = 0; i < count; i++) {
                if (!FamiliarHelper.spawnNextMissingFamiliar(owner)) throw new AssertionError("Unable to establish old familiar roster");
            }
        }

        @Override
        public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            owner.discard();
        }
    }
}
