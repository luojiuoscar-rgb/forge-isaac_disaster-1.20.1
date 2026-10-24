package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.LemonPuddleEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Lemon;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl.LemonParty;
import net.minecraftforge.registries.RegistryManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl.SomethingsWrong;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectPlacement;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_ground_effect")
@PrefixGameTestTemplate(false)
public final class GroundEffectGameTests {
    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void potionRefreshesEveryFiveTicksIndependentlyOfDamageAndCombinesAllContactingPuddles(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = waterOwner(helper, "potion-cadence", 3);
        Vec3 center = supportedCenter(helper);
        Vec3 strongCenter = center.add(0.5D, 0, 0);
        level.setBlockAndUpdate(BlockPos.containing(strongCenter).below(), Blocks.STONE.defaultBlockState());
        var target = contactTarget(level, center);
        var manager = new GroundEffectManager();
        try {
            water(manager, owner.getUUID(), center, 1, 100, 3);
            var strong = manager.spawn(ModGroundEffects.POTION.get(), owner.getUUID(), strongCenter, 1, 100, 0);
            strong.setPotions(List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 20, 1),
                    new PotionProfile(MobEffects.WEAKNESS, 60, 0)));
            var weak = manager.spawn(ModGroundEffects.POTION.get(), owner.getUUID(), center.add(-0.5D, 0, 0), 1, 100, 0);
            weak.setPotions(List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 100, 0),
                    new PotionProfile(MobEffects.BLINDNESS, 80, 0)));
            helper.assertTrue(manager.activeCount() == 3,
                    "Different centers keep all three partially overlapping complete instances");
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 17 && target.hasEffect(MobEffects.WEAKNESS)
                            && target.hasEffect(MobEffects.BLINDNESS),
                    "All contacting potion lists apply alongside the same owner's one damage pulse");
            var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slow != null && slow.getAmplifier() == 1 && slow.getDuration() == 20,
                    "Vanilla merging keeps the stronger shorter slowness visible instead of extending it with weaker duration");
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            target.removeEffect(MobEffects.WEAKNESS);
            target.removeEffect(MobEffects.BLINDNESS);
            for (int i = 0; i < 4; i++) manager.tick(level);
            helper.assertTrue(!target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                            && !target.hasEffect(MobEffects.WEAKNESS) && !target.hasEffect(MobEffects.BLINDNESS)
                            && target.getHealth() == 17,
                    "Neither potion refresh nor damage repeats before its own cooldown");
            manager.tick(level);
            helper.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                            && target.hasEffect(MobEffects.WEAKNESS) && target.hasEffect(MobEffects.BLINDNESS)
                            && target.getHealth() == 17,
                    "At five ticks all potion puddles refresh while the ten-tick damage cooldown is still active");
            for (int i = 0; i < 5; i++) manager.tick(level);
            helper.assertTrue(target.getHealth() == 14 && manager.activeCount() == 3,
                    "The next damage pulse occurs ten ticks later without consuming or suppressing potion contacts");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void finiteOversizedRadiusIsRejectedBeforeAllocatingItsSpatialGrid(GameTestHelper helper) {
        var manager = new GroundEffectManager();
        Vec3 center = supportedCenter(helper);
        helper.assertTrue(manager.spawn(ModGroundEffects.WATER.get(), null, center, 1.0E100D, 100, 1) == null
                        && manager.activeCount() == 0 && manager.drainDespawns().isEmpty(),
                "A finite positive radius exceeding 4096 spatial cells is rejected without creating state");
        helper.assertTrue(water(manager, null, center) != null && manager.activeCount() == 1,
                "Rejecting an oversized radius leaves normal spawns usable");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void potionPuddlesApplyMultipleEffectsAndSynchronizeColorChanges(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 center = supportedCenter(helper);
        var target = contactTarget(level, center);
        var manager = new GroundEffectManager();
        try {
            var puddle = manager.spawn(ModGroundEffects.POTION.get(), null, center, 1, 100, 0);
            puddle.setColor(0x2255DD);
            puddle.setPotions(List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 100, 1),
                    new PotionProfile(MobEffects.WEAKNESS, 60, 0)));
            manager.tick(level);
            helper.assertTrue(target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() == 100
                            && target.getEffect(MobEffects.WEAKNESS).getDuration() == 60 && target.getHealth() == 20,
                    "One generic potion puddle applies its complete configured effect list without contact damage");
            var stream = new GroundEffectStream(() -> 0L);
            var state = stream.spawn(GroundEffectSnapshotBatchS2CPacket.Entry.from(puddle), 10);
            helper.assertTrue(state.getColor() == 0x2255DD, "The instance tint reaches the client");
            puddle.setColor(0xBB3311);
            helper.assertTrue(stream.applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry.from(puddle), 10)
                            && stream.get(puddle.slot(), puddle.generation()).getColor() == 0xBB3311,
                    "Changing only the tint produces a revision update");
        } finally {
            manager.clear();
            target.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void failedContactCallbacksRemoveOnlyTheirPuddleWithoutCrashingTheTick(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 center = supportedCenter(helper);
        var target = contactTarget(level, center);
        var manager = new GroundEffectManager();
        var broken = new GroundEffectType<AbstractGroundEffect>((type, slot, generation, owner, position,
                radius, lifetime, damage, order, animationTicks) -> new AbstractGroundEffect(type, slot, generation,
                owner, position, radius, lifetime, damage, order, animationTicks) {
            @Override public int getColor() { return 0; }
            @Override public void onContactEffect(GroundEffectContact contact) {
                throw new IllegalStateException("Deliberate callback failure");
            }
        });
        try {
            manager.spawn(broken, null, center, 1, 100, 0);
            manager.tick(level);
            helper.assertTrue(manager.activeCount() == 0 && target.getHealth() == 20,
                    "The bad callback is contained and its state is removed");
            var normal = manager.spawn(ModGroundEffects.WATER.get(), null, center, 1, 100, 1);
            helper.assertTrue(normal != null, "A failed callback cannot break later spawns");
        } finally {
            manager.clear();
            target.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void slownessKeepsAffectingInvulnerableAlliesWhenItsOwnerIsUnavailable(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = waterOwner(helper, "slow-unresolved", 3);
        Vec3 center = supportedCenter(helper);
        var target = contactTarget(level, center);
        target.setInvulnerable(true);
        var manager = new GroundEffectManager();
        try {
            var effect = manager.spawn(ModGroundEffects.POTION.get(), owner.getUUID(), center, 1, 100, 0);
            effect.setColor(0x000000);
            effect.setPotions(List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 100, 1)));
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            manager.tick(level);
            var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slow != null && slow.getAmplifier() == 1 && slow.getDuration() == 100,
                    "Neutral slowness does not require a loaded owner or a damageable target");
            helper.assertTrue(target.getHealth() == 20, "No damage is inflicted");
        } finally {
            manager.clear();
            target.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void somethingsWrongCreatesBlackPaintAndSlowsOwnerAlliesAndEnemiesWithoutDamage(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = waterOwner(helper, "slowness-pill", 3);
        Vec3 center = supportedCenter(helper);
        owner.setPos(center);
        var ally = contactTarget(level, center.add(0.2, 0, 0));
        var enemy = contactTarget(level, center.add(-0.2, 0, 0));
        var scoreboard = level.getScoreboard();
        var team = scoreboard.addPlayerTeam("slow_" + UUID.randomUUID().toString().substring(0, 8));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
        var manager = GroundEffectRuntime.INSTANCE.manager(level);
        manager.clear();
        try {
            for (boolean horse : List.of(false, true)) {
                owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                ally.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                enemy.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                var context = new ExecutableEffectContext(owner);
                context.set(ContextKeys.BOOLEAN, List.of(horse));
                helper.assertTrue(new SomethingsWrong().applyEffect(context), "Both pill sizes execute successfully");
                helper.assertTrue(manager.activeCount() == 1, "The pill creates a ground effect instead of a particle cloud");
                var effect = manager.activeEffects().get(0);
                helper.assertTrue(effect.getType() == ModGroundEffects.POTION.get()
                                && effect.typeId().equals(ModGroundEffects.POTION.getId())
                                && effect.getColor() == 0 && effect.damage() == 0
                                && effect.lifetime() == (horse ? 1500 : 600)
                                && Math.abs(effect.radius() - (float) StatManager.RANGE.getBonus() * (horse ? 4F : 2.5F)) < 1e-6,
                        "Black slowness paint retains the previous range and lifetime, with zero damage");
                float ownerHealth = owner.getHealth();
                float allyHealth = ally.getHealth();
                float enemyHealth = enemy.getHealth();
                // Wait through any spawn animation without advancing entity effect durations.
                for (int i = 0; i < 10; i++) manager.tick(level);
                for (var target : List.of(owner, ally, enemy)) {
                    var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    helper.assertTrue(slow != null && slow.getAmplifier() == (horse ? 2 : 1)
                                    && slow.getDuration() == (horse ? 150 : 100),
                            "Owner, allies and enemies all receive the previous cloud's actual slowness level and duration");
                }
                helper.assertTrue(owner.getHealth() == ownerHealth && ally.getHealth() == allyHealth
                                && enemy.getHealth() == enemyHealth, "Slowness paint does not deal damage");
                helper.assertTrue(level.getEntitiesOfClass(AreaEffectCloud.class, owner.getBoundingBox().inflate(20),
                        cloud -> cloud.getOwner() == owner).isEmpty(), "No particle cloud remains in either pill path");
                manager.clear();
            }
        } finally {
            manager.clear();
            level.getEntitiesOfClass(AreaEffectCloud.class, owner.getBoundingBox().inflate(20),
                    cloud -> cloud.getOwner() == owner).forEach(Entity::discard);
            scoreboard.removePlayerTeam(team);
            ally.discard();
            enemy.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void unresolvedOwnersCannotTurnTheirLemonPaintAgainstAllies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "lemon-ownerlost", 3);
        Vec3 center = supportedCenter(helper);
        Zombie ally = contactTarget(level, center);
        var scoreboard = level.getScoreboard();
        var team = scoreboard.addPlayerTeam("lemon_" + UUID.randomUUID().toString().substring(0, 8));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(), team);
        var manager = new GroundEffectManager();
        try {
            manager.spawn(ModGroundEffects.LEMON.get(), owner.getUUID(), center, 0.5D, 100, 3.0F);
            manager.tick(level);
            helper.assertTrue(ally.getHealth() == 20, "Lemon paint respects the owner's team");
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            manager.tick(level);
            helper.assertTrue(ally.getHealth() == 20 && manager.activeCount() == 1,
                    "Missing ownership never disables ally filtering, while the paint keeps its own lifetime");
        } finally {
            manager.clear();
            scoreboard.removePlayerTeam(team);
            ally.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void lemonActivesUseUpdatedBalanceAndKeepTheirExistingSizes(GameTestHelper helper) {
        ServerPlayer owner = waterOwner(helper, "lemon-actives", 3);
        Vec3 center = supportedCenter(helper);
        owner.setPos(center);
        var manager = GroundEffectRuntime.INSTANCE.manager(helper.getLevel());
        manager.clear();
        try {
            int index = 0;
            for (var source : List.of(ModExecutableEffects.LEMON_MISHAP, ModExecutableEffects.FREE_LEMONADE)) {
                source.get().apply(new ExecutableEffectContext(owner));
                helper.assertTrue(manager.activeCount() == 1, "Each lemon active creates exactly one ground effect");
                var effect = manager.activeEffects().get(0);
                helper.assertTrue(effect.animationDurationTicks() == 10
                                && effect.currentRadius() == 0 && !effect.isSettled(),
                        "Every lemon producer starts ten-tick horizontal growth at zero size");
                helper.assertTrue(effect instanceof LemonPuddleEffect && effect.getType() == ModGroundEffects.LEMON.get(),
                        "Both actives use the registered lemon type");
                float expectedDamage = (float) StatManager.DAMAGE.getBonus() * (index == 0 ? 4.0F : 5.0F);
                float expectedRadius = (float) StatManager.getNearbyRange() * (index == 0 ? 0.4F : 0.8F);
                helper.assertTrue(effect.damage() == expectedDamage && Math.abs(effect.radius() - expectedRadius) < 1.0E-6D
                                && effect.lifetime() == (index == 0 ? 200 : 360),
                        "Actives use the requested damage and duration while retaining their original sizes");
                helper.assertTrue(Math.abs(effect.position().y - center.y - 0.002D) < 1.0E-6D,
                        "Lemon effects are projected onto the ground");
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class,
                                owner.getBoundingBox().inflate(20)).isEmpty(), "No area-effect cloud entity is spawned");
                manager.clear();
                index++;
            }
            helper.assertTrue(new Lemon().applyEffect(new ExecutableEffectContext(owner)),
                    "Lemon's default three-value configuration creates a puddle");
            var defaults = manager.activeEffects().get(0);
            helper.assertTrue(defaults.lifetime() == 100
                            && defaults.damage() == (float) StatManager.DAMAGE.getBonus() * 2.5F
                            && Math.abs(defaults.radius() - (float) StatManager.getNearbyRange() * 0.4F) < 1.0E-6D,
                    "Default range, duration and damage remain 0.4, 100 and 2.5");
            manager.clear();
            var custom = new ExecutableEffectContext(owner);
            custom.set(ContextKeys.DOUBLE, List.of(0.6, 140., 1.25));
            helper.assertTrue(new Lemon().applyEffect(custom), "Exactly three values configure lemon paint");
            var configured = manager.activeEffects().get(0);
            helper.assertTrue(configured.lifetime() == 140
                            && configured.damage() == (float) StatManager.DAMAGE.getBonus() * 1.25F
                            && Math.abs(configured.radius() - (float) StatManager.getNearbyRange() * 0.6F) < 1.0E-6D,
                    "The third value controls damage directly");
            manager.clear();
            owner.setPos(center.add(0, 20, 0));
            helper.assertTrue(!new Lemon().applyEffect(new ExecutableEffectContext(owner)) && manager.activeCount() == 0,
                    "An unreachable ground surface rejects lemon creation");
        } finally {
            manager.clear();
            helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void lemonPartyAndHorsePillUseTheSameGroundTypeWithTheirOwnValues(GameTestHelper helper) {
        ServerPlayer owner = waterOwner(helper, "lemon-pill", 3);
        owner.setPos(supportedCenter(helper));
        var manager = GroundEffectRuntime.INSTANCE.manager(helper.getLevel());
        manager.clear();
        try {
            for (boolean horse : List.of(false, true)) {
                var context = new ExecutableEffectContext(owner);
                context.set(ContextKeys.BOOLEAN, List.of(horse));
                helper.assertTrue(new LemonParty().applyEffect(context), "Lemon Party executes successfully");
                helper.assertTrue(manager.activeCount() == 1, "The pill creates one ground effect");
                var effect = manager.activeEffects().get(0);
                helper.assertTrue(effect.animationDurationTicks() == 10
                                && effect.currentRadius() == 0 && !effect.isSettled(),
                        "Every lemon producer starts ten-tick horizontal growth at zero size");
                helper.assertTrue(effect.getType() == ModGroundEffects.LEMON.get()
                                && effect.lifetime() == (horse ? 400 : 200)
                                && effect.damage() == (float) StatManager.DAMAGE.getBonus() * (horse ? 4.0F : 2.5F)
                                && Math.abs(effect.radius() - (float) StatManager.getNearbyRange() * (horse ? 0.8F : 0.4F)) < 1.0E-6D,
                        "Normal and horse pills preserve their distinct radius, duration and damage");
                manager.clear();
            }
        } finally {
            manager.clear();
            helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void newerWeakLemonPaintUsesItsOwnDamageAndTheOwnersSharedCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "lemon-overwrite", 3);
        Vec3 center = supportedCenter(helper);
        Zombie target = contactTarget(level, center);
        var manager = new GroundEffectManager();
        try {
            water(manager, owner.getUUID(), center, 1, 100, 9.0F);
            var lemon = manager.spawn(ModGroundEffects.LEMON.get(), owner.getUUID(), center, 0.25D, 100, 2.0F);
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 18, "The later weak lemon uses two damage, not nine or a sum");
            manager.remove(lemon);
            target.setPos(center.add(0.7D, 0, 0));
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(target.getHealth() == 18, "Changing ground type cannot bypass this owner's cooldown");
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 9, "After ten ticks the untouched water uses its stored nine damage");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void losingSupportRemovesWaterAndItsDamageWithoutDroppingToALowerFloor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos support = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        Vec3 feet = new Vec3(support.getX() + 0.5D, support.getY() + 1, support.getZ() + 0.5D);
        Vec3 placed = GroundEffectPlacement.projectToGround(level, feet).orElseThrow();
        var manager = new GroundEffectManager();
        var water = water(manager, null, placed);
        for (int i = 0; i < 10; i++) manager.tick(level);
        helper.assertTrue(manager.activeCount() == 1, "An unchanged supporting block keeps water alive");
        level.setBlockAndUpdate(support, Blocks.AIR.defaultBlockState());
        for (int i = 0; i < 10; i++) manager.tick(level);
        helper.assertTrue(manager.activeCount() == 0 && manager.queryVisible(placed, 48).isEmpty(),
                "Unsupported water leaves the contact and visibility indexes within ten ticks");
        helper.assertTrue(manager.drainDespawns().contains(water.identity()),
                "Removing support emits the ordinary authoritative despawn");
        helper.assertTrue(water.position().equals(placed), "Water never projects onto the lower floor");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void supportChecksKeepPartialAndExtendedCollisionShapes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var manager = new GroundEffectManager();
        int index = 0;
        for (var block : new Block[]{Blocks.STONE_SLAB, Blocks.OAK_FENCE}) {
            BlockPos support = helper.absolutePos(new BlockPos(1 + index++ * 2, 2, 1));
            level.setBlockAndUpdate(support, block.defaultBlockState());
            Vec3 above = new Vec3(support.getX() + 0.5D, support.getY() + 2, support.getZ() + 0.5D);
            Vec3 placed = GroundEffectPlacement.projectToGround(level, above).orElseThrow();
            water(manager, null, placed);
        }
        for (int i = 0; i < 10; i++) manager.tick(level);
        helper.assertTrue(manager.activeCount() == 2, "Slab and fence collision surfaces both support water");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void forgeRegistryContainsTheWaterFactoryUsedByTheRuntime(GameTestHelper helper) {
        var registry = RegistryManager.ACTIVE.getRegistry(ModGroundEffects.GROUND_EFFECT_KEY);
        var water = ModGroundEffects.WATER.get();
        helper.assertTrue(registry != null && registry.getValue(ResourceLocation.parse("isaac_disaster:water")) == water,
                "The water type is present in the real custom Forge registry");
        var lemon = ModGroundEffects.LEMON.get();
        helper.assertTrue(registry.getValue(ModGroundEffects.LEMON.getId()) == lemon,
                "The lemon factory is registered directly alongside water");
        var manager = new GroundEffectManager();
        var instance = manager.spawn(water, null, helper.absoluteVec(new Vec3(1, 1, 1)), 0.5D, 100, 1.0F);
        helper.assertTrue(instance != null && instance.getType() == water
                        && instance.typeId().equals(ModGroundEffects.WATER.getId()),
                "The registered factory creates a tracked instance with its registered network id");
        var yellow = manager.spawn(lemon, null, instance.position().add(3, 0, 0), 1, 100, 2.5F);
        var snapshot = GroundEffectSnapshotBatchS2CPacket.Entry.from(yellow);
        helper.assertTrue(snapshot.typeId().equals(ModGroundEffects.LEMON.getId()) && snapshot.color() == ColorManager.LEMON,
                "Actual registry identity and instance color reach the network snapshot");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void waterOnlyDamagesTargetsWhoseFeetTouchItsSurface(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "water-feet", 3);
        Vec3 center = supportedCenter(helper);
        Zombie below = contactTarget(level, center.add(0, -1, 0));
        Zombie above = contactTarget(level, center.add(0, 1, 0));
        Zombie airborne = contactTarget(level, center.add(0, 20, 0));
        Zombie touching = contactTarget(level, center);
        GroundEffectManager manager = new GroundEffectManager();
        try {
            water(manager, owner, center);
            manager.tick(level);
            helper.assertTrue(below.getHealth() == 20 && above.getHealth() == 20 && airborne.getHealth() == 20,
                    "Water cannot hit a lower floor, upper floor, or airborne target");
            helper.assertTrue(touching.getHealth() == 17, "Feet on the surface receive current owner damage");
        } finally {
            manager.clear();
            below.discard();
            above.discard();
            airborne.discard();
            touching.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void sameOwnerSharesCooldownAcrossTypesAndDispatchesOnlyTheLatestType(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "water-types", 3);
        Vec3 center = supportedCenter(helper);
        Zombie target = contactTarget(level, center);
        GroundEffectManager manager = new GroundEffectManager();
        int[] calls = new int[3];
        try {
            var first = manager.spawn(probeType("first", target.getUUID(), calls, 0), owner.getUUID(), center, 2, 100, 1.0F);
            manager.tick(level);
            helper.assertTrue(calls[0] == 1, "The initial type receives one contact callback");
            manager.remove(first);
            manager.spawn(probeType("second", target.getUUID(), calls, 1), owner.getUUID(), center, 2, 100, 1.0F);
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(calls[1] == 0, "Changing type cannot bypass this owner's ten-tick target cooldown");
            manager.spawn(probeType("third", target.getUUID(), calls, 2), owner.getUUID(), center, 2, 100, 1.0F);
            manager.tick(level);
            helper.assertTrue(calls[1] == 0 && calls[2] == 1,
                    "After the shared cooldown, only the newest overlapping type receives one callback");
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(calls[1] == 0 && calls[2] == 1,
                    "Overlapping types cannot stack pulses within the shared cooldown");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void differentOwnersCanEachDamageTheSameTargetDuringTheirOwnCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer first = waterOwner(helper, "water-first", 3);
        ServerPlayer second = waterOwner(helper, "water-second", 5);
        Vec3 center = supportedCenter(helper);
        Zombie target = contactTarget(level, center);
        GroundEffectManager manager = new GroundEffectManager();
        try {
            water(manager, first, center);
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 17, "The first owner deals three damage");
            water(manager, second, center);
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 12,
                    "The second owner deals five damage while the first owner's cooldown is still active");
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 12, "Each owner then respects its own cooldown");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(first, Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(second, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static Vec3 supportedCenter(GameTestHelper helper) {
        Vec3 center = helper.absoluteVec(new Vec3(1.5D, 1, 1.5D));
        helper.getLevel().setBlockAndUpdate(BlockPos.containing(center).below(), Blocks.STONE.defaultBlockState());
        return center;
    }

    private static Zombie contactTarget(ServerLevel level, Vec3 position) {
        Zombie target = new Zombie(EntityType.ZOMBIE, level);
        target.setNoAi(true);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.setPos(position);
        level.addFreshEntity(target);
        return target;
    }

    private static GroundEffectType<ContactProbeEffect> probeType(String name, UUID target, int[] calls, int counter) {
        return new GroundEffectType<>((type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks) ->
                        new ContactProbeEffect(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks,
                                target, calls, counter));
    }

    private static final class ContactProbeEffect extends AbstractGroundEffect {
        private final UUID target;
        private final int[] calls;
        private final int counter;

        ContactProbeEffect(GroundEffectType<ContactProbeEffect> type, int slot, int generation, UUID owner,
                           Vec3 position, double radius, int lifetime, float damage, long order, int animationDurationTicks, UUID target, int[] calls, int counter) {
            super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
            this.target = target;
            this.calls = calls;
            this.counter = counter;
        }

        @Override public int getColor() { return 0; }
        @Override public void onContactDamage(GroundEffectContact contact) {
            if (contact.target().getUUID().equals(target)) calls[counter]++;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void commandCreatesWaterAtPlayerGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "water-command"));
        player.setPos(supportedCenter(helper));
        var source = player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        int result = level.getServer().getCommands().performPrefixedCommand(source, "isd water @s");
        helper.assertTrue(result == 1, "The water command reports one generated puddle");
        helper.assertTrue(GroundEffectRuntime.INSTANCE.activeCount(level) == 1,
                "The command creates one server-side water state");
        GroundEffectRuntime.INSTANCE.manager(level).clear();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void overlappingWaterDealsOneGroupedPulsePerCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "water-owner"));
        owner.setPos(supportedCenter(helper));
        level.addNewPlayer(owner);
        owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
        zombie.setNoAi(true);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0D);
        zombie.setPos(owner.getX(), owner.getY(), owner.getZ());
        level.addFreshEntity(zombie);
        GroundEffectManager manager = new GroundEffectManager();
        try {
            helper.assertTrue(level.getEntity(owner.getUUID()) == owner, "The owner is indexed by UUID");
            helper.assertTrue(zombie.getArmorValue() == 0, "The target has no armor mitigation");
            water(manager, owner, owner.position());
            water(manager, owner, owner.position().add(0.1D, 0, 0));
            float before = zombie.getHealth();
            manager.tick(level);
            float afterFirst = zombie.getHealth();
            helper.assertTrue(Math.abs(before - afterFirst - 3.0F) < 1.0E-4F,
                    "Overlapping water applies one pulse equal to current player damage: actual="
                            + (before - afterFirst));
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(zombie.getHealth() == afterFirst, "Cooldown suppresses duplicate pulses");
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5.0D);
            manager.tick(level);
            helper.assertTrue(Math.abs(afterFirst - zombie.getHealth() - 3.0F) < 1.0E-4F,
                    "The existing puddles retain their spawn damage on the next pulse: actual="
                            + (afterFirst - zombie.getHealth()));
        } finally {
            manager.clear();
            zombie.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void waterPreservesVelocityEvenWhenKnockbackListenerRequestsMovement(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Cow owner = new Cow(EntityType.COW, level);
        Cow target = new Cow(EntityType.COW, level);
        owner.setPos(helper.absoluteVec(new Vec3(1, 1, 1)));
        target.setPos(helper.absoluteVec(new Vec3(3, 1, 1)));
        owner.setNoAi(true);
        target.setNoAi(true);
        level.addFreshEntity(owner);
        level.addFreshEntity(target);
        Vec3 initial = new Vec3(0.2D, 0.37D, -0.1D);
        target.setDeltaMovement(initial);
        int[] calls = {0};
        Consumer<LivingKnockBackEvent> listener = event -> {
            if (event.getEntity() != target) return;
            calls[0]++;
            helper.assertTrue(event.getStrength() == 0 && !event.isCanceled(),
                    "Water dispatches an uncanceled zero-strength event");
            event.setStrength(0.8F);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingKnockBackEvent.class, listener);
        try {
            helper.assertTrue(target.hurt(ModDamageSources.water(level, owner), 1.0F), "Water damage is accepted");
            helper.assertTrue(calls[0] == 1, "Water dispatches exactly one knockback event");
            helper.assertTrue(target.getDeltaMovement().equals(initial),
                    "Water preserves all velocity components after Forge listeners finish");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            target.discard();
            owner.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 140)
    public static void waterExpiresAfterItsConfiguredLifetime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GroundEffectManager manager = GroundEffectRuntime.INSTANCE.manager(level);
        manager.clear();
        water(manager, null, supportedCenter(helper));
        for (int i = 0; i < 99; i++) manager.tick(level);
        helper.assertTrue(manager.activeCount() == 1, "Supported water survives the first 99 server ticks");
        manager.tick(level);
        helper.assertTrue(manager.activeCount() == 0, "A water puddle expires after 100 server ticks");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void partiallyOverlappingWaterKeepsItsWholeOutlineAndDamageAfterNewWaterExpires(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer oldOwner = waterOwner(helper, "old-water", 3);
        ServerPlayer newOwner = waterOwner(helper, "new-water", 7);
        Vec3 center = supportedCenter(helper);
        Zombie target = new Zombie(EntityType.ZOMBIE, level);
        target.setNoAi(true);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.setPos(center.add(0.2D, 0, 0));
        level.addFreshEntity(target);
        GroundEffectManager manager = new GroundEffectManager();
        try {
            var original = water(manager, oldOwner.getUUID(), center, 0.5D, 100, 3.0F);
            var newer = water(manager, newOwner.getUUID(), center.add(0.4D, 0, 0), 0.5D, 2, 7.0F);
            helper.assertTrue(manager.activeCount() == 2 && original.contains(target.position())
                            && newer.contains(target.position()) && original.coverage() == original.footprint(),
                    "Partial overlap keeps both instances and their complete fixed outlines");
            float before = target.getHealth();
            manager.tick(level);
            helper.assertTrue(Math.abs(before - target.getHealth() - 10.0F) < 1.0E-4F,
                    "Partially overlapping water from different owners applies each owner's damage pulse");
            float afterNewWater = target.getHealth();
            for (int i = 0; i < 12; i++) manager.tick(level);
            helper.assertTrue(manager.activeCount() == 1 && manager.activeEffects().get(0) == original,
                    "Only the longer-lived complete old instance remains");
            helper.assertTrue(Math.abs(afterNewWater - target.getHealth() - 3.0F) < 1.0E-4F,
                    "The complete old water remains after the newer overlapping instance expires");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(oldOwner, Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(newOwner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void refreshingWaterReusesItsIdentityWithoutResettingTargetCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "repaint-water", 3);
        Vec3 center = supportedCenter(helper);
        Zombie target = new Zombie(EntityType.ZOMBIE, level);
        target.setNoAi(true);
        target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.setPos(center);
        level.addFreshEntity(target);
        GroundEffectManager manager = new GroundEffectManager();
        try {
            var original = water(manager, owner, center);
            manager.tick(level);
            float afterFirst = target.getHealth();
            for (int i = 0; i < 9; i++) {
                helper.assertTrue(water(manager, owner, center) == original,
                        "Repeated paint reuses the original identity");
                manager.tick(level);
            }
            helper.assertTrue(target.getHealth() == afterFirst && manager.activeCount() == 1,
                    "Refreshing neither multiplies instances nor bypasses the target cooldown");
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            water(manager, owner, center);
            manager.tick(level);
            helper.assertTrue(Math.abs(afterFirst - target.getHealth() - 5.0F) < 1.0E-4F,
                    "The next pulse after refreshing still uses current player damage");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void growthOnlyPaintsCompletedInstancesAndLaterCompletionPermanentlyWins(GameTestHelper helper) {
        var manager = new GroundEffectManager();
        var level = helper.getLevel();
        Vec3 center = supportedCenter(helper);
        var type = ModGroundEffects.LEMON.get();
        var old = manager.spawn(type, null, center, 0.5, 100, 1);
        var slow = manager.spawn(type, null, center, 3, 100, 1, 6);
        var fast = manager.spawn(type, null, center, 1.5, 100, 1, 2);
        helper.assertTrue(old.contains(center), "Growing paint does not cut existing settled paint");
        manager.tick(level);
        helper.assertTrue(old.contains(center) && !slow.isSettled() && !fast.isSettled(), "No early settlement");
        manager.tick(level);
        helper.assertTrue(manager.activeCount() == 2 && fast.isSettled() && !slow.isSettled(),
                "Fast completion erases old settled paint but skips the still growing instance");
        helper.assertTrue(slow.coverage() == slow.footprint(), "Pending paint cannot be clipped");
        long fastOrder = fast.creationOrder();
        for (int i = 0; i < 4; i++) manager.tick(level);
        helper.assertTrue(slow.isSettled() && slow.creationOrder() > fastOrder && manager.activeCount() == 1,
                "The earlier-born slow instance completes later and paints over the newer fast instance");
        manager.remove(slow);
        helper.assertTrue(manager.activeCount() == 0, "Permanently erased paint never returns");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void simultaneousCompletionUsesBirthOrderAndNormalTicksDoNotRepaint(GameTestHelper helper) {
        var manager = new GroundEffectManager();
        Vec3 center = supportedCenter(helper);
        var type = ModGroundEffects.LEMON.get();
        var args = 4;
        var first = manager.spawn(type, null, center, 1, 100, 1, args);
        var second = manager.spawn(type, null, center, 2, 100, 1, args);
        manager.tick(helper.getLevel());
        helper.assertTrue(manager.spawn(type, null, center, 1, 100, 1, args) == first,
                "Refreshing pending paint reuses its identity");
        helper.assertTrue(first.animationElapsedTicks() == 1 && first.age() == 0, "Refresh does not replay growth");
        for (int i = 0; i < 3; i++) manager.tick(helper.getLevel());
        helper.assertTrue(manager.activeCount() == 1 && manager.activeEffects().get(0) == second
                        && second.creationOrder() > first.creationOrder(),
                "Same-tick completion paints in original birth order even after a refresh");
        int revision = second.revision();
        for (int i = 0; i < 5; i++) manager.tick(helper.getLevel());
        helper.assertTrue(second.revision() == revision, "Settled paint does not repeatedly recalculate coverage");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void expirationBeforeOrOnCompletionDoesNotEraseOldPaint(GameTestHelper helper) {
        Vec3 center = supportedCenter(helper);
        for (int lifetime : new int[]{5, 10}) {
            var manager = new GroundEffectManager();
            var type = ModGroundEffects.LEMON.get();
            var old = manager.spawn(type, null, center, 0.5, 100, 1);
            var transientPaint = manager.spawn(type, null, center, 2, lifetime, 1, 10);
            for (int i = 0; i < lifetime; i++) manager.tick(helper.getLevel());
            helper.assertTrue(manager.activeCount() == 1 && old.contains(center) && !transientPaint.isSettled(),
                    "Lifetime includes growth and expiry takes precedence over completion");
            manager.clear();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void settledAnimatedRefreshKeepsItsOutlineAndErasesIncludedPaintWithoutReplayingGrowth(GameTestHelper helper) {
        var manager = new GroundEffectManager();
        Vec3 center = supportedCenter(helper);
        var type = ModGroundEffects.LEMON.get();
        var args = 2;
        var original = manager.spawn(type, null, center, 2, 100, 1, args);
        manager.tick(helper.getLevel());
        manager.tick(helper.getLevel());
        var covering = manager.spawn(type, null, center, 0.5, 100, 1);
        helper.assertTrue(original.contains(center) && covering.contains(center) && manager.activeCount() == 2
                        && original.coverage() == original.footprint(),
                "A smaller puddle leaves the complete settled animated outline intact");
        helper.assertTrue(manager.spawn(type, null, center, 2, 100, 1, args) == original,
                "Refreshing settled paint preserves its identity");
        helper.assertTrue(original.age() == 0 && original.animationElapsedTicks() == 2
                        && original.currentRadius() == 2 && original.contains(center)
                        && manager.activeCount() == 1 && manager.drainDespawns().contains(covering.identity()),
                "Refresh keeps full size and permanently removes the fully included smaller instance");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void animatedContactsExpandAndUseTheOwnersSharedCooldown(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer owner = waterOwner(helper, "growth-contact", 3);
        Vec3 center = supportedCenter(helper);
        Zombie target = contactTarget(level, center.add(0.8, 0, 0));
        var manager = new GroundEffectManager();
        try {
            var expanding = manager.spawn(ModGroundEffects.LEMON.get(), owner.getUUID(), center, 2, 100, 3,
                    10);
            for (int i = 0; i < 3; i++) manager.tick(level);
            helper.assertTrue(target.getHealth() == 20, "Target outside the growing outline takes no damage");
            for (int i = 0; i < 2; i++) manager.tick(level);
            helper.assertTrue(target.getHealth() == 17 && expanding.currentRadius() == 1,
                    "At half growth the actual contact range grows to half the target radius");
            water(manager, owner.getUUID(), target.position(), 0.5, 100, 2);
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(target.getHealth() == 17, "Growth and a new water type share the target-owner cooldown");
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 14, "The completed lemon uses its fixed damage after cooldown");
        } finally {
            manager.clear();
            target.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void midwaySnapshotsResumeGrowthAndRefreshDoesNotResetTheClientAnimation(GameTestHelper helper) {
        var manager = new GroundEffectManager();
        Vec3 center = supportedCenter(helper);
        var type = ModGroundEffects.LEMON.get();
        var args = 10;
        var effect = manager.spawn(type, null, center, 1, 100, 1, args);
        int initialRevision = effect.revision();
        for (int i = 0; i < 5; i++) manager.tick(helper.getLevel());
        helper.assertTrue(effect.revision() == initialRevision, "Growth sends no per-tick radius revisions");
        var clientNanos = new AtomicLong();
        var stream = new GroundEffectStream(clientNanos::get);
        var snapshot = GroundEffectSnapshotBatchS2CPacket.Entry.from(effect);
        var state = stream.spawn(snapshot, 105);
        helper.assertTrue(snapshot.animationElapsedTicks() == 5 && state.animationScale(stream.localAnimationTick()) == 0.5,
                "Entering visibility midway starts at the sampled progress");
        manager.spawn(type, null, center, 1, 100, 1, args);
        helper.assertTrue(stream.applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry.from(effect), 105),
                "The lifetime refresh produces a newer revision");
        helper.assertTrue(stream.get(effect.slot(), effect.generation()).animationScale(stream.localAnimationTick()) == 0.5,
                "A lifetime refresh cannot restart the client animation");
        for (int i = 0; i < 5; i++) manager.tick(helper.getLevel());
        helper.assertTrue(stream.applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry.from(effect), 110),
                "Completion updates the authoritative draw order");
        helper.assertTrue(stream.states().get(0).creationOrder() == effect.creationOrder()
                        && stream.states().get(0).animationScale(stream.localAnimationTick()) == 0.5,
                "The server's completion changes paint order without overriding local growth");
        clientNanos.set(250_000_000L);
        helper.assertTrue(stream.states().get(0).animationScale(stream.localAnimationTick()) == 1,
                "The client completes the remaining five ticks using its own elapsed time");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void growingPaintLosingSupportIsRemovedWithoutSettlement(GameTestHelper helper) {
        Vec3 center = supportedCenter(helper);
        var manager = new GroundEffectManager();
        var effect = manager.spawn(ModGroundEffects.LEMON.get(), null, center, 1, 100, 1, 20);
        helper.getLevel().setBlockAndUpdate(BlockPos.containing(center).below(), Blocks.AIR.defaultBlockState());
        for (int i = 0; i < 10; i++) manager.tick(helper.getLevel());
        helper.assertTrue(manager.activeCount() == 0 && !effect.isSettled(), "Unsupported growth is removed before completion");
        manager.clear();
        helper.succeed();
    }

    private static WaterPuddleEffect water(GroundEffectManager manager, ServerPlayer owner, Vec3 position) {
        return water(manager, owner == null ? null : owner.getUUID(), position,
                WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME,
                owner == null ? 0 : (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    private static WaterPuddleEffect water(GroundEffectManager manager, UUID owner, Vec3 position,
                                            double radius, int lifetime, float damage) {
        return manager.spawn(ModGroundEffects.WATER.get(), owner, position, radius, lifetime, damage);
    }

    private static ServerPlayer waterOwner(GameTestHelper helper, String name, double damage) {
        ServerLevel level = helper.getLevel();
        var owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), name));
        owner.setPos(helper.absoluteVec(new Vec3(4, 1, 4)));
        level.addNewPlayer(owner);
        owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        return owner;
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void countCommandReturnsAndStoresWaterAcrossDimensionsIncludingZero(GameTestHelper helper)
            throws Exception {
        ServerLevel level = helper.getLevel();
        var runtime = GroundEffectRuntime.INSTANCE;
        var server = level.getServer();
        ServerLevel nether = server.getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "The fixture has a second loaded dimension");
        var scoreboard = server.getScoreboard();
        var objective = scoreboard.addObjective("water_count_test", ObjectiveCriteria.DUMMY,
                Component.literal("Water count test"), ObjectiveCriteria.RenderType.INTEGER);
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        GroundEffectManager here = runtime.manager(level);
        GroundEffectManager there = runtime.manager(nether);
        here.clear();
        there.clear();
        try {
            scoreboard.getOrCreatePlayerScore("#water", objective).setScore(-1);
            helper.assertTrue(dispatcher.execute("isd water count", source) == 0, "An empty server returns zero");
            dispatcher.execute("execute store result score #water water_count_test run isd water count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#water", objective).getScore() == 0,
                    "execute store records zero rather than a generic command success value");

            Vec3 center = supportedCenter(helper);
            water(here, null, center);
            var other = water(there, null, new Vec3(0, 64, 0));
            helper.assertTrue(dispatcher.execute("isd water count", source) == 2,
                    "Counts include water in every loaded dimension");
            dispatcher.execute("execute store result score #water water_count_test run isd water count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#water", objective).getScore() == 2,
                    "execute store records the global active count");
            water(here, null, center);
            helper.assertTrue(dispatcher.execute("isd water count", source) == 2,
                    "Refreshing the same water does not increase the instance count");
            there.remove(other);
            helper.assertTrue(dispatcher.execute("isd water count", source) == 1, "Removed water is excluded");
            here.clear();
            helper.assertTrue(dispatcher.execute("isd water count", source) == 0, "Cleared water is excluded");
        } finally {
            here.clear();
            there.clear();
            scoreboard.removeObjective(objective);
        }
        helper.succeed();
    }
}
