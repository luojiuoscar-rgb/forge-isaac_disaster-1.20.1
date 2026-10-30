package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.ModAttackPatterns;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeContactGameTests {
    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 20)
    public static void aReturningKnifeSplitsOncePerThrowAndResetsOnlyWhenRelaunched(GameTestHelper helper) {
        var f = fixture(helper);
        f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AttackContext source = shotContext(f);
        source.addSplitModule(ModSplitModules.COMPOUND_FRACTURE.getId(), 1);
        AtomicInteger bursts = new AtomicInteger();
        Consumer<AttackFiredEvent> fire = event -> {
            if (event.getOwner() == f.owner && event.getOrigin() == AttackOrigin.SPLIT_CHILD) {
                bursts.incrementAndGet();
                cleanupBullets(f);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(fire);
        f.main.launch(0.2, 0.1, source);
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(10, () -> {
            try {
                helper.assertTrue(bursts.get() == 1 && !f.main.isInFlight()
                                && f.main.getSplitTriggerCounts().getEntityHits() == 1,
                        "Outward motion, return and idle share a single split opportunity");
                helper.assertTrue(f.main.launch(0.2, 0.1, source)
                                && f.main.getSplitTriggerCounts().getEntityHits() == 0,
                        "A fresh throw resets its runtime without mutating the prepared source");
            } catch (RuntimeException | Error exception) {
                MinecraftForge.EVENT_BUS.unregister(fire);
                cleanupBullets(f);
                f.close();
                throw exception;
            }
        });
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(bursts.get() == 2, "The next throw can split once on the same enemy again");
                helper.succeed();
            } finally { MinecraftForge.EVENT_BUS.unregister(fire); cleanupBullets(f); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 35)
    public static void theGenericSingleShotEffectNeedsNoHeldAttackAndCompletesItsFullRange(GameTestHelper helper) {
        var f = fixture(helper);
        AttackContext source = shotContext(f).toBuilder().range(2.5).speed(1).build();
        f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        f.ability.addAttackType(ModAttackTypes.MOMS_KNIFE.getId(), -1, f.owner);
        var context = new ExecutableEffectContext(f.owner);
        context.set(ContextKeys.ATTACK_CONTEXT, source);
        helper.assertTrue(((IAbilityEffect) ModExecutableEffects.MOMS_KNIFE_SHOT.get()).applyEffect(context),
                "The same generic effect spawns a one-way shot without passive or holding eligibility");
        var shots = shots(f);
        helper.assertTrue(shots.size() == 1, "Exactly one independent shot is created");
        var shot = shots.get(0);
        long launchedAt = f.level.getGameTime();
        helper.onEachTick(() -> {
            if (!f.closed && !shot.isRemoved() && shot.getTrajectoryAge() < f.level.getGameTime() - launchedAt) shot.tick();
        });
        helper.runAfterDelay(28, () -> {
            try {
                helper.assertTrue(shot.isRemoved() && Math.abs(shot.getTraveled() - 24) < 1e-6
                                && Math.abs(shot.position().distanceTo(source.getPos()) - 24) < 1e-6,
                        "The one-way knife finishes its range without any held attack: traveled=" + shot.getTraveled());
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void previewDoesNotRollCountEventsAndFiringIncludesTheCopiesBeforeItsOneEvent(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new);
            for (int i = 0; i < 2; i++) items.addItem(f.owner, new ItemStack(ModPassiveItems.MOMS_KNIFE.get()));
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(3);
            AtomicInteger events = new AtomicInteger();
            Consumer<IsaacGetBulletCountEvent> count = event -> {
                if (event.getPlayer() == f.owner) {
                    events.incrementAndGet();
                    helper.assertTrue(event.getCount() == 4, "The quantity event already includes the extra knife copy");
                    event.setCount(5);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(count);
            try {
                helper.assertTrue(HeldMomsKnifeGroup.heldCount(f.owner) == 4 && events.get() == 0,
                        "Held preview reads the stable count without quantity events");
                helper.assertTrue(ModAttackTypes.MOMS_KNIFE.get().getBulletCount(f.owner) == 5 && events.get() == 1,
                        "Actual firing publishes exactly one quantity event");
            } finally { MinecraftForge.EVENT_BUS.unregister(count); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void duplicateItemsAddToTheFrontVolleyWithoutChangingOtherAttackCounts(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new);
            var attack = ModAttackTypes.MOMS_KNIFE.get();
            for (int count : new int[]{1, 2, 4, 16, 20}) {
                items.addItem(f.owner, new ItemStack(ModPassiveItems.MOMS_KNIFE.get()));
                f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(count);
                int copies = items.getItemCountFromAll(ModPassiveItems.MOMS_KNIFE.getId());
                int expected = Math.min(16, count + copies - 1);
                helper.assertTrue(HeldMomsKnifeGroup.heldCount(f.owner) == expected,
                        "Every extra copy adds one to the stable count");
                helper.assertTrue(attack.getBulletCount(f.owner) == expected, "Preview and firing counts agree");
                helper.assertTrue(ModAttackTypes.BULLET.get().getBulletCount(f.owner) == Math.min(count, 17),
                        "Duplicate knives do not alter other attack counts");
                HeldMomsKnifeGroup.refresh(f.owner);
                helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == expected, "The actual held group follows the count");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void abilityExtraPreparesTwelveIndependentKnivesAndPublishesOneFiredEvent(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            AtomicInteger prepared = new AtomicInteger();
            List<AttackFiredEvent> fired = new ArrayList<>();
            Consumer<AttackContextPrepareEvent> prepare = event -> {
                if (event.getRequest().getOwner() == f.owner) prepared.incrementAndGet();
            };
            Consumer<AttackFiredEvent> fire = event -> { if (event.getOwner() == f.owner) fired.add(event); };
            MinecraftForge.EVENT_BUS.addListener(prepare);
            MinecraftForge.EVENT_BUS.addListener(fire);
            try {
                AttackExecutor.perform(extraRequest(f, 12));
                var shots = shots(f);
                helper.assertTrue(shots.size() == 12 && prepared.get() == 12,
                        "All twelve contexts are prepared and emitted: shots=" + shots.size() + ", prepared=" + prepared);
                helper.assertTrue(fired.size() == 1 && fired.get(0).getPreparedContexts().size() == 12
                                && fired.get(0).getOrigin() == AttackOrigin.ABILITY_EXTRA
                                && fired.get(0).getSourceId().equals(ModExecutableEffects.TAMMYS_HEAD.getId()),
                        "One batch reports its true initiating effect");
                helper.assertTrue(!f.main.isInFlight(), "An extra attack does not launch held knives");
                for (var shot : shots) helper.assertTrue(Math.abs(shot.getVelocity().length() - 1) < 1e-6,
                        "The provided shot speed is preserved");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(prepare);
                MinecraftForge.EVENT_BUS.unregister(fire);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void rejectedIndependentSpawnsPublishNoFiredEvent(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            AtomicInteger attempts = new AtomicInteger(), fired = new AtomicInteger();
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                        && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) {
                    attempts.incrementAndGet();
                    event.setCanceled(true);
                }
            };
            Consumer<AttackFiredEvent> fire = event -> { if (event.getOwner() == f.owner) fired.incrementAndGet(); };
            MinecraftForge.EVENT_BUS.addListener(reject);
            MinecraftForge.EVENT_BUS.addListener(fire);
            try {
                AttackExecutor.perform(extraRequest(f, 12));
                helper.assertTrue(attempts.get() == 12 && fired.get() == 0 && shots(f).isEmpty(),
                        "Failed batches attempt every context without inventing a fired event");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(reject);
                MinecraftForge.EVENT_BUS.unregister(fire);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void realPreparationRetainsTheNormalBoneSplitModule(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.COMPOUND_FRACTURE.get().handleObtain(f.owner, null);
            ModPassiveAbilities.IPECAC.get().handleObtain(f.owner, null);
            AttackExecutor.perform(extraRequest(f, 1));
            var shots = shots(f);
            helper.assertTrue(shots.size() == 1, "The real preparation pipeline emits its explicit count");
            var knife = shots.get(0);
            helper.assertTrue(knife.getTriggers().contains(ModExecutableEffects.IPECAC.get()),
                    "Ipecac keeps its normal hit trigger");
            helper.assertTrue(knife.getSplitSequence().contains(ModSplitModules.COMPOUND_FRACTURE.getId()),
                    "The knife retains the prepared Compound Fracture split module");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void everyOneWayKnifeSplitsOnlyOnItsFirstEnemyThroughTheNormalPipeline(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            Vec3 origin = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
            Cow near = f.cow(origin.add(0, 0, 1)), far = f.cow(origin.add(0, 0, 3));
            AttackContext source = shotContext(f);
            source.addSplitModule(ModSplitModules.COMPOUND_FRACTURE.getId(), 1);
            AtomicInteger splits = new AtomicInteger(), bursts = new AtomicInteger();
            Consumer<BulletSplitEvent> split = event -> {
                if (event.getParent() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner) {
                    splits.incrementAndGet();
                    helper.assertTrue(event.getReferenceContext().getPos().distanceTo(near.getBoundingBox().getCenter()) < 1e-6,
                            "The split uses the first enemy's contact position");
                }
            };
            Consumer<AttackFiredEvent> fire = event -> {
                if (event.getOwner() == f.owner && event.getOrigin() == AttackOrigin.SPLIT_CHILD) bursts.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(split);
            MinecraftForge.EVENT_BUS.addListener(fire);
            try {
                for (int i = 0; i < 2; i++) {
                    var knife = f.oneWay(10);
                    helper.assertTrue(knife.initializeOneWay(f.owner, origin, origin.add(0, 0, 1), 10, source),
                            "The prepared single-use knife initializes");
                    knife.tick();
                    helper.assertTrue(knife.getDamagedEntities().size() == 2 && knife.getSplitTriggerCounts().getEntityHits() == 1,
                            "Both enemies take contact damage but only the first triggers a split");
                }
                helper.assertTrue(splits.get() == 2 && bursts.get() == 2, "Each knife independently runs the real split pipeline once");
                helper.assertTrue(near.getHealth() == 996 && far.getHealth() == 996, "Splitting does not suppress later knife damage");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(split);
                MinecraftForge.EVENT_BUS.unregister(fire);
                cleanupBullets(f);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void cancellingTheFirstSplitDoesNotRetryAtTheNextEnemy(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            Vec3 origin = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
            f.cow(origin.add(0, 0, 1));
            f.cow(origin.add(0, 0, 3));
            AttackContext source = shotContext(f);
            source.addSplitModule(ModSplitModules.COMPOUND_FRACTURE.getId(), 1);
            var knife = f.oneWay(10);
            knife.initializeOneWay(f.owner, origin, origin.add(0, 0, 1), 10, source);
            AtomicInteger splits = new AtomicInteger();
            Consumer<BulletSplitEvent> cancel = event -> {
                if (event.getParent() == knife) { splits.incrementAndGet(); event.setCanceled(true); }
            };
            MinecraftForge.EVENT_BUS.addListener(cancel);
            try {
                knife.tick();
                helper.assertTrue(splits.get() == 1 && knife.getSplitTriggerCounts().getEntityHits() == 1,
                        "Cancellation consumes the one split opportunity for this knife");
                helper.assertTrue(BulletRuntime.INSTANCE.manager(f.level).activeStates().stream().noneMatch(b -> b.getOwner() == f.owner),
                        "A cancelled split emits no children");
            } finally { MinecraftForge.EVENT_BUS.unregister(cancel); cleanupBullets(f); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void twoFastOneWayKnivesRememberTargetsIndependentlyAndStackTheirDamage(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            Vec3 origin = PlayerMomsKnifeMotion.idlePosition(f.owner, 1);
            Cow near = f.cow(origin.add(0, 0, 1)), far = f.cow(origin.add(0, 0, 3));
            var first = f.oneWay(25);
            var second = f.oneWay(25);
            AtomicInteger events = new AtomicInteger();
            Consumer<IsaacAttackAfterHitEvent> hit = event -> {
                if (event.getBulletObject() == first || event.getBulletObject() == second) events.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(hit);
            try {
                first.tick();
                second.tick();
                helper.assertTrue(first.isRemoved() && second.isRemoved(), "The final high-speed step is still processed");
                helper.assertTrue(events.get() == 4 && first.getDamagedEntities().size() == 2
                                && second.getDamagedEntities().size() == 2, "Each knife reports each swept enemy independently");
                helper.assertTrue(near.getHealth() == 996 && far.getHealth() == 996, "Both knives deal full damage in the same tick");
            } finally { MinecraftForge.EVENT_BUS.unregister(hit); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 50)
    public static void tammysTwoRoundsKeepTheirTimingAndCompleteFlightAfterThePassiveIsRemoved(GameTestHelper helper) {
        var f = fixture(helper);
        List<PlayerMomsKnifeEntity> emitted = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        List<AttackFiredEvent> fired = new ArrayList<>();
        long start = f.level.getGameTime();
        Vec3 center = f.owner.getEyePosition().add(0, -0.15 * f.owner.getBbHeight(), 0);
        Consumer<EntityJoinLevelEvent> spawn = event -> {
            if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                    && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) {
                emitted.add(knife);
                times.add(f.level.getGameTime());
                helper.assertTrue(knife.position().distanceTo(center) < 1e-6, "Tammy uses its own center rather than a rear scatter");
            }
        };
        Consumer<AttackFiredEvent> fire = event -> { if (event.getOwner() == f.owner) fired.add(event); };
        MinecraftForge.EVENT_BUS.addListener(spawn);
        MinecraftForge.EVENT_BUS.addListener(fire);
        f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        var context = new ExecutableEffectContext(f.owner);
        context.set(ContextKeys.AMPLIFIER, 2.0);
        ModExecutableEffects.TAMMYS_HEAD.get().apply(context);
        helper.runAfterDelay(1, () -> helper.assertTrue(emitted.size() == 12,
                "The first round fires at the first scheduler pass: start=" + start + ", times=" + times));
        helper.runAfterDelay(4, () -> helper.assertTrue(emitted.size() == 12,
                "Only the later rounds wait for their five-tick interval"));
        helper.onEachTick(() -> {
            for (int i = 0; i < emitted.size(); i++) {
                var knife = emitted.get(i);
                if (!knife.isRemoved() && knife.getTrajectoryAge() < f.level.getGameTime() - times.get(i)) knife.tick();
            }
        });
        helper.runAfterDelay(12, () -> f.ability.addAttackType(ModAttackTypes.MOMS_KNIFE.getId(), -1, f.owner));
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(emitted.size() == 24 && fired.size() == 2,
                        "Two rounds each emit twelve shots and one event: shots=" + emitted.size() + ", fired=" + fired.size());
                helper.assertTrue(times.subList(0, 12).stream().allMatch(t -> t == start)
                                && times.subList(12, 24).stream().allMatch(t -> t == start + 5),
                        "The five-tick schedule is preserved: start=" + start + ", times=" + times);
                for (var knife : emitted) helper.assertTrue(knife.isRemoved() && Math.abs(knife.getTraveled() - 24) < 1e-5,
                        "Each independent knife completes all 24 blocks after switching and removing the passive");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(spawn);
                MinecraftForge.EVENT_BUS.unregister(fire);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 20)
    public static void firstContactEventsAreOncePerThrowButDamageContinues(GameTestHelper helper) {
        var f = fixture(helper);
        Cow target = f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AtomicInteger before = new AtomicInteger(), after = new AtomicInteger();
        Consumer<IsaacAttackBeforeHitEntityEvent> pre = event -> {
            if (event.getBulletObject() == f.main) { before.incrementAndGet(); event.setDamage(4); }
        };
        Consumer<IsaacAttackAfterHitEvent> post = event -> {
            if (event.getBulletObject() == f.main) after.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(pre);
        MinecraftForge.EVENT_BUS.addListener(post);
        helper.assertTrue(f.main.launch(0.2, 0.1, shotContext(f)), "The first throw launches");
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(10, () -> {
            try {
                helper.assertTrue(before.get() == 1 && after.get() == 1,
                        "Outward, return and idle do not repeat events: before=" + before + ", after=" + after);
                helper.assertTrue(target.getHealth() < 994, "Later ticks continue applying ordinary knife damage");
                helper.assertTrue(f.main.getDamagedEntities().contains(target.getUUID()), "The whole throw remembers its contact");
                helper.assertTrue(f.main.launch(0.2, 0.1, shotContext(f)), "The next throw launches");
                helper.assertTrue(!f.main.getDamagedEntities().contains(target.getUUID()), "A new throw resets first contact memory");
            } catch (RuntimeException | Error exception) {
                MinecraftForge.EVENT_BUS.unregister(pre);
                MinecraftForge.EVENT_BUS.unregister(post);
                f.close();
                throw exception;
            }
        });
        helper.runAfterDelay(12, () -> {
            try {
                helper.assertTrue(before.get() == 2 && after.get() == 2, "The next throw may trigger again on the same enemy");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(pre);
                MinecraftForge.EVENT_BUS.unregister(post);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 10)
    public static void cancelledFirstContactDoesNotRetryOrSuppressLaterDamage(GameTestHelper helper) {
        var f = fixture(helper);
        Cow target = f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AtomicInteger before = new AtomicInteger(), after = new AtomicInteger();
        Consumer<IsaacAttackBeforeHitEntityEvent> pre = event -> {
            if (event.getBulletObject() == f.main) { before.incrementAndGet(); event.setCanceled(true); }
        };
        Consumer<IsaacAttackAfterHitEvent> post = event -> {
            if (event.getBulletObject() == f.main) after.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(pre);
        MinecraftForge.EVENT_BUS.addListener(post);
        f.main.launch(0.2, 0.01, shotContext(f));
        f.main.tick();
        helper.assertTrue(target.getHealth() == 1000, "Cancellation prevents this tick's hit");
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(before.get() == 1 && after.get() == 0 && target.getHealth() < 1000,
                        "Later ticks damage without retrying either first-contact event");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(pre);
                MinecraftForge.EVENT_BUS.unregister(post);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 10)
    public static void boneTearsUseParentRuntimeValuesAndOnlySplitOnce(GameTestHelper helper) {
        var f = fixture(helper);
        Cow target = f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AttackContext attack = shotContext(f).toBuilder().speed(0.8).range(11).build();
        attach(f, attack, false);
        List<AttackFiredEvent> bones = new ArrayList<>();
        Consumer<AttackFiredEvent> fire = event -> {
            if (event.getOwner() == f.owner && event.getOrigin() == AttackOrigin.SPLIT_CHILD)
                bones.add(event);
        };
        Consumer<IsaacAttackBeforeHitEntityEvent> damage = event -> {
            if (event.getBulletObject() == f.main) event.setDamage(8);
        };
        MinecraftForge.EVENT_BUS.addListener(fire);
        MinecraftForge.EVENT_BUS.addListener(damage);
        f.main.launch(2, 0.01, attack);
        f.main.tick();
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(bones.size() == 1, "Continued contact produces one bone burst: " + bones.size());
                var children = bones.get(0).getPreparedContexts();
                helper.assertTrue(children.size() >= 1 && children.size() <= 3, "One to three bone tears are produced");
                for (var child : children) {
                    helper.assertTrue(child.getDamage() == 1 && child.getBulletRange() == 2
                                    && Math.abs(child.getBulletSpeed() - 0.8) < 1e-9,
                            "The normal split pipeline reads parent damage/range and prepared shot speed");
                    helper.assertTrue(child.getMainAxis().y == 0 && Math.abs(child.getMainAxis().length() - 1) < 1e-6,
                            "The original horizontal bone pattern is reused");
                    helper.assertTrue(child.getVisualIds().contains(ModBulletVisuals.COMPOUND_FRACTURE_BONE_TEAR.getId())
                                    && child.getSplitSequence().isEmpty(),
                            "Bone visuals survive while normal inheritance excludes recursive Compound Fracture");
                }
                helper.assertTrue(f.main.getDamagedEntities().contains(target.getUUID()), "The parent owns its own contact record");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(fire);
                MinecraftForge.EVENT_BUS.unregister(damage);
                cleanupBullets(f);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 10)
    public static void cricketsBodyAndEnhancedBoneTearsBothSurviveTheFullPreparationFlow(GameTestHelper helper) {
        var f = fixture(helper);
        var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new);
        ModPassiveAbilities.CRICKETS_BODY.get().handleObtain(f.owner, null);
        ModPassiveAbilities.COMPOUND_FRACTURE.get().handleObtain(f.owner, null);
        items.addItem(f.owner, new ItemStack(ModPassiveItems.CRICKETS_BODY.get()));
        f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AttackContext attack = shotContext(f);
        prepare(f, attack);
        helper.assertTrue(attack.getSplitSequence().contains(ModSplitModules.COMPOUND_FRACTURE_ENHANCED.getId()),
                "The enhancement is selected during preparation");
        items.removeFromIndex(f.owner, 0);
        AttackContext next = shotContext(f);
        prepare(f, next);
        helper.assertTrue(next.getSplitSequence().contains(ModSplitModules.COMPOUND_FRACTURE.getId())
                        && !next.getSplitSequence().contains(ModSplitModules.COMPOUND_FRACTURE_ENHANCED.getId()),
                "Removing Cricket's Body affects the next throw rather than the already prepared one");
        List<AttackFiredEvent> bursts = new ArrayList<>();
        Consumer<AttackFiredEvent> fire = event -> {
            if (event.getOwner() == f.owner
                    && event.getOrigin() == AttackOrigin.SPLIT_CHILD) {
                bursts.add(event);
                cleanupBullets(f);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(fire);
        f.main.launch(0.2, 0.01, attack);
        f.main.tick();
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(bursts.size() == 1, "Continuous contact still triggers one prepared burst");
                var children = bursts.get(0).getPreparedContexts();
                long bones = children.stream().filter(child -> child.getVisualIds()
                        .contains(ModBulletVisuals.COMPOUND_FRACTURE_BONE_TEAR.getId())).count();
                helper.assertTrue(children.size() == 12 && bones == 8,
                        "The real preparation chain emits eight bone tears and four ordinary tears: " + children.size());
                for (var child : children) helper.assertTrue(child.getDamage() == 1 && child.getSplitSequence().isEmpty(),
                        "Enhanced children keep half damage without Cricket's Body or bone recursion");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(fire);
                cleanupBullets(f);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact")
    public static void cricketsBodyAloneSplitsAKnifeIntoFourHalfDamageTears(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.CRICKETS_BODY.get().handleObtain(f.owner, null);
            f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 1));
            AttackContext attack = shotContext(f);
            prepare(f, attack);
            var preparation = new ExecutableEffectContext(f.owner);
            preparation.set(ContextKeys.ATTACK_CONTEXT, attack);
            helper.assertTrue(attack.getSplitSequence().contains(ModSplitModules.CRICKETS_BODY.getId()),
                    "Preparation consistently attaches Cricket's Body; the module decides its supported types");
            for (var type : List.of(ModAttackTypes.BULLET.get(), ModAttackTypes.LASER.get())) {
                AttackContext ordinary = shotContext(f).toBuilder().attackType(type).build();
                preparation.set(ContextKeys.ATTACK_CONTEXT, ordinary);
                ModExecutableEffects.CRICKETS_BODY_ATTACHMENT.get().apply(preparation);
                helper.assertTrue(ordinary.getSplitSequence().contains(ModSplitModules.CRICKETS_BODY.getId()),
                        "Ordinary bullets and lasers retain their existing Cricket's Body attachment");
            }
            try {
                f.main.launch(0.2, 0.01, attack);
                f.main.tick();
                var children = BulletRuntime.INSTANCE.manager(f.level).activeStates().stream()
                        .filter(b -> b.getOwner() == f.owner).toList();
                helper.assertTrue(children.size() == 4 && children.stream().allMatch(child -> child.getDamage() == 1),
                        "Cricket's Body alone emits four half-damage tears on the first successful contact");
            } finally { cleanupBullets(f); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 10)
    public static void ipecacAndBoneTearsBothFireOnceOnTheSameEnemy(GameTestHelper helper) {
        var f = fixture(helper);
        f.owner.setInvulnerable(true);
        Vec3 location = PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3);
        Cow target = f.cow(location);
        AttackContext attack = shotContext(f);
        attach(f, attack, true);
        AtomicInteger explosions = new AtomicInteger(), bones = new AtomicInteger();
        Consumer<ExplosionEvent.Detonate> explosion = event -> {
            if (event.getExplosion().getIndirectSourceEntity() == f.owner) explosions.incrementAndGet();
        };
        Consumer<AttackFiredEvent> fire = event -> {
            if (event.getOwner() == f.owner && event.getOrigin() == AttackOrigin.SPLIT_CHILD) {
                bones.incrementAndGet();
                cleanupBullets(f);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(explosion);
        MinecraftForge.EVENT_BUS.addListener(fire);
        f.main.launch(0.2, 0.01, attack);
        f.main.tick();
        helper.onEachTick(() -> {
            if (!f.closed) {
                target.setPos(location.add(0, -0.45, 0));
                target.setDeltaMovement(Vec3.ZERO);
                f.main.tick();
            }
        });
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(explosions.get() == 1 && bones.get() == 1,
                        "Ipecac and bone tears each trigger once: explosions=" + explosions + ", bones=" + bones);
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(explosion);
                MinecraftForge.EVENT_BUS.unregister(fire);
                cleanupBullets(f);
                f.close();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "knife_contact", timeoutTicks = 10)
    public static void idleDamageAndFailedFirstHitsDoNotLeakOrRetryEffects(GameTestHelper helper) {
        var f = fixture(helper);
        Cow target = f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(0, 0, 0.3));
        AtomicInteger before = new AtomicInteger(), after = new AtomicInteger();
        Consumer<IsaacAttackBeforeHitEntityEvent> pre = event -> { if (event.getBulletObject() == f.main) before.incrementAndGet(); };
        Consumer<IsaacAttackAfterHitEvent> post = event -> { if (event.getBulletObject() == f.main) after.incrementAndGet(); };
        MinecraftForge.EVENT_BUS.addListener(pre);
        MinecraftForge.EVENT_BUS.addListener(post);
        f.main.tick();
        helper.assertTrue(target.getHealth() < 1000 && before.get() == 0 && after.get() == 0,
                "Idle still damages without publishing entity hit effects");
        target.setInvulnerable(true);
        f.main.launch(0.2, 0.01, shotContext(f));
        helper.onEachTick(() -> { if (!f.closed) f.main.tick(); });
        helper.runAfterDelay(2, () -> target.setInvulnerable(false));
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(before.get() == 1 && after.get() == 0 && target.getHealth() < 998,
                        "A failed first hit is remembered while subsequent ticks still damage");
                helper.succeed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(pre);
                MinecraftForge.EVENT_BUS.unregister(post);
                f.close();
            }
        });
    }

    private static void attach(PlayerMomsKnifeGameTests.Fixture f, AttackContext attack, boolean ipecac) {
        var context = new ExecutableEffectContext(f.owner);
        context.set(ContextKeys.ATTACK_CONTEXT, attack);
        ModExecutableEffects.COMPOUND_FRACTURE_ATTACHMENT.get().apply(context);
        if (ipecac) ModExecutableEffects.IPECAC_ATTACHMENT.get().apply(context);
    }

    private static void cleanupBullets(PlayerMomsKnifeGameTests.Fixture f) {
        BulletRuntime.INSTANCE.manager(f.level).activeStates().stream().filter(b -> b.getOwner() == f.owner)
                .toList().forEach(BulletState::discard);
    }

    private static void prepare(PlayerMomsKnifeGameTests.Fixture f, AttackContext attack) {
        var request = AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.PREPARE_AND_EXECUTE, List.of(attack), false);
        MinecraftForge.EVENT_BUS.post(new AttackContextPrepareEvent(request, new AttackPlan(request, List.of(attack)), attack, 0));
    }

    private static AttackContext shotContext(PlayerMomsKnifeGameTests.Fixture f) {
        return AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get())
                .position(PlayerMomsKnifeMotion.idlePosition(f.owner, 1)).mainAxis(f.owner.getLookAngle())
                .range(24).speed(1).build();
    }

    private static AttackRequest extraRequest(PlayerMomsKnifeGameTests.Fixture f, int count) {
        var contexts = ModAttackPatterns.RING.get().generate(new AttackPatternContext(shotContext(f), count));
        return AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, false).withSourceId(ModExecutableEffects.TAMMYS_HEAD.getId());
    }

    private static List<PlayerMomsKnifeEntity> shots(PlayerMomsKnifeGameTests.Fixture f) {
        return f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).toList();
    }

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper) {
        var result = new PlayerMomsKnifeGameTests.Fixture(helper, 0);
        // Use this test's loaded chunk and a separate height, without taking global fixture slots.
        result.owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(0, 768, 0));
        result.main.initialize(result.owner);
        return result;
    }
}
