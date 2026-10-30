package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModules;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsKnifeShot;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BrimstonePlusMonstrosLung;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BookPlusMomsKnife;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MonstrosLungPlusMomsKnife;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_knife_synergy")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeSynergyGameTests {
    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void derivedKnifeRequestsDoNotTriggerBookOrLung(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            var contexts = attack.getAttackContexts(f.owner, 1);
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            f.owner.getRandom().setSeed(seedForBook(true));
            AttackExecutor.perform(AttackRequest.withContexts(f.owner, attack, AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.EXECUTE_ONLY, contexts, false));
            helper.assertTrue(!f.main.isInFlight(), "A derived request leaves the held main knife alone");
            helper.assertTrue(extraCount(f) == 1, "The explicit single shot does not recursively trigger book or lung");
        }
        helper.succeed();
    }

    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void sourceModulesFollowSetActivationAndLastItemRemoval(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var modules = f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new);
            var queue = modules.getTriggerModules();
            var bookId = ModTriggerModules.BOOK_MOMS_KNIFE.getId();
            var lungId = ModTriggerModules.MONSTROS_LUNG_MOMS_KNIFE.getId();
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 2);
            helper.assertTrue(!queue.contains(bookId), "Two books do not grant the synergy module");
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 1);
            helper.assertTrue(queue.snapshot().getStacks(bookId) == 1, "Activating Book grants one module");
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), -1);
            helper.assertTrue(!queue.contains(bookId), "Losing the set removes its module");
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 1);
            var lung = ModPassiveAbilities.MONSTROS_LUNG.get();
            lung.handleObtain(f.owner, null);
            lung.handleObtain(f.owner, null);
            helper.assertTrue(queue.snapshot().getStacks(bookId) == 1 && queue.snapshot().getStacks(lungId) == 2,
                    "Reactivation and duplicate items retain source counts");
            CompoundTag saved = new CompoundTag();
            modules.saveNBTData(saved);
            EffectModules restored = new EffectModules();
            restored.loadNBTData(saved);
            helper.assertTrue(restored.getTriggerModules().snapshot().getStacks(bookId) == 1
                            && restored.getTriggerModules().snapshot().getStacks(lungId) == 2,
                    "The existing module persistence retains both sources");
            var reference = ModAttackTypes.MOMS_KNIFE.get().createAttackContext(f.owner, f.owner);
            f.owner.getRandom().setSeed(seedForBook(true));
            publishFired(f, reference, 1);
            helper.assertTrue(extraCount(f) == 5, "Two lung copies still produce only four, plus one Book knife");
            f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).forEach(PlayerMomsKnifeEntity::discard);
            lung.handleRemove(f.owner, null);
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), -1);
            helper.assertTrue(!queue.contains(bookId) && queue.snapshot().getStacks(lungId) == 1,
                    "Set removal and a non-last item removal update only their sources");
            publishFired(f, reference, 1);
            helper.assertTrue(extraCount(f) == 4, "The remaining lung module continues to work");
            f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).forEach(PlayerMomsKnifeEntity::discard);
            lung.handleRemove(f.owner, null);
            helper.assertTrue(!queue.contains(lungId), "The last lung removal clears its module");
            f.owner.getRandom().setSeed(seedForBook(true));
            publishFired(f, reference, 1);
            helper.assertTrue(extraCount(f) == 0, "Removed sources produce no further extras");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void attackTypeCountsAloneDoNotGrantTheLungEffect(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.ability.addAttackType(ModAttackTypes.MONSTROS_LUNG.getId(), 1, f.owner);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.onReleased(f.owner);
            helper.assertTrue(f.main.isInFlight() && extraCount(f) == 0,
                    "Only the source-owned trigger module grants the synergy");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void lungUsesTheReleaseSnapshotEvenIfLiveChargeIsCleared(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            double[] snapshot = {-1};
            Consumer<AttackFiredEvent> clearLive = event -> {
                if (event.getOwner() != f.owner) return;
                snapshot[0] = event.getChargeFraction();
                f.ability.clearChargeAmount(attack.getChargeBarId());
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, AttackFiredEvent.class, clearLive);
            try {
                f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
                attack.onReleased(f.owner);
                helper.assertTrue(snapshot[0] == 1 && f.ability.getChargeAmount(attack.getChargeBarId()) == 0,
                        "The event retains full charge after live state clears");
                helper.assertTrue(extraCount(f) == 4, "Lung reads the event snapshot and still emits four knives");
            } finally { MinecraftForge.EVENT_BUS.unregister(clearLive); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void aTapSnapshotsTheKnifesMinimumOneTickCharge(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), 0);
            double expected = 1.0 / attack.getTotalCharge(f.owner);
            double[] actual = {-1};
            Consumer<AttackFiredEvent> listener = event -> {
                if (event.getOwner() == f.owner) actual[0] = event.getChargeFraction();
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try {
                attack.onReleased(f.owner);
                helper.assertTrue(f.main.isInFlight() && Math.abs(actual[0] - expected) < 1e-12,
                        "A zero-tick tap snapshots the same one-tick minimum used by knife movement");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void synergyEffectsValidateEventsAndPropagateSpawnFailures(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var book = new BookPlusMomsKnife();
            var lung = new MonstrosLungPlusMomsKnife();
            var reference = ModAttackTypes.MOMS_KNIFE.get().createAttackContext(f.owner, f.owner);
            reference.freeze();
            var request = AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.EXECUTE_ONLY, List.of(reference), false);
            var context = new ExecutableEffectContext(f.owner);
            var cow = f.cow(f.owner.position().add(10, 0, 0));
            var foreign = AttackContext.builder(cow, cow).build();
            for (IAbilityEffect effect : List.of(book, lung)) {
                helper.assertTrue(!effect.applyEffect(new ExecutableEffectContext(cow)), "A non-player input fails");
                helper.assertTrue(!effect.applyEffect(new ExecutableEffectContext(f.owner)), "A missing event fails");
                context.set(ContextKeys.EVENT, new AttackFiredEvent(request, List.of(foreign), foreign, 1));
                helper.assertTrue(!effect.applyEffect(context), "A mismatched successful context owner fails");
                var other = AttackRequest.withContexts(cow, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.PLAYER_PRIMARY,
                        AttackPipelineMode.EXECUTE_ONLY, List.of(foreign), false);
                context.set(ContextKeys.EVENT, new AttackFiredEvent(other, List.of(foreign), foreign, 1));
                helper.assertTrue(!effect.applyEffect(context), "A mismatched event owner fails");
                var derived = AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(), AttackOrigin.ABILITY_EXTRA,
                        AttackPipelineMode.EXECUTE_ONLY, List.of(reference), false);
                context.set(ContextKeys.EVENT, new AttackFiredEvent(derived, List.of(reference), reference, 1));
                helper.assertTrue(effect.applyEffect(context), "A derived event is a successful no-op");
                var ordinary = AttackRequest.withContexts(f.owner, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY,
                        AttackPipelineMode.EXECUTE_ONLY, List.of(reference), false);
                context.set(ContextKeys.EVENT, new AttackFiredEvent(ordinary, List.of(reference), reference, 1));
                helper.assertTrue(effect.applyEffect(context), "Other attack types are successful no-ops");
            }
            helper.assertTrue(extraCount(f) == 0, "Invalid and ineligible executions spawn nothing");
            context.set(ContextKeys.EVENT, new AttackFiredEvent(request, List.of(reference), reference, 1));
            f.owner.getRandom().setSeed(seedForBook(false));
            helper.assertTrue(book.applyEffect(context) && extraCount(f) == 0, "A missed Book chance safely completes");
            helper.assertTrue(lung.applyEffect(context) && extraCount(f) == 4, "The effect can execute directly from valid event data");
            f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).forEach(PlayerMomsKnifeEntity::discard);
            AtomicInteger attempts = new AtomicInteger();
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner
                        && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) {
                    attempts.incrementAndGet();
                    event.setCanceled(true);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(reject);
            try {
                f.owner.getRandom().setSeed(seedForBook(true));
                helper.assertTrue(!book.applyEffect(context), "Book propagates a rejected nested shot");
                helper.assertTrue(!lung.applyEffect(context), "Lung propagates rejected nested shots");
                helper.assertTrue(attempts.get() == 5 && extraCount(f) == 0, "All four lung shots are attempted even after one fails");
            } finally { MinecraftForge.EVENT_BUS.unregister(reject); }
        }
        helper.succeed();
    }

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper) {
        return new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement());
    }

    private static long seedForBook(boolean proc) {
        for (long seed = 0; seed < 100000; seed++) {
            if ((RandomSource.create(seed).nextDouble() < 0.25) == proc) return seed;
        }
        throw new IllegalStateException("No deterministic book seed");
    }

    private static long extraCount(PlayerMomsKnifeGameTests.Fixture f) {
        return f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).count();
    }

    private static void launch(PlayerMomsKnifeGameTests.Fixture f, List<AttackContext> contexts) {
        AttackExecutor.perform(AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(),
                AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.EXECUTE_ONLY, contexts, false));
    }

    private static void publishFired(PlayerMomsKnifeGameTests.Fixture f, AttackContext reference, double charge) {
        reference = reference.bindAttackTypeOrCopy(ModAttackTypes.MOMS_KNIFE.get());
        reference.freeze();
        var request = AttackRequest.withContexts(f.owner, ModAttackTypes.MOMS_KNIFE.get(),
                AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.EXECUTE_ONLY, List.of(reference), false);
        MinecraftForge.EVENT_BUS.post(new AttackFiredEvent(request, List.of(reference), reference, charge));
    }


    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void bookProcImmediatelyAddsOneSingleTripInsteadOfAnotherHeldKnife(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(1);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            f.owner.getRandom().setSeed(seedForBook(true));
            attack.onReleased(f.owner);
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 1,
                    "Book must not turn its proc into a second returning knife");
            helper.assertTrue(extraCount(f) == 1, "Book emits one one-way knife in the launch tick");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void fullChargeLungAddsFourImmediatelyForTheWholeSixteenKnifeVolley(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.onReleased(f.owner);
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 16, "The main volley still has sixteen slots");
            helper.assertTrue(extraCount(f) == 4, "Lung adds four independent knives immediately, once per volley");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void bookMissAndCountReadsDoNotGenerateKnivesOrConsumeAnExtraRoll(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(1);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            long seed = seedForBook(true);
            f.owner.getRandom().setSeed(seed);
            helper.assertTrue(attack.getBulletCount(f.owner) == 1, "Book does not add returning contexts");
            helper.assertTrue(f.owner.getRandom().nextDouble() == RandomSource.create(seed).nextDouble(),
                    "Knife count reads do not roll book before a successful launch");
            f.owner.getRandom().setSeed(seed);
            helper.assertTrue(ModAttackTypes.BULLET.get().getBulletCount(f.owner) == 2,
                    "Other attacks keep their ordinary book count proc");
            f.owner.getRandom().setSeed(seedForBook(false));
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.onReleased(f.owner);
            helper.assertTrue(extraCount(f) == 0 && HeldMomsKnifeGroup.knives(f.owner).size() == 1,
                    "A missed book roll safely launches only the main knife");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void everyLungChargeTierHasTheSameCountForOneAndSixteenHeldKnives(GameTestHelper helper) {
        for (int held : new int[]{1, 16}) {
            try (var f = fixture(helper)) {
                ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
                f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(held);
                var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
                int total = attack.getTotalCharge(f.owner);
                int[] amounts = {1, (int) Math.ceil(total * 0.25), (int) Math.ceil(total * 0.5),
                        (int) Math.ceil(total * 0.75), total};
                int[] expected = {1, 2, 3, 4, 4};
                for (int tier = 0; tier < amounts.length; tier++) {
                    f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY)
                            .forEach(PlayerMomsKnifeEntity::discard);
                    HeldMomsKnifeGroup.knives(f.owner).forEach(PlayerMomsKnifeEntity::discard);
                    HeldMomsKnifeGroup.refresh(f.owner);
                    f.ability.setChargeAmount(attack.getChargeBarId(), amounts[tier]);
                    launch(f, attack.getAttackContexts(f.owner, held));
                    helper.assertTrue(extraCount(f) == expected[tier],
                            "Lung tier " + tier + " adds " + expected[tier] + " once for held=" + held);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void scatterStartsBehindTheViewAndPreservesSpeedPlaneAndTwentyFourBlockPath(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            f.owner.setYRot(37);
            for (float pitch : new float[]{0, -45, -90, 90}) {
                f.owner.setXRot(pitch);
                Vec3 axis = f.owner.getLookAngle();
                Vec3 normal = axis.cross(GeometryHelper.lateralAxis(axis));
                var reference = AttackContext.builder(f.owner, f.owner).attackType(ModAttackTypes.MOMS_KNIFE.get())
                        .mainAxis(axis).speed(5).build();
                publishFired(f, reference, 1);
                var extras = f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).toList();
                helper.assertTrue(extras.size() == 4, "One four-knife scatter is created");
                for (var knife : extras) {
                    Vec3 offset = PlayerMomsKnifeMotion.offsetToLocal(knife.position()
                            .subtract(PlayerMomsKnifeMotion.barragePosition(f.owner)), f.owner.getYRot(), pitch);
                    helper.assertTrue(Math.abs(offset.z) < 1e-5 && Math.abs(offset.x) <= 1.00001 && Math.abs(offset.y) <= 1.00001,
                            "The start is exactly one block behind with square view-plane scatter: " + offset);
                    helper.assertTrue(Math.abs(knife.getVelocity().dot(normal)) < 1e-5
                                    && Math.abs(knife.getVelocity().length() - 5) < 1e-5,
                            "The launch direction stays in the view plane and retains shot speed");
                    helper.assertTrue(knife.getRange() == 24 && knife.getHeldSlot() == -1,
                            "Detached knives have twenty-four block range and never claim a held slot");
                    Vec3 from = knife.position();
                    for (int step = 0; step < 5; step++) knife.tick();
                    helper.assertTrue(knife.isRemoved() && Math.abs(knife.position().distanceTo(from) - 24) < 1e-4,
                            "Five speed-five moves clip the last move and discard at exactly twenty-four blocks");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void bookUsesTheFrontRayFromItsRandomBehindPlayerOrigin(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
            f.cow(f.owner.getEyePosition().add(f.owner.getLookAngle().scale(6)));
            Vec3 aim = PlayerMomsKnifeAim.point(f.owner);
            f.owner.getRandom().setSeed(seedForBook(true));
            var reference = AttackContext.builder(f.owner, f.owner).speed(0.3).build();
            publishFired(f, reference, 1);
            var knife = f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).findFirst().orElseThrow();
            Vec3 expected = aim.subtract(knife.position()).normalize();
            helper.assertTrue(knife.getVelocity().normalize().distanceTo(expected) < 1e-6,
                    "Book aims from the actual scattered start at the current first ray hit");
            helper.assertTrue(Math.abs(knife.getVelocity().length() - 0.3) < 1e-6 && knife.getRange() == 24,
                    "Book inherits prepared speed and always travels twenty-four blocks");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void cancelledAttackAndEmptyFiringQueueNeverTriggerExtras(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            Consumer<BeforePerformAttackEvent> cancelAttack = event -> {
                if (event.getEntity() == f.owner) event.setCanceled(true);
            };
            Consumer<AttackPlanEvent> clearPlan = event -> {
                if (event.getOwner() == f.owner) event.replaceBaseContexts(List.of());
            };
            for (int phase = 0; phase < 2; phase++) {
                if (phase == 0) MinecraftForge.EVENT_BUS.addListener(cancelAttack);
                else MinecraftForge.EVENT_BUS.addListener(clearPlan);
                try {
                    f.owner.getRandom().setSeed(seedForBook(true));
                    f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
                    attack.onReleased(f.owner);
                    helper.assertTrue(extraCount(f) == 0 && !f.main.isInFlight(),
                            "Whole-attack cancellation and an empty pre-preparation queue both suppress payoffs");
                } finally {
                    MinecraftForge.EVENT_BUS.unregister(cancelAttack);
                    MinecraftForge.EVENT_BUS.unregister(clearPlan);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void firstSuccessfulContextSuppliesTheExtrasAndAllFailedLaunchesAddNothing(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            var contexts = attack.getAttackContexts(f.owner, 2);
            var invalid = contexts.get(0).toBuilder().position(new Vec3(Double.NaN, 0, 0)).build();
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            launch(f, List.of(invalid));
            helper.assertTrue(extraCount(f) == 0, "A fully failed launch has no synergy payoff");
            var valid = contexts.get(1).toBuilder().speed(0.37).build();
            launch(f, List.of(invalid, valid));
            helper.assertTrue(extraCount(f) == 4, "A later successful context triggers the payoff once");
            for (var knife : f.knives()) if (knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) {
                helper.assertTrue(Math.abs(knife.getVelocity().length() - 0.37) < 1e-6,
                        "Prepared speed comes from the first successful context");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void removingOneConfirmedEntityStillPaysOffOnceForTheRest(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(3);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            attack.onTick(f.owner);
            Consumer<AttackContextPrepareEvent> removeFirst = event -> {
                if (event.getOwner() == f.owner && event.getContextIndex() == 0) f.main.discard();
            };
            MinecraftForge.EVENT_BUS.addListener(removeFirst);
            try {
                f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
                attack.onReleased(f.owner);
                helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).stream().filter(PlayerMomsKnifeEntity::isInFlight).count() == 2
                                && extraCount(f) == 4,
                        "The two surviving confirmed main entities trigger one four-knife payoff");
            } finally { MinecraftForge.EVENT_BUS.unregister(removeFirst); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void brimstoneLungKeepsItsThreeToFiveFixedBeamsInTheSameViewPlane(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var type = ModAttackTypes.BRIMSTONE.get();
            var effect = new BrimstonePlusMonstrosLung();
            for (float pitch : new float[]{0, -45, -90, 90}) {
                f.owner.setXRot(pitch);
                var request = AttackRequest.generated(f.owner, type, AttackOrigin.PLAYER_PRIMARY,
                        AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false);
                var reference = type.createAttackContext(f.owner, f.owner);
                var event = new AttackPlanEvent(request, new AttackPlan(request, List.of(reference)));
                var context = new ExecutableEffectContext(f.owner);
                context.set(ContextKeys.EVENT, event);
                helper.assertTrue(effect.applyEffect(context), "The original beam synergy still executes");
                var extras = event.getExtraContexts();
                Vec3 axis = f.owner.getLookAngle();
                Vec3 normal = axis.cross(GeometryHelper.lateralAxis(axis));
                helper.assertTrue(extras.size() >= 3 && extras.size() <= 5, "The original beam count remains three to five");
                for (var beam : extras) {
                    helper.assertTrue(beam.usesFixedLaunchTransform() && beam.getPos().distanceTo(reference.getPos()) < 1e-6
                                    && Math.abs(beam.getMainAxis().dot(normal)) < 1e-6,
                            "The extracted pattern retains fixed origin and view-plane directions");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy")
    public static void sharedShotEffectRejectsInvalidInputsAndCancelledWorldInsertion(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var effect = (MomsKnifeShot) ModExecutableEffects.MOMS_KNIFE_SHOT.get();
            helper.assertTrue(!effect.applyEffect(new ExecutableEffectContext(f.owner)), "Missing attack context fails");
            var cow = f.cow(f.owner.position().add(10, 0, 0));
            helper.assertTrue(!effect.applyEffect(new ExecutableEffectContext(cow)), "A non-player cannot own the shot effect");
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.ATTACK_CONTEXT, AttackContext.builder(cow, cow).build());
            helper.assertTrue(!effect.applyEffect(context), "Mismatched ownership fails");
            context.set(ContextKeys.ATTACK_CONTEXT, AttackContext.builder(f.owner, f.owner)
                    .position(new Vec3(Double.NaN, 0, 0)).build());
            helper.assertTrue(!effect.applyEffect(context), "An invalid start fails initialization");
            context.set(ContextKeys.ATTACK_CONTEXT, AttackContext.builder(f.owner, f.owner)
                    .position(PlayerMomsKnifeMotion.barragePosition(f.owner)).range(24).speed(1).build());
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife
                        && knife.getOwner() == f.owner && knife.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(reject);
            try { helper.assertTrue(!effect.applyEffect(context), "Rejected world insertion returns false"); }
            finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(effect.applyEffect(context) && extraCount(f) == 1, "A valid direct call creates exactly one shot");
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 1, "The main slot remains intact");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "knife_synergy", timeoutTicks = 30)
    public static void bookAndLungFireNowWhileOnlyOneBrimstoneBarrageWaitsForReturn(GameTestHelper helper) {
        var f = fixture(helper);
        StatManager.modifySetWithId(f.owner, ModSetAbilities.BOOK.getId(), 3);
        ModPassiveAbilities.MONSTROS_LUNG.get().handleObtain(f.owner, null);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(2);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(0.3);
        AtomicInteger countEvents = new AtomicInteger();
        Consumer<IsaacGetBulletCountEvent> listener = event -> {
            if (event.getPlayer() == f.owner) countEvents.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        f.owner.getRandom().setSeed(seedForBook(true));
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        helper.assertTrue(extraCount(f) == 5 && countEvents.get() == 1,
                "Book plus lung adds five in the same tick without recursive count reads");
        helper.assertTrue(ScheduledFuncHelper.getPlayerTasks(f.owner.getUUID()).isEmpty(),
                "Immediate extras do not schedule a barrage before the main turnaround");
        helper.onEachTick(() -> {
            for (var knife : HeldMomsKnifeGroup.knives(f.owner)) if (!knife.isRemoved()) knife.tick();
        });
        helper.runAfterDelay(22, () -> {
            try {
                helper.assertTrue(extraCount(f) == 10, "A seven-tick wait adds five Brimstone shots to the five immediate extras");
                helper.assertTrue(countEvents.get() == 1 && HeldMomsKnifeGroup.knives(f.owner).size() == 16,
                        "Extras neither reroll the count nor overwrite held slots");
                helper.succeed();
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); f.close(); }
        });
    }
}
