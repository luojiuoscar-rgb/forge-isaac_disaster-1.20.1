package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EffectPriorityGameTests {
    private static final ResourceLocation MIXED = id("mixed");
    private static final ResourceLocation ADD = id("add");
    private static final ResourceLocation CANCEL = id("cancel");
    private static final ResourceLocation CONDITIONAL = id("conditional");
    private static final ResourceLocation MUTATE = id("mutate");

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "priority_test_" + name);
    }

    private static TestEvent event(ExecutableEffectContext context) {
        return (TestEvent) context.get(ContextKeys.EVENT);
    }

    private static SimpleTrigger trigger(double priority, Consumer<ExecutableEffectContext> action,
                                         Predicate<ExecutableEffectContext> condition) {
        IExecutableEffect effect = new IExecutableEffect() {
            @Override
            public double getPriority() { return priority; }

            @Override
            public void apply(ExecutableEffectContext context) { action.accept(context); }
        };
        return new SimpleTrigger(ModTriggerTypes.ATTACK_PLAN, () -> effect, condition);
    }

    private static TriggerModule module(SimpleTrigger... triggers) {
        return new TriggerModule(new CompositeTrigger(List.of(triggers))) {};
    }

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModTriggerModules.TRIGGER_MODULE_KEY, helper -> {
                helper.register(MIXED, module(
                        trigger(100, context -> event(context).trace.add("high"), null),
                        trigger(-100, context -> {
                            var current = event(context);
                            current.amount *= context.get(ContextKeys.AMPLIFIER);
                            current.trace.add("multiply:" + context.get(ContextKeys.AMPLIFIER));
                        }, context -> event(context).amount == 4)));
                helper.register(ADD, module(trigger(0, context -> {
                    var current = event(context);
                    current.amount += context.get(ContextKeys.AMPLIFIER);
                    current.trace.add("add:" + context.get(ContextKeys.AMPLIFIER));
                }, null)));
                helper.register(CANCEL, module(trigger(100, context -> event(context).setCanceled(true), null)));
                helper.register(CONDITIONAL, module(
                        trigger(0, context -> event(context).trace.add("conditional"),
                                context -> !event(context).isCanceled()),
                        trigger(-100, context -> event(context).trace.add("unconditional"), null)));
                helper.register(MUTATE, module(trigger(100, context -> {
                    queue((ServerPlayer) context.getEntity()).remove(ADD);
                    var nested = new ExecutableEffectContext(context.getEntity());
                    var nestedEvent = new TestEvent();
                    nested.set(ContextKeys.EVENT, nestedEvent);
                    // A nested dispatch uses its own snapshot, contexts and execution list.
                    TriggerModuleEvents.dispatch(nested, ModTriggerTypes.BREAK_BLOCK);
                    event(context).trace.add("mutate");
                }, null)));
            });
        }
    }

    @Cancelable
    public static final class TestEvent extends Event {
        double amount = 2;
        final List<String> trace = new ArrayList<>();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "effect_priority"));
        queue(player).clear();
        return player;
    }

    private static TriggerModuleQueue queue(ServerPlayer player) {
        return player.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new)
                .getTriggerModules();
    }

    private static TestEvent dispatch(ServerPlayer player) {
        var event = new TestEvent();
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, event);
        TriggerModuleEvents.dispatch(context, ModTriggerTypes.ATTACK_PLAN);
        return event;
    }

    @GameTest(template = "trajectory_empty")
    public static void effectsInterleaveAcrossModulesWithTheirOwnAmplifiers(GameTestHelper helper) {
        for (boolean reverse : List.of(false, true)) {
            var player = player(helper);
            var queue = queue(player);
            if (reverse) queue.add(ADD, 2);
            queue.add(MIXED, 3);
            if (!reverse) queue.add(ADD, 2);
            var event = dispatch(player);
            helper.assertTrue(event.amount == 12, "Addition precedes multiplication regardless of module order");
            helper.assertTrue(event.trace.equals(List.of("high", "add:2.0", "multiply:3.0")),
                    "Each invocation keeps its module amplifier and evaluates its condition after addition");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void cancellationIsVisibleToLaterConditionsWithoutImplicitGlobalSkip(GameTestHelper helper) {
        var player = player(helper);
        queue(player).add(CONDITIONAL, 1);
        queue(player).add(CANCEL, 1);
        var event = dispatch(player);
        helper.assertTrue(event.isCanceled(), "High-priority cancellation ran first");
        helper.assertTrue(event.trace.equals(List.of("unconditional")),
                "Conditions see cancellation, while intentional unconditional effects still run");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void nestedDispatchAndModuleMutationDoNotRewriteTheCurrentBatch(GameTestHelper helper) {
        var player = player(helper);
        queue(player).add(ADD, 2);
        queue(player).add(MUTATE, 1);
        var event = dispatch(player);
        helper.assertTrue(event.amount == 4 && event.trace.equals(List.of("mutate", "add:2.0")),
                "Already collected effects retain the original snapshot");
        helper.assertTrue(dispatch(player).amount == 2, "The next dispatch observes removal");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void inventoryRoundTripStoresNoPriorityAndKeepsStableOrder(GameTestHelper helper) {
        var player = player(helper);
        var queue = queue(player);
        queue.add(ADD, 1);
        queue.add(MIXED, 3);
        queue.add(ADD, 1);
        var saved = queue.saveNBTData();
        for (Tag value : saved) {
            helper.assertTrue(!((CompoundTag) value).contains("priority"), "Priority is effect metadata, not saved module state");
        }
        var loaded = new TriggerModuleQueue();
        loaded.loadNBTData(saved);
        helper.assertTrue(loaded.snapshot().modules().equals(queue.snapshot().modules()), "Counts and insertion order round-trip");
        helper.assertTrue(loaded.snapshot().modules().get(0).id().equals(ADD), "Changing stacks retains equal-priority tie order");
        helper.succeed();
    }
}
