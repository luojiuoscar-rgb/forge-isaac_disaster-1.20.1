package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Ipecac;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Kamikaze;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("isaac_disaster_explosion_migration")
@PrefixGameTestTemplate(false)
public final class ExplosionMigrationGameTests {
    @GameTest(template = "trajectory_empty", batch = "explosion_migration")
    public static void bombSnapshotsMutableDamageBeforeCachedEffects(GameTestHelper helper) {
        Vec3 center = center(helper);
        Cow owner = cow(helper, center.add(20, 0, 0));
        Cow target = cow(helper, center.add(4, 0, 0));
        IsaacBomb bomb = new IsaacBomb(helper.getLevel(), center.x, center.y, center.z, owner, BombData.SMALL);
        // PrimedTnt's body offset is part of the regular bomb's explosion center.
        target.setPos(center.add(4, bomb.getBbHeight() * 0.0625D, 0));
        bomb.setCenterDamage(81);
        bomb.setDamageRadius(3);
        bomb.setBlockPower(0);
        int[] callbacks = {0};
        bomb.getCachedEffect().add(context -> {
            callbacks[0]++;
            helper.assertTrue(context.getEntity() == owner, "Cached effects retain the bomb owner");
            helper.assertTrue(context.get(ContextKeys.ENTITY).contains(bomb), "Cached effects retain the bomb entity");
            helper.assertTrue(context.get(ContextKeys.SECONDARY_LIVING_ENTITIES).contains(target),
                    "Cached effects receive targets inside the current damage radius");
            // Effects can mutate the live bomb, but this detonation owns an immutable snapshot.
            bomb.setCenterDamage(1000);
            bomb.setDamageRadius(1);
        });
        AttackProbe probe = new AttackProbe(target);
        MinecraftForge.EVENT_BUS.register(probe);
        try {
            assertFullExposure(helper, new Vec3(bomb.getX(), bomb.getY(0.0625D), bomb.getZ()), target);
            bomb.explode();
            helper.assertTrue(callbacks[0] == 1, "The regular bomb invokes cached effects once");
            helper.assertTrue(probe.attacks.size() == 1, "One detonation applies one damage event");
            LivingAttackEvent attack = probe.attacks.get(0);
            close(helper, attack.getAmount(), 18.777779F, "Mutable radius 3 means actual entity radius 6");
            helper.assertTrue(attack.getSource().getDirectEntity() == bomb
                            && attack.getSource().getEntity() == owner,
                    "Bomb damage retains direct bomb and indirect owner attribution");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(probe);
            bomb.discard();
            owner.discard();
            target.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_migration")
    public static void gigaUsesForgeLifecycleAndItsOriginalDamageCurve(GameTestHelper helper) {
        Vec3 center = center(helper);
        Cow owner = cow(helper, center.add(20, 0, 0));
        Cow target = cow(helper, center.add(2, 0, 0));
        GigaBomb bomb = new GigaBomb(helper.getLevel(), center.x, center.y, center.z, owner, 4, 1);
        bomb.setCenterDamage(1);
        bomb.setDamageRadius(100);
        int[] cachedCallbacks = {0};
        IExecutableEffect cachedEffect = context -> cachedCallbacks[0]++;
        CompositeExecutableEffect exposedCache = bomb.getCachedEffect();
        exposedCache.add(cachedEffect);
        AttackProbe probe = new AttackProbe(target);
        MinecraftForge.EVENT_BUS.register(probe);
        try {
            bomb.explode();
            helper.assertTrue(probe.detonations == 1, "Giga participates in the common Forge detonation hook");
            helper.assertTrue(probe.attacks.size() == 1, "Giga applies one damage event");
            close(helper, probe.attacks.get(0).getAmount(), 75, "Giga preserves center 300 and radius equal to power");
            close(helper, (float) target.getDeltaMovement().x, 2.5F, "Giga preserves its horizontal push");
            helper.assertTrue(cachedCallbacks[0] == 0, "Giga does not execute attached bomb item effects");
            helper.assertTrue(bomb.isRemoved(), "Giga discards itself after exploding");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(probe);
            exposedCache.remove(cachedEffect);
            bomb.discard();
            owner.discard();
            target.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_migration")
    public static void kamikazeUsesCenterDamageAndPreservesSelfPayment(GameTestHelper helper) {
        Vec3 center = center(helper);
        Cow owner = cow(helper, center);
        Cow target = cow(helper, center.add(4, 0, 0));
        AttackProbe probe = new AttackProbe(target);
        MinecraftForge.EVENT_BUS.register(probe);
        try {
            assertFullExposure(helper, center, target);
            new Kamikaze().applyEffect(new ExecutableEffectContext(owner));
            helper.assertTrue(probe.attacks.size() == 1, "Kamikaze has one explosion damage pass");
            close(helper, probe.attacks.get(0).getAmount(), 13.75F, "Kamikaze's 35 is center damage with distance falloff");
            close(helper, owner.getHealth(), 1000 - (float) StatManager.MAX_HEALTH.getBonus(),
                    "Kamikaze still pays only its dedicated self damage");
            helper.assertTrue(owner.getDeltaMovement().equals(Vec3.ZERO), "Friendly exclusion also avoids owner knockback");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(probe);
            owner.discard();
            target.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "explosion_migration")
    public static void ipecacKeepsPoisonAfterItsSingleExplosionPass(GameTestHelper helper) {
        Vec3 center = center(helper);
        Cow owner = cow(helper, center.add(20, 0, 0));
        Cow target = cow(helper, center.add(2, 0, 0));
        ExecutableEffectContext context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.TARGET_POSITION, center);
        context.set(ContextKeys.DOUBLE, List.of(0.0));
        AttackProbe probe = new AttackProbe(target);
        MinecraftForge.EVENT_BUS.register(probe);
        try {
            assertFullExposure(helper, center, target);
            new Ipecac().applyEffect(context);
            helper.assertTrue(probe.attacks.size() == 1, "Ipecac has one explosion damage pass");
            close(helper, probe.attacks.get(0).getAmount(), 2.125F, "Ipecac's compensated center 4 uses radius 4");
            helper.assertTrue(target.hasEffect(ModEffects.DEADLY_POISON.get()), "Ipecac still applies deadly poison");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(probe);
            owner.discard();
            target.discard();
        }
    }

    private static Vec3 center(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 0, 1));
        // The GameTest anchor may be at Y=-60, so a relative height can still be underground.
        return new Vec3(anchor.getX() + 0.5D, 220, anchor.getZ() + 0.5D);
    }

    private static Cow cow(GameTestHelper helper, Vec3 pos) {
        Cow cow = new Cow(EntityType.COW, helper.getLevel());
        cow.setPos(pos);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        cow.setHealth(1000);
        helper.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static void assertFullExposure(GameTestHelper helper, Vec3 center, Cow target) {
        float exposure = Explosion.getSeenPercent(center, target);
        close(helper, exposure, 1, "Open-air damage fixture must have full explosion exposure at " + center);
    }

    private static void close(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.001F, message + ": " + actual + " vs " + expected);
    }

    public static final class AttackProbe {
        private final Cow target;
        private final List<LivingAttackEvent> attacks = new ArrayList<>();
        private int detonations;

        private AttackProbe(Cow target) {
            this.target = target;
        }

        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            if (event.getEntity() == target) attacks.add(event);
        }

        @SubscribeEvent
        public void onDetonate(ExplosionEvent.Detonate event) {
            if (event.getAffectedEntities().contains(target)) detonations++;
        }
    }
}
