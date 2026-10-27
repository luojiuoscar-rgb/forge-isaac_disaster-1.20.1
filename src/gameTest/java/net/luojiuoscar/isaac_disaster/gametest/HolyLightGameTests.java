package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Real-world checks for placement, targeting and the single server damage pulse. */
@GameTestHolder("isaac_disaster_holylight")
@PrefixGameTestTemplate(false)
public final class HolyLightGameTests {
    private static final float DAMAGE = 6.0F;
    private static final double EPSILON = 0.01D;

    private HolyLightGameTests() {
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 180)
    public static void warningWaitsThirtyFiveWorldTicksThenDamagesOnlyOnceAndExpires(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Cow target = fixture.cow(fixture.center);
        helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "Supported warning is created");
        long start = fixture.level.getGameTime();
        fixture.manager.tick(fixture.level);
        health(helper, target, 20, "Creation does not deal warning damage");
        at(helper, fixture, 34, () -> {
            helper.assertTrue(fixture.level.getGameTime() - start == 34,
                    "The boundary check advances real server world time");
            fixture.manager.tick(fixture.level);
            health(helper, target, 20, "There is no damage through age 34");
        });
        at(helper, fixture, 35, () -> {
            fixture.manager.tick(fixture.level);
            health(helper, target, 14, "Age 35 fires the configured six damage");
            for (int i = 0; i < 10; i++) fixture.manager.tick(fixture.level);
            health(helper, target, 14, "Repeated processing of the same world tick cannot refire");
        });
        at(helper, fixture, 40, () -> {
            fixture.manager.tick(fixture.level);
            health(helper, target, 14, "The remaining firing frames do not deal repeated damage");
        });
        at(helper, fixture, 149, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().size() == 1, "The final scorch tick remains active");
            health(helper, target, 14, "Scorch does not damage or ignite its target");
            helper.assertTrue(!target.isOnFire(), "Holy Light does not apply fire ticks");
        });
        at(helper, fixture, 150, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().size() == 0 && fixture.manager.snapshots().isEmpty(),
                    "35 warning, 15 firing and 100 scorch ticks end without retained snapshots");
            fixture.close();
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void skippedWarningDamagesInsideSpawnAndDoesNotRepeat(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow target = fixture.cow(fixture.center);
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "Immediate strike is created");
            health(helper, target, 14, "skipWarning resolves damage before spawn returns");
            for (int i = 0; i < 12; i++) fixture.manager.tick(fixture.level);
            health(helper, target, 14, "Repeated manager processing cannot replay an immediate strike");
            helper.assertTrue(fixture.manager.snapshots().get(0).skipWarning(), "The visual skips warning too");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void radiusUsesTargetBoundingBoxAndExcludesTheOutsideBox(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow edge = fixture.cow(fixture.center.add(0.8D, 0, 0));
            Cow outside = fixture.cow(fixture.center.add(-0.9D, 0, 0));
            Cow tangent = fixture.cow(fixture.center);
            tangent.setPos(fixture.center.add(0.4D + tangent.getBbWidth() / 2.0D, 0, 0));
            helper.assertTrue(fixture.strike(fixture.center, 0.4D, false), "Warning samples the exact impact height");
            double impactY = fixture.manager.snapshots().get(0).position().y;
            fixture.manager.clear();
            Cow topBoundary = fixture.cow(new Vec3(fixture.center.x, impactY + 2.5D, fixture.center.z));
            helper.assertTrue(fixture.strike(fixture.center, 0.4D, true), "Small strike is created");
            health(helper, edge, 14, "A cow whose center is outside but bounding box intersects is hit");
            health(helper, tangent, 14, "An exactly tangent bounding-box face is included by the broad-phase query");
            health(helper, topBoundary, 14, "The exact upper vertical boundary is included by the broad-phase query");
            health(helper, outside, 20, "A cow whose nearest bounding-box face is outside is excluded");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void independentImmediateStrikesBothHarmTheSameTarget(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow target = fixture.cow(fixture.center);
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "First strike is created");
            helper.assertTrue(target.invulnerableTime > 0, "The first hit enters vanilla hurt cooldown");
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "Second strike is independent");
            health(helper, target, 8, "The dedicated damage type permits both same-tick damage pulses");
            helper.assertTrue(fixture.manager.snapshots().size() == 2, "Overlapping strikes retain separate visuals");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void selfTeamAndOwnedPetAreExcludedWhileEnemyIsHit(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.owner.setPos(fixture.center);
            Cow ally = fixture.cow(fixture.center.add(0.1D, 0, 0));
            Cow enemy = fixture.cow(fixture.center.add(-0.1D, 0, 0));
            Wolf pet = new Wolf(EntityType.WOLF, fixture.level);
            pet.setTame(true);
            pet.setOwnerUUID(fixture.owner.getUUID());
            fixture.addMob(pet, fixture.center.add(0, 0, 0.1D));
            float ownerHealth = fixture.owner.getHealth();
            float petHealth = pet.getHealth();
            var scoreboard = fixture.level.getScoreboard();
            var team = scoreboard.addPlayerTeam("hl_" + UUID.randomUUID().toString().substring(0, 8));
            scoreboard.addPlayerToTeam(fixture.owner.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
            try {
                helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "Target-rule strike is created");
                health(helper, fixture.owner, ownerHealth, "The caster cannot damage itself");
                health(helper, ally, 20, "An allied mob is excluded by the shared target rules");
                health(helper, pet, petHealth, "The caster's tame pet is excluded");
                health(helper, enemy, 14, "An ordinary nonhostile enemy mob remains a valid target");
            } finally {
                scoreboard.removePlayerTeam(team);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void armorStillReducesDamageAndTargetsDoNotBurn(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow armored = fixture.cow(fixture.center);
            armored.getAttribute(Attributes.ARMOR).setBaseValue(20);
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "Armored-target strike is created");
            helper.assertTrue(armored.getHealth() > 14 && armored.getHealth() < 20,
                    "Armor reduces six damage rather than being bypassed");
            helper.assertTrue(!armored.isOnFire(), "Holy Light damage does not ignite the target");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void wallBlocksDamageAndOtherFloorsAreExcluded(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow behindWall = fixture.cow(fixture.center.add(1.6D, 0, 0));
            Cow visible = fixture.cow(fixture.center.add(-0.6D, 0, 0));
            Cow upstairs = fixture.cow(fixture.center.add(-1.3D, 2, 0));
            Cow downstairs = fixture.cow(fixture.center.add(0, -3, 0));
            BlockPos wall = BlockPos.containing(fixture.center).east();
            fixture.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            fixture.level.setBlockAndUpdate(wall.above(), Blocks.STONE.defaultBlockState());
            fixture.level.setBlockAndUpdate(BlockPos.containing(upstairs.position()).below(), Blocks.STONE.defaultBlockState());
            fixture.level.setBlockAndUpdate(BlockPos.containing(downstairs.position()).below(), Blocks.STONE.defaultBlockState());
            helper.assertTrue(fixture.strike(fixture.center, 2.5D, true), "Wide strike is created");
            health(helper, behindWall, 20, "A collision wall blocks the near-ground radial pulse");
            health(helper, visible, 14, "An unobstructed target inside the same radius is hit");
            health(helper, upstairs, 20,
                    "An upper-floor target inside the vertical range is shielded by its solid floor");
            health(helper, downstairs, 20, "The lower floor bounding box is below the damage cylinder");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void nullCasterIsRejectedWithoutAllocatingAVisual(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Cow target = fixture.cow(fixture.center);
            helper.assertTrue(!fixture.manager.spawn(fixture.level, null, fixture.center, DAMAGE, 0.8D, true),
                    "Creation requires an explicit caster");
            helper.assertTrue(fixture.manager.snapshots().size() == 0 && fixture.manager.snapshots().isEmpty(),
                    "Missing caster cannot allocate a strike");
            health(helper, target, 20, "Rejected creation cannot damage a nearby target");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 50)
    public static void casterRemovedDuringWarningLeavesVisualButCannotDealDamage(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Cow target = fixture.cow(fixture.center);
        helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "Delayed strike is created");
        fixture.level.removePlayerImmediately(fixture.owner, Entity.RemovalReason.DISCARDED);
        at(helper, fixture, 35, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().size() == 1, "An unresolved caster leaves its visual active");
            health(helper, target, 20, "A retained Java reference cannot replace loaded owner UUID resolution");
            fixture.close();
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void placementUsesSlabAndBothStairStepSurfaceHeights(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlockPos support = BlockPos.containing(fixture.center).below();
            fixture.level.setBlockAndUpdate(support, Blocks.STONE_SLAB.defaultBlockState());
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "Bottom slab accepts a strike");
            near(helper, fixture.manager.snapshots().get(0).position().y, support.getY() + 0.5D,
                    "Bottom slab uses its collision surface, not block top");
            fixture.manager.clear();
            fixture.level.setBlockAndUpdate(support,
                    Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
            Vec3 low = new Vec3(support.getX() + 0.25D, support.getY() + 1, support.getZ() + 0.5D);
            Vec3 high = new Vec3(support.getX() + 0.75D, support.getY() + 1, support.getZ() + 0.5D);
            helper.assertTrue(fixture.strike(low, 0.8D, false), "Low stair step accepts a strike");
            near(helper, fixture.manager.snapshots().get(0).position().y, support.getY() + 0.5D,
                    "The low stair step has a half-block surface");
            fixture.manager.clear();
            helper.assertTrue(fixture.strike(high, 0.8D, false), "High stair step accepts a strike");
            near(helper, fixture.manager.snapshots().get(0).position().y, support.getY() + 1,
                    "The high stair step has a full-block surface");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void ceilingClipsBeamAndOpenColumnUsesSixteenBlockLimit(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlockPos ceiling = BlockPos.containing(fixture.center).above(3);
            fixture.level.setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "A strike fits below the ceiling");
            HolyLightSnapshot clipped = fixture.manager.snapshots().get(0);
            near(helper, clipped.position().y + clipped.beamHeight(), ceiling.getY(),
                    "The visible beam ends at the underside collision surface");
            fixture.manager.clear();
            fixture.level.setBlockAndUpdate(ceiling, Blocks.AIR.defaultBlockState());
            helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "An open-column strike is created");
            near(helper, fixture.manager.snapshots().get(0).beamHeight(), 16,
                    "An open beam is capped at sixteen blocks");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void distantGroundAndLiquidStayAirborneWhileInvalidRequestsAreRejected(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            helper.assertTrue(fixture.strike(fixture.center.add(0, 7, 0), 0.8D, false),
                    "A candidate seven blocks above support is valid in the air");
            near(helper, fixture.manager.snapshots().get(0).position().y, fixture.center.y + 7,
                    "Distant support cannot pull an airborne strike down");
            helper.assertTrue(!fixture.manager.snapshots().get(0).grounded(), "Airborne strikes cannot leave scorch");
            fixture.manager.clear();
            Vec3 unsupported = fixture.center.add(0, 20, 0);
            fixture.level.setBlockAndUpdate(BlockPos.containing(unsupported).below(), Blocks.WATER.defaultBlockState());
            helper.assertTrue(fixture.strike(unsupported, 0.8D, false), "Liquid permits a visual but is not collision support");
            helper.assertTrue(!fixture.manager.snapshots().get(0).grounded(), "Liquid cannot generate a scorch decal");
            fixture.manager.clear();
            helper.assertTrue(fixture.strike(fixture.center.add(0, 9, 0), 0.8D, false), "Airborne placement needs no floor");
            fixture.manager.clear();
            helper.assertTrue(!fixture.strike(new Vec3(Double.NaN, fixture.center.y, fixture.center.z), 0.8D, false),
                    "NaN positions are rejected");
            helper.assertTrue(!fixture.strike(fixture.center, 0, false)
                            && !fixture.strike(fixture.center, Double.POSITIVE_INFINITY, false),
                    "Invalid radii are rejected");
            helper.assertTrue(fixture.manager.snapshots().size() == 0 && fixture.manager.snapshots().isEmpty(),
                    "Rejected requests leave no visual state");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 50)
    public static void removingSupportDuringWarningCancelsBeforeDamage(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Cow target = fixture.cow(fixture.center);
        helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "Warning is created on solid support");
        fixture.level.setBlockAndUpdate(BlockPos.containing(fixture.center).below(), Blocks.AIR.defaultBlockState());
        at(helper, fixture, 35, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().size() == 0 && fixture.manager.snapshots().isEmpty(),
                    "Lost support cancels the instance before its warning can fire");
            health(helper, target, 20, "A canceled unsupported strike cannot damage its target");
            fixture.close();
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 50)
    public static void clearRemovesPendingAndFiredStrikesWithoutDelayedDamage(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Cow target = fixture.cow(fixture.center);
        helper.assertTrue(fixture.strike(fixture.center, 0.8D, false), "Pending strike is created");
        helper.assertTrue(fixture.strike(fixture.center, 0.8D, true), "Fired strike is created");
        health(helper, target, 14, "Only the immediate strike has dealt damage");
        fixture.manager.clear();
        helper.assertTrue(fixture.manager.snapshots().size() == 0 && fixture.manager.snapshots().isEmpty(),
                "clear removes pending and fired state");
        at(helper, fixture, 35, () -> {
            fixture.manager.tick(fixture.level);
            health(helper, target, 14, "Cleared warning state cannot fire later");
            helper.assertTrue(fixture.manager.snapshots().size() == 0, "Ticking a cleared manager keeps it empty");
            fixture.close();
            helper.succeed();
        });
    }

    private static void at(GameTestHelper helper, Fixture fixture, long delay, Runnable check) {
        helper.runAfterDelay(delay, () -> {
            try {
                check.run();
            } catch (RuntimeException | Error failure) {
                fixture.close();
                throw failure;
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void airborneStrikeKeepsItsHeightAndDamagesNearbyFlyingTargets(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Vec3 airborne = fixture.center.add(0, 4, 0);
            Cow flying = fixture.cow(airborne);
            Cow grounded = fixture.cow(fixture.center);
            helper.assertTrue(fixture.strike(airborne, 0.8, true), "Airborne strikes can be created");
            near(helper, fixture.manager.snapshots().get(0).position().y, airborne.y,
                    "Airborne coordinates must not snap four blocks down to the floor");
            helper.assertTrue(!fixture.manager.snapshots().get(0).grounded(), "Airborne strikes have no scorch");
            health(helper, flying, 14, "The flying target near the airborne strike is hit");
            health(helper, grounded, 20, "The ground target four blocks below is not hit");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void nearbyGroundSnapsButUnsupportedAirIsStillValid(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            helper.assertTrue(fixture.strike(fixture.center.add(0, 0.6, 0), 0.8, true), "Near-ground strike is valid");
            near(helper, fixture.manager.snapshots().get(0).position().y, fixture.center.y,
                    "A nearby surface attracts the strike without requiring exact coordinates");
            helper.assertTrue(fixture.manager.snapshots().get(0).grounded(), "Near-ground strikes have scorch");
            fixture.manager.clear();
            helper.assertTrue(fixture.strike(fixture.center.add(0, 1, 0), .8, true), "The snap boundary is valid");
            helper.assertTrue(fixture.manager.snapshots().get(0).grounded(), "Support exactly one block down still snaps");
            fixture.manager.clear();
            helper.assertTrue(fixture.strike(fixture.center.add(0, 1.00001, 0), .8, true), "Just outside the boundary is valid air");
            helper.assertTrue(!fixture.manager.snapshots().get(0).grounded(), "Support beyond the snap boundary stays airborne");
            helper.assertTrue(fixture.strike(fixture.center.add(0, 20, 0), 0.8, true),
                    "Air without support is still a valid strike position");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void immediateZeroDamageStrikeBroadcastsItsUniqueSoundExactlyOnce(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            AtomicInteger sounds = new AtomicInteger();
            Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
                if (event.getLevel() == fixture.level && event.getSound() != null
                        && event.getSound().value().getLocation().getPath().equals("holy_light_shot")
                        && event.getPosition().distanceToSqr(fixture.center) < 0.01) sounds.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try {
                helper.assertTrue(fixture.manager.spawn(fixture.level, fixture.owner, fixture.center, 0, 0.8, true),
                        "Zero damage can preview the entire strike including its firing sound");
                helper.assertTrue(sounds.get() == 1, "Skipping warning broadcasts the firing sound inside spawn");
                for (int i = 0; i < 10; i++) fixture.manager.tick(fixture.level);
                helper.assertTrue(sounds.get() == 1, "Repeated ticks cannot replay the firing sound");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void doubleWarningSpeedFiresAtTickEighteenWithoutChangingDamage(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Cow target = fixture.cow(fixture.center);
        helper.assertTrue(fixture.manager.spawn(fixture.level, fixture.owner, fixture.center, DAMAGE, .8, false, 2),
                "A double-speed warning is created");
        helper.assertTrue(fixture.manager.snapshots().get(0).warningTicks() == 18, "The duration is shared with clients");
        at(helper, fixture, 17, () -> {
            fixture.manager.tick(fixture.level);
            health(helper, target, 20, "The scaled warning is harmless until its exact boundary");
        });
        at(helper, fixture, 18, () -> {
            fixture.manager.tick(fixture.level);
            health(helper, target, 14, "Double warning speed fires at ceil(35 / 2)");
            fixture.close();
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void airborneStrikeSurvivesWithoutSupportAndExpiresAfterFifteenFiringTicks(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        Vec3 position = fixture.center.add(0, 4, 0);
        Cow target = fixture.cow(position);
        helper.assertTrue(fixture.strike(position, .8, true), "Airborne firing is valid");
        at(helper, fixture, 14, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().size() == 1, "The beam remains for its full 0.75 seconds");
            health(helper, target, 14, "Holding the beam does not deal repeated damage");
        });
        at(helper, fixture, 15, () -> {
            fixture.manager.tick(fixture.level);
            helper.assertTrue(fixture.manager.snapshots().isEmpty(), "Airborne strikes skip the scorch stage");
            fixture.close();
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void validAirOriginDirectlyUnderCeilingIsNotRejectedByRaisedGroundProbe(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Vec3 position = fixture.center.add(0, 3.9, 0);
            fixture.level.setBlockAndUpdate(BlockPos.containing(fixture.center).above(4), Blocks.STONE.defaultBlockState());
            helper.assertTrue(fixture.strike(position, .8, true), "The real origin is in air even when its raised ground probe is inside the ceiling");
            HolyLightSnapshot snapshot = fixture.manager.snapshots().get(0);
            near(helper, snapshot.position().y, position.y, "The origin keeps its requested height");
            near(helper, snapshot.beamHeight(), .1, "The nearby ceiling still clips the visual beam");
            helper.assertTrue(!snapshot.grounded(), "An overhead block cannot become ground support");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void undeadMultiplierIsPerTargetAndCapturedUntilFiring(GameTestHelper helper) {
        Fixture fixture = new Fixture(helper);
        try {
            Cow ordinary = fixture.cow(fixture.center.add(-.35, 0, 0));
            Zombie undead = new Zombie(EntityType.ZOMBIE, fixture.level);
            undead.getAttribute(Attributes.ARMOR).setBaseValue(0);
            undead.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
            fixture.addMob(undead, fixture.center.add(.35, 0, 0));
            helper.assertTrue(fixture.manager.spawn(fixture.level, fixture.owner, fixture.center,
                    4, .8, false, 7), "The strike has five warning ticks and the shared undead bonus");
            HolyLightSnapshot snapshot = fixture.manager.snapshots().get(0);
            helper.assertTrue(snapshot.warningTicks() == 5, "Warning speed seven produces five ticks");
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
            at(helper, fixture, 4, () -> {
                fixture.manager.tick(fixture.level);
                health(helper, ordinary, 20, "The shortened warning is harmless");
            });
            at(helper, fixture, 5, () -> {
                fixture.manager.tick(fixture.level);
                health(helper, ordinary, 16, "Ordinary targets take the saved base damage");
                health(helper, undead, 12, "Only undead targets take twice that base damage");
                fixture.manager.tick(fixture.level);
                health(helper, undead, 12, "Holding the same beam does not repeat damage");
                fixture.close();
                helper.succeed();
            });
        } catch (RuntimeException | Error failure) {
            fixture.close();
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "holy_light", timeoutTicks = 30)
    public static void everyStrikeOverloadDoublesDamageForUndead(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Zombie undead = new Zombie(EntityType.ZOMBIE, fixture.level);
            undead.getAttribute(Attributes.ARMOR).setBaseValue(0);
            fixture.addMob(undead, fixture.center);
            helper.assertTrue(fixture.strike(fixture.center, .8, true), "The original overload remains usable");
            health(helper, undead, 8, "The original overload also applies the shared undead multiplier");
        }
        helper.succeed();
    }

    private static void health(GameTestHelper helper, LivingEntity target, float expected, String message) {
        helper.assertTrue(Math.abs(target.getHealth() - expected) < 1.0E-4F,
                message + " (expected " + expected + ", got " + target.getHealth() + ")");
    }

    private static void near(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON,
                message + " (expected " + expected + ", got " + actual + ")");
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerLevel level;
        private final HolyLightManager manager = new HolyLightManager();
        private final ServerPlayer owner;
        private final Vec3 center;
        private final List<Mob> targets = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            center = helper.absoluteVec(new Vec3(1.5D, 1, 1.5D));
            level.setBlockAndUpdate(BlockPos.containing(center).below(), Blocks.STONE.defaultBlockState());
            // Empty the ray column explicitly so nearby template decorations cannot become a ceiling.
            for (int y = 0; y <= 17; y++) {
                level.setBlockAndUpdate(BlockPos.containing(center).above(y), Blocks.AIR.defaultBlockState());
            }
            UUID ownerId = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(ownerId,
                    "hl_" + ownerId.toString().substring(0, 8)));
            owner.setPos(center.add(0, 10, 0));
            owner.setNoGravity(true);
            level.addNewPlayer(owner);
        }

        private Cow cow(Vec3 position) {
            Cow cow = new Cow(EntityType.COW, level);
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
            cow.setHealth(20);
            addMob(cow, position);
            return cow;
        }

        private void addMob(Mob mob, Vec3 position) {
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            mob.setPos(position);
            level.addFreshEntity(mob);
            targets.add(mob);
        }

        private boolean strike(Vec3 position, double radius, boolean skipWarning) {
            return manager.spawn(level, owner, position, DAMAGE, radius, skipWarning);
        }

        @Override
        public void close() {
            manager.clear();
            for (Mob target : targets) target.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
