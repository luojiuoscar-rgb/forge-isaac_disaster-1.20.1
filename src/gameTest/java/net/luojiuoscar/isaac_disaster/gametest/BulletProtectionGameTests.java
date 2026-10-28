package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BulletAttack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BulletProtectionGameTests {
    @GameTest(template = "trajectory_empty", batch = "bullet_protection")
    public static void globalCapRejectsExcessAndResumesAfterRemoval(GameTestHelper helper) {
        var runtime = BulletRuntime.INSTANCE;
        ServerLevel level = helper.getLevel();
        ServerLevel other = level.getServer().getLevel(Level.NETHER);
        int baseline = runtime.activeCount(level.getServer());
        BulletState first = null, second = null, replacement = null, rejected = null;
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, baseline + 2);
            first = runtime.spawn(level, state(helper));
            second = runtime.spawn(other, state(helper));
            helper.assertTrue(first != null && second != null, "Available capacity can be used across dimensions");
            rejected = runtime.spawn(level, state(helper));
            helper.assertTrue(rejected == null, "Reaching the global cap rejects the next bullet");
            helper.assertTrue(runtime.activeCount(level.getServer()) == baseline + 2, "The cap is never exceeded");
            runtime.manager(level).remove(first);
            replacement = runtime.spawn(other, state(helper));
            helper.assertTrue(replacement != null, "Removing a bullet immediately restores capacity");
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, 1);
            helper.assertTrue(runtime.activeCount(level.getServer()) == baseline + 2, "Lowering the cap keeps existing bullets");
            BulletState afterLowering = runtime.spawn(level, state(helper));
            if (afterLowering != null) runtime.manager(level).remove(afterLowering);
            helper.assertTrue(afterLowering == null, "A lowered cap refuses new bullets until count falls below it");
        } finally {
            if (first != null) runtime.manager(level).remove(first);
            if (second != null) runtime.manager(other).remove(second);
            if (replacement != null) runtime.manager(other).remove(replacement);
            if (rejected != null) runtime.manager(level).remove(rejected);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_protection")
    public static void cappedAttackSkipsStateConstruction(GameTestHelper helper) {
        var runtime = BulletRuntime.INSTANCE;
        ServerLevel level = helper.getLevel();
        BulletState live = runtime.spawn(level, state(helper));
        var owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "bullet_cap"));
        var attack = new BulletAttack(0) {
            @Override protected BulletState createOptimizedState(AttackContext context) {
                helper.fail("A full server must reject attacks before constructing projectile state");
                return null;
            }
        };
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.MAX_ACTIVE_BULLETS, runtime.activeCount(level.getServer()));
            attack.shoot(AttackContext.builder(owner, owner).build());
        } finally {
            if (live != null) runtime.manager(level).remove(live);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_protection")
    public static void countCommandStoresLiveCountsIncludingZero(GameTestHelper helper) throws Exception {
        var runtime = BulletRuntime.INSTANCE;
        ServerLevel level = helper.getLevel();
        var server = level.getServer();
        var scoreboard = server.getScoreboard();
        var objective = scoreboard.addObjective("bullet_cap_test", ObjectiveCriteria.DUMMY,
                Component.literal("Bullet protection test"), ObjectiveCriteria.RenderType.INTEGER);
        var source = server.createCommandSourceStack().withSuppressedOutput();
        var dispatcher = server.getCommands().getDispatcher();
        int baseline = runtime.activeCount(server);
        BulletState live = null;
        try {
            scoreboard.getOrCreatePlayerScore("#probe", objective).setScore(-1);
            dispatcher.execute("execute store result score #probe bullet_cap_test run isd bullet count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#probe", objective).getScore() == baseline,
                    "The command stores the actual count, including zero");
            live = runtime.spawn(level, state(helper));
            helper.assertTrue(live != null, "The fixture bullet can spawn");
            dispatcher.execute("execute store result score #probe bullet_cap_test run isd bullet count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#probe", objective).getScore() == baseline + 1,
                    "A later command sees newly spawned bullets");
            runtime.manager(level).remove(live);
            dispatcher.execute("execute store result score #probe bullet_cap_test run isd bullet count", source);
            helper.assertTrue(scoreboard.getOrCreatePlayerScore("#probe", objective).getScore() == baseline,
                    "The stored count falls again after removal");
        } finally {
            if (live != null) runtime.manager(level).remove(live);
            scoreboard.removeObjective(objective);
        }
        helper.succeed();
    }

    private static BulletState state(GameTestHelper helper) {
        return BulletState.builder().position(helper.absoluteVec(new Vec3(1, 3, 1)))
                .velocity(new Vec3(1, 0, 0)).lifetime(200).range(64).spectral(true).build();
    }
}
