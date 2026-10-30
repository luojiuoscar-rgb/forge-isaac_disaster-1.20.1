package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("isaac_disaster_brother_bobby")
@PrefixGameTestTemplate(false)
public final class BrotherBobbyGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final ResourceLocation BOBBY = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "brother_bobby");

    @GameTest(template = "trajectory_empty")
    public static void commandSpawnsOneOwnedFamiliarAndRemovalCleansItUp(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(BOBBY), "Both entity registries expose Brother Bobby");
            f.command("add isaac_disaster:brother_bobby 3");
            helper.assertTrue(f.data.getEffectiveCount(BOBBY) == 3, "All three requested babies receive slots");
            BrotherBobbyEntity bobby = f.spawn();
            helper.assertTrue(bobby.getOwner() == f.owner && bobby.isValidFamiliar(), "The spawned familiar is owned and linked");
            helper.assertTrue(Math.abs(bobby.getBbWidth() - 0.525F) < 0.0001
                            && Math.abs(bobby.getBbHeight() - 0.8625F) < 0.0001,
                    "The runtime dimensions use sixty percent of the exported size");
            assertPosition(helper, f.placement(), bobby.position());
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(f.owner) && FamiliarHelper.spawnNextMissingFamiliar(f.owner),
                    "Further requested babies are admitted");
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(f.owner), "The fulfilled request admits no extra entity");
            f.command("remove isaac_disaster:brother_bobby 3");
            helper.assertTrue(bobby.isRemoved() && f.data.getEffectiveCount(BOBBY) == 0,
                    "Removing the requirement immediately discards the entity");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void realEntityContinuouslyFollowsAndMaintainsOneBlockVerticalDistance(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, 1);
            BrotherBobbyEntity bobby = f.spawn();
            Vec3 target = f.hover();
            bobby.setPos(target.add(0, 2, 0));
            Vec3 previous = bobby.position();
            double previousSpeed = 0;
            for (int i = 0; i < 100; i++) {
                bobby.tick();
                double speed = bobby.position().distanceTo(previous);
                helper.assertTrue(bobby.position().y <= previous.y
                                && bobby.position().y >= target.y + 0.99,
                        "Vertical pursuit approaches a stable parking location without rebound");
                helper.assertTrue(Math.abs(speed - previousSpeed) < 0.3,
                        "The real entity accelerates and brakes gradually");
                previous = bobby.position();
                previousSpeed = speed;
            }
            helper.assertTrue(Math.abs(previous.distanceTo(target) - 1.0) < 0.001 && bobby.getDeltaMovement().equals(Vec3.ZERO),
                    "The follower finishes braking inside the comfort radius");
            bobby.setPos(target.add(0, 3, 0));
            bobby.tick();
            helper.assertTrue(bobby.position().y < target.y + 3, "Following resumes at three blocks without an outer gate");
            helper.assertTrue(bobby.noPhysics && bobby.isNoGravity(), "Following keeps the airborne physics policy");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void movementFacesTheOwnerAndStoppingKeepsTheHeading(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, 1);
            BrotherBobbyEntity bobby = f.spawn();
            bobby.setPos(f.hover().add(5, 0, 0));
            bobby.tick();
            helper.assertTrue(Math.abs(bobby.getYRot() - 90) < 0.001, "Westward pursuit faces west");
            for (int i = 0; i < 100; i++) bobby.tick();
            helper.assertTrue(Math.abs(bobby.getYRot() - 90) < 0.001, "Stopping does not reset heading");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void recallPreservesTheEntityAndResetsPursuit(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.data.addCount(BOBBY, FAMILIAR_SOURCE, 1);
            BrotherBobbyEntity bobby = f.spawn();
            UUID original = bobby.getUUID();
            bobby.setPos(f.hover().add(0, 5, 0));
            bobby.tick();
            Vec3 distant = f.owner.position().add(0, 17, 0);
            bobby.setPos(distant);
            bobby.tick();
            helper.assertTrue(bobby.isRecallPending() && !bobby.isRemoved(), "A distant familiar waits for shared recall");
            assertPosition(helper, distant, bobby.position());
            helper.assertTrue(FamiliarHelper.tickFamiliarAdmissions(f.owner), "The admission scheduler handles recall");
            assertPosition(helper, f.placement(), bobby.position());
            helper.assertTrue(original.equals(bobby.getUUID()) && f.data.containsEntity(BOBBY, original)
                            && f.data.getCount(BOBBY) == 1 && !bobby.isRecallPending(),
                    "Recall preserves UUID, ownership and the requested count");
            bobby.setPos(f.hover().add(0, 3, 0));
            bobby.tick();
            helper.assertTrue(bobby.position().y < f.hover().y + 3 && bobby.getDeltaMovement().length() < 0.2,
                    "Recall clears momentum but continuous close following still resumes gently");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void clearingTheRosterDiscardsTheRuntimeEntity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.command("add isaac_disaster:brother_bobby");
            BrotherBobbyEntity bobby = f.spawn();
            f.command("clear");
            helper.assertTrue(bobby.isRemoved() && f.data.getCount(BOBBY) == 0,
                    "The existing clear command removes the new type too");
        }
        helper.succeed();
    }

    private static void assertPosition(GameTestHelper helper, Vec3 expected, Vec3 actual) {
        helper.assertTrue(expected.distanceToSqr(actual) < 1.0e-10,
                "Expected " + expected + ", got " + actual);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bobby-test"));
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 1, 1));
            owner.setPos(anchor.getX() + 0.5, 100, anchor.getZ() + 0.5);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        private Vec3 hover() {
            return owner.position().add(0, owner.getBbHeight() * 0.55, 0);
        }

        private Vec3 placement() { return hover().add(0, 0, -1.0); }

        private BrotherBobbyEntity spawn() {
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "The real scheduler spawns Brother Bobby");
            return (BrotherBobbyEntity) helper.getLevel().getEntity(data.getEntry(BOBBY).orElseThrow().getEntityId(0));
        }

        private void command(String operation) {
            var source = owner.createCommandSourceStack().withPermission(2).withSuppressedOutput();
            int result = helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "isd familiar @s " + operation);
            helper.assertTrue(result == 1, "The existing familiar command accepts " + operation);
        }

        @Override
        public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            owner.discard();
        }
    }
}
