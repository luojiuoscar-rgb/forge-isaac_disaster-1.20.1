package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.PanicEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PanicAndRingGameTests {
    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void ringPetrifiesSlimesThroughoutItsHorizontalAnnulus(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ring_height"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        for (var offset : new Vec3[]{new Vec3(1.8, 1.0, 0), new Vec3(1.8, -1.0, 0),
                new Vec3(2.0, 2.0, 0), new Vec3(0, -2.0, 2.0), new Vec3(1.2, 1.5, 1.2)}) {
            var slime = helper.spawn(EntityType.SLIME, new BlockPos(1, 1, 1));
            slime.setSize(1, true);
            slime.setPos(player.position().add(offset));
            try {
                ModExecutableEffects.DADS_RING.get().apply(new ExecutableEffectContext(player));
                helper.assertTrue(slime.hasEffect(ModEffects.PETRIFIED.get()),
                        "Slimes inside the horizontal annulus must be petrified at offset " + offset);
                helper.assertTrue(EntityVisualState.isFrozen(slime), "Petrification activates the freeze state");
            } finally {
                slime.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void ringExcludesItsHorizontalCenterAndTargetsBeyondCylinderBounds(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ring_bounds"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        for (var offset : new Vec3[]{new Vec3(0.75, 1.5, 0), new Vec3(1.0, 1.5, 0),
                new Vec3(2.01, 0, 0), new Vec3(1.8, 0, 1.8),
                new Vec3(1.5, 2.01, 0), new Vec3(1.5, -2.01, 0)}) {
            var slime = helper.spawn(EntityType.SLIME, new BlockPos(1, 1, 1));
            slime.setSize(1, true);
            slime.setPos(player.position().add(offset));
            try {
                ModExecutableEffects.DADS_RING.get().apply(new ExecutableEffectContext(player));
                helper.assertTrue(!slime.hasEffect(ModEffects.PETRIFIED.get()),
                        "Targets outside the cylindrical annulus must be excluded at offset " + offset);
            } finally {
                slime.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void fearCancelsMeleeAndTargetsThenAllowsRecovery(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        var victim = helper.spawn(EntityType.COW, new BlockPos(2, 1, 1));
        zombie.setTarget(victim);
        zombie.setAggressive(true);
        zombie.addEffect(new MobEffectInstance(ModEffects.PANIC.get(), 80));
        helper.assertTrue(zombie.getTarget() == null && !zombie.isAggressive(), "Fear clears the current attack");
        zombie.setTarget(victim);
        helper.assertTrue(zombie.getTarget() == null, "Target goals cannot reacquire while feared");
        float health = victim.getHealth();
        zombie.doHurtTarget(victim);
        helper.assertTrue(victim.getHealth() == health, "A direct melee call cannot bypass fear");
        var arrow = EntityType.ARROW.create(helper.getLevel());
        arrow.setOwner(zombie);
        helper.assertTrue(!victim.hurt(victim.damageSources().arrow(arrow, zombie), 4),
                "Attributed indirect attacks cannot bypass fear");
        helper.assertTrue(victim.getHealth() == health, "Fear also cancels indirect damage");
        zombie.removeEffect(ModEffects.PANIC.get());
        zombie.setTarget(victim);
        helper.assertTrue(zombie.getTarget() == victim, "Targets can be acquired after fear ends");
        zombie.doHurtTarget(victim);
        helper.assertTrue(victim.getHealth() < health, "Normal attacks recover after fear ends");
        zombie.discard(); victim.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void fearExtinguishesAnAlreadyLitCreeper(GameTestHelper helper) {
        Creeper creeper = helper.spawn(EntityType.CREEPER, new BlockPos(1, 1, 1));
        creeper.setNoAi(true);
        creeper.ignite();
        for (int tick = 0; tick < 24; tick++) creeper.tick();
        helper.assertTrue(creeper.getSwelling(1) > 0.5f, "The creeper is already preparing to explode");
        creeper.addEffect(new MobEffectInstance(ModEffects.PANIC.get(), 100));
        for (int tick = 0; tick < 40; tick++) creeper.tick();
        helper.assertTrue(creeper.isAlive(), "Fear prevents the original fuse from exploding");
        helper.assertTrue(!creeper.isIgnited() && creeper.getSwellDir() < 0, "Fear cancels ignition and swelling");
        helper.assertTrue(creeper.getSwelling(1) == 0, "The fuse progress is reset rather than paused");
        creeper.explodeCreeper();
        helper.assertTrue(creeper.isAlive(), "A direct explosion call is also suppressed");
        creeper.removeEffect(ModEffects.PANIC.get());
        creeper.ignite();
        helper.assertTrue(creeper.isIgnited(), "Ignition can be used again after fear ends");
        creeper.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void fearBlocksNewProjectilesAndRestoresTheirSpawning(GameTestHelper helper) {
        Zombie shooter = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        shooter.addEffect(new MobEffectInstance(ModEffects.PANIC.get(), 80));
        var blocked = EntityType.ARROW.create(helper.getLevel());
        blocked.setOwner(shooter);
        blocked.setPos(shooter.position().add(0, 1, 0));
        helper.assertTrue(!helper.getLevel().addFreshEntity(blocked), "Feared mobs cannot create projectiles");
        shooter.removeEffect(ModEffects.PANIC.get());
        var allowed = EntityType.ARROW.create(helper.getLevel());
        allowed.setOwner(shooter);
        allowed.setPos(shooter.position().add(0, 1, 0));
        helper.assertTrue(helper.getLevel().addFreshEntity(allowed), "Projectile spawning recovers after fear");
        allowed.discard(); shooter.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void charmOverridesFearWithoutLeavingAFleeGoal(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        var victim = helper.spawn(EntityType.COW, new BlockPos(2, 1, 1));
        zombie.addEffect(new MobEffectInstance(ModEffects.PANIC.get(), 100));
        zombie.addEffect(new MobEffectInstance(ModEffects.CHARM.get(), 80));
        ModEffects.PANIC.get().applyEffectTick(zombie, 0);
        helper.assertTrue(!PanicEffect.suppressesAttacks(zombie), "Charm retains its attack behavior");
        zombie.setTarget(victim);
        helper.assertTrue(zombie.getTarget() == victim, "Charm can acquire targets");
        helper.assertTrue(zombie.goalSelector.getAvailableGoals().stream()
                .noneMatch(goal -> goal.getGoal() instanceof AvoidEntityGoal), "The fear flee goal is removed while charmed");
        zombie.removeEffect(ModEffects.CHARM.get());
        ModEffects.PANIC.get().applyEffectTick(zombie, 0);
        helper.assertTrue(zombie.getTarget() == null && PanicEffect.suppressesAttacks(zombie), "Remaining fear resumes after charm");
        zombie.removeEffect(ModEffects.PANIC.get());
        zombie.discard(); victim.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "panic_ring")
    public static void ringVisualRemainsUntilTheLastCopyIsRemoved(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ring_visual"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        StatManager.addPeriodicModule(player, ModPeriodicModules.DADS_RING.getId(), 2);
        ModExecutableEffects.DADS_RING.get().apply(new ExecutableEffectContext(player));
        helper.assertTrue(EntityVisualState.hasLayer(player, ModVisualLayers.DADS_RING.getId()), "The active ring has a visual state");
        helper.assertTrue(!EntityVisualState.isFrozen(player), "The aura does not freeze its owner");
        StatManager.addPeriodicModule(player, ModPeriodicModules.DADS_RING.getId(), -1);
        helper.assertTrue(EntityVisualState.hasLayer(player, ModVisualLayers.DADS_RING.getId()), "One remaining copy preserves the ring");
        StatManager.addPeriodicModule(player, ModPeriodicModules.DADS_RING.getId(), -1);
        helper.assertTrue(!EntityVisualState.hasLayer(player, ModVisualLayers.DADS_RING.getId()), "The last removal immediately clears the ring");
        helper.succeed();
    }
}
