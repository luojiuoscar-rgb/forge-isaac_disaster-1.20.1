package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.AbstractLaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.HookWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.OuroborosWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.RingWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.WiggleWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WormTrajectoryGameTests {
    private static List<ResourceLocation> worms() {
        return List.of(
            ModTrajectoryModules.HOOK_WORM.getId(),
            ModTrajectoryModules.WIGGLE_WORM.getId(),
            ModTrajectoryModules.RING_WORM.getId(),
            ModTrajectoryModules.OUROBOROS_WORM.getId());
    }

    private static ServerPlayer player(GameTestHelper h) {
        var p = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "worm-test"));
        p.setPos(h.absolutePos(net.minecraft.core.BlockPos.ZERO).getCenter().add(0, 20, 0));
        return p;
    }

    private static AttackContext context(ServerPlayer p, AttackType variant) {
        return AttackContext.builder(p, p)
            .position(p.position())
            .mainAxis(new Vec3(1, 0, 0))
            .attackType(variant)
            .range(24)
            .build();
    }

    @GameTest(template = "trajectory_empty")
    public static void allFourWormsAttachThroughRealPrepareAndCopy(GameTestHelper h) {
        var p = player(h);
        var triggers =
            p.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .orElseThrow(IllegalStateException::new)
                .getTriggerModules();
        for (var trigger :
            List.of(
                ModTriggerModules.HOOK_WORM,
                ModTriggerModules.WIGGLE_WORM,
                ModTriggerModules.RING_WORM,
                ModTriggerModules.OUROBOROS_WORM)) triggers.add(trigger.getId(), 2);
        h.assertTrue(
            ModTrajectoryModules.HOOK_WORM.get() instanceof HookWormTrajectoryModule, "Hook class");
        h.assertTrue(
            ModTrajectoryModules.WIGGLE_WORM.get() instanceof WiggleWormTrajectoryModule,
            "Wiggle class");
        h.assertTrue(
            ModTrajectoryModules.RING_WORM.get() instanceof RingWormTrajectoryModule, "Ring class");
        h.assertTrue(
            ModTrajectoryModules.OUROBOROS_WORM.get() instanceof OuroborosWormTrajectoryModule,
            "Ouroboros class");
        for (var type :
            List.of(
                ModAttackTypes.BULLET.get(),
                ModAttackTypes.C_SECTION.get(),
                ModAttackTypes.LASER.get(),
                ModAttackTypes.BRIMSTONE.get())) {
            var c = context(p, type);
            var request =
                AttackRequest.withContexts(
                    p,
                    type,
                    AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.PREPARE_AND_EXECUTE,
                    List.of(c),
                    false);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent(
                    request, new AttackPlan(request, List.of(c)), c, 0));
            c.freeze();
            h.assertTrue(
                c.getTrajectorySpecs().size() == 4
                    && c.getTrajectorySpecs().stream().allMatch(s -> s.amplifier() == 1),
                "Four modules once, stacks preserved");
            var b = BulletState.from(c).baseSpeed(1).build();
            var m = TrajectoryEvaluator.evaluate(b, new Vec3(1, 0, 0), 1, 1);
            b.advanceTrajectory(m, 1);
            var copied = b.getAttackContext().toBuilder().build();
            copied.freeze();
            h.assertTrue(
                copied.getInheritedTrajectorySnapshot().restore().kinematics().initialized(),
                "Primary state survives child context");
            var child = BulletState.from(copied).build();
            child.getTrajectoryRuntime().kinematics().advanceDistance(10);
            h.assertTrue(
                Math.abs(b.getTrajectoryRuntime().kinematics().distance() - 1) < 1e-7,
                "Child state independent");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void hookLaserTerminatesAndWormCurvesCollide(GameTestHelper h) {
        var p = player(h);
        for (var variant : List.of(ModAttackTypes.LASER.get(), ModAttackTypes.BRIMSTONE.get())) {
            var c = context(p, variant);
            for (var id : worms()) c.addTrajectoryModule(id, 1);
            c.freeze();
            var laser = new AbstractLaserAttack.LaserProjectile(c);
            laser.setStep(0.1);
            laser.setSpectral(true);
            laser.damage = 0;
            new Probe().run(laser, h, c);
            h.assertTrue(
                laser.traveled >= laser.getRange(),
                "All-worm laser completes its extended free-motion budget");
        }
        for (int i = 0; i < 3; i++) {
            ResourceLocation id = worms().get(i == 0 ? 0 : i + 1);
            Vec3 relative = i == 0 ? new Vec3(2, 0, 1) : i == 1 ? new Vec3(4, 0, -1) : new Vec3(2, 1, 0);
            var wall = net.minecraft.core.BlockPos.containing(p.position().add(relative));
            h.getLevel()
                .setBlockAndUpdate(
                    wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            try {
                var c = context(p, ModAttackTypes.LASER.get());
                c.addTrajectoryModule(id, 1);
                c.freeze();
                var laser = new AbstractLaserAttack.LaserProjectile(c);
                laser.setStep(0.1);
                laser.setWidth(0.1);
                laser.damage = 0;
                new Probe().run(laser, h, c);
                h.assertTrue(
                    laser.getLastBlockHit() != null && laser.getLastBlockHit().getBlockPos().equals(wall),
                    "Collision uses actual worm path: "
                        + id
                        + "; expected="
                        + wall
                        + "; hit="
                        + (laser.getLastBlockHit() == null ? null : laser.getLastBlockHit().getBlockPos())
                        + "; final="
                        + laser.getPosition()
                        + "; origin="
                        + c.getPos());
            } finally {
                h.getLevel().removeBlock(wall, false);
            }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void ordinaryHookCollisionConsumesLateralPath(GameTestHelper h) {
        var p = player(h);
        var wall = net.minecraft.core.BlockPos.containing(p.position().add(2, 0, 1));
        h.getLevel()
            .setBlockAndUpdate(wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        try {
            var c = context(p, ModAttackTypes.BULLET.get());
            c.addTrajectoryModule(ModTrajectoryModules.HOOK_WORM.getId(), 1);
            c.freeze();
            var b = BulletState.from(c).baseSpeed(1).lifetime(2).build();
            var manager = new BulletManager();
            manager.spawn(b);
            for (int i = 0; i < 4 && b.isAlive(); i++) manager.tick(h.getLevel());
            h.assertTrue(
                b.getLastBlockHit() != null && b.getLastBlockHit().getBlockPos().equals(wall),
                "Real manager collides on lateral segment");
        } finally {
            h.getLevel().removeBlock(wall, false);
        }
        h.succeed();
    }

    private static final class Probe extends LaserAttack {
        Probe() {
            super(0);
        }

        void run(LaserProjectile laser, GameTestHelper h, AttackContext c) {
            int steps = 0;
            while (laser.traveled < laser.getRange() && steps++ < 1000) {
                stepLaser(laser, h.getLevel(), c);
            }
            h.assertTrue(steps <= 1000, "Worm laser finishes within the step budget");
        }
    }
}
