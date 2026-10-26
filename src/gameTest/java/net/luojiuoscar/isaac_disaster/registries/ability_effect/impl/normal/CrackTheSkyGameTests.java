package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackTypes;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GameTestHolder("isaac_disaster_crack_the_sky")
@PrefixGameTestTemplate(false)
public final class CrackTheSkyGameTests {
    @GameTest(template = "trajectory_empty", batch = "crack_the_sky", timeoutTicks = 30)
    public static void groundSearchKeepsTheIndoorFloorAndSlabHeight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 feet = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        BlockPos support = BlockPos.containing(feet).below();
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(support.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(support.above(2), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(support.above(3), Blocks.STONE.defaultBlockState());
        Vec3 ground = CrackTheSky.groundBelow(level, feet).orElseThrow();
        helper.assertTrue(Math.abs(ground.y - (support.getY() + 1.002)) < .0001,
                "An indoor cast stays below the ceiling instead of using the rooftop");
        level.setBlockAndUpdate(support, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        Vec3 slab = CrackTheSky.groundBelow(level, feet).orElseThrow();
        helper.assertTrue(Math.abs(slab.y - (support.getY() + .502)) < .0001,
                "The landing uses the exact slab collision top");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "crack_the_sky", timeoutTicks = 30)
    public static void missingColumnsAreRejectedWithoutLoadingThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 unloaded = new Vec3(10_000_000.5, 100, 10_000_000.5);
        BlockPos column = BlockPos.containing(unloaded);
        helper.assertTrue(!level.hasChunkAt(column), "The selected remote column starts unloaded");
        helper.assertTrue(CrackTheSky.groundBelow(level, unloaded).isEmpty(), "An unloaded column cannot be a landing");
        helper.assertTrue(!level.hasChunkAt(column), "Checking a landing does not load the column");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "crack_the_sky", timeoutTicks = 30)
    public static void retargetIgnoresAnimalsAndEnemiesOutsideTheDisk(GameTestHelper helper) throws ReflectiveOperationException {
        ServerLevel level = helper.getLevel();
        Vec3 feet = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos base = BlockPos.containing(feet).below().offset(x, 0, z);
                level.setBlockAndUpdate(base, Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 3; y++) level.setBlockAndUpdate(base.above(y), Blocks.AIR.defaultBlockState());
            }
        }
        Vec3 ground = CrackTheSky.groundBelow(level, feet).orElseThrow();
        UUID ownerId = UUID.randomUUID();
        ServerPlayer owner = FakePlayerFactory.get(level, new GameProfile(ownerId, "sky_" + ownerId.toString().substring(0, 8)));
        owner.setPos(feet.add(0, 5, 0));
        level.addNewPlayer(owner);
        Cow cow = new Cow(EntityType.COW, level);
        Zombie enemy = new Zombie(EntityType.ZOMBIE, level);
        cow.setPos(feet.add(-.1, 0, 0));
        enemy.setPos(feet.add(.5, 0, 0));
        level.addFreshEntity(cow);
        level.addFreshEntity(enemy);
        try {
            Vec3 corrected = retarget(level, owner, ground, feet, 4);
            helper.assertTrue(Math.abs(corrected.x - enemy.getX()) < .0001,
                    "The farther monster is selected instead of the nearer harmless animal");
            helper.assertTrue(retarget(level, owner, ground, feet, .2).equals(ground),
                    "Retargeting cannot move a strike outside the scatter disk");
        } finally {
            cow.discard();
            enemy.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "crack_the_sky", timeoutTicks = 30)
    public static void allSevenStrikesCanLockTheSameNearbyEnemy(GameTestHelper helper) throws ReflectiveOperationException {
        ServerLevel level = helper.getLevel();
        Vec3 feet = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos base = BlockPos.containing(feet).below().offset(x, 0, z);
                level.setBlockAndUpdate(base, Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 3; y++) level.setBlockAndUpdate(base.above(y), Blocks.AIR.defaultBlockState());
            }
        }
        UUID ownerId = UUID.randomUUID();
        ServerPlayer owner = FakePlayerFactory.get(level, new GameProfile(ownerId, "sky_" + ownerId.toString().substring(0, 8)));
        owner.setPos(feet);
        level.addNewPlayer(owner);
        Zombie enemy = new Zombie(EntityType.ZOMBIE, level);
        enemy.setPos(feet.add(.5, 0, 0));
        level.addFreshEntity(enemy);
        var manager = HolyLightRuntime.INSTANCE.manager(level);
        Set<Long> existing = manager.snapshots().stream().map(snapshot -> snapshot.id()).collect(Collectors.toSet());
        MinecraftServer server = level.getServer();
        var originalSettings = IsaacDatapackData.forServer(server);
        var gameplay = originalSettings.gameplay().toJson();
        gameplay.addProperty("nearby_range", 2D);
        var values = new LinkedHashMap<>(originalSettings.values());
        values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.parse(gameplay));
        Method install = IsaacDatapackData.class.getDeclaredMethod("install", MinecraftServer.class, IsaacDatapackTypes.Snapshot.class);
        install.setAccessible(true);
        try {
            // Every random point is within three blocks of this enemy and has a valid floor.
            install.invoke(null, server, new IsaacDatapackTypes.Snapshot(values));
            helper.assertTrue(StatManager.getNearbyRange() == 2D, "The emission reads the scoped datapack setting");
            Method spawn = CrackTheSky.class.getDeclaredMethod("spawn", ServerPlayer.class);
            spawn.setAccessible(true);
            for (int i = 0; i < 7; i++) spawn.invoke(null, owner);
            var strikes = manager.snapshots().stream().filter(snapshot -> !existing.contains(snapshot.id())).toList();
            helper.assertTrue(strikes.size() == 7, "An ordinary sequence creates seven strikes");
            for (var strike : strikes) {
                helper.assertTrue(Math.abs(strike.position().x - enemy.getX()) < .0001
                                && Math.abs(strike.position().z - enemy.getZ()) < .0001,
                        "Every strike can lock the same nearby enemy: actual=" + strike.position()
                                + ", target=" + enemy.position());
            }
        } finally {
            install.invoke(null, server, originalSettings);
            enemy.discard();
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static Vec3 retarget(ServerLevel level, ServerPlayer owner, Vec3 ground, Vec3 center,
                                 double radius) throws ReflectiveOperationException {
        Method method = CrackTheSky.class.getDeclaredMethod("retarget", ServerLevel.class, ServerPlayer.class,
                Vec3.class, Vec3.class, double.class);
        method.setAccessible(true);
        return (Vec3) method.invoke(null, level, owner, ground, center, radius);
    }
}
