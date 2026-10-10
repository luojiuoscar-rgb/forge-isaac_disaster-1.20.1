package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModules;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Ipecac;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.MyReflection;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.TinyPlanet;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.HookWorm;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.OuroborosWorm;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.RingWorm;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl.WiggleWorm;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.AbstractLaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.GravityTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.MyReflectionBulletTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.MyReflectionLaserTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.WiggleWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Runs against real Forge registries/capabilities; excluded from the mod jar. */
@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TrajectoryModuleGameTests {
    @GameTest(template = "trajectory_empty")
    public static void externalRulesAreIndexedAndRemovedStatesDoNotLeak(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ResourceLocation a = ExternalTrajectoryRuleRegistration.A;
        ResourceLocation b = ExternalTrajectoryRuleRegistration.B;
        ResourceLocation e = ExternalTrajectoryRuleRegistration.E;
        WiggleWormTrajectoryModule.State oldState = new WiggleWormTrajectoryModule.State();
        oldState.phase(4);
        TrajectoryRuntime inherited =
            new TrajectoryRuntime(Vec3.ZERO, new Vec3(1, 0, 0), List.of(new TrajectorySpec(a, 0)));
        inherited.states().put(a, oldState);
        AttackContext context =
            AttackContext.builder(player, player)
                .attackType(ModAttackTypes.BULLET.get())
                .inheritTrajectorySnapshot(inherited.snapshot())
                .build();
        context.addTrajectoryModule(a, 3);
        context.addTrajectoryModule(b, 2);
        context.freeze();
        helper.assertTrue(
            context.getTrajectorySpecs().equals(List.of(new TrajectorySpec(e, 1))),
            "External registry entry participates in index");
        helper.assertTrue(
            BulletState.from(context).build().getTrajectoryRuntime().states().isEmpty(),
            "Consumed state is not inherited or synchronized");
        AttackContext copy = context.toBuilder().build();
        copy.freeze();
        helper.assertTrue(
            copy.getTrajectorySpecs().equals(context.getTrajectorySpecs()),
            "Resolved copy stays stable");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredPlanetRuleResolvesBeforeCreationAndPreservesLaser(
        GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.TINY_PLANET.getId(), 3);
        modules(player).getTriggerModules().add(ModTriggerModules.MY_REFLECTION.getId(), 2);
        for (AttackType type :
            List.of(
                ModAttackTypes.BULLET.get(),
                ModAttackTypes.LASER.get())) {
            AttackContext context = AttackContext.builder(player, player).attackType(type).build();
            AttackContext prepared = prepare(player, context);
            boolean laser = ModAttackTypes.LASER.getId().equals(prepared.getRootTypeId());
            ResourceLocation planet =
                laser
                    ? ModTrajectoryModules.TINY_PLANET_LASER.getId()
                    : ModTrajectoryModules.TINY_PLANET_BULLET.getId();
            ResourceLocation mirror =
                laser
                    ? ModTrajectoryModules.MY_REFLECTION_LASER.getId()
                    : ModTrajectoryModules.MY_REFLECTION_BULLET.getId();
            helper.assertTrue(
                prepared.getTrajectorySpecs().contains(new TrajectorySpec(planet, 2)),
                "Planet keeps its stacks");
            helper.assertTrue(
                prepared.getTrajectorySpecs().stream().anyMatch(s -> s.id().equals(mirror)) == laser,
                "Ordinary-only coverage rule runs through live registry before construction");
            AttackContext copied = prepared.toBuilder().build();
            copied.freeze();
            helper.assertTrue(
                copied.getTrajectorySpecs().equals(prepared.getTrajectorySpecs()),
                "Copy does not rerun rewrites");
            CaptureAttack capture = new CaptureAttack();
            AttackPipeline.executeRequest(
                AttackRequest.withContexts(
                    player,
                    capture,
                    AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.EXECUTE_ONLY,
                    List.of(copied),
                    false));
            helper.assertTrue(
                capture.executed.get(0).getTrajectorySpecs().equals(prepared.getTrajectorySpecs()),
                "Direct execution retains resolved snapshot");
            helper.assertTrue(
                BulletState.from(copied)
                    .build()
                    .getTrajectorySpecs()
                    .equals(prepared.getTrajectorySpecs()),
                "Constructed bullet receives resolved modules");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void reflectionTargetDependsOnShooterAndSurvivesNetworkCopy(GameTestHelper helper) {
        ServerPlayer owner = player(helper);
        Vec3 origin = owner.position().add(0, 12, 0);
        owner.setPos(origin);
        var module = new MyReflectionBulletTrajectoryModule();
        for (boolean self : List.of(true, false)) {
            Object shooter = self ? owner : new Object();
            BulletState bullet =
                BulletState.builder()
                    .owner(owner)
                    .shooter(shooter)
                    .position(origin)
                    .velocity(new Vec3(1, 0, 0))
                    .baseSpeed(1)
                    .range(100)
                    .build();
            MyReflectionBulletTrajectoryModule.State state = module.createState();
            module.initialize(
                new TrajectoryContext(
                    bullet,
                    new TrajectoryContext.Input(
                        origin, bullet.velocity(), bullet.velocity(), Vec3.ZERO, 0),
                    origin,
                    0,
                    state));
            bullet.setPosition(origin.add(20, 0, 0));
            owner.setPos(origin.add(20, 0, 25));
            var motion =
                module.apply(
                    new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                        bullet,
                        new TrajectoryContext.Input(
                            bullet.position(),
                            bullet.velocity(),
                            bullet.velocity(),
                            Vec3.ZERO,
                            1),
                        owner.position().add(0, owner.getBbHeight() * 0.6, 0),
                        0,
                        state));
            if (self)
                helper.assertTrue(
                    motion.desiredPosition().z > origin.z, "Self-fired bullet follows moved owner");
            else {
                BulletState reference =
                    BulletState.builder().position(origin).velocity(bullet.velocity()).range(100).build();
                reference.setPosition(bullet.position());
                var fixed =
                    module.apply(
                        new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                            reference,
                            new TrajectoryContext.Input(
                                reference.position(),
                                reference.velocity(),
                                reference.velocity(),
                                Vec3.ZERO,
                                1),
                            origin.add(0, -50, 0),
                            0,
                            module.createState()));
                helper.assertTrue(
                    fixed.desiredPosition().equals(motion.desiredPosition()),
                    "Other shooter retains spawn target");
            }
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            MyReflectionBulletTrajectoryModule.State decoded;
            try {
                module.writeState(buffer, state.copy());
                decoded = module.readState(buffer);
            } finally {
                buffer.release();
            }
            BulletState client =
                BulletState.builder()
                    .position(bullet.position())
                    .restoreTrajectory(bullet.getTrajectoryRuntime().snapshot())
                    .velocity(bullet.velocity())
                    .range(100)
                    .build();
            owner.setPos(origin.add(30, 0, -20));
            Vec3 anchor = owner.position().add(0, owner.getBbHeight() * 0.6, 0);
            var serverNext =
                module.apply(
                    new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                        bullet,
                        new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext.Input(
                            bullet.position(),
                            bullet.velocity(),
                            bullet.velocity(),
                            Vec3.ZERO,
                            1),
                        anchor,
                        0,
                        state));
            var clientNext =
                module.apply(
                    new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                        client,
                        new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext.Input(
                            client.position(),
                            client.velocity(),
                            client.velocity(),
                            Vec3.ZERO,
                            1),
                        anchor,
                        0,
                        decoded));
            helper.assertTrue(
                serverNext.desiredPosition().distanceTo(clientNext.desiredPosition()) < 1e-8,
                "Client without entity references uses the same target policy");
            owner.setPos(origin);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void ownSpawnIsUsedByOrdinaryFetusAndLaserChildren(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Vec3 spawn = player.position().add(10, 12, 5);
        AttackContext context =
            AttackContext.builder(player, player)
                .position(spawn)
                .inheritTrajectorySnapshot(
                    new TrajectoryRuntime(spawn.add(-20, 0, 0), new Vec3(0, 0, 1), List.of())
                        .snapshot())
                .mainAxis(new Vec3(1, 0, 0))
                .range(30)
                .build();
        BulletState ordinary = new SpawnBulletProbe().create(context);
        BulletState fetus = new SpawnFetusProbe().create(context);
        helper.assertTrue(
            ordinary.getTrajectoryRuntime().origin().equals(ordinary.position()),
            "Ordinary origin includes actual spawn offsets");
        helper.assertTrue(
            fetus.getTrajectoryRuntime().origin().equals(fetus.position()),
            "Fetus origin includes its own center offset");
        AbstractLaserAttack.LaserProjectile parent =
            new AbstractLaserAttack.LaserProjectile(
                context.copy().bindAttackTypeOrCopy(ModAttackTypes.LASER.get()));
        helper.assertTrue(
            parent.getTrajectoryRuntime().origin().equals(spawn),
            "Laser does not inherit parent origin");
        var mirror = new MyReflectionLaserTrajectoryModule();
        var mirrorState = mirror.createState();
        mirror.apply(
            new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                parent,
                new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext.Input(
                    spawn,
                    new Vec3(1, 0, 0),
                    new Vec3(1, 0, 0),
                    Vec3.ZERO,
                    1),
                Vec3.ZERO,
                0,
                mirrorState));
        Vec3 childSpawn = spawn.add(6, 3, 1);
        AbstractLaserAttack.LaserProjectile child =
            new AbstractLaserAttack.LaserProjectile(
                parent.getAttackContext().toBuilder().position(childSpawn).build());
        helper.assertTrue(
            child.getTrajectoryRuntime().origin().equals(childSpawn),
            "Laser child owns its spawn origin");
        helper.assertTrue(
            parent.getTrajectoryRuntime().origin().equals(spawn), "Parent origin unchanged");
        double rest = MyReflectionLaserTrajectoryModule.PRELUDE - mirrorState.distance();
        var result =
            mirror.apply(
                new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext(
                    child,
                    new net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext.Input(
                        childSpawn,
                        new Vec3(1, 0, 0),
                        new Vec3(rest, 0, 0),
                        Vec3.ZERO,
                        1),
                    Vec3.ZERO,
                    0,
                    mirrorState.copy()));
        helper.assertTrue(
            result.desiredPosition().distanceTo(childSpawn) < 1e-7,
            "Copied laser Reflection returns to child origin");
        for (var type : List.of(ModAttackTypes.LASER.get(), ModAttackTypes.BRIMSTONE.get())) {
            AbstractLaserAttack.LaserProjectile typed =
                new AbstractLaserAttack.LaserProjectile(context.toBuilder().attackType(type).build());
            typed.setAttackSequenceIndex(type == ModAttackTypes.LASER.get() ? 5 : 0);
            helper.assertTrue(
                typed.getTypeId().equals(type.getId()),
                "Projectile family is independent of sequence index");
        }
        helper.succeed();
    }

    private static final class SpawnBulletProbe
        extends net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BulletAttack {
        SpawnBulletProbe() {
            super(0);
        }

        BulletState create(AttackContext context) {
            return createOptimizedState(context);
        }
    }

    private static final class SpawnFetusProbe
        extends net.luojiuoscar.isaac_disaster.registries.attack_type.impl.CSectionAttack {
        SpawnFetusProbe() {
            super(0, 0);
        }

        BulletState create(AttackContext context) {
            return createOptimizedState(context);
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void ipecacGravityUsesLiveRegistryAndPreservesLaserPaths(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.IPECAC.getId(), 1);
        helper.assertTrue(
            ModTrajectoryModules.GRAVITY.get() instanceof GravityTrajectoryModule,
            "Independent gravity module");
        AttackContext context =
            prepare(
                player,
                AttackContext.builder(player, player)
                    .mainAxis(new Vec3(1, 0, 0))
                    .position(player.position().add(0, 12, 0))
                    .range(30)
                    .build());
        helper.assertTrue(
            context
                .getTrajectorySpecs()
                .equals(List.of(new TrajectorySpec(ModTrajectoryModules.GRAVITY.getId(), 0))),
            "Ipecac attaches gravity");
        helper.assertTrue(
            context.getTrigger().getView().contains(ModExecutableEffects.IPECAC.get()),
            "Explosion triggers preserved");
        for (AttackType source :
            java.util.List.of(
                ModAttackTypes.BULLET.get(),
                ModAttackTypes.C_SECTION.get(),
                ModAttackTypes.LASER.get(),
                ModAttackTypes.BRIMSTONE.get())) {
            BulletState bullet =
                BulletState.from(context.copy().bindAttackTypeOrCopy(source))
                    .velocity(new Vec3(1, 0, 0))
                    .baseSpeed(1)
                    .lifetime(1000)
                    .build();
            Vec3 start = bullet.position();
            for (int i = 0; i < 2; i++) {
                var motion =
                    net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator.evaluate(
                        bullet, new Vec3(1, 0, 0), 1, 1);
                bullet.advanceTrajectory(motion, 1);
            }
            boolean laser =
                source == ModAttackTypes.LASER.get() || source == ModAttackTypes.BRIMSTONE.get();
            helper.assertTrue(
                Math.abs(bullet.position().y - start.y - (laser ? 0 : -0.15)) < 1e-8,
                "Gravity by projectile family");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void reflectionLaserCollidesOnLateralPrelude(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Vec3 origin = helper.absolutePos(net.minecraft.core.BlockPos.ZERO).getCenter().add(0, 12, 0);
        player.setPos(origin);
        AttackContext context =
            AttackContext.builder(player, player)
                .attackType(ModAttackTypes.LASER.get())
                .position(origin)
                .mainAxis(new Vec3(1, 0, 0))
                .range(16)
                .build();
        context.addTrajectoryModule(ModTrajectoryModules.MY_REFLECTION_LASER.getId(), 1);
        var wall = net.minecraft.core.BlockPos.containing(origin.add(3.5, 0, -1.5));
        helper
            .getLevel()
            .setBlockAndUpdate(wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        try {
            AbstractLaserAttack.LaserProjectile laser = new AbstractLaserAttack.LaserProjectile(context);
            laser.setStep(0.1);
            laser.setWidth(0.1);
            laser.damage = 0;
            new CollisionProbe().run(laser, helper.getLevel(), context);
            helper.assertTrue(laser.traveled >= 16, "Off-axis obstacle terminates laser");
            Vec3 hit = laser.position.subtract(origin);
            helper.assertTrue(
                hit.x > 2 && hit.x < 5 && hit.z < -1,
                "Collision follows lateral prelude, not launch axis");
        } finally {
            helper.getLevel().removeBlock(wall, false);
        }
        helper.succeed();
    }

    private static final class CollisionProbe extends LaserAttack {
        CollisionProbe() {
            super(0);
        }

        void run(
            LaserProjectile laser,
            net.minecraft.server.level.ServerLevel level,
            AttackContext context) {
            int steps = 0;
            while (laser.traveled < laser.getRange() && steps++ < 1000) {
                stepLaser(laser, level, context);
            }
            if (steps > 1000) throw new AssertionError("Reflection laser exceeded step budget");
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void reflectionDispatchesRealAttackFamiliesAndSurvivesDirectExecution(
        GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.MY_REFLECTION.getId(), 2);
        helper.assertTrue(
            ModTrajectoryModules.MY_REFLECTION_BULLET.get()
                instanceof MyReflectionBulletTrajectoryModule,
            "Ordinary module registration");
        helper.assertTrue(
            ModTrajectoryModules.MY_REFLECTION_LASER.get() instanceof MyReflectionLaserTrajectoryModule,
            "Laser module registration");
        for (AttackType type :
            List.of(
                ModAttackTypes.BULLET.get(),
                ModAttackTypes.LASER.get())) {
            AttackContext context = AttackContext.builder(player, player).build();
            AttackRequest request =
                AttackRequest.withContexts(
                    player,
                    type,
                    AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.PREPARE_AND_EXECUTE,
                    List.of(context),
                    false);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent(
                    request,
                    new AttackPlan(request, List.of(context)),
                    context.bindAttackTypeOrCopy(type),
                    0));
            ResourceLocation expected =
                type instanceof AbstractLaserAttack
                    ? ModTrajectoryModules.MY_REFLECTION_LASER.getId()
                    : ModTrajectoryModules.MY_REFLECTION_BULLET.getId();
            helper.assertTrue(context.getAttackType() == type, "Source type available at prepare");
            helper.assertTrue(
                context.getTrajectorySpecs().equals(List.of(new TrajectorySpec(expected, 1))),
                "Family: " + type.getId());
            CaptureAttack capture = new CaptureAttack();
            AttackPipeline.executeRequest(
                AttackRequest.withContexts(
                    player,
                    capture,
                    AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.EXECUTE_ONLY,
                    List.of(context.copy()),
                    false));
            helper.assertTrue(
                capture.executed.get(0).getTrajectorySpecs().equals(context.getTrajectorySpecs()),
                "Execute only preserves stacks");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void laserLoopTerminatesAcrossHomingInterruptions(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setPos(helper.absolutePos(net.minecraft.core.BlockPos.ZERO).getCenter().add(0, 12, 0));
        var target = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
        target.setNoAi(true);
        target.setInvulnerable(true);
        target.setPos(player.position().add(4, 0, 0));
        helper.getLevel().addFreshEntity(target);
        try {
            for (int combination = 0; combination < 4; combination++) {
                for (boolean brimstone : List.of(false, true)) {
                    AttackContext context =
                        AttackContext.builder(player, player)
                            .attackType(
                                brimstone ? ModAttackTypes.BRIMSTONE.get() : ModAttackTypes.LASER.get())
                            .mainAxis(new Vec3(1, 0, 0))
                            .position(player.position().add(0, 1, 0))
                            .range(16)
                            .build();
                    if (combination != 1)
                        context.addTrajectoryModule(ModTrajectoryModules.TINY_PLANET_LASER.getId(), 1);
                    if (combination != 0)
                        context.addTrajectoryModule(ModTrajectoryModules.MY_REFLECTION_LASER.getId(), 1);
                    if (combination == 3) {
                        context.addTrajectoryModule(ModTrajectoryModules.HOOK_WORM.getId(), 1);
                        context.addTrajectoryModule(ModTrajectoryModules.WIGGLE_WORM.getId(), 1);
                        context.addTrajectoryModule(ModTrajectoryModules.RING_WORM.getId(), 1);
                        context.addTrajectoryModule(ModTrajectoryModules.OUROBOROS_WORM.getId(), 1);
                    }
                    AbstractLaserAttack.LaserProjectile laser = new AbstractLaserAttack.LaserProjectile(context);
                    laser.setAttackSequenceIndex(brimstone ? 1 : 0);
                    laser.setStep(0.1);
                    laser.setWidth(0.1);
                    laser.setSpectral(true);
                    laser.damage = 0;
                    HomingProbe probe = new HomingProbe(target);
                    probe.run(laser, helper.getLevel(), context);
                    helper.assertTrue(
                        laser.traveled >= 16, "Laser exhausts range and returns from real stepping loop");
                    helper.assertTrue(
                        probe.homingSteps > 0 && probe.resumedSteps > 0,
                        "Homing enters and trajectory resumes");
                    helper.assertTrue(probe.steps < 900, "Finite laser work including both free preludes");
                }
            }
        } finally {
            target.discard();
        }
        helper.succeed();
    }

    private static final class HomingProbe extends LaserAttack {
        final LivingEntity target;
        int steps, homingSteps, resumedSteps;

        HomingProbe(LivingEntity target) {
            super(0);
            this.target = target;
        }

        void run(
            LaserProjectile laser,
            net.minecraft.server.level.ServerLevel level,
            AttackContext context) {
            while (laser.traveled < laser.getRange() && steps < 900) {
                stepLaser(laser, level, context);
            }
        }

        @Override
        protected void stepLaser(
            LaserProjectile laser,
            net.minecraft.server.level.ServerLevel level,
            AttackContext context) {
            steps++;
            laser.homing = steps >= 20 && steps < 50 || steps >= 90 && steps < 120;
            if (laser.homing) {
                target.setPos(laser.position.add(4, 0, 0));
                laser.homingTarget = target;
            }
            double phase = laser.getTrajectoryRuntime().distance();
            super.stepLaser(laser, level, context);
            if (laser.isCurrentlyHoming) {
                homingSteps++;
                if (laser.getTrajectoryRuntime().distance() != phase)
                    throw new AssertionError("Homing advanced the orbit phase");
            } else if (homingSteps > 0) resumedSteps++;
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void planetRuntimeSamplesOrbitAndFreeLaserRange(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.TINY_PLANET.getId(), 1);
        helper.assertTrue(
            ModTrajectoryModules.TINY_PLANET_BULLET.get() instanceof TinyPlanetTrajectoryModule,
            "Independent module registration");
        for (AttackType source :
            java.util.List.of(
                ModAttackTypes.BULLET.get(), ModAttackTypes.LASER.get())) {
            for (Vec3 direction :
                List.of(new Vec3(1, 0, 0), new Vec3(1, 1, 0).normalize(), new Vec3(0, 1, 0))) {
                boolean laser =
                    source == ModAttackTypes.LASER.get() || source == ModAttackTypes.BRIMSTONE.get();
                AttackContext context =
                    AttackContext.builder(player, player)
                        .mainAxis(direction)
                        .position(player.position().add(0, 1, 0))
                        .range(100)
                        .build();
                context.setAttackType(source);
                context = prepare(player, context);
                BulletState bullet =
                    BulletState.from(context.copy().bindAttackTypeOrCopy(source))
                        .baseSpeed(0.1)
                        .lifetime(laser ? 1000 : 2)
                        .build();
                net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator.initialize(bullet);
                for (int i = 0; i < 400; i++) {
                    var motion =
                        net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator.evaluate(
                            bullet, direction.scale(0.1), 0.1, 1);
                    helper.assertTrue(bullet.advanceTrajectory(motion, 1), "Range overrides old lifetime");
                    if (!laser && i > 90) {
                        Vec3 relative = bullet.position().subtract(bullet.getTrajectoryRuntime().anchor());
                        helper.assertTrue(
                            Math.abs(Math.hypot(relative.x, relative.z) - 3) < 1e-6, "Server orbit radius");
                    }
                    if (laser && i < 200)
                        helper.assertTrue(bullet.traveled() == 0, "Free prelude through evaluator");
                }
                double expected = laser ? 40 - 5 - 6 * Math.PI : 40;
                helper.assertTrue(
                    Math.abs(bullet.traveled() - expected) < 1e-6, "Range cost through runtime");
                if (laser)
                    helper.assertTrue(
                        bullet.velocity().normalize().dot(direction) > 0.99999, "3D direction restored");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void allAttackFamiliesAcquireModulesOnlyInPrepare(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.RING_WORM.getId(), 2);
        for (AttackType type :
            List.of(
                ModAttackTypes.BULLET.get(),
                ModAttackTypes.LASER.get(),
                ModAttackTypes.C_SECTION.get(),
                ModAttackTypes.BRIMSTONE.get(),
                ModAttackTypes.TECHNOLOGY2.get())) {
            List<AttackContext> contexts = type.getAttackContexts(player, 1);
            helper.assertTrue(!contexts.isEmpty(), "Attack contexts exist: " + type.getId());
            for (AttackContext context : contexts) {
                helper.assertTrue(
                    context.getTrajectorySpecs().isEmpty(), "No pre-prepare player table injection");
                AttackContext prepared = prepare(player, context);
                helper.assertTrue(
                    prepared
                        .getTrajectorySpecs()
                        .equals(List.of(new TrajectorySpec(ModTrajectoryModules.RING_WORM.getId(), 1))),
                    "Family attachment: " + type.getId());
                helper.assertTrue(prepared.isFrozen(), "Prepared context is frozen");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void abilitiesAttachStacksAndPersistOnlyTriggerModules(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        EffectModules modules = modules(player);
        List<PassiveAbility> passives =
            List.of(new TinyPlanet(0, 0), new MyReflection(0, 0), new Ipecac(0, 0));
        List<TrinketAbility> trinkets =
            List.of(
                new HookWorm(0, 0), new WiggleWorm(0, 0), new RingWorm(0, 0), new OuroborosWorm(0, 0));
        TrinketAbilityContext trinketContext = new TrinketAbilityContext(null, false);
        for (int i = 0; i < 2; i++) {
            passives.forEach(ability -> ability.handleObtain(player, null));
            trinkets.forEach(ability -> ability.onEquipped(player, trinketContext));
        }
        AttackContext prepared = prepare(player, AttackContext.builder(player, player).build());
        helper.assertTrue(
            prepared.getTrajectorySpecs().size() == 6,
            "Seven attachments resolve to six after Planet covers Reflection");
        helper.assertTrue(
            prepared.getTrajectorySpecs().stream().allMatch(spec -> spec.amplifier() == 1),
            "Two trigger stacks produce amplifier one");
        helper.assertTrue(
            prepared.getTrigger().getView().contains(ModExecutableEffects.IPECAC.get()),
            "Ipecac still attaches its hit effects");

        CompoundTag saved = new CompoundTag();
        modules.saveNBTData(saved);
        modules.init();
        modules.loadNBTData(saved);
        helper.assertTrue(
            prepare(player, AttackContext.builder(player, player).build())
                .getTrajectorySpecs()
                .equals(prepared.getTrajectorySpecs()),
            "Save/reload preserves module order and stacks");
        passives.forEach(ability -> ability.handleRemove(player, null));
        trinkets.forEach(ability -> ability.onUnequipped(player, trinketContext));
        helper.assertTrue(
            prepare(player, AttackContext.builder(player, player).build()).getTrajectorySpecs().stream()
                .allMatch(spec -> spec.amplifier() == 0),
            "Removing one copy retains one stack");
        passives.forEach(ability -> ability.handleRemove(player, null));
        trinkets.forEach(ability -> ability.onUnequipped(player, trinketContext));
        helper.assertTrue(
            prepare(player, AttackContext.builder(player, player).build())
                .getTrajectorySpecs()
                .isEmpty(),
            "Removing all sources removes trajectories");
        trinkets.get(0).onEquipped(player, trinketContext);
        helper.assertTrue(
            prepare(player, AttackContext.builder(player, player).build())
                .getTrajectorySpecs()
                .equals(List.of(new TrajectorySpec(ModTrajectoryModules.HOOK_WORM.getId(), 0))),
            "Reequip attaches once");
        CompoundTag playerData = new CompoundTag();
        player
            .getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
            .orElseThrow(IllegalStateException::new)
            .saveNBTData(playerData);
        helper.assertTrue(
            !playerData.contains("trajectories"), "PlayerAbility no longer serializes trajectories");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void preparedCopiesAndDirectExecutionKeepIndependentModules(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        modules(player).getTriggerModules().add(ModTriggerModules.WIGGLE_WORM.getId(), 2);
        AttackContext parent = prepare(player, AttackContext.builder(player, player).build());
        ResourceLocation id = ModTrajectoryModules.WIGGLE_WORM.getId();
        WiggleWormTrajectoryModule.State runtime = new WiggleWormTrajectoryModule.State();
        runtime.phase(2.5D);
        TrajectoryRuntime inherited =
            new TrajectoryRuntime(parent.getPos(), parent.getMainAxis(), parent.getTrajectorySpecs());
        inherited.states().put(id, runtime);
        parent = parent.toBuilder().inheritTrajectorySnapshot(inherited.snapshot()).build();
        AttackContext child = parent.toBuilder().direction(new Vec3(-1, 0, 0)).build();
        child.addTrajectoryModule(id, 1);
        runtime.phase(99);
        ((WiggleWormTrajectoryModule.State) child.getInheritedTrajectorySnapshot().restore().states().get(id)).phase(88);
        helper.assertTrue(
            parent.getTrajectorySpecs().get(0).amplifier() == 1, "Parent stack isolation");
        helper.assertTrue(
            child.getTrajectorySpecs().get(0).amplifier() == 2, "Child independently adds stacks");
        helper.assertTrue(
            ((WiggleWormTrajectoryModule.State) child.getInheritedTrajectorySnapshot().restore().states().get(id)).phase() == 2.5D,
            "Runtime defensive copies");
        CaptureAttack capture = new CaptureAttack();
        AttackPipeline.executeRequest(
            AttackRequest.withContexts(
                player,
                capture,
                AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.EXECUTE_ONLY,
                List.of(child),
                false));
        helper.assertTrue(
            capture.executed.get(0).getTrajectorySpecs().get(0).amplifier() == 2,
            "Direct execution must not attach player modules again");
        child.addTrajectoryModule(id, 5);
        helper.assertTrue(
            child.getTrajectorySpecs().get(0).amplifier() == 2, "Execution freezes attachment");
        for (AttackType source : List.of(ModAttackTypes.BULLET.get(), ModAttackTypes.C_SECTION.get())) {
            BulletState bullet = BulletState.from(child.copy().bindAttackTypeOrCopy(source)).build();
            helper.assertTrue(
                bullet.getTrajectorySpecs().equals(child.getTrajectorySpecs()), "Bullet family snapshot");
            ((WiggleWormTrajectoryModule.State) bullet.getTrajectoryRuntime().states().get(id)).phase(7);
            helper.assertTrue(
                ((WiggleWormTrajectoryModule.State) child.getInheritedTrajectorySnapshot().restore().states().get(id)).phase() == 2.5D,
                "Bullet runtime isolation");
        }
        AbstractLaserAttack.LaserProjectile laser =
            new AbstractLaserAttack.LaserProjectile(
                child.copy().bindAttackTypeOrCopy(ModAttackTypes.LASER.get()));
        helper.assertTrue(
            laser.getTrajectorySpecs().equals(child.getTrajectorySpecs()), "Laser family snapshot");
        helper.assertTrue(
            laser.getAttackContext().getTrajectorySpecs().equals(child.getTrajectorySpecs()),
            "Laser child context retains modules");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void finalPreparedFrameAndSnapshotAreIndependentForBothRoots(
        GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Vec3 spawn = player.position().add(5, 8, 9);
        AttackContext initial =
            AttackContext.builder(player, player)
                .attackType(ModAttackTypes.BULLET.get())
                .position(Vec3.ZERO)
                .mainAxis(new Vec3(1, 0, 0))
                .build();
        AttackContext changed =
            initial.toBuilder().position(spawn).direction(new Vec3(0, 1, 2)).build();
        changed.setMainAxis(new Vec3(0, 0, -1));
        changed.addTrajectoryModule(ModTrajectoryModules.WIGGLE_WORM.getId(), 1);
        changed.freeze();
        for (IBulletObject projectile :
            List.of(
                BulletState.from(changed).build(),
                new AbstractLaserAttack.LaserProjectile(
                    changed.copy().bindAttackTypeOrCopy(ModAttackTypes.LASER.get())))) {
            helper.assertTrue(
                projectile.getTrajectoryRuntime().origin().equals(spawn), "Uses final spawn position");
            helper.assertTrue(
                projectile.getTrajectoryRuntime().launchDirection().equals(new Vec3(0, 0, -1)),
                "Uses final prepared axis");
            var id = ModTrajectoryModules.WIGGLE_WORM.getId();
            projectile.getTrajectoryRuntime().putState(id, new WiggleWormTrajectoryModule.State());
            ((WiggleWormTrajectoryModule.State) projectile.getTrajectoryRuntime().states().get(id)).phase(2.5);
            AttackContext exported = projectile.getAttackContext();
            ((WiggleWormTrajectoryModule.State) projectile.getTrajectoryRuntime().states().get(id)).phase(7);
            if (projectile instanceof BulletState) {
                helper.assertTrue(
                    ((WiggleWormTrajectoryModule.State) exported.copy().getInheritedTrajectorySnapshot().restore().states().get(id)).phase()
                        == 2.5,
                    "Exported and copied bullet contexts cannot alias live runtime");
            } else {
                helper.assertTrue(
                    exported.getTrajectorySpecs().equals(changed.getTrajectorySpecs()),
                    "Laser export retains its immutable module configuration");
            }
        }
        helper.assertTrue(
            initial.getInheritedTrajectorySnapshot() == null,
            "Fresh attack preparation does not allocate trajectory state");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(
            helper.getLevel(), new GameProfile(UUID.randomUUID(), "trajectory-test"));
    }

    private static EffectModules modules(ServerPlayer player) {
        return player
            .getCapability(EffectModulesProvider.EFFECT_MODULES)
            .orElseThrow(IllegalStateException::new);
    }

    private static AttackContext prepare(ServerPlayer player, AttackContext context) {
        if (context.getRootTypeId() == null) context.setAttackType(ModAttackTypes.BULLET.get());
        CaptureAttack capture = new CaptureAttack(context.getAttackType());
        AttackPipeline.executeRequest(
            AttackRequest.withContexts(
                player,
                capture,
                AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.PREPARE_AND_EXECUTE,
                List.of(context),
                false));
        return capture.executed.get(0);
    }

    private static final class CaptureAttack extends AttackType {
        private final List<AttackContext> executed = new ArrayList<>();
        private final AttackType type;

        private CaptureAttack() {
            this(ModAttackTypes.BULLET.get());
        }

        private CaptureAttack(AttackType type) {
            super(0);
            this.type = type;
        }

        @Override
        public ResourceLocation getRootId() {
            return type.getRootId();
        }

        @Override
        public ResourceLocation getId() {
            return type.getId();
        }

        @Override
        public List<AttackContext> getAttackContexts(ServerPlayer player, int count) {
            return List.of();
        }

        @Override
        public void performAttack(List<AttackContext> contexts) {
            executed.addAll(contexts);
        }

        @Override
        public void makeSound(LivingEntity entity) {
        }

        @Override
        public void shoot(AttackContext context) {
            executed.add(context);
        }
    }
}
