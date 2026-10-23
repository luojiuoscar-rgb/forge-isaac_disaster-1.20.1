package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.minecraftforge.registries.RegistryManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_ground_effect")
@PrefixGameTestTemplate(false)
public final class GroundEffectGameTests {
    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void losingSupportRemovesWaterAndItsDamageWithoutDroppingToALowerFloor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos support = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        Vec3 feet = new Vec3(support.getX() + 0.5D, support.getY() + 1, support.getZ() + 0.5D);
        Vec3 placed = GroundEffectPlacement.projectToGround(level, feet).orElseThrow();
        var manager = new GroundEffectManager();
        var water = manager.spawnWater(null, placed);
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
            manager.spawnWater(null, placed);
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
        helper.assertTrue(water == WaterPuddleEffect.DEFINITION,
                "Runtime creation uses the registered singleton definition");
        var manager = new GroundEffectManager();
        var instance = manager.spawn(water, null, helper.absoluteVec(new Vec3(1, 1, 1)), 0.5D, 100);
        helper.assertTrue(instance != null && instance.getType() == water
                        && instance.typeId().equals(ModGroundEffects.WATER.getId()),
                "The registered factory creates a tracked instance with its registered network id");
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
            manager.spawnWater(owner, center);
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
            var first = manager.spawn(probeType("first", target.getUUID(), calls, 0), owner.getUUID(), center, 2, 100);
            manager.tick(level);
            helper.assertTrue(calls[0] == 1, "The initial type receives one contact callback");
            manager.remove(first);
            manager.spawn(probeType("second", target.getUUID(), calls, 1), owner.getUUID(), center, 2, 100);
            for (int i = 0; i < 9; i++) manager.tick(level);
            helper.assertTrue(calls[1] == 0, "Changing type cannot bypass this owner's ten-tick target cooldown");
            manager.spawn(probeType("third", target.getUUID(), calls, 2), owner.getUUID(), center, 2, 100);
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
            manager.spawnWater(first, center);
            manager.tick(level);
            helper.assertTrue(target.getHealth() == 17, "The first owner deals three damage");
            manager.spawnWater(second, center);
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
        return new GroundEffectType<>(ResourceLocation.fromNamespaceAndPath("isaac_disaster", "contact_probe_" + name),
                (type, slot, generation, owner, position, radius, lifetime, order) ->
                        new ContactProbeEffect(type, slot, generation, owner, position, radius, lifetime, order,
                                target, calls, counter));
    }

    private static final class ContactProbeEffect extends AbstractGroundEffect {
        private final UUID target;
        private final int[] calls;
        private final int counter;

        ContactProbeEffect(GroundEffectType<ContactProbeEffect> type, int slot, int generation, UUID owner,
                           Vec3 position, double radius, int lifetime, long order, UUID target, int[] calls, int counter) {
            super(type, slot, generation, owner, position, radius, lifetime, order);
            this.target = target;
            this.calls = calls;
            this.counter = counter;
        }

        @Override public int getColor() { return 0; }
        @Override public void onContactEffect(GroundEffectContact contact) {
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
            manager.spawnWater(owner, owner.position());
            manager.spawnWater(owner, owner.position().add(0.1D, 0, 0));
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
            helper.assertTrue(Math.abs(afterFirst - zombie.getHealth() - 5.0F) < 1.0E-4F,
                    "The existing puddles use updated player damage on the next pulse: actual="
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
        manager.spawnWater(null, supportedCenter(helper));
        for (int i = 0; i < 99; i++) manager.tick(level);
        helper.assertTrue(manager.activeCount() == 1, "Supported water survives the first 99 server ticks");
        manager.tick(level);
        helper.assertTrue(manager.activeCount() == 0, "A water puddle expires after 100 server ticks");
        manager.clear();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "ground_effect", timeoutTicks = 40)
    public static void overwrittenDamageDoesNotReturnWhenTheNewWaterExpires(GameTestHelper helper) {
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
            var original = manager.spawnWater(oldOwner.getUUID(), center, 0.5D, 100);
            manager.spawnWater(newOwner.getUUID(), center.add(0.4D, 0, 0), 0.5D, 2);
            float before = target.getHealth();
            manager.tick(level);
            helper.assertTrue(Math.abs(before - target.getHealth() - 7.0F) < 1.0E-4F,
                    "Only the latest owner's water deals damage at the covered location");
            float afterNewWater = target.getHealth();
            for (int i = 0; i < 12; i++) manager.tick(level);
            helper.assertTrue(manager.activeCount() == 1 && manager.activeEffects().get(0) == original,
                    "The longer-lived uncovered part of the old water remains");
            helper.assertTrue(target.getHealth() == afterNewWater,
                    "Erased old damage stays erased after the new water expires");
            target.setPos(center.add(-0.3D, 0, 0));
            manager.tick(level);
            helper.assertTrue(Math.abs(afterNewWater - target.getHealth() - 3.0F) < 1.0E-4F,
                    "The untouched old part still deals its owner's current damage");
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
            var original = manager.spawnWater(owner, center);
            manager.tick(level);
            float afterFirst = target.getHealth();
            for (int i = 0; i < 9; i++) {
                helper.assertTrue(manager.spawnWater(owner, center) == original,
                        "Repeated paint reuses the original identity");
                manager.tick(level);
            }
            helper.assertTrue(target.getHealth() == afterFirst && manager.activeCount() == 1,
                    "Refreshing neither multiplies instances nor bypasses the target cooldown");
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            manager.spawnWater(owner, center);
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
            here.spawnWater(null, center);
            var other = there.spawnWater(null, new Vec3(0, 64, 0));
            helper.assertTrue(dispatcher.execute("isd water count", source) == 2,
                    "Counts include water in every loaded dimension");
            dispatcher.execute("execute store result score #water water_count_test run isd water count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#water", objective).getScore() == 2,
                    "execute store records the global active count");
            here.spawnWater(null, center);
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
