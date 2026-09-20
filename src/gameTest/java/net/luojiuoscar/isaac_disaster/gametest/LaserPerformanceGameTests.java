package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LaserPerformanceGameTests {
    @GameTest(template = "trajectory_empty", timeoutTicks = 200)
    public static void planetOuroborosBurstKeepsFiniteWorkAndOrdinaryMotion(GameTestHelper h) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "laser-pressure"));
        player.setPos(h.absolutePos(BlockPos.ZERO).getCenter().add(0, 100, 0));
        var ctx = AttackContext.builder(player, player).position(player.position()).mainAxis(new Vec3(1, 0.3, 0))
                .attackType(ModAttackTypes.LASER.get()).range(16).build();
        ctx.addTrajectoryModule(ModTrajectoryModules.TINY_PLANET_LASER.getId(), 1);
        ctx.addTrajectoryModule(ModTrajectoryModules.OUROBOROS_WORM.getId(), 1);
        ctx.freeze();
        var manager = new BulletManager();
        var ordinaryCtx = AttackContext.builder(player, player).position(player.position().add(0, 10, 0))
                .mainAxis(new Vec3(1, 0, 0)).attackType(ModAttackTypes.BULLET.get()).range(32).build();
        ordinaryCtx.addTrajectoryModule(ModTrajectoryModules.OUROBOROS_WORM.getId(), 1);
        ordinaryCtx.freeze();
        BulletState ordinary = manager.spawn(BulletState.from(ordinaryCtx).baseSpeed(0.5)
                .velocity(new Vec3(0.5, 0, 0)).lifetime(2).build());
        long started = System.nanoTime();
        long raw = 0, edges = 0, particles = 0, shapes = 0, queries = 0;
        try {
            for (int tick = 0; tick < 4; tick++) {
                for (int shot = 0; shot < 8; shot++) {
                    var laser = new LaserAttack.LaserProjectile(ctx);
                    laser.setStep(0.1); laser.setWidth(0.25); laser.setSpectral(true); laser.damage = 0;
                    new Probe().run(laser, h, ctx);
                    var stats = laser.traceStats();
                    h.assertTrue(stats.termination() == LaserAttack.TraceTermination.RANGE, "Burst laser finishes its range");
                    h.assertTrue(stats.steps() < 1000 && stats.collisionSegments() < 12000, "Finite work per shot");
                    h.assertTrue(stats.particles() * 5 < stats.rawSegments() * 2, "Visual packets no longer scale with fine collision edges");
                    h.assertTrue(stats.entityQueries() <= stats.steps(), "Empty-space broad phase runs at most once per step");
                    raw += stats.rawSegments(); edges += stats.collisionSegments(); particles += stats.particles();
                    shapes += stats.blockShapeQueries(); queries += stats.entityQueries();
                }
                Vec3 before = ordinary.position();
                manager.tick();
                h.assertTrue(ordinary.isAlive() && ordinary.position().distanceTo(before) > 0.01, "Ordinary movement resumes after burst");
                h.assertTrue(ordinary.velocity().lengthSqr() > 1e-8, "Ordinary speed is retained");
                h.assertTrue(Math.abs(ordinary.traveled() - (tick + 1) * 0.5) < 1e-7, "Primary range keeps advancing");
            }
        } finally { manager.clear(); }
        IsaacDisaster.LOGGER.info("Laser pressure sample: shots=32, elapsedMs={}, rawEdges={}, collisionEdges={}, particles={}, blockShapes={}, entityQueries={}",
                (System.nanoTime() - started) / 1_000_000.0, raw, edges, particles, shapes, queries);
        h.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void terminalResidueAndInheritedPreludeDoNotSpendExtraWork(GameTestHelper h) {
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "laser-end"));
        var ctx = AttackContext.builder(player, player).position(h.absolutePos(BlockPos.ZERO).getCenter().add(0, 100, 0))
                .mainAxis(new Vec3(1, 0, 0)).attackType(ModAttackTypes.LASER.get()).range(16).build();
        var laser = new LaserAttack.LaserProjectile(ctx);
        laser.setStep(0.1); laser.traveled = Math.nextDown(16.0);
        new Probe().run(laser, h, ctx);
        h.assertTrue(laser.traveled == 16 && laser.traceStats().steps() == 0, "Terminal epsilon is normalized before evaluation");
        var straight = new LaserAttack.LaserProjectile(ctx);
        straight.setStep(0.1); straight.setWidth(0.1); straight.setSpectral(true);
        new Probe().run(straight, h, ctx);
        h.assertTrue(straight.traveled == 16 && straight.traceStats().termination() == LaserAttack.TraceTermination.RANGE,
                "Plain laser still completes exact range");
        h.succeed();
    }

    private static final class Probe extends LaserAttack {
        Probe() { super(0); }
        void run(LaserProjectile laser, GameTestHelper h, AttackContext ctx) { traceLaser(laser, h.getLevel(), ctx); }
    }
}
