package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class PlayerMomsKnifeMultishotGameTests {
    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void stableItemCountsReachTheAttributeBeforeAttackReads(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            var items = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new);
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(1);
            var stack = new ItemStack(ModPassiveItems.THE_INNER_EYE.get());
            ((PassiveAbility) ((PassiveItem) stack.getItem()).getAbility()).onObtain(f.owner, stack);
            helper.assertTrue(f.owner.getAttributeValue(ModAttributes.BULLET_COUNT.get()) == 3,
                    "The item itself updates multishot before its pending copy enters storage");
            items.addItem(f.owner, stack);
            int actual = ModAttackTypes.BULLET.get().getBulletCount(f.owner);
            helper.assertTrue(actual == 3, "Inner Eye must produce three through the shared count attribute: " + actual);
            helper.assertTrue(f.owner.getAttributeValue(ModAttributes.BULLET_COUNT.get()) == 3,
                    "The stable bonus must be observable without executing an attack plan");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void holdingAndLaunchingFourKnivesDoesNotStopAfterTheFirst(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(4);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            attack.onTick(f.owner);
            helper.assertTrue(f.knives().size() == 4, "Four knives must be visible before firing: " + f.knives().size());
            f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
            attack.performAttack(attack.getAttackContexts(f.owner, 4));
            helper.assertTrue(f.knives().stream().filter(PlayerMomsKnifeEntity::isInFlight).count() == 4,
                    "A single release launches every prepared context in the same tick");
            helper.assertTrue(!attack.isChargeEligible(f.owner), "The entire volley gates charging");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void twoKnivesHaveParallelAxesAndHalfABlockBetweenTheirHandles(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            var contexts = ModAttackTypes.MOMS_KNIFE.get().getAttackContexts(f.owner, 2);
            helper.assertTrue(contexts.size() == 2, "Two contexts are generated instead of silently ignoring count");
            helper.assertTrue(contexts.get(0).getMainAxis().distanceTo(contexts.get(1).getMainAxis()) < 1e-8,
                    "Dual knives are parallel");
            helper.assertTrue(Math.abs(contexts.get(0).getPos().distanceTo(contexts.get(1).getPos()) - 0.5) < 1e-8,
                    "Each handle is offset by a quarter block");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void allHeldCountsAndVerticalPosesUseDistinctIndexedSlots(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            f.owner.setYRot(37);
            for (float pitch : new float[]{0, 45, -90, 90}) {
                f.owner.setXRot(pitch);
                Vec3 pivot = f.owner.getEyePosition().add(0, -0.15 * f.owner.getBbHeight(), 0);
                double radius = Math.max(0.6, f.owner.getBbWidth());
                for (int count : new int[]{1, 2, 3, 4, 16, 80, 1}) {
                    f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(count);
                    attack.onTick(f.owner);
                    var knives = HeldMomsKnifeGroup.knives(f.owner);
                    helper.assertTrue(knives.size() == Math.min(count, 16), "The held count is clamped and refreshed: " + count);
                    for (var knife : knives) {
                        helper.assertTrue(knife.getUUID().equals(f.ability.getRuntimeEntities().getId(
                                ModAttackTypes.MOMS_KNIFE.getId(), knife.getHeldSlot())), "Each knife owns its own slot");
                        var expected = attack.getAttackContexts(f.owner, count).get(knife.getHeldSlot());
                        Vec3 grip = knife.getRenderAnchor(f.owner, 1);
                        Vec3 direction = knife.getRenderDirection(f.owner, 1);
                        helper.assertTrue(grip.distanceTo(expected.getPos()) < 1e-5,
                                "Rendering, collision and launch share the same handle; count=" + count
                                        + ", slot=" + knife.getHeldSlot() + ", pitch=" + pitch);
                        helper.assertTrue(direction.distanceTo(expected.getMainAxis()) < 1e-5,
                                "The held fan remains well-defined at every pitch");
                        if (count >= 3) {
                            Vec3 radial = grip.subtract(pivot);
                            helper.assertTrue(Math.abs(radial.length() - radius) < 1e-4,
                                    "Every fan handle stays the original holding distance from the player");
                            helper.assertTrue(radial.normalize().dot(direction) > 1 - 1e-6,
                                    "A fan blade points outward from the player through its own handle");
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void countChangesWaitForEveryReturningKnifeBeforeRebuildingAndCharging(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        helper.onEachTick(() -> {
            if (!f.closed) HeldMomsKnifeGroup.knives(f.owner).forEach(PlayerMomsKnifeEntity::tick);
        });
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(4);
        attack.onTick(f.owner);
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        var contexts = attack.getAttackContexts(f.owner, 4).stream()
                .map(c -> c.toBuilder().range(2).speed(1).build()).toList();
        attack.performAttack(contexts);
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
        f.ability.setHoldRightClick(true);
        attack.onTick(f.owner);
        helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 4, "Flight freezes the number and layout");
        helper.assertTrue(f.ability.getChargeAmount(attack.getChargeBarId()) == 0, "Flight keeps charge hidden and zero");
        helper.runAfterDelay(6, () -> {
            try {
                attack.onTick(f.owner);
                helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 2, "The new stable count applies after return");
                helper.assertTrue(f.ability.getChargeAmount(attack.getChargeBarId()) > 0, "Held right click starts the next charge");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void authoritativeGripUsesTheSameCurrentLookAsTheLocalFan(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.setYRot(63);
            f.owner.setYHeadRot(-47);
            f.owner.setXRot(28);
            Vec3 expected = f.owner.getEyePosition().add(0, -0.15 * f.owner.getBbHeight(), 0)
                    .add(f.owner.getLookAngle().scale(Math.max(0.6, f.owner.getBbWidth())));
            helper.assertTrue(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).distanceTo(expected) < 1e-6,
                    "The authoritative grip follows input look even while head yaw is interpolating");
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            attack.onTick(f.owner);
            for (var knife : HeldMomsKnifeGroup.knives(f.owner)) {
                var context = attack.getAttackContexts(f.owner, 2).get(knife.getHeldSlot());
                helper.assertTrue(knife.getRenderAnchor(f.owner, 1).distanceTo(context.getPos()) < 1e-6,
                        "Held and attack contexts share their authoritative anchor");
                helper.assertTrue(knife.getRenderDirection(f.owner, 1).distanceTo(f.owner.getLookAngle().normalize()) < 1e-4,
                        "Both parallel blades use the same input direction");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void wizAddsTemporaryKnivesOnlyOnReleaseAndRestoresTheSingleHeldKnife(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(2);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(2);
        f.owner.addEffect(new MobEffectInstance(ModEffects.THE_WIZ.get(), 200, 1));
        f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                modules -> modules.getTriggerModules().addIfNotExist(ModTriggerModules.PLAYER_PERMANENT_MODULE.getId(), 1));
        attack.onTick(f.owner);
        helper.assertTrue(f.knives().size() == 1 && f.main.getRenderDirection(f.owner, 1).distanceTo(f.owner.getLookAngle()) < 1e-6,
                "Wiz leaves held knives unchanged; count=" + f.knives().size() + ", direction="
                        + f.main.getRenderDirection(f.owner, 1) + ", look=" + f.owner.getLookAngle());
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        var launched = HeldMomsKnifeGroup.knives(f.owner);
        helper.assertTrue(launched.size() == 2 && launched.stream().allMatch(PlayerMomsKnifeEntity::isInFlight),
                "The real attack-plan pipeline emits both Wiz directions");
        for (var knife : launched) helper.assertTrue(Math.abs(Math.abs(knife.getRenderDirection(f.owner, 1).x) - Math.sqrt(0.5)) < 1e-5,
                "A launched knife keeps its planned forty-five-degree heading");
        helper.runAfterDelay(4, () -> {
            try {
                attack.onTick(f.owner);
                helper.assertTrue(f.knives().size() == 1, "Temporary Wiz knives are removed after all knives return");
                helper.assertTrue(f.main.getRenderDirection(f.owner, 1).distanceTo(f.owner.getLookAngle()) < 1e-5,
                        "The original held direction is restored");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 20)
    public static void randomCountEventsAreOnlyEvaluatedOnceWhenReleasing(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(2);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(2);
        AtomicInteger rolls = new AtomicInteger();
        Consumer<IsaacGetBulletCountEvent> listener = event -> {
            if (event.getPlayer() == f.owner) { rolls.incrementAndGet(); event.setCount(event.getCount() + 1); }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        for (int i = 0; i < 5; i++) attack.onTick(f.owner);
        helper.assertTrue(rolls.get() == 0 && f.knives().size() == 1, "Idle previews do not consume random count events");
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
        helper.assertTrue(rolls.get() == 1 && f.knives().size() == 2, "One release rolls once and adds the temporary knife");
        helper.runAfterDelay(4, () -> {
            try {
                attack.onTick(f.owner);
                helper.assertTrue(rolls.get() == 1 && f.knives().size() == 1, "Return restores the stable count without a new roll");
                helper.succeed();
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void cancellingOnePreparedContextDoesNotCancelOrBlockTheRest(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(3);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            attack.onTick(f.owner);
            Consumer<AttackContextPrepareEvent> listener = event -> {
                if (event.getOwner() == f.owner && event.getContextIndex() == 1) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try {
                f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
                attack.onReleased(f.owner);
                helper.assertTrue(f.knives().size() == 3 && f.knives().stream().filter(PlayerMomsKnifeEntity::isInFlight).count() == 2,
                        "Only the cancelled context stays held; the remaining two launch together");
            } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife", timeoutTicks = 25)
    public static void sixteenMainKnivesQueueOnlyOneBrimstoneBarrage(GameTestHelper helper) {
        var f = new PlayerMomsKnifeGameTests.Fixture(helper);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
        var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
        attack.onTick(f.owner);
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.performAttack(attack.getAttackContexts(f.owner, 16).stream()
                .map(c -> c.toBuilder().range(2).speed(1).build()).toList());
        helper.runAfterDelay(1, () -> helper.assertTrue(f.knives().stream().noneMatch(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY),
                "The barrage still waits for the turnaround"));
        helper.runAfterDelay(15, () -> {
            try {
                long extras = f.knives().stream().filter(k -> k.getMode() == PlayerMomsKnifeEntity.Mode.ONE_WAY).count();
                helper.assertTrue(extras == 10, "Sixteen main knives must add ten extras, not one hundred sixty: " + extras);
                helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).size() == 16, "Barrage knives never occupy held slots");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void dualHeldDamageStacksAndAllSlotsAreRemovedTogether(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(2);
            var attack = (MomsKnifeAttack) ModAttackTypes.MOMS_KNIFE.get();
            attack.onTick(f.owner);
            var target = f.cow(PlayerMomsKnifeMotion.idlePosition(f.owner, 1).add(new Vec3(0, 0, 0.5)));
            var knives = HeldMomsKnifeGroup.knives(f.owner);
            knives.forEach(PlayerMomsKnifeEntity::tick);
            helper.assertTrue(Math.abs(target.getHealth() - 996) < 1e-5,
                    "Each touching knife independently deals two damage in the same tick: " + target.getHealth());
            f.ability.getRuntimeEntities().discardAll(f.level);
            helper.assertTrue(knives.stream().allMatch(PlayerMomsKnifeEntity::isRemoved), "Discarding runtime entities clears every held slot");
            helper.assertTrue(HeldMomsKnifeGroup.knives(f.owner).isEmpty(), "No stale held references survive cleanup");
        }
        helper.succeed();
    }
}
