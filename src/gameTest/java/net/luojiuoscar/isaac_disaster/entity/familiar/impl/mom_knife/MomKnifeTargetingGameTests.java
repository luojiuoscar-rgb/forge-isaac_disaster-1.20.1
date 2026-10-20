package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_mom_knife")
@PrefixGameTestTemplate(false)
public final class MomKnifeTargetingGameTests {
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();
    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", setupTicks = 5)
    public static void ownerSharesThePoolAndOtherKnivesRandomlyUseTheSameList(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 0)) {
            Cow near = f.cow(0, -2);
            Cow far = f.cow(0, -3.8);
            MomKnifeTargeting first = MomKnifeTargeting.get(f.owner);
            MomKnifeTargeting second = MomKnifeTargeting.get(f.owner);
            helper.assertTrue(first == second, "All knives of one owner share one candidate pool");
            var selected = f.choose(first, 0);
            helper.assertTrue(selected == near, "The first knife chooses the nearest candidate; owner="
                    + f.diagnostic(near, selected));
            Set<UUID> chosen = new HashSet<>();
            for (int attempt = 0; attempt < 64; attempt++) {
                var target = f.choose(second, 1);
                helper.assertTrue(target == near || target == far, "Other knives choose from the shared list");
                chosen.add(target.getUUID());
            }
            helper.assertTrue(chosen.size() == 2, "Random knives may choose either candidate rather than always the nearest");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", setupTicks = 5)
    public static void rangeUsesTheOwnerAndStillIncludesNonFriendlyAnimals(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 0)) {
            Cow validAnimal = f.cow(0, -2);
            Cow friendly = f.cow(0, -1);
            f.owner.getScoreboard().addPlayerToTeam(f.owner.getScoreboardName(), f.team);
            f.owner.getScoreboard().addPlayerToTeam(friendly.getScoreboardName(), f.team);
            f.cow(0, 2);
            Cow outer = f.cow(0, -4.1);
            f.cow(3.5, -3.5);
            var selected = MomKnifeTargeting.get(f.owner).select(f.owner,
                    f.owner.position().add(0, 0, -6), 0, f.random, 1);
            helper.assertTrue(selected == validAnimal || selected == outer,
                    "The one-layer 5.5-block rear region includes the outer knife slot; owner="
                            + f.diagnostic(outer, selected));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", timeoutTicks = 30, setupTicks = 5)
    public static void newCandidatesAppearOnlyAtTheNextFiveTickBatch(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 0);
        Cow far = f.cow(0, -3);
        MomKnifeTargeting pool = MomKnifeTargeting.get(f.owner);
        helper.assertTrue(f.choose(pool, 0) == far, "Initial scan discovers the far candidate");
        Cow near = f.cow(0, -1.5);
        helper.assertTrue(f.choose(MomKnifeTargeting.get(f.owner), 0) == far,
                "Another knife in the same tick reuses the scanned candidates");
        helper.runAfterDelay(4, () -> helper.assertTrue(f.choose(pool, 0) == far, "No new world scan before five ticks"));
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(f.choose(pool, 0) == near, "The fifth tick refreshes the owner-wide candidate pool");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", setupTicks = 5)
    public static void cachedTargetsAreRevalidatedBeforeLocking(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 0)) {
            Cow near = f.cow(0, -1.5);
            Cow far = f.cow(0, -3);
            MomKnifeTargeting pool = MomKnifeTargeting.get(f.owner);
            helper.assertTrue(f.choose(pool, 0) == near, "Near candidate is initially valid");
            near.discard();
            var selected = f.choose(pool, 0);
            helper.assertTrue(selected == far, "Death invalidates a cached candidate even in the same tick; owner="
                    + f.diagnostic(far, selected));
            f.owner.setYRot(180);
            helper.assertTrue(f.choose(pool, 0) == null, "Turning excludes cached candidates now in front of the owner");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", setupTicks = 5)
    public static void differentOwnersDoNotShareCandidates(GameTestHelper helper) {
        try (Fixture first = new Fixture(helper, 0); Fixture second = new Fixture(helper, 12, first.owner.getY())) {
            Cow firstTarget = first.cow(0, -2);
            Cow secondTarget = second.cow(0, -2);
            MomKnifeTargeting firstPool = MomKnifeTargeting.get(first.owner);
            MomKnifeTargeting secondPool = MomKnifeTargeting.get(second.owner);
            helper.assertTrue(firstPool != secondPool, "Each owner has an independent pool");
            var firstChoice = first.choose(firstPool, 0);
            var secondChoice = second.choose(secondPool, 0);
            helper.assertTrue(firstChoice == firstTarget && secondChoice == secondTarget,
                    "Each pool selects within its own owner's region; first=" + first.diagnostic(firstTarget, firstChoice)
                            + ", second=" + second.diagnostic(secondTarget, secondChoice));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "mom_knife_targeting", setupTicks = 5)
    public static void halfBlockLayerGrowthLimitsTheSharedSearchRegion(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, 0)) {
            Cow target = f.cow(0, -5.55);
            target.setPos(f.owner.getBoundingBox().getCenter().add(0, -target.getBbHeight() * 0.5, -5.55));
            MomKnifeTargeting pool = MomKnifeTargeting.get(f.owner);
            helper.assertTrue(pool.select(f.owner, f.owner.position(), 0, f.random, 1) == null,
                    "One layer excludes a target beyond 5.5 blocks");
            helper.assertTrue(pool.select(f.owner, f.owner.position(), 0, f.random, 2) == target,
                    "Two layers refresh the pool immediately and include a target inside six blocks");
            target.setPos(target.position().add(0, 0, -0.5));
            helper.assertTrue(pool.select(f.owner, f.owner.position(), 0, f.random, 2) == null,
                    "Two layers exclude a cached target beyond six blocks");
            helper.assertTrue(pool.select(f.owner, f.owner.position(), 0, f.random, 3) == target,
                    "Three layers include the target inside 6.5 blocks");
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final RandomSource random = RandomSource.create(17);
        final List<Cow> cows = new ArrayList<>();
        final PlayerTeam team;

        Fixture(GameTestHelper helper, double offset) {
            this(helper, offset, 1000 + NEXT_FIXTURE.getAndIncrement() * 32);
        }

        Fixture(GameTestHelper helper, double offset, double altitude) {
            this.helper = helper;
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, "kb-" + id.toString().substring(0, 8)));
            // Spawn chunks are already tracked; template chunks can still be hidden during a batch.
            // Independent heights keep these fixtures separate from every neighboring test.
            BlockPos anchor = helper.getLevel().getSharedSpawnPos();
            owner.setPos(anchor.getX() + 0.5 + offset, altitude, anchor.getZ() + 0.5);
            owner.setYRot(0);
            team = owner.getScoreboard().addPlayerTeam("kt-" + id.toString().substring(0, 8));
        }

        Cow cow(double x, double z) {
            Cow cow = EntityType.COW.create(helper.getLevel());
            helper.assertTrue(cow != null, "Cow is created");
            cow.setNoAi(true);
            cow.setNoGravity(true);
            cow.setPos(owner.position().add(x, 0, z));
            helper.assertTrue(helper.getLevel().addFreshEntity(cow), "Cow enters the world");
            cows.add(cow);
            return cow;
        }

        String diagnostic(Cow expected, net.minecraft.world.entity.LivingEntity selected) {
            return "owner=" + owner.position() + ", expected=" + expected + ", selected=" + selected
                    + ", lookup=" + helper.getLevel().getEntity(expected.getUUID())
                    + ", valid=" + MomKnifeTargeting.validTarget(owner, expected);
        }

        net.minecraft.world.entity.LivingEntity choose(MomKnifeTargeting pool, int index) {
            return pool.select(owner, owner.position().add(0, owner.getBbHeight() * 0.7, -0.95), index, random, 1);
        }

        @Override public void close() {
            for (Cow cow : cows) cow.discard();
            owner.getScoreboard().removePlayerTeam(team);
            owner.discard();
        }
    }
}
