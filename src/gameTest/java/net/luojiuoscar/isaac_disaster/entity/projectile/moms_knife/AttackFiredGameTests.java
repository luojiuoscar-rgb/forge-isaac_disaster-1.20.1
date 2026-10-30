package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LeadPencilShotCounter;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LeadPencilBurst;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BulletAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_attack_fired")
@PrefixGameTestTemplate(false)
public final class AttackFiredGameTests {
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void everyPipelineModeExecutesThroughTheAttackTypeWithItsOwnPlan(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            var attack = new PlanAwareBulletAttack();
            for (var mode : AttackPipelineMode.values()) {
                AttackRequest request = switch (mode) {
                    case FULL, PLAN_PREPARE_AND_EXECUTE -> AttackRequest.generated(p.f.owner, attack,
                            AttackOrigin.PLAYER_PRIMARY, mode, false);
                    case PREPARE_AND_EXECUTE, EXECUTE_ONLY -> AttackRequest.withContexts(p.f.owner, attack,
                            AttackOrigin.PLAYER_PRIMARY, mode, p.contexts(attack, 1), false);
                };
                AttackExecutor.perform(request);
                helper.assertTrue(attack.lastRequest == request,
                        "The attack type receives the current request's plan in " + mode);
            }
            helper.assertTrue(attack.executions == 4 && p.events.size() == 4 && p.progress() == 4,
                    "Each pipeline mode executes and counts once through the attack type");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void technologyTwoExtraFireAdvancesPencil(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            p.fire(ModAttackTypes.TECHNOLOGY2.get(), 1, AttackOrigin.ABILITY_EXTRA);
            helper.assertTrue(p.events.size() == 1, "Technology 2 reports an actual firing");
            helper.assertTrue(p.progress() == 1, "Technology 2 extra firing advances Pencil");
            helper.assertTrue(p.events.get(0).getAttackType().getId().equals(ModAttackTypes.TECHNOLOGY2.getId()),
                    "The event identifies Technology 2");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void realMultishotCountsOnceAndCarriesPreparedSnapshots(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            p.fire(ModAttackTypes.BULLET.get(), 20, AttackOrigin.PLAYER_PRIMARY);
            helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Twenty real tears count as one fired attack");
            var event = p.events.get(0);
            helper.assertTrue(event.getPreparedContexts().size() == 20, "Event keeps the complete prepared volley");
            helper.assertTrue(event.getFirstFiredContext().isFrozen(), "Successful context is frozen");
            helper.assertTrue(event.getPreparedContexts().stream().allMatch(AttackContext::isFrozen), "All prepared snapshots are frozen");
            helper.assertTrue(!event.isCancelable(), "Already fired attacks cannot be cancelled retroactively");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void plansEmptyRequestsAndDelegatingNoopsDoNotCount(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            var type = ModAttackTypes.BULLET.get();
            var request = AttackRequest.generated(p.f.owner, type, AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false);
            MinecraftForge.EVENT_BUS.post(new AttackPlanEvent(request, new AttackPlan(request, p.contexts(type, 1))));
            p.fire(type, 0, AttackOrigin.PLAYER_PRIMARY);
            p.fire(ModAttackTypes.MONSTROS_LUNG.get(), 0, AttackOrigin.PLAYER_PRIMARY);
            helper.assertTrue(p.events.isEmpty() && p.progress() == 0, "Planning and no-op execution never signal firing");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void wholeAttackCancellationDoesNotCount(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            Consumer<BeforePerformAttackEvent> whole = e -> { if (e.getEntity() == p.f.owner) e.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BeforePerformAttackEvent.class, whole);
            try {
                AttackExecutor.perform(AttackRequest.generated(p.f.owner, ModAttackTypes.BULLET.get(),
                        AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false));
            } finally { MinecraftForge.EVENT_BUS.unregister(whole); }
            helper.assertTrue(p.events.isEmpty() && p.progress() == 0, "Whole-attack cancellation leaves progress unchanged");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void preparationIsNonCancelableAndPartialSpawnSuccessCountsOnce(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            AtomicInteger prepared = new AtomicInteger();
            Consumer<AttackContextPrepareEvent> prepare = e -> {
                if (e.getOwner() != p.f.owner) return;
                helper.assertTrue(!e.isCancelable(), "Every attack uses non-cancelable preparation");
                helper.assertTrue(e.getClass() == AttackContextPrepareEvent.class, "All attacks publish the same preparation event");
                prepared.incrementAndGet();
                e.getAttackContext().setBulletScale(0.5, false);
            };
            var attack = new RejectingBulletAttack(1);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, prepare);
            try { p.fire(attack, 3, AttackOrigin.PLAYER_PRIMARY); }
            finally {
                MinecraftForge.EVENT_BUS.unregister(prepare);
            }
            helper.assertTrue(prepared.get() == 3 && attack.attempts == 3, "All prepared contexts reach runtime, with one rejected spawn");
            helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Partial success counts exactly once");
            helper.assertTrue(p.events.get(0).getPreparedContexts().size() == 3,
                    "The fired snapshot retains every prepared context");
            helper.assertTrue(p.events.get(0).getPreparedContexts().stream().allMatch(c -> c.getBulletScaleModifier() == 0.5),
                    "Preparation still applies effects to the whole volley");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void rejectedRuntimeSpawnDoesNotCount(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            var attack = new RejectingBulletAttack(2);
            p.fire(attack, 2, AttackOrigin.PLAYER_PRIMARY);
            helper.assertTrue(attack.attempts == 2, "Both invalid states reach the real runtime spawn guard");
            helper.assertTrue(p.events.isEmpty() && p.progress() == 0, "Backend rejection does not signal firing");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void lasersFetusesAndDerivedShotsUseTheSameEvent(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            p.fire(ModAttackTypes.LASER.get(), 4, AttackOrigin.PLAYER_PRIMARY);
            p.fire(ModAttackTypes.C_SECTION.get(), 4, AttackOrigin.PLAYER_PRIMARY);
            for (var origin : List.of(AttackOrigin.ABILITY_EXTRA, AttackOrigin.SPLIT_CHILD,
                    AttackOrigin.BULLET_SECONDARY, AttackOrigin.SYSTEM)) p.fire(ModAttackTypes.BULLET.get(), 2, origin);
            helper.assertTrue(p.events.size() == 6 && p.progress() == 6, "Every firing origin advances the counter");
            helper.assertTrue(p.events.get(0).getAttackType() == ModAttackTypes.LASER.get(), "Laser identity is retained");
            helper.assertTrue(p.events.get(1).getAttackType() == ModAttackTypes.C_SECTION.get(), "Fetus identity is retained");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void executeOnlyStillSignalsActualFiring(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            var type = ModAttackTypes.BULLET.get();
            AttackExecutor.perform(AttackRequest.withContexts(p.f.owner, type, AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.EXECUTE_ONLY, p.contexts(type, 3), false));
            helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Skipping preparation still counts a real derived volley");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired", timeoutTicks = 30)
    public static void brimstoneCountsAtFirstActualPulseOnly(GameTestHelper helper) {
        Probe p = new Probe(helper);
        p.fire(ModAttackTypes.BRIMSTONE.get(), 4, AttackOrigin.PLAYER_PRIMARY);
        helper.assertTrue(p.events.isEmpty() && p.progress() == 0, "Merely scheduling a beam does not count");
        helper.runAfterDelay(3, () -> helper.assertTrue(p.events.size() == 1 && p.progress() == 1,
                "First actual multi-beam pulse counts once"));
        helper.runAfterDelay(17, () -> {
            try {
                helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Thirteen pulses do not recount the volley");
                helper.succeed();
            } finally { p.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired", timeoutTicks = 12)
    public static void cancelledScheduledBrimstoneDoesNotCount(GameTestHelper helper) {
        Probe p = new Probe(helper);
        p.fire(ModAttackTypes.BRIMSTONE.get(), 2, AttackOrigin.PLAYER_PRIMARY);
        ScheduledFuncHelper.clearPlayerTasks(p.f.owner.getUUID());
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(p.events.isEmpty() && p.progress() == 0, "Cancellation before the first pulse produces no firing event");
                helper.succeed();
            } finally { p.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired", timeoutTicks = 12)
    public static void delayedBrimstoneRetainsChargeAfterReleaseClearsIt(GameTestHelper helper) {
        Probe p = new Probe(helper);
        var charge = (IChargeableAttack) ModAttackTypes.BRIMSTONE.get();
        int total = Math.max(1, charge.getTotalCharge(p.f.owner));
        int amount = total / 2;
        double expected = (double) amount / total;
        p.f.ability.setChargeAmount(charge.getChargeBarId(), amount);
        p.fire(ModAttackTypes.BRIMSTONE.get(), 2, AttackOrigin.PLAYER_PRIMARY);
        p.f.ability.clearChargeAmount(charge.getChargeBarId());
        helper.assertTrue(p.events.isEmpty(), "The snapshot is captured before the deferred pulse");
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(p.events.size() == 1 && Math.abs(p.events.get(0).getChargeFraction() - expected) < 1e-12,
                        "Deferred firing reports the original charge rather than the cleared live value");
                helper.succeed();
            } finally { p.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired", timeoutTicks = 30)
    public static void overlappingBrimstoneRequestsRemainIndependent(GameTestHelper helper) {
        Probe p = new Probe(helper);
        p.fire(ModAttackTypes.BRIMSTONE.get(), 2, AttackOrigin.PLAYER_PRIMARY);
        p.fire(ModAttackTypes.BRIMSTONE.get(), 3, AttackOrigin.PLAYER_PRIMARY);
        helper.runAfterDelay(17, () -> {
            try {
                helper.assertTrue(p.events.size() == 2 && p.progress() == 2, "Overlapping volleys each count once");
                helper.assertTrue(p.events.stream().map(e -> e.getPreparedContexts().size()).sorted().toList().equals(List.of(2, 3)),
                        "Each deferred request retains its own prepared contexts");
                helper.succeed();
            } finally { p.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void sixteenReturningKnivesAndRejectedRelaunchCountOnce(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            p.f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            HeldMomsKnifeGroup.refresh(p.f.owner);
            var type = ModAttackTypes.MOMS_KNIFE.get();
            var charge = (IChargeableAttack) type;
            p.f.ability.setChargeAmount(charge.getChargeBarId(), charge.getTotalCharge(p.f.owner));
            var contexts = type.getAttackContexts(p.f.owner, 16);
            AttackExecutor.perform(AttackRequest.withContexts(p.f.owner, type, AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, false));
            helper.assertTrue(p.f.knives().stream().filter(PlayerMomsKnifeEntity::isInFlight).count() == 16,
                    "The complete held group really launches");
            helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Sixteen main knives count once");
            AttackExecutor.perform(AttackRequest.withContexts(p.f.owner, type, AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, false));
            helper.assertTrue(p.events.size() == 1 && p.progress() == 1, "Rejected release during flight does not count");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void fifteenthRealVolleyFiresBurstWithoutRecursiveProgress(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            for (int i = 0; i < 14; i++) p.fire(ModAttackTypes.BULLET.get(), 1, AttackOrigin.PLAYER_PRIMARY);
            helper.assertTrue(p.progress() == 14 && p.events.size() == 14, "Fourteen real attacks retain progress");
            p.fire(ModAttackTypes.BULLET.get(), 1, AttackOrigin.PLAYER_PRIMARY);
            helper.assertTrue(p.progress() == 0, "Fifteenth volley resets before derived work");
            helper.assertTrue(p.events.stream().filter(e -> e.getOrigin() == AttackOrigin.PLAYER_PRIMARY).count() == 15,
                    "Exactly fifteen primary events");
            var extras = p.events.stream().filter(e -> e.getOrigin() == AttackOrigin.ABILITY_EXTRA).toList();
            helper.assertTrue(extras.size() == 1 && extras.get(0).getPreparedContexts().size() == 12,
                    "One twelve-tear burst fires without recursively advancing the counter");
            helper.assertTrue(extras.get(0).getSourceId().equals(ModExecutableEffects.LEAD_PENCIL_BURST.getId()),
                    "The burst identifies its initiating effect separately from the bullet attack type");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "attack_fired")
    public static void directPencilBurstDoesNotAdvanceOwnedCounter(GameTestHelper helper) {
        try (Probe p = new Probe(helper)) {
            p.fire(ModAttackTypes.BULLET.get(), 1, AttackOrigin.SYSTEM);
            helper.assertTrue(p.progress() == 1, "System firing advances the owned counter");
            helper.assertTrue(new LeadPencilBurst().applyEffect(new ExecutableEffectContext(p.f.owner)),
                    "The reusable burst executes directly");
            helper.assertTrue(p.events.size() == 2 && p.progress() == 1, "A successful direct Pencil burst leaves progress untouched");
            helper.assertTrue(p.events.get(0).getSourceId().equals(ModAttackTypes.BULLET.getId()),
                    "Unattributed requests identify their attack backend");
        }
        helper.succeed();
    }

    private static final class PlanAwareBulletAttack extends BulletAttack {
        AttackRequest lastRequest;
        int executions;

        PlanAwareBulletAttack() { super(0); }

        @Override
        public void performAttack(AttackPlan plan, List<AttackContext> contexts, Consumer<AttackContext> onFired) {
            lastRequest = plan.getRequest();
            executions++;
            super.performAttack(contexts, onFired);
        }
    }

    private static final class RejectingBulletAttack extends BulletAttack {
        private final int rejectedStates;
        int attempts;

        RejectingBulletAttack(int rejectedStates) {
            super(0);
            this.rejectedStates = rejectedStates;
        }

        @Override
        protected BulletState createOptimizedState(AttackContext context) {
            BulletState state = super.createOptimizedState(context);
            if (attempts++ < rejectedStates) state.setVelocity(Vec3.ZERO);
            return state;
        }
    }

    private static final class Probe implements AutoCloseable {
        final PlayerMomsKnifeGameTests.Fixture f;
        final List<AttackFiredEvent> events = new ArrayList<>();
        final Consumer<AttackFiredEvent> listener;

        Probe(GameTestHelper helper) {
            f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement());
            ModPassiveAbilities.LEAD_PENCIL.get().handleObtain(f.owner, null);
            listener = event -> { if (event.getOwner() == f.owner) events.add(event); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackFiredEvent.class, listener);
        }

        List<AttackContext> contexts(AttackType type, int count) {
            List<AttackContext> result = new ArrayList<>();
            for (int i = 0; i < count; i++) result.add(type.createAttackContext(f.owner, f.owner));
            return result;
        }

        void fire(AttackType type, int count, AttackOrigin origin) {
            AttackExecutor.perform(AttackRequest.withContexts(f.owner, type, origin,
                    AttackPipelineMode.PREPARE_AND_EXECUTE, contexts(type, count), false));
        }

        double progress() {
            Double value = f.owner.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                    .orElseThrow(IllegalStateException::new).getDouble(LeadPencilShotCounter.DATA_KEY);
            return value == null ? 0 : value;
        }

        @Override
        public void close() {
            MinecraftForge.EVENT_BUS.unregister(listener);
            ScheduledFuncHelper.clearPlayerTasks(f.owner.getUUID());
            f.close();
        }
    }
}
