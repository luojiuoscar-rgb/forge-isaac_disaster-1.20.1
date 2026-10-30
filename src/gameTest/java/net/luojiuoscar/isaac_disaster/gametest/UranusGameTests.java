package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.FrozenEffect;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.UranusAttachment;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.SinusInfectionDamage;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.AquariusPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.system.freeze.state.FrozenImpactState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@GameTestHolder("isaac_disaster_uranus")
@PrefixGameTestTemplate(false)
public final class UranusGameTests {
    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void lethalTearBecomesOneHealthStatueWithoutDeathAndCannotHeal(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            AtomicInteger deaths = new AtomicInteger();
            Consumer<LivingDeathEvent> listener = event -> { if (event.getEntity() == target) deaths.incrementAndGet(); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LivingDeathEvent.class, listener);
            try {
                f.tears.spawn(f.tear(500));
                f.tears.tick(f.level);
                h.assertTrue(target.isAlive() && target.getHealth() == 1, "A real tear converts lethal damage to one health");
                h.assertTrue(target.getEffect(ModEffects.FROZEN.get()).isInfiniteDuration(), "The freeze is infinite");
                h.assertTrue(deaths.get() == 0, "The first conversion is canceled before ordinary death listeners");
                target.heal(100);
                h.assertTrue(target.getHealth() == 1, "Healing cannot raise an ice statue above one health");
                h.assertTrue(FrozenEffect.sourceUuid(target).equals(f.owner.getUUID()), "The original player is saved on the target");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void nonlethalTearSlowsForEightyTicks(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            f.tears.spawn(f.tear(2));
            f.tears.tick(f.level);
            var slow = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            h.assertTrue(slow != null && slow.getAmplifier() == 0 && slow.getDuration() == 80, "Normal tears apply slowness I for 80 ticks");
            h.assertTrue(!target.hasEffect(ModEffects.FROZEN.get()), "Nonlethal hits do not freeze");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void lasersTechnologyTwoAndBrimstoneKeepLethalContext(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            for (AttackType type : List.of(ModAttackTypes.LASER.get(), ModAttackTypes.TECHNOLOGY2.get(), ModAttackTypes.BRIMSTONE.get())) {
                Cow target = f.cow(f.center);
                var attack = f.attack(type, 5000);
                type.performAttack(List.of(attack));
                if (type == ModAttackTypes.BRIMSTONE.get()) ScheduledFuncHelper.tick(f.level.getServer());
                h.assertTrue(target.isAlive() && target.getHealth() == 1 && target.hasEffect(ModEffects.FROZEN.get()),
                        "Lethal context is retained for " + type.getId());
                h.assertTrue(attack.getColorRl().equals(ModBulletColors.URANUS.getId()), "Lasers carry ice blue");
                target.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void batchLaserAndSinusDamageAlsoFreeze(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow first = f.cow(f.center);
            ModAttackTypes.LASER.get().performAttack(List.of(f.attack(ModAttackTypes.LASER.get(), 500),
                    f.attack(ModAttackTypes.LASER.get(), 500)));
            h.assertTrue(first.getHealth() == 1 && first.hasEffect(ModEffects.FROZEN.get()), "Batched laser damage retains its projectile");
            first.discard();
            Cow second = f.cow(f.center);
            var tear = f.tear(500);
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.BULLET, tear);
            context.set(ContextKeys.BULLET_STICK, new BulletStickContext(tear, second, second.getUUID(), second.position(), 20, 200, null));
            h.assertTrue(new SinusInfectionDamage().applyEffect(context), "Sinus tick executes");
            h.assertTrue(second.getHealth() == 1 && second.hasEffect(ModEffects.FROZEN.get()), "Continuous tear damage can freeze");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void restrictedOnlySlowsAndIndependentDamageNeverFreezes(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            ModPassiveAbilities.URANUS.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            target.hurt(f.level.damageSources().playerAttack(f.owner), 2);
            h.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && !target.hasEffect(ModEffects.FROZEN.get()), "Restricted attacks only slow");
            target.invulnerableTime = 0;
            target.hurt(ModDamageSources.water(f.level, f.owner), 500);
            h.assertTrue(target.isDeadOrDying() && !target.hasEffect(ModEffects.FROZEN.get()), "Independent water damage has no projectile death attachment");
            Cow noContext = f.cow(f.center.add(1, 0, 0));
            noContext.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 0), 500);
            h.assertTrue(noContext.isDeadOrDying(), "A tear source without a qualifying projectile cannot freeze");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void bossesAndImmuneMobsAreNotConverted(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            WitherBoss boss = f.add(new WitherBoss(EntityType.WITHER, f.level), f.center);
            boss.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 0, f.tear(500)), 500);
            h.assertTrue(!boss.hasEffect(ModEffects.FROZEN.get()) && boss.isDeadOrDying(), "Bosses are excluded");
            Cow immune = f.add(new Cow(EntityType.COW, f.level) {
                @Override public boolean canBeAffected(MobEffectInstance effect) {
                    return effect.getEffect() != ModEffects.FROZEN.get() && super.canBeAffected(effect);
                }
            }, f.center.add(1, 0, 0));
            immune.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 0, f.tear(500)), 500);
            h.assertTrue(immune.isDeadOrDying() && !immune.hasEffect(ModEffects.FROZEN.get()), "Failed effect application must not cancel death");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void shatterIsDeferredAndUsesDamageAtExecution(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            target.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
            Vec3 center = target.getBoundingBox().getCenter();
            FrozenImpactState.shatter(target);
            h.assertTrue(target.isDeadOrDying() && f.shards().isEmpty(), "No bullets are added inside the damage traversal");
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(13);
            ScheduledFuncHelper.tick(f.level.getServer());
            h.assertTrue(f.shards().isEmpty(), "The death's own END pass cannot emit the shards");
            ScheduledFuncHelper.tick(f.level.getServer());
            h.assertTrue(f.shards().size() == 12, "True death emits exactly twelve shards");
            for (var shard : f.shards()) {
                h.assertTrue(shard.getPosition().equals(center) && Math.abs(shard.getVelocity().y) < 1e-8, "Shards launch horizontally from the saved box center");
                h.assertTrue(shard.getOwner() == f.owner && shard.getDamage() == 13, "Execution-time owner damage is captured");
                h.assertTrue(!shard.isHoming() && !shard.isPiercing() && !shard.isSpectral() && !shard.isControllable(), "Clean shards have no inherited mechanics");
                h.assertTrue(shard.getSplitSequence().isEmpty(), "Clean shards do not split");
            }
            ScheduledFuncHelper.tick(f.level.getServer());
            h.assertTrue(f.shards().size() == 12, "The burst does not repeat");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void deadStatueCanOwnDamagingShardsAndChainFreeze(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow statue = f.cow(f.center);
            statue.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1));
            FrozenImpactState.shatter(statue);
            ScheduledFuncHelper.tick(f.level.getServer());
            ScheduledFuncHelper.tick(f.level.getServer());
            var shards = f.shards();
            h.assertTrue(shards.size() == 12 && shards.get(0).getOwner() == statue, "Missing player falls back to the dead mob");
            Cow target = f.cow(f.center.add(2, 0, 0));
            var shard = shards.get(0);
            target.setHealth(.5F);
            h.assertTrue(target.hurt(ModDamageSources.tear(f.level, statue, shard.getVelocity(), 0, shard), shard.getDamage()), "Dead frozen owners are not blocked from dealing projectile damage");
            h.assertTrue(target.getHealth() == 1 && target.hasEffect(ModEffects.FROZEN.get()), "The clean shard can start another freeze");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void canceledDeathCannotEmitShards(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            target.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
            Consumer<LivingDeathEvent> cancel = event -> {
                if (event.getEntity() == target) { event.setCanceled(true); target.setHealth(1); }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, cancel);
            try {
                FrozenImpactState.shatter(target);
                ScheduledFuncHelper.tick(f.level.getServer());
                ScheduledFuncHelper.tick(f.level.getServer());
                h.assertTrue(target.isAlive() && f.shards().isEmpty(), "A canceled death cannot produce a burst");
                h.assertTrue(f.owner.getUUID().equals(FrozenEffect.sourceUuid(target)), "Canceling death preserves freeze ownership");
            } finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus_real_tick", timeoutTicks = 20)
    public static void burstWaitsUntilNextRealServerTick(GameTestHelper h) {
        Fixture f = new Fixture(h);
        Cow target = f.cow(f.center);
        target.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
        int deathTick = f.level.getServer().getTickCount();
        int[] observed = {-1, -1};
        Consumer<TickEvent.ServerTickEvent> monitor = event -> {
            if (event.phase != TickEvent.Phase.END || event.getServer() != f.level.getServer()) return;
            int age = event.getServer().getTickCount() - deathTick;
            if (age >= 0 && age <= 1) observed[age] = f.shards().size();
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, TickEvent.ServerTickEvent.class, monitor);
        FrozenImpactState.shatter(target);
        h.runAfterDelay(3, () -> {
            try (f) {
                h.assertTrue(observed[0] == 0, "The death tick END has no burst, got " + observed[0]);
                h.assertTrue(observed[1] == 12, "The following tick END has exactly twelve shards, got " + observed[1]);
                h.succeed();
            } finally { MinecraftForge.EVENT_BUS.unregister(monitor); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void poisonCannotFreezeAndPotionContactStillWorksOnFrozenMobs(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            ModPassiveAbilities.URANUS.get().handleObtain(f.owner, null);
            Cow poisoned = f.cow(f.center.add(1, 0, 0));
            poisoned.setHealth(.5F);
            ModEffects.DEADLY_POISON.get().applyEffectTick(poisoned, 0);
            h.assertTrue(poisoned.isDeadOrDying() && !poisoned.hasEffect(ModEffects.FROZEN.get()), "Owning Uranus does not grant independent poison lethal freezing");
            Cow statue = f.cow(f.center);
            statue.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
            var position = GroundEffectPlacement.projectToGround(f.level, statue.position()).orElseThrow();
            var potion = f.ground.spawn(ModGroundEffects.POTION.get(), f.owner.getUUID(), position, .75, 80, 0);
            potion.setPotions(List.of(new PotionProfile(MobEffects.REGENERATION, 60, 0)));
            f.ground.tick(f.level);
            h.assertTrue(statue.hasEffect(MobEffects.REGENERATION), "Freezing only prevents ground damage, not independent potion contacts");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void directFreezeKeepsItsOwnerIndependentOfTriggerSourceId(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            var attack = f.attack(ModAttackTypes.BULLET.get(), 500).toBuilder()
                    .trigger(new CompositeTrigger(List.of(new SimpleTrigger(ModTriggerTypes.KILL_ENTITY,
                            ModExecutableEffects.URANUS_FREEZE_ON_KILL, null, ModTriggerModules.FIRE_MIND.getId()))))
                    .build();
            var bullet = BulletState.from(attack).build();
            target.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 0, bullet), bullet.getDamage());
            h.assertTrue(target.hasEffect(ModEffects.FROZEN.get()) && target.getHealth() == 1
                    && f.owner.getUUID().equals(FrozenEffect.sourceUuid(target)),
                    "Direct freezing records the actual projectile owner despite a different trigger source ID");
            h.assertTrue(f.water().size() == 1 && f.owner.getUUID().equals(f.water().get(0).ownerUuid()),
                    "Even the first puddle is attributed to the freezing player");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void invalidSourceDoesNotDamageThroughOldWaterAndNewWaterBecomesUnowned(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow statue = f.cow(f.center);
            statue.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
            Cow victim = f.cow(f.center.add(.1, 0, 0));
            f.owner.setHealth(0);
            f.ground.tick(f.level);
            h.assertTrue(victim.getHealth() == 200, "Old sourced water requires a living valid source");
            statue.tickCount = 2;
            FrozenEffect.tickFrozen(statue);
            h.assertTrue(f.water().stream().anyMatch(w -> w.ownerUuid() == null && w.damage() == 1), "Later water falls back to an unowned one-damage snapshot");
            f.ground.tick(f.level);
            h.assertTrue(victim.getHealth() == 199, "Unowned replacement water damages all unfrozen living targets");
            f.owner.setHealth(20);
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void sourcedAndSourcelessWaterSnapshotDamageAndExpire(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow statue = f.cow(f.center);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
            statue.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1), f.owner);
            h.assertTrue(f.water().size() == 1 && f.water().get(0).damage() == 7, "The first freeze immediately creates a sourced snapshot");
            var water = f.water().get(0);
            h.assertTrue(water.lifetime() == 80 && water.radius() == .75 && water.ownerUuid().equals(f.owner.getUUID()), "Water has the approved dimensions and ownership");
            for (int i = 0; i < 90; i++) { statue.tickCount = i * 2; FrozenEffect.tickFrozen(statue); f.ground.tick(f.level); }
            h.assertTrue(!f.water().isEmpty(), "Stationary freezing keeps refreshing water beyond eighty ticks");
            statue.removeEffect(ModEffects.FROZEN.get());
            for (int i = 0; i < 80; i++) f.ground.tick(f.level);
            h.assertTrue(f.water().isEmpty(), "Water expires after refreshing stops");
            statue.setHealth(200);
            statue.invulnerableTime = 0;
            statue.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1));
            h.assertTrue(f.water().size() == 1 && f.water().get(0).ownerUuid() == null && f.water().get(0).damage() == 1,
                    "Without a player, a cow's missing attack attribute falls back to one damage");
            Cow victim = f.cow(f.center.add(.1, 0, 0));
            f.ground.tick(f.level);
            h.assertTrue(victim.getHealth() == 199, "Ownerless water damages nearby living entities");
            h.assertTrue(statue.getHealth() == 200, "All water skips frozen mobs");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void aquariusOnlyInheritsSlowAndDropsFrozenCachedTarget(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            Cow target = f.cow(f.center);
            Vec3 position = GroundEffectPlacement.projectToGround(f.level, f.center).orElseThrow();
            var puddle = f.ground.spawn(ModGroundEffects.AQUARIUS.get(), f.owner.getUUID(), position, .75, 100, 2);
            var prepare = new ExecutableEffectContext(f.owner);
            prepare.set(ContextKeys.EFFECT_ATTACHMENT_NODE, puddle);
            new UranusAttachment().apply(prepare);
            f.ground.tick(f.level);
            h.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && !target.hasEffect(ModEffects.FROZEN.get()), "Puddles only inherit slowness");
            h.assertTrue(puddle.getColor() == 0xA7E8FF, "The shared color pipeline gets ice blue");
            target.invulnerableTime = 0;
            target.setHealth(1);
            for (int i = 0; i < 10; i++) f.ground.tick(f.level);
            h.assertTrue(target.isDeadOrDying(), "Lethal Aquarius water cannot freeze");
            Zombie tracked = f.add(new Zombie(EntityType.ZOMBIE, f.level), f.center.add(2, 0, 0));
            tracked.setOnGround(true);
            var moving = f.ground.spawn(ModGroundEffects.AQUARIUS.get(), f.owner.getUUID(), position, .75, 100, 0);
            moving.setHoming(true);
            var first = moving.nextPosition(f.level, box -> List.of(tracked));
            h.assertTrue(first.x > position.x, "A target is acquired");
            tracked.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1));
            h.assertTrue(moving.nextPosition(f.level, box -> List.of(tracked)).equals(position), "A cached target is dropped immediately when frozen");
        }
        h.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "uranus")
    public static void lifecycleAndEntityNbtPreserveSourceAndOneHealthFlag(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            var ability = ModPassiveAbilities.URANUS.get();
            ability.handleObtain(f.owner, null);
            ability.handleObtain(f.owner, null);
            var modules = f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new).getTriggerModules();
            h.assertTrue(modules.get(ModTriggerModules.URANUS.getId()).stacks() == 2, "Two copies share one counted module");
            var fired = f.tear(500);
            ability.handleRemove(f.owner, null);
            ability.handleRemove(f.owner, null);
            h.assertTrue(!modules.contains(ModTriggerModules.URANUS.getId()), "The last removal clears its module");
            Cow target = f.cow(f.center);
            target.hurt(ModDamageSources.tear(f.level, f.owner, Vec3.ZERO, 0, fired), 500);
            h.assertTrue(target.hasEffect(ModEffects.FROZEN.get()), "An existing projectile retains its attachment after unequip");
            CompoundTag saved = target.saveWithoutId(new CompoundTag());
            Cow loaded = new Cow(EntityType.COW, f.level);
            loaded.load(saved);
            h.assertTrue(FrozenEffect.isIceStatue(loaded) && f.owner.getUUID().equals(FrozenEffect.sourceUuid(loaded))
                    && loaded.hasEffect(ModEffects.FROZEN.get()), "Entity NBT preserves the flag, source and infinite effect");
            target.removeEffect(ModEffects.FROZEN.get());
            FrozenEffect.tickFrozen(target);
            h.assertTrue(!FrozenEffect.isIceStatue(target) && FrozenEffect.sourceUuid(target) == null, "Removing the effect clears its lifecycle data");
            h.assertTrue(ModBulletVisuals.URANUS_ICE_TEAR.get().getPriority() == 3
                    && !ModBulletVisuals.URANUS_ICE_TEAR.get().acceptsTint(), "Ice material wins without accepting tint");
        }
        h.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 center;
        final BulletManager tears = new BulletManager();
        final GroundEffectManager ground;
        final List<Mob> targets = new ArrayList<>();
        final Set<Long> previousWater;
        final Set<BulletState> previousBullets;

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            center = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
            for (int x = -2; x <= 3; x++) for (int z = -2; z <= 2; z++) {
                var floor = BlockPos.containing(center).offset(x, -1, z);
                level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 5; y++) level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
            }
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "ice_" + id.toString().substring(0, 8)));
            owner.setPos(center.add(-1.5, 0, 0));
            level.addNewPlayer(owner);
            ground = GroundEffectRuntime.INSTANCE.manager(level);
            previousWater = ground.activeEffects().stream().map(AbstractGroundEffect::identity).collect(Collectors.toSet());
            previousBullets = Set.copyOf(BulletRuntime.INSTANCE.manager(level).activeStates());
        }

        <T extends Mob> T add(T mob, Vec3 position) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
            mob.setHealth(200);
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.setPos(position);
            level.addFreshEntity(mob);
            targets.add(mob);
            return mob;
        }

        Cow cow(Vec3 pos) { return add(new Cow(EntityType.COW, level), pos); }

        AttackContext attack(AttackType type, double damage) {
            var attack = AttackContext.builder(owner, owner).attackType(type).position(center.add(-1.5, .8, 0))
                    .mainAxis(new Vec3(1, 0, 0)).damage(damage).bulletScaleModifier(0.0).bulletKnockback(0.2)
                    .range(5).speed(2).color(ModBulletColors.BASE.getId()).useFixedLaunchTransform().build();
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, attack);
            new UranusAttachment().apply(context);
            return attack;
        }

        BulletState tear(double damage) {
            return BulletState.from(attack(ModAttackTypes.BULLET.get(), damage)).lifetime(10)
                    .collisionWidth(.2).collisionHeight(.2).build();
        }

        List<AbstractGroundEffect> water() {
            return ground.activeEffects().stream().filter(w -> !previousWater.contains(w.identity())).toList();
        }

        List<BulletState> shards() {
            return BulletRuntime.INSTANCE.manager(level).activeStates().stream().filter(b -> !previousBullets.contains(b)
                    && (b.getOwner() == owner || targets.contains(b.getOwner()))).toList();
        }

        @Override public void close() {
            ScheduledFuncHelper.clearPlayerTasks(owner.getUUID());
            water().forEach(ground::remove);
            shards().forEach(BulletRuntime.INSTANCE.manager(level)::remove);
            tears.clear();
            targets.forEach(Entity::discard);
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
