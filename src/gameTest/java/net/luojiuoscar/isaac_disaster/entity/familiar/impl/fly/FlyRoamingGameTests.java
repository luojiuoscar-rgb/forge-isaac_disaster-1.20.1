package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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

/** Real server regressions for a blocked ring, indoor wandering and normal-state recovery. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FlyRoamingGameTests {
    private static final ResourceLocation BLUE_FLY =
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "fly_roam", timeoutTicks = 190)
    public static void blockedRingRoamsInsideRoomRetriesThenRecovers(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            helper.assertTrue(AbstractFlyFamiliarEntity.FlyState.ORBIT.ordinal() == 0
                            && AbstractFlyFamiliarEntity.FlyState.CHASE.ordinal() == 1
                            && AbstractFlyFamiliarEntity.FlyState.RETURN.ordinal() == 2
                            && AbstractFlyFamiliarEntity.FlyState.ROAM.ordinal() == 3,
                    "Adding ROAM preserves all existing synchronized state byte values");
            fixture.assertBlockedRing();
            helper.onEachTick(new RoomJourney(fixture));
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_roam", timeoutTicks = 90)
    public static void hostileInterruptsIndoorRoamImmediately(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            helper.onEachTick(new Runnable() {
                private Vec3 previous = fixture.fly.position();
                private int elapsed;
                private int spawnedAt = -1;
                private boolean finished;

                @Override
                public void run() {
                    if (finished) return;
                    try {
                        fixture.checkMovement(previous, true);
                        previous = fixture.fly.position();
                        elapsed++;
                        if (spawnedAt < 0
                                && fixture.fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ROAM) {
                            // A husk does not burn in daylight and remains beyond contact range,
                            // so CHASE is observable even though this room blocks its destination.
                            fixture.hostile(fixture.owner.position().add(3.5, 0, 0));
                            spawnedAt = elapsed;
                        } else if (spawnedAt >= 0
                                && fixture.fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.CHASE) {
                            helper.assertTrue(elapsed - spawnedAt <= 5,
                                    "A new hostile interrupts the roam timer on the next shared target scan");
                            finished = true;
                            fixture.close();
                            helper.succeed();
                            return;
                        }
                        helper.assertTrue(spawnedAt < 0 ? elapsed < 45 : elapsed - spawnedAt <= 5,
                                "An indoor fly enters ROAM and promptly acquires a new hostile" + fixture.describe());
                    } catch (RuntimeException | Error failure) {
                        finished = true;
                        fixture.close();
                        throw failure;
                    }
                }
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_roam", timeoutTicks = 120)
    public static void hugeOwnerKeepsFlyRoamingWithinRecallRange(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper, true);
        try {
            helper.assertTrue(fixture.owner.getBbWidth() > 4.7 && fixture.owner.getBbHeight() > 14.3,
                    "The huge owner has actual eightfold player dimensions");
            FlyOwnerFrame frame = FlyOwnerFrame.get(fixture.owner);
            frame.refresh(fixture.owner, helper.getLevel().getGameTime());
            helper.assertTrue(frame.position(fixture.fly).distanceTo(fixture.owner.position()) > 16,
                    "The owner's nominal ring exceeds the shared sixteen-block familiar recall range");
            helper.onEachTick(new Runnable() {
                private Vec3 previous = fixture.fly.position();
                private int elapsed;
                private boolean sawRoam;
                private boolean finished;

                @Override
                public void run() {
                    if (finished) return;
                    try {
                        fixture.checkMovement(previous, false);
                        previous = fixture.fly.position();
                        elapsed++;
                        helper.assertTrue(fixture.fly.distanceToSqr(fixture.owner) <= 256.00001,
                                "A huge owner never sends its fly outside the shared recall distance"
                                        + fixture.describe());
                        helper.assertTrue(fixture.data.getCount(BLUE_FLY) == 1
                                        && fixture.data.containsEntity(BLUE_FLY, fixture.fly.getUUID())
                                        && fixture.data.getEntry(BLUE_FLY).orElseThrow().getEntityCount() == 1,
                                "The huge-owner fly remains requested and registered throughout real ticks"
                                        + fixture.describe());
                        sawRoam |= fixture.fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ROAM;
                        helper.assertTrue(sawRoam || elapsed < 5,
                                "An unreachable nominal ring triggers roaming immediately without travelling outward"
                                        + fixture.describe());
                        if (elapsed >= 100) {
                            helper.assertTrue(sawRoam, "The huge owner's fly uses the reachable roaming fallback");
                            finished = true;
                            fixture.close();
                            helper.succeed();
                        }
                    } catch (RuntimeException | Error failure) {
                        finished = true;
                        fixture.close();
                        throw failure;
                    }
                }
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "fly_roam", timeoutTicks = 130)
    public static void roamingUsesWiderHorizontalAndVerticalSpaceWhenAvailable(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        helper.onEachTick(new Runnable() {
            private Vec3 previous = fixture.fly.position();
            private double maxRadius;
            private double minY = Double.POSITIVE_INFINITY;
            private double maxY = Double.NEGATIVE_INFINITY;
            private boolean opened;
            private boolean finished;

            @Override
            public void run() {
                if (finished) return;
                try {
                    fixture.checkMovement(previous, !opened);
                    previous = fixture.fly.position();
                    if (fixture.fly.getFlyState() == AbstractFlyFamiliarEntity.FlyState.ROAM) {
                        if (!opened) {
                            opened = true;
                            fixture.removeRoom();
                        }
                        Vec3 offset = fixture.fly.getBoundingBox().getCenter()
                                .subtract(fixture.owner.getBoundingBox().getCenter());
                        maxRadius = Math.max(maxRadius, offset.horizontalDistance());
                        minY = Math.min(minY, offset.y);
                        maxY = Math.max(maxY, offset.y);
                        helper.assertTrue(offset.horizontalDistance() <= 2.00001 && Math.abs(offset.y) <= 0.90001,
                                "Open-space wandering stays within its enlarged owner-relative range");
                    } else if (opened) {
                        helper.assertTrue(maxRadius > 1.4 && maxY - minY > 0.8,
                                "A roam spell explores beyond the old horizontal and vertical limits"
                                        + " [radius=" + maxRadius + ", vertical=" + (maxY - minY) + "]");
                        finished = true;
                        fixture.close();
                        helper.succeed();
                    }
                } catch (RuntimeException | Error failure) {
                    finished = true;
                    fixture.close();
                    throw failure;
                }
            }
        });
    }

    private static final class RoomJourney implements Runnable {
        private final Fixture fixture;
        private Vec3 previous;
        private Vec3 firstRoamPosition;
        private int elapsed;
        private int enteredAt = -1;
        private int retryAt = -1;
        private double minRoamY = Double.POSITIVE_INFINITY;
        private double maxRoamY = Double.NEGATIVE_INFINITY;
        private double maxHorizontalExcursion;
        private double maxRoamStep;
        private boolean roomRemoved;
        private boolean finished;

        private RoomJourney(Fixture fixture) {
            this.fixture = fixture;
            previous = fixture.fly.position();
        }

        @Override
        public void run() {
            if (finished) return;
            try {
                fixture.checkMovement(previous, !roomRemoved);
                Vec3 current = fixture.fly.position();
                double step = current.distanceTo(previous);
                previous = current;
                elapsed++;
                var state = fixture.fly.getFlyState();
                if (enteredAt < 0) {
                    if (state == AbstractFlyFamiliarEntity.FlyState.ROAM) {
                        enteredAt = elapsed;
                        firstRoamPosition = current;
                        fixture.helper.assertTrue(elapsed <= 3,
                                "A blocked actual ring immediately triggers ROAM"
                                        + fixture.describe());
                    } else {
                        fixture.helper.assertTrue(elapsed < 3,
                                "A blocked ring immediately switches to roaming" + fixture.describe());
                    }
                }
                if (enteredAt >= 0 && retryAt < 0) {
                    int duration = elapsed - enteredAt;
                    if (state == AbstractFlyFamiliarEntity.FlyState.ROAM) {
                        Vec3 ownerCenter = fixture.owner.getBoundingBox().getCenter();
                        Vec3 bodyCenter = fixture.fly.getBoundingBox().getCenter();
                        fixture.helper.assertTrue(bodyCenter.subtract(ownerCenter).horizontalDistanceSqr() <= 4.00001,
                                "Indoor wandering stays within two horizontal blocks of the moving owner"
                                        + fixture.describe());
                        fixture.helper.assertTrue(Math.abs(bodyCenter.y - ownerCenter.y) <= 0.90001,
                                "Indoor wandering stays within 0.9 blocks above or below the owner's waist"
                                        + fixture.describe());
                        fixture.helper.assertTrue(step <= 0.30001,
                                "Idle roaming uses at most 0.3 blocks per tick" + fixture.describe());
                        minRoamY = Math.min(minRoamY, bodyCenter.y);
                        maxRoamY = Math.max(maxRoamY, bodyCenter.y);
                        maxHorizontalExcursion = Math.max(maxHorizontalExcursion,
                                current.subtract(firstRoamPosition).horizontalDistance());
                        maxRoamStep = Math.max(maxRoamStep, step);
                        fixture.helper.assertTrue(duration <= 81,
                                "ROAM has a finite retry timer" + fixture.describe());
                        // Move slowly while wandering. The physical room stays fixed, and
                        // total owner displacement remains much smaller than its free margin.
                        if (duration < 40) fixture.owner.setPos(fixture.owner.position().add(0.003, 0, 0));
                        if (duration == 30) {
                            fixture.removeRoom();
                            roomRemoved = true;
                        }
                    } else {
                        fixture.helper.assertTrue((state == AbstractFlyFamiliarEntity.FlyState.RETURN
                                        || state == AbstractFlyFamiliarEntity.FlyState.ORBIT)
                                        && duration >= 40 && duration <= 81,
                                "A 40-80 tick roam is followed by an observable RETURN attempt"
                                        + ", duration=" + duration + fixture.describe());
                        fixture.helper.assertTrue(maxHorizontalExcursion > 0.4 && maxRoamY - minRoamY > 0.8,
                                "Roaming visibly explores horizontal and vertical space instead of pushing the ring"
                                        + ", horizontal=" + maxHorizontalExcursion
                                        + ", vertical=" + (maxRoamY - minRoamY) + fixture.describe());
                        fixture.helper.assertTrue(maxRoamStep >= 0.299,
                                "An unobstructed roam segment uses the full 0.3 block movement budget"
                                        + ", maxStep=" + maxRoamStep + fixture.describe());
                        retryAt = elapsed;
                    }
                } else if (retryAt >= 0) {
                    fixture.helper.assertTrue(state != AbstractFlyFamiliarEntity.FlyState.ROAM,
                            "Clearing the room lets the normal return/orbit behavior recover" + fixture.describe());
                    if (state == AbstractFlyFamiliarEntity.FlyState.ORBIT && step > 0.1) {
                        finished = true;
                        fixture.close();
                        fixture.helper.succeed();
                        return;
                    }
                    fixture.helper.assertTrue(elapsed - retryAt < 60,
                            "Removing finite room walls restores a moving normal orbit" + fixture.describe());
                }
                fixture.helper.assertTrue(elapsed < 185, "The indoor roaming journey completes in bounded time");
            } catch (RuntimeException | Error failure) {
                finished = true;
                fixture.close();
                throw failure;
            }
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final BlueFlyEntity fly;
        private final BlockPos origin;
        private final AABB interior;
        private final List<Entity> entities = new ArrayList<>();
        private final List<BlockPos> room = new ArrayList<>();
        private final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
        private boolean closed;

        private Fixture(GameTestHelper helper) {
            this(helper, false);
        }

        private Fixture(GameTestHelper helper, boolean hugeOwner) {
            this.helper = helper;
            Vec3 anchor = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1)));
            origin = new BlockPos((int) Math.floor(anchor.x),
                    hugeOwner ? 280 : 220 + NEXT_FIXTURE.getAndIncrement() * 16,
                    (int) Math.floor(anchor.z));
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fly_roam"));
            owner.setGameMode(GameType.SURVIVAL);
            owner.setInvulnerable(false);
            owner.getAbilities().invulnerable = false;
            owner.setHealth(owner.getMaxHealth());
            owner.setPos(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            fly = new SeededFly(helper);
            interior = new AABB(origin.offset(-1, 0, -1), origin.offset(2, 3, 2));
            try {
                // Separate Y bands isolate concurrently running tests and their owner scans.
                int reach = hugeOwner ? 20 : 9;
                int clearHeight = hugeOwner ? 18 : 6;
                AABB arena = new AABB(origin.offset(-reach, -3, -reach),
                        origin.offset(reach + 1, clearHeight + 1, reach + 1));
                for (Entity stale : helper.getLevel().getEntities((Entity) null, arena,
                        entity -> !(entity instanceof Player))) stale.discard();
                for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-reach, -3, -reach),
                        origin.offset(reach, clearHeight, reach))) {
                    replace(pos, Blocks.AIR.defaultBlockState());
                }
                for (int x = -2; !hugeOwner && x <= 2; x++) {
                    for (int y = -1; y <= 3; y++) {
                        for (int z = -2; z <= 2; z++) {
                            if (Math.abs(x) == 2 || Math.abs(z) == 2 || y == -1 || y == 3) {
                                BlockPos pos = origin.offset(x, y, z);
                                room.add(pos);
                                replace(pos, Blocks.STONE.defaultBlockState());
                            }
                        }
                    }
                }
                if (hugeOwner) ScaleUtils.setScale(owner, 8);
                fly.setOwner(owner);
                fly.setPos(owner.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0));
                data.addCount(BLUE_FLY, 1);
                entities.add(fly);
                helper.assertTrue(helper.getLevel().addFreshEntity(fly), "The server accepts the owned roaming fly");
                data.registerEntity(BLUE_FLY, fly.getUUID());
            } catch (RuntimeException | Error failure) {
                close();
                throw failure;
            }
        }

        private void assertBlockedRing() {
            FlyOwnerFrame frame = FlyOwnerFrame.get(owner);
            frame.refresh(owner, helper.getLevel().getGameTime());
            Vec3 slot = frame.position(fly);
            helper.assertTrue(!FlyNavigation.clearPath(fly, slot, slot),
                    "The requested ring position intersects the actual room wall");
        }

        private void hostile(Vec3 position) {
            Husk hostile = EntityType.HUSK.create(helper.getLevel());
            if (hostile == null) throw new AssertionError("The fixture creates a husk");
            hostile.setNoAi(true);
            hostile.setNoGravity(true);
            hostile.setPos(position);
            entities.add(hostile);
            helper.assertTrue(helper.getLevel().addFreshEntity(hostile), "The server accepts the stationary hostile");
        }

        private void checkMovement(Vec3 previous, boolean roomPresent) {
            helper.assertTrue(!fly.isRemoved() && fly.isValidFamiliar(), "The owned fly remains alive" + describe());
            helper.assertTrue(fly.position().distanceTo(previous) <= 0.60001,
                    "Every actual server tick obeys the 0.6 block maximum without teleporting" + describe());
            helper.assertTrue(FlyNavigation.clearPath(fly, previous, fly.position()),
                    "Every actual movement segment respects the complete body collision sweep" + describe());
            if (roomPresent) {
                AABB body = fly.getBoundingBox();
                helper.assertTrue(body.minX >= interior.minX - 0.00001 && body.maxX <= interior.maxX + 0.00001
                                && body.minY >= interior.minY - 0.00001 && body.maxY <= interior.maxY + 0.00001
                                && body.minZ >= interior.minZ - 0.00001 && body.maxZ <= interior.maxZ + 0.00001,
                        "The fly body stays inside the independent three-by-three room bounds" + describe());
            }
        }

        private String describe() {
            return " [owner=" + owner.position() + ", fly=" + fly.position() + ", state=" + fly.getFlyState()
                    + ", removed=" + fly.isRemoved() + ", valid=" + fly.isValidFamiliar() + "]";
        }

        private void removeRoom() {
            for (BlockPos pos : room) replace(pos, Blocks.AIR.defaultBlockState());
        }

        private void replace(BlockPos pos, BlockState replacement) {
            BlockState original = helper.getLevel().getBlockState(pos);
            if (original.equals(replacement)) return;
            originalBlocks.putIfAbsent(pos.immutable(), original);
            helper.getLevel().setBlock(pos, replacement, 2);
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            for (Entity entity : entities) if (!entity.isRemoved()) entity.discard();
            originalBlocks.forEach((pos, state) -> helper.getLevel().setBlock(pos, state, 2));
            data.clearRequirements();
            owner.discard();
        }
    }

    private static final class SeededFly extends BlueFlyEntity {
        private SeededFly(GameTestHelper helper) {
            super(ModEntities.BLUE_FLY.get(), helper.getLevel());
            random.setSeed(0x5EEDBEEFL);
        }
    }
}
