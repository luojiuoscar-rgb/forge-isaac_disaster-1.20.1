package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.resources.ResourceLocation;
import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_fly_stability")
@PrefixGameTestTemplate(false)
public final class FlyOrbitOrderGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    @GameTest(template = "trajectory_empty", batch = "fly_orbit_order")
    public static void newMemberOfAnExistingTypeJoinsTheTail(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var blue = f.add(ModEntities.BLUE_FLY.get());
            var green = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            long tick = helper.getLevel().getGameTime();
            f.refresh(tick);
            var laterBlue = f.add(ModEntities.BLUE_FLY.get());
            f.refresh(++tick);
            f.assertOrder(List.of(blue, green, laterBlue));
            f.refresh(++tick);
            f.assertOrder(List.of(blue, green, laterBlue));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_orbit_order")
    public static void firstSnapshotPreservesCrossTypeAdmissionOrder(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(ModEntities.BLUE_FLY.get());
            var second = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            var third = f.add(ModEntities.BLUE_FLY.get());
            f.refresh(helper.getLevel().getGameTime());
            f.assertOrder(List.of(first, second, third));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_orbit_order")
    public static void registrationOrderWinsWhenEntitiesWereCreatedEarlier(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var earlierBlue = f.create(ModEntities.BLUE_FLY.get());
            var first = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            f.admit(earlierBlue);
            var third = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            f.refresh(helper.getLevel().getGameTime());
            f.assertOrder(List.of(first, earlierBlue, third));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_orbit_order")
    public static void reRegisteringAMemberBeforeTheNextSnapshotPlacesItAtTheTail(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(ModEntities.BLUE_FLY.get());
            var second = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            var third = f.add(ModEntities.BLUE_FLY.get());
            long tick = helper.getLevel().getGameTime();
            f.refresh(tick);
            f.data.removeEntity(first.getFamiliarType(), first.getUUID());
            f.data.registerEntity(first.getFamiliarType(), FAMILIAR_SOURCE, first.getUUID());
            f.refresh(tick + 1);
            f.assertOrder(List.of(second, third, first));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_orbit_order")
    public static void removingAHeadPreservesSurvivorsAndAppendsLaterMembers(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var first = f.add(ModEntities.BLUE_FLY.get());
            var second = f.add(ModEntities.LOCUST_OF_PESTILENCE.get());
            long tick = helper.getLevel().getGameTime();
            f.refresh(tick);
            var third = f.add(ModEntities.BLUE_FLY.get());
            f.refresh(++tick);
            f.remove(first);
            var fourth = f.add(ModEntities.BLUE_FLY.get());
            f.refresh(++tick);
            helper.assertTrue(!f.frame.contains(first), "Removed members leave the ring");
            f.assertOrder(List.of(second, third, fourth));
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final FlyOwnerFrame frame;
        final List<AbstractFlyFamiliarEntity> entities = new ArrayList<>();
        long lastTick = Long.MIN_VALUE;
        double expectedPhase;

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fly_order"));
            owner.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 3, 1))));
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            frame = FlyOwnerFrame.get(owner);
        }

        AbstractFlyFamiliarEntity add(EntityType<? extends AbstractFlyFamiliarEntity> type) {
            AbstractFlyFamiliarEntity fly = create(type);
            admit(fly);
            return fly;
        }

        AbstractFlyFamiliarEntity create(EntityType<? extends AbstractFlyFamiliarEntity> type) {
            AbstractFlyFamiliarEntity fly = type.create(helper.getLevel());
            if (fly == null) throw new AssertionError("Missing fly factory");
            fly.setOwner(owner);
            fly.placeAtOwner();
            entities.add(fly);
            return fly;
        }

        void admit(AbstractFlyFamiliarEntity fly) {
            data.addCount(fly.getFamiliarType(), FAMILIAR_SOURCE, 1);
            data.registerEntity(fly.getFamiliarType(), FAMILIAR_SOURCE, fly.getUUID());
            helper.assertTrue(helper.getLevel().addFreshEntity(fly), "The mixed fly is admitted");
        }

        void remove(AbstractFlyFamiliarEntity fly) {
            data.removeEntity(fly.getFamiliarType(), fly.getUUID());
            data.addCount(fly.getFamiliarType(), FAMILIAR_SOURCE, -1);
            fly.discardSilently();
        }

        void assertOrder(List<AbstractFlyFamiliarEntity> expected) {
            Vec3 center = owner.getBoundingBox().getCenter();
            double radius = FlyOrbitLayout.radius(owner.getBbWidth(), expected.size());
            for (int i = 0; i < expected.size(); i++) {
                AbstractFlyFamiliarEntity fly = expected.get(i);
                Vec3 wanted = FlyOrbitLayout.position(center, radius, fly.getBbHeight(), i, expected.size(), expectedPhase);
                helper.assertTrue(frame.contains(fly) && frame.position(fly).distanceTo(wanted) < 1e-7,
                        "Admission order determines ring position " + i);
            }
        }

        void refresh(long tick) {
            if (lastTick != tick) {
                if (lastTick != Long.MIN_VALUE) {
                    expectedPhase = FlyOrbitLayout.advance(expectedPhase,
                            FlyOrbitLayout.radius(owner.getBbWidth(), data.getEntries().stream()
                                    .mapToInt(entry -> entry.getEntityCount()).sum()));
                }
                lastTick = tick;
            }
            frame.refresh(owner, tick);
        }

        @Override
        public void close() {
            entities.forEach(AbstractFlyFamiliarEntity::discardSilently);
            data.clearRequirements();
            owner.discard();
        }
    }
}
