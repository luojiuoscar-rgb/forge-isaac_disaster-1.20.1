package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup.Pose;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.CollectHeldKnivesEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import io.netty.buffer.Unpooled;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_loki_knife")
@PrefixGameTestTemplate(false)
public final class LokisHornsMomsKnifeGameTests {
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void confirmedKnifePreparationCannotCancelTheVolley(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            AtomicInteger cancelable = new AtomicInteger();
            AtomicInteger prepared = new AtomicInteger();
            Consumer<AttackContextPrepareEvent> listener = event -> {
                if (event.getOwner() != f.owner) return;
                prepared.incrementAndGet();
                if (event.isCancelable()) cancelable.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try { fire(f); }
            finally { MinecraftForge.EVENT_BUS.unregister(listener); }
            helper.assertTrue(prepared.get() == 4 && cancelable.get() == 0,
                    "Confirmed knife entries use non-cancelable preparation notifications");
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "All four confirmed entries launch after preparation");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void preparationCannotChangeTheConfirmedQueueAndLaterListenersStillRun(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            AtomicInteger rejected = new AtomicInteger();
            AtomicInteger notified = new AtomicInteger();
            Consumer<AttackContextPrepareEvent> attempts = event -> {
                if (event.getOwner() != f.owner) return;
                var plan = (HeldMomsKnifeAttackPlan) event.getPlan();
                try { plan.includeSource(ResourceLocation.parse("example_addon:late")); }
                catch (IllegalStateException expected) { rejected.incrementAndGet(); }
                try { plan.replaceBaseContexts(List.of()); }
                catch (IllegalStateException expected) { rejected.incrementAndGet(); }
                try { plan.appendExtraContexts(List.of(event.getAttackContext())); }
                catch (IllegalStateException expected) { rejected.incrementAndGet(); }
                helper.assertTrue(plan.finalizeContexts().size() == 4, "Repeated finalization keeps the confirmed sequence");
            };
            Consumer<AttackContextPrepareEvent> later = event -> {
                if (event.getOwner() == f.owner) notified.incrementAndGet();
            };
            MinecraftForge.EVENT_BUS.addListener(attempts);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, later);
            try { fire(f); }
            finally {
                MinecraftForge.EVENT_BUS.unregister(attempts);
                MinecraftForge.EVENT_BUS.unregister(later);
            }
            helper.assertTrue(rejected.get() == 12 && notified.get() == 4,
                    "Queue edits are rejected without skipping later preparation listeners");
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "The original confirmed queue still launches");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void separatePlansKeepTheirOwnSourceSelectionAndEntityQueue(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            var attack = ModAttackTypes.MOMS_KNIFE.get();
            var contexts = attack.getAttackContexts(f.owner, 1);
            var request = AttackRequest.withContexts(f.owner, attack, AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.EXECUTE_ONLY, contexts, false);
            var withExtras = (HeldMomsKnifeAttackPlan) attack.createPlan(request, contexts);
            withExtras.includeSource(ModTriggerModules.LOKIS_HORNS.getId());
            var first = withExtras.finalizeContexts();
            var other = attack.createPlan(request, contexts);
            helper.assertTrue(other.finalizeContexts().size() == 1 && first.size() == 4,
                    "Source admission belongs to one request, not to a player or global cache");
            var charge = (IChargeableAttack) attack;
            f.ability.setChargeAmount(charge.getChargeBarId(), charge.getTotalCharge(f.owner));
            first.forEach(AttackContext::freeze);
            attack.performAttack(withExtras, first, ignored -> {});
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "Finalizing another request cannot replace the first request's entity queue");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void lokiAddsThreeHeldKnivesBeforeAnyProbabilityRoll(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement())) {
            ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(f.owner, null);
            helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 4,
                    "Loki adds left, right and rear held knives before firing");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void additionalHeldKnivesAreOutsideTheSixteenFrontKnifeLimit(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement())) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(f.owner, null);
            helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 19,
                    "Sixteen front knives and three source-owned extra knives coexist");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void chanceFailureLeavesTheSameThreeKnivesHeld(GameTestHelper helper) {
        try (var f = fixture(helper, -5)) {
            var extras = extras(f);
            fire(f);
            helper.assertTrue(extras.size() == 3 && extras.stream().noneMatch(PlayerMomsKnifeEntity::isInFlight),
                    "Zero Loki chance keeps all three source-owned knives idle");
            helper.assertTrue(f.main.isInFlight() && HeldMomsKnifeGroup.isInFlight(f.owner), "The front knife still launches");
            helper.assertTrue(extras(f).equals(extras), "Failure preserves the actual held entities");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void guaranteedChanceLaunchesAllNineteenAndCountsOneRequest(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            HeldMomsKnifeGroup.refresh(f.owner);
            List<AttackFiredEvent> events = new ArrayList<>();
            Consumer<AttackFiredEvent> listener = event -> { if (event.getOwner() == f.owner) events.add(event); };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try { fire(f); }
            finally { MinecraftForge.EVENT_BUS.unregister(listener); }
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 19
                    && HeldMomsKnifeGroup.knives(f.owner).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "All sixteen front and three extra knives launch together");
            helper.assertTrue(events.size() == 1 && events.get(0).getPreparedContexts().size() == 19,
                    "Nineteen knives produce a single fired request");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void removedConfirmedEntityDoesNotRemapOtherHeldKnivesOrTheirEffects(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            var left = extras(f).stream().filter(k -> k.getHeldSlot() == 0).findFirst().orElseThrow();
            var right = extras(f).stream().filter(k -> k.getHeldSlot() == 1).findFirst().orElseThrow();
            var rear = extras(f).stream().filter(k -> k.getHeldSlot() == 2).findFirst().orElseThrow();
            Consumer<AttackContextPrepareEvent> listener = event -> {
                if (event.getOwner() != f.owner) return;
                event.getAttackContext().setBulletScale(1.5 + event.getContextIndex(), false);
                if (event.getContextIndex() == 1) left.discard();
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, listener);
            try { fire(f); }
            finally { MinecraftForge.EVENT_BUS.unregister(listener); }
            helper.assertTrue(left.isRemoved() && right.isInFlight() && rear.isInFlight(),
                    "Removing the confirmed left entity does not shift or replace the right and rear knives");
            helper.assertTrue(extras(f).size() == 2 && right.getAttackContext().getBulletScaleModifier() == 3.5
                    && rear.getAttackContext().getBulletScaleModifier() == 4.5,
                    "Each remaining entity receives its own prepared effects without recreating the removed entity");
            helper.assertTrue(right.getSourceId().equals(ModTriggerModules.LOKIS_HORNS.getId()),
                    "The entity still exposes the contributor's source ID");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void collectionDoesNotRollChanceAndCopiesDoNotDuplicateEntries(GameTestHelper helper) {
        try (var f = fixture(helper, 0)) {
            f.owner.getRandom().setSeed(841);
            double expected = f.owner.getRandom().nextDouble();
            f.owner.getRandom().setSeed(841);
            for (int i = 0; i < 10; i++) HeldMomsKnifeGroup.refresh(f.owner);
            helper.assertTrue(f.owner.getRandom().nextDouble() == expected, "Held collection consumes no random firing rolls");
            ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(f.owner, null);
            helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 4, "Two Loki copies still contribute only three entries");
            ModPassiveAbilities.LOKIS_HORNS.get().handleRemove(f.owner, null);
            helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 4, "Removing one copy retains the entries");
            ModPassiveAbilities.LOKIS_HORNS.get().handleRemove(f.owner, null);
            helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 1 && extras(f).isEmpty(), "Final removal cleans only Loki slots");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife", timeoutTicks = 55)
    public static void knivesReturnToTheirOwnSlotsAndTheWholeGroupBlocksCharging(GameTestHelper helper) {
        var f = fixture(helper, 15);
        var original = extras(f);
        fire(f);
        var charge = (IChargeableAttack) ModAttackTypes.MOMS_KNIFE.get();
        helper.assertTrue(!charge.isChargeEligible(f.owner), "The launched group blocks the next charge");
        helper.runAfterDelay(36, () -> {
            try {
                helper.assertTrue(original.stream().noneMatch(PlayerMomsKnifeEntity::isInFlight), "All three knives return");
                helper.assertTrue(charge.isChargeEligible(f.owner), "Charging becomes eligible after the entire group returns");
                helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).containsAll(original), "Returning preserves source-owned entities");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void addonEntriesCanBeHeldAndLaunchedWithoutLokiOrCoreChanges(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement())) {
            ResourceLocation source = ResourceLocation.parse("example_addon:held_blade");
            Pose pose = new Pose(new Vec3(0, 0.7, 0), new Vec3(0, 1, 0));
            Consumer<CollectHeldKnivesEvent> collect = event -> {
                if (event.getPlayer() == f.owner) {
                    event.addEntries(source, List.of(pose));
                    event.addEntries(source, List.of(pose));
                }
            };
            Consumer<AttackPlanEvent> plan = event -> {
                if (event.getOwner() == f.owner && event.getPlan() instanceof HeldMomsKnifeAttackPlan knifePlan) {
                    knifePlan.includeSource(source);
                    knifePlan.includeSource(source);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(collect);
            MinecraftForge.EVENT_BUS.addListener(plan);
            try {
                helper.assertTrue(HeldMomsKnifeGroup.refresh(f.owner).size() == 2, "An addon contributes one deduplicated held entry");
                var knife = HeldMomsKnifeGroup.knives(f.owner).stream()
                        .filter(held -> held.getHeldAttackSlot().sourceId().equals(source)).findFirst().orElseThrow();
                fire(f);
                helper.assertTrue(knife.isInFlight() && knife.getHeldAttackSlot().sourceId().equals(source),
                        "The standard attack pipeline launches the addon's named slot");
                helper.assertTrue(HeldMomsKnifeGroup.isInFlight(f.owner), "Addon returning knives participate in the group lock");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(collect);
                MinecraftForge.EVENT_BUS.unregister(plan);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void equalPosesRemainSeparateAndSourceGroupsReceiveIndependentSlots(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement())) {
            ResourceLocation first = ResourceLocation.parse("example_addon:first_group");
            ResourceLocation second = ResourceLocation.parse("example_addon:second_group");
            Pose pose = new Pose(new Vec3(0, 0.7, 0), new Vec3(0, 1, 0));
            Consumer<CollectHeldKnivesEvent> collect = event -> {
                if (event.getPlayer() != f.owner) return;
                event.addEntries(first, List.of(pose, pose));
                event.addEntries(second, List.of(pose));
            };
            Consumer<AttackPlanEvent> plan = event -> {
                if (event.getOwner() != f.owner) return;
                if (event.getPlan() instanceof HeldMomsKnifeAttackPlan knifePlan) {
                    knifePlan.includeSource(first);
                    knifePlan.includeSource(second);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(collect);
            MinecraftForge.EVENT_BUS.addListener(plan);
            try {
                var held = HeldMomsKnifeGroup.refresh(f.owner);
                helper.assertTrue(held.size() == 4 && held.stream().map(PlayerMomsKnifeEntity::getHeldAttackSlot).distinct().count() == 4,
                        "Identical poses still create separate knives with manager-assigned identities");
                helper.assertTrue(held.get(0) == f.main && held.get(1).getHeldAttackSlot().sourceId().equals(first)
                        && held.get(3).getHeldAttackSlot().sourceId().equals(second), "The front group precedes extra groups in contribution order");
                fire(f);
                helper.assertTrue(held.stream().allMatch(PlayerMomsKnifeEntity::isInFlight), "Both source groups launch through their allocated slots");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(collect);
                MinecraftForge.EVENT_BUS.unregister(plan);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void collectionOwnsItsPoseListsAndOnlyAcceptsEachSourceOnce(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement())) {
            ResourceLocation source = ResourceLocation.parse("example_addon:group");
            Pose pose = new Pose(Vec3.ZERO, new Vec3(0, 0, -1));
            List<Pose> poses = new ArrayList<>(List.of(pose, pose));
            var event = new CollectHeldKnivesEvent(f.owner);
            event.addEntries(source, poses);
            var snapshot = event.getGroups();
            poses.clear();
            event.addEntries(source, List.of(pose));
            event.addEntries(ResourceLocation.parse("example_addon:later_group"), List.of(pose));
            helper.assertTrue(event.getGroups().get(source).size() == 2, "Caller mutations and duplicate contributions cannot replace the first group");
            helper.assertTrue(snapshot.size() == 1, "Previously returned group snapshots remain stable");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void sourceAndPoseRoundTripInTheRealEntitySpawnPayload(GameTestHelper helper) {
        try (var f = fixture(helper, 0)) {
            var original = extras(f).get(0);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            var copy = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
            try {
                original.writeSpawnData(buffer);
                copy.setPos(original.position());
                copy.readSpawnData(buffer);
                helper.assertTrue(copy.getHeldAttackSlot().equals(original.getHeldAttackSlot())
                        && copy.getSourceId().equals(ModTriggerModules.LOKIS_HORNS.getId()), "Source and slot survive the spawn payload");
                helper.assertTrue(copy.getRenderAnchor(f.owner, 1).distanceTo(original.getRenderAnchor(f.owner, 1)) < 1e-6,
                        "The extra knife retains its source's held pose");
                helper.assertTrue(buffer.readableBytes() == 0, "The spawn payload is consumed exactly");
            } finally { buffer.release(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loki_knife")
    public static void wizDoesNotDuplicateLokiAndSwitchingHandsCleansEverySource(GameTestHelper helper) {
        try (var f = fixture(helper, 15)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
            f.owner.addEffect(new MobEffectInstance(ModEffects.THE_WIZ.get(), 100));
            HeldMomsKnifeGroup.refresh(f.owner);
            fire(f);
            helper.assertTrue(extras(f).size() == 3 && extras(f).stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                    "Wiz changes the front plan without multiplying source-owned Loki knives");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (var knife : List.copyOf(HeldMomsKnifeGroup.knives(f.owner))) knife.tick();
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).isEmpty(), "Switching away removes knives from every source");
        }
        helper.succeed();
    }

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper, double luck) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_FIXTURE.getAndIncrement());
        f.owner.getAttribute(Attributes.LUCK).setBaseValue(luck);
        ModPassiveAbilities.LOKIS_HORNS.get().handleObtain(f.owner, null);
        HeldMomsKnifeGroup.refresh(f.owner);
        return f;
    }

    private static List<PlayerMomsKnifeEntity> extras(PlayerMomsKnifeGameTests.Fixture f) {
        return HeldMomsKnifeGroup.knives(f.owner).stream()
                .filter(knife -> knife.getHeldAttackSlot().sourceId().equals(ModTriggerModules.LOKIS_HORNS.getId())).toList();
    }

    private static void fire(PlayerMomsKnifeGameTests.Fixture f) {
        var charge = (IChargeableAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.ability.setChargeAmount(charge.getChargeBarId(), charge.getTotalCharge(f.owner));
        AttackExecutor.perform(AttackRequest.generated(f.owner, ModAttackTypes.MOMS_KNIFE.get(),
                AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false));
        f.ability.clearChargeAmount(charge.getChargeBarId());
    }
}
