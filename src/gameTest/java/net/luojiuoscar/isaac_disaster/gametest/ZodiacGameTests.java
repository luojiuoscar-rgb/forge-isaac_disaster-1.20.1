package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.VirgoShield;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.AquariusPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_zodiac")
@PrefixGameTestTemplate(false)
public final class ZodiacGameTests {
    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void virgoDuplicatesOnlyStackQualityAndOneShieldRoll(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            double quality = f.owner.getAttributeValue(StatManager.PILL_QUALITY.getAttribute());
            ModPassiveAbilities.VIRGO.get().handleObtain(f.owner, null);
            ModPassiveAbilities.VIRGO.get().handleObtain(f.owner, null);
            helper.assertTrue(f.owner.getAttributeValue(StatManager.PILL_QUALITY.getAttribute()) == quality + 2,
                    "Every copy adds exactly one quality");
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(10);
            f.owner.getRandom().setSeed(4096);
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.EVENT, new LivingHurtEvent(f.owner, f.level.damageSources().generic(), 1));
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.ON_HURT);
            helper.assertTrue(f.owner.getEffect(ModEffects.INVINCIBLE.get()).getDuration() == 200,
                    "Two copies roll once and add only 200 ticks");
            ModPassiveAbilities.VIRGO.get().handleRemove(f.owner, null);
            ModPassiveAbilities.VIRGO.get().handleRemove(f.owner, null);
            helper.assertTrue(f.owner.getAttributeValue(StatManager.PILL_QUALITY.getAttribute()) == quality,
                    "Removing both copies restores quality");
            helper.assertTrue(!f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new)
                    .getTriggerModules().contains(ModTriggerModules.VIRGO.getId()), "The trigger is removed symmetrically");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void virgoIgnoresCanceledAndZeroDamageAndCancelsFalsePhdQuality(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            double quality = f.owner.getAttributeValue(StatManager.PILL_QUALITY.getAttribute());
            ModPassiveAbilities.VIRGO.get().handleObtain(f.owner, null);
            ModPassiveAbilities.FALSE_PHD.get().handleObtain(f.owner, null);
            helper.assertTrue(f.owner.getAttributeValue(StatManager.PILL_QUALITY.getAttribute()) == quality,
                    "Virgo and False PHD cancel through the existing quality attribute");
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(100);
            var context = new ExecutableEffectContext(f.owner);
            var canceled = new LivingHurtEvent(f.owner, f.level.damageSources().generic(), 1);
            canceled.setCanceled(true);
            context.set(ContextKeys.EVENT, canceled);
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.ON_HURT);
            context.set(ContextKeys.EVENT, new LivingHurtEvent(f.owner, f.level.damageSources().generic(), 0));
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.ON_HURT);
            helper.assertTrue(!f.owner.hasEffect(ModEffects.INVINCIBLE.get()), "Neither event creates a shield");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void virgoAddsToFiniteShieldAndPreservesInfiniteShield(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), 140, 3));
            helper.assertTrue(new VirgoShield().applyEffect(new ExecutableEffectContext(f.owner)), "A valid shield succeeds");
            helper.assertTrue(f.owner.getEffect(ModEffects.INVINCIBLE.get()).getDuration() == 340, "Shield durations add even when the existing shield has a higher amplifier");
            f.owner.removeEffect(ModEffects.INVINCIBLE.get());
            f.owner.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), -1));
            new VirgoShield().applyEffect(new ExecutableEffectContext(f.owner));
            helper.assertTrue(f.owner.getEffect(ModEffects.INVINCIBLE.get()).isInfiniteDuration(), "Infinite shield remains infinite");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void aquariusSharesTenTickCooldownAndAppliesOnlySuccessfulHitStatuses(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            var calls = new AtomicInteger();
            var effect = f.puddle(f.center, 13.2f, false);
            effect.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, () -> context -> calls.incrementAndGet());
            f.manager.tick(f.level);
            health(helper, target, 186.8f);
            for (int i = 0; i < 9; i++) f.manager.tick(f.level);
            helper.assertTrue(calls.get() == 1, "Standing in water does not repeatedly reapply status between damage pulses");
            f.manager.tick(f.level);
            health(helper, target, 173.6f);
            helper.assertTrue(calls.get() == 2, "The saved status is reused on the next successful pulse");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void canceledWaterDamageDoesNotApplyItsSavedStatus(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            var effect = f.puddle(f.center, 13.2f, false);
            effect.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, ModExecutableEffects.SCORPIO);
            Consumer<LivingAttackEvent> cancel = event -> { if (event.getEntity() == target) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, cancel);
            try { f.manager.tick(f.level); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            health(helper, target, 200);
            helper.assertTrue(!target.hasEffect(ModEffects.DEADLY_POISON.get()), "Canceled damage cannot poison");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void waterSharesStatusPreparationButNeverExplodesBurnsBlocksOrProcsHolyLight(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.IPECAC.get().handleObtain(f.owner, null);
            ModPassiveAbilities.FIRE_MIND.get().handleObtain(f.owner, null);
            ModPassiveAbilities.HOLY_LIGHT.get().handleObtain(f.owner, null);
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(100);
            Cow target = f.cow(f.center);
            var effect = f.puddle(f.center, 13.2f, false);
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, effect);
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
            int strikes = HolyLightRuntime.INSTANCE.manager(f.level).snapshots().size();
            f.manager.tick(f.level);
            health(helper, target, 186.8f);
            var poison = target.getEffect(ModEffects.DEADLY_POISON.get());
            helper.assertTrue(poison != null && poison.getDuration() == 40
                    && poison.getAmplifier() == DeadlyPoisonEffect.amplifierForDamage(10),
                    "Ipecac water uses current full panel damage for poison while base damage remains saved");
            helper.assertTrue(target.hasEffect(ModEffects.BURNING.get()), "Fire Mind water applies pure burning");
            helper.assertTrue(effect.getColor() == 0xFF9933, "Fire Mind's effect tier wins over Ipecac's base tint");
            helper.assertTrue(f.level.isEmptyBlock(BlockPos.containing(f.center))
                    && f.level.getBlockState(BlockPos.containing(f.center).below()).is(Blocks.STONE),
                    "Water does not place fire or destroy support");
            helper.assertTrue(HolyLightRuntime.INSTANCE.manager(f.level).snapshots().size() == strikes,
                    "Water cannot create holy strikes");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void periodicTrailUsesRealMovementAndDuplicatesDoNotGenerateTwice(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.setPos(f.center);
            ModPassiveAbilities.AQUARIUS.get().handleObtain(f.owner, null);
            ModPassiveAbilities.AQUARIUS.get().handleObtain(f.owner, null);
            var periodic = ModPeriodicModules.AQUARIUS.get();
            var context = new ExecutableEffectContext(f.owner);
            periodic.fire(context);
            var runtime = GroundEffectRuntime.INSTANCE.manager(f.level);
            helper.assertTrue(runtime.activeCount() == 0, "Initial stationary sampling creates nothing");
            f.owner.setPos(f.center.add(0.2, 0, 0));
            f.owner.setDeltaMovement(Vec3.ZERO);
            periodic.fire(context);
            helper.assertTrue(runtime.activeCount() == 1, "Actual movement creates one patch even with zero velocity and two copies");
            periodic.fire(context);
            helper.assertTrue(runtime.activeCount() == 1, "Stationary player does not refresh the patch");
            var effect = runtime.activeEffects().get(0);
            helper.assertTrue(Math.abs(effect.damage() - 13.2) < 0.001 && effect.lifetime() == 100
                    && effect.radius() == 0.75, "Damage, lifetime and radius are fixed per patch");
            ModPassiveAbilities.AQUARIUS.get().handleRemove(f.owner, null);
            ModPassiveAbilities.AQUARIUS.get().handleRemove(f.owner, null);
            helper.assertTrue(runtime.activeCount() == 1, "Unequipping preserves an existing patch");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingMovesHorizontallyBeforeDamageAndKeepsAgeAndSeed(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(3, 0, 0));
            var effect = f.puddle(f.center, 1, true);
            long seed = effect.shapeSeed();
            f.manager.tick(f.level);
            f.manager.tick(f.level);
            helper.assertTrue(Math.abs(effect.position().x - f.center.x - 0.5) < 1e-7
                    && effect.position().y == f.center.y + 0.002, "Water moves only horizontally at 0.25 blocks per tick");
            helper.assertTrue(effect.age() == 2 && effect.shapeSeed() == seed && effect.revision() == 1,
                    "Movement retains lifetime and shape and synchronizes after two ticks");
            target.discard();
            Vec3 stopped = effect.position();
            f.manager.tick(f.level);
            helper.assertTrue(effect.position().equals(stopped), "Missing target stops movement");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingFallsOffPlatformByDisappearingInsteadOfProjectingDown(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos floor = BlockPos.containing(f.center).below();
            f.level.setBlockAndUpdate(floor.east(), Blocks.AIR.defaultBlockState());
            f.level.setBlockAndUpdate(floor.east().below(), Blocks.STONE.defaultBlockState());
            f.cow(f.center.add(2, 0, 0));
            var effect = f.puddle(new Vec3(floor.getX() + 0.98, f.center.y, f.center.z), 1, true);
            f.manager.tick(f.level);
            helper.assertTrue(f.manager.activeCount() == 0 && f.manager.drainDespawns().contains(effect.identity()),
                    "Losing the original support removes the patch even when a lower floor exists");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingStopsAtWallsAndIgnoresAirborneTargets(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(3, 0, 0));
            target.setOnGround(false);
            var effect = f.puddle(f.center, 1, true);
            f.manager.tick(f.level);
            helper.assertTrue(effect.position().x == f.center.x, "Airborne targets do not attract water");
            target.setOnGround(true);
            BlockPos wall = BlockPos.containing(f.center).east();
            f.level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            for (int i = 0; i < 2; i++) f.manager.tick(f.level);
            helper.assertTrue(effect.position().x < wall.getX() && f.manager.activeCount() == 1,
                    "A step ending exactly on the wall face must stop before entering it: " + effect.position());
            for (int i = 0; i < 8; i++) f.manager.tick(f.level);
            helper.assertTrue(effect.position().x < wall.getX() && f.manager.activeCount() == 1,
                    "A wall stops the patch without making it climb or vanish: " + effect.position());
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingSearchHasFixedInclusiveEightBlockRadius(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(8.01, 0, 0));
            var effect = f.puddle(f.center, 0, true);
            Vec3 start = effect.position();
            f.manager.tick(f.level);
            helper.assertTrue(effect.position().equals(start), "A target outside eight blocks cannot attract water");
            target.setPos(f.center.add(8, 0, 0));
            for (int i = 0; i < 4; i++) f.manager.tick(f.level);
            helper.assertTrue(effect.position().x > start.x, "The eight-block boundary is included");
            target.setPos(effect.position().add(8.01, -0.002, 0));
            Vec3 stopped = effect.position();
            f.manager.tick(f.level);
            helper.assertTrue(effect.position().equals(stopped), "A cached target outside eight blocks is dropped immediately");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingReusesValidTargetsAndRetriesEmptyQueriesEveryFourTicks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var effect = f.puddle(f.center, 0, true);
            var calls = new AtomicInteger();
            for (int i = 0; i < 8; i++) {
                Vec3 next = effect.nextPosition(f.level, bounds -> { calls.incrementAndGet(); return List.of(); });
                helper.assertTrue(next.equals(effect.position()), "Empty searches leave the water stationary");
            }
            helper.assertTrue(calls.get() == 2, "Eight empty ticks issue only two candidate searches");
            Cow target = f.cow(f.center.add(3, 0, 0));
            Vec3 next = effect.nextPosition(f.level, bounds -> { calls.incrementAndGet(); return List.of(target); });
            helper.assertTrue(next.x > effect.position().x && calls.get() == 3, "The next retry acquires the new target");
            f.manager.relocate(effect, next);
            for (int i = 0; i < 8; i++) {
                f.manager.relocate(effect, effect.nextPosition(f.level, bounds -> {
                    throw new AssertionError("A valid target must not repeat a candidate query");
                }));
            }
            target.setOnGround(false);
            Vec3 stopped = effect.position();
            helper.assertTrue(effect.nextPosition(f.level, bounds -> List.of(target)).equals(stopped),
                    "An airborne cached target is rejected immediately");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void homingSharedCandidatesStillFilterEachOwnerAndRefreshNextTick(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow alliedOwner = f.cow(f.center.add(6, 0, 0));
            Cow ally = f.cow(f.center.add(3, 0, 0));
            Cow other = f.cow(f.center.add(0, 0, 4));
            var team = f.level.getScoreboard().addPlayerTeam("z_" + UUID.randomUUID().toString().substring(0, 8));
            try {
                f.level.getScoreboard().addPlayerToTeam(f.owner.getScoreboardName(), team);
                f.level.getScoreboard().addPlayerToTeam(ally.getScoreboardName(), team);
                var first = f.puddle(f.center, 0, true);
                var second = f.manager.spawn(ModGroundEffects.AQUARIUS.get(), alliedOwner.getUUID(),
                        f.center.add(0, 0.002, 0), AquariusPuddleEffect.RADIUS, 100, 0);
                second.setHoming(true);
                f.manager.tick(f.level);
                helper.assertTrue(first.position().z > f.center.z && first.position().x == f.center.x,
                        "The player-owned patch excludes its ally");
                helper.assertTrue(second.position().x > f.center.x && second.position().z == f.center.z,
                        "Sharing candidates cannot reuse another owner's ally filtering");
                other.discard();
                ally.discard();
                alliedOwner.discard();
                Cow newlySpawned = f.cow(f.center.add(0, 0, -3));
                for (int i = 0; i < 5; i++) f.manager.tick(f.level);
                helper.assertTrue(first.position().z < f.center.z,
                        "A retry sees a newly spawned candidate instead of a stale regional snapshot");
                newlySpawned.discard();
            } finally { f.level.getScoreboard().removePlayerTeam(team); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void removedSupportImmediatelyRemovesHomingWater(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.puddle(f.center, 1, true);
            f.level.setBlockAndUpdate(BlockPos.containing(f.center).below(), Blocks.AIR.defaultBlockState());
            f.manager.tick(f.level);
            helper.assertTrue(f.manager.activeCount() == 0, "Support loss is checked even when no target exists");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void placementUsesSlabAndStairLocalTopAndNeverLoadsMissingChunks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos floor = BlockPos.containing(f.center).below();
            f.level.setBlockAndUpdate(floor, Blocks.STONE_SLAB.defaultBlockState());
            var slab = GroundEffectPlacement.projectToGround(f.level, f.center).orElseThrow(IllegalStateException::new);
            helper.assertTrue(Math.abs(slab.y - (f.center.y - 0.5 + 0.002)) < 1e-7, "Bottom slab uses its actual top");
            f.level.setBlockAndUpdate(floor, Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            var stair = GroundEffectPlacement.projectToGround(f.level,
                    new Vec3(floor.getX() + 0.5, f.center.y, floor.getZ() + 0.75)).orElseThrow(IllegalStateException::new);
            helper.assertTrue(Math.abs(stair.y - (f.center.y - 0.5 + 0.002)) < 1e-7, "Lower stair tread is not snapped to the upper tread");
            Vec3 far = new Vec3(1_000_000, f.center.y, 1_000_000);
            helper.assertTrue(!f.level.hasChunkAt(BlockPos.containing(far)), "Fixture chunk starts unloaded");
            helper.assertTrue(GroundEffectPlacement.projectToGround(f.level, far).isEmpty()
                    && !f.level.hasChunkAt(BlockPos.containing(far)), "Placement does not load the chunk");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "zodiac")
    public static void ownerAndAlliedTargetsAreExcludedAndMissingOwnerCannotDealDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            var scoreboard = f.level.getScoreboard();
            var team = scoreboard.addPlayerTeam("z_" + UUID.randomUUID().toString().substring(0, 8));
            try {
                scoreboard.addPlayerToTeam(f.owner.getScoreboardName(), team);
                scoreboard.addPlayerToTeam(target.getScoreboardName(), team);
                f.owner.setPos(f.center);
                f.puddle(f.center, 13.2f, false);
                f.manager.tick(f.level);
                health(helper, target, 200);
                helper.assertTrue(f.owner.getHealth() == f.owner.getMaxHealth(), "Owner is not damaged");
            } finally { scoreboard.removePlayerTeam(team); }
            f.level.removePlayerImmediately(f.owner, Entity.RemovalReason.DISCARDED);
            for (int i = 0; i < 12; i++) f.manager.tick(f.level);
            health(helper, target, 200);
        }
        helper.succeed();
    }

    private static void health(GameTestHelper helper, Cow target, float expected) {
        helper.assertTrue(Math.abs(target.getHealth() - expected) < 0.002,
                "Expected health " + expected + ", got " + target.getHealth());
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 center;
        final GroundEffectManager manager = new GroundEffectManager();
        final List<Cow> targets = new ArrayList<>();

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            center = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
            for (int x = -1; x <= 5; x++) for (int z = -1; z <= 1; z++) {
                BlockPos floor = BlockPos.containing(center).below().offset(x, 0, z);
                level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
                for (int y = 1; y <= 4; y++) level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
            }
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "z_" + id.toString().substring(0, 8)));
            owner.setPos(center.add(6, 3, 0));
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
            level.addNewPlayer(owner);
        }

        Cow cow(Vec3 at) {
            var target = new Cow(EntityType.COW, level);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setOnGround(true);
            target.setPos(at);
            level.addFreshEntity(target);
            targets.add(target);
            return target;
        }

        AquariusPuddleEffect puddle(Vec3 position, float damage, boolean homing) {
            var effect = manager.spawn(ModGroundEffects.AQUARIUS.get(), owner.getUUID(),
                    position.add(0, 0.002, 0), AquariusPuddleEffect.RADIUS, 100, damage);
            effect.setHoming(homing);
            return effect;
        }

        @Override
        public void close() {
            manager.clear();
            GroundEffectRuntime.INSTANCE.manager(level).clear();
            for (Cow target : targets) target.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
