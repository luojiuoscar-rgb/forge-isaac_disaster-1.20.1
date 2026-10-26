package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.*;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LeadPencilShotCounter;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LeadPencilGameTests {
    @GameTest(template = "trajectory_empty")
    public static void burstFiresDirectlyWithoutReadingOrChangingShotProgress(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ResourceLocation counterKey = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "lead_pencil_shot_count");
        data(player).setDouble(counterKey, 7);
        List<AttackContext> burst = new ArrayList<>();
        Consumer<AttackContextPrepareEvent> listener = event -> {
            if (event.getOwner() != player || event.getOrigin() != AttackOrigin.ABILITY_EXTRA) return;
            burst.add(event.getAttackContext());
            event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, listener);
        try {
            // A standalone burst needs only its owner, without a primary attack-plan event.
            var context = new ExecutableEffectContext(player);
            helper.assertTrue(((IAbilityEffect) ModExecutableEffects.LEAD_PENCIL_BURST.get()).applyEffect(context),
                    "Cancelled individual tears still complete the burst successfully");
            helper.assertTrue(burst.size() == 12, "Direct burst call fires twelve ordinary tears immediately");
            helper.assertTrue(data(player).getDouble(counterKey) == 7, "Burst leaves counting state unchanged");
            data(player).removeDouble(counterKey);
            helper.assertTrue(((IAbilityEffect) ModExecutableEffects.LEAD_PENCIL_BURST.get()).applyEffect(context),
                    "Standalone burst succeeds without counter state");
            helper.assertTrue(burst.size() == 24, "Burst also fires without any counter state");
            helper.assertTrue(!data(player).hasDouble(counterKey), "Burst does not create counting state");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void countsRoundsAndPreparesFreshOrdinaryTears(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ModPassiveAbilities.LEAD_PENCIL.get().handleObtain(player, null);
        ModPassiveAbilities.LEAD_PENCIL.get().handleObtain(player, null);
        List<AttackContext> burst = new ArrayList<>();
        Consumer<AttackContextPrepareEvent> listener = event -> {
            if (event.getOwner() == player && event.getOrigin() == AttackOrigin.ABILITY_EXTRA) {
                helper.assertTrue(event.getAttackType() == ModAttackTypes.BULLET.get(), "Burst always uses ordinary tears");
                helper.assertTrue(event.getPipelineMode() == AttackPipelineMode.PREPARE_AND_EXECUTE, "Burst prepares each projectile");
                burst.add(event.getAttackContext());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, listener);
        try {
            var reference = ModAttackTypes.BULLET.get().createAttackContext(player, player);
            for (int round = 1; round <= 30; round++) {
                AttackType primary = switch (round % 4) {
                    case 0 -> ModAttackTypes.BRIMSTONE.get();
                    case 1 -> ModAttackTypes.C_SECTION.get();
                    case 2 -> ModAttackTypes.MONSTROS_LUNG.get();
                    default -> ModAttackTypes.BULLET.get();
                };
                publish(player, primary, AttackOrigin.PLAYER_PRIMARY, 20);
                helper.assertTrue(burst.size() == (round / 15) * 12, "Exactly twelve extra tears per fifteen rounds");
                helper.assertTrue(data(player).getDouble(LeadPencilShotCounter.DATA_KEY) == round % 15, "One count per round regardless of copies and multishot");
            }
            for (var tear : burst) {
                helper.assertTrue(tear.getDamage() == reference.getDamage(), "Full ordinary tear damage, independent of primary weapon");
                helper.assertTrue(tear.getBulletRange() == reference.getBulletRange(), "Full ordinary tear range");
                helper.assertTrue(tear.getBulletSpeed() == reference.getBulletSpeed(), "Ordinary tear speed");
                helper.assertTrue(tear.getBulletScale() == reference.getBulletScale(), "No lung size randomization");
                helper.assertTrue(tear.getMainAxis().dot(reference.getMainAxis()) >= Math.cos(Math.toRadians(15)) - 1e-12, "Fifteen-degree cone around current aim");
                helper.assertTrue(tear.getColorRl().equals(ModBulletColors.BLOOD_TEAR.getId()), "Unmodified burst is blood red");
                helper.assertTrue(tear.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId())), "Burst carries gravity");
            }
            for (AttackOrigin origin : List.of(AttackOrigin.ABILITY_EXTRA, AttackOrigin.SPLIT_CHILD, AttackOrigin.BULLET_SECONDARY, AttackOrigin.SYSTEM)) {
                publish(player, ModAttackTypes.BULLET.get(), origin, 12);
            }
            publish(player, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY, 0);
            helper.assertTrue(data(player).getDouble(LeadPencilShotCounter.DATA_KEY) == 4, "Every firing origin counts while empty plans do not");
            helper.assertTrue(burst.size() == 24, "No recursive barrage");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void progressPersistsAndLastRemovalClearsIt(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var ability = ModPassiveAbilities.LEAD_PENCIL.get();
        ability.handleObtain(player, null);
        ability.handleObtain(player, null);
        for (int i = 0; i < 9; i++) publish(player, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY, 1);
        CompoundTag saved = new CompoundTag();
        data(player).saveNBTData(saved);
        ExtraData reloaded = new ExtraData();
        reloaded.loadNBTData(saved);
        ServerPlayer clone = player(helper);
        ability.handleObtain(clone, null);
        data(clone).copyFrom(reloaded);
        publish(clone, ModAttackTypes.C_SECTION.get(), AttackOrigin.PLAYER_PRIMARY, 4);
        helper.assertTrue(data(clone).getDouble(LeadPencilShotCounter.DATA_KEY) == 10, "Save/load and clone retain shared weapon progress");
        ability.handleRemove(player, null);
        helper.assertTrue(data(player).getDouble(LeadPencilShotCounter.DATA_KEY) == 9, "Removing a non-last copy keeps progress");
        ability.handleRemove(player, null);
        helper.assertTrue(!data(player).hasDouble(LeadPencilShotCounter.DATA_KEY), "Last removal clears owned progress");
        ability.handleObtain(player, null);
        publish(player, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY, 1);
        helper.assertTrue(data(player).getDouble(LeadPencilShotCounter.DATA_KEY) == 1, "Re-obtain starts from zero");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void burstRollsPoisonIndependentlyAndRetainsSplitAndOrbitModules(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ModPassiveAbilities.LEAD_PENCIL.get().handleObtain(player, null);
        var modules = player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .orElseThrow(IllegalStateException::new).getTriggerModules();
        for (var module : List.of(ModTriggerModules.THE_COMMON_COLD, ModTriggerModules.HAEMOLACRIA,
                ModTriggerModules.CRICKETS_BODY, ModTriggerModules.PARASITE, ModTriggerModules.COMPOUND_FRACTURE,
                ModTriggerModules.TINY_PLANET)) modules.add(module.getId(), 1);
        List<AttackContext> burst = new ArrayList<>();
        Consumer<AttackContextPrepareEvent> listener = event -> {
            if (event.getOwner() != player || event.getOrigin() != AttackOrigin.ABILITY_EXTRA) return;
            burst.add(event.getAttackContext());
            event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, listener);
        try {
            for (int i = 0; i < 20; i++) {
                data(player).setDouble(LeadPencilShotCounter.DATA_KEY, 14);
                publish(player, ModAttackTypes.C_SECTION.get(), AttackOrigin.PLAYER_PRIMARY, 1);
            }
            helper.assertTrue(burst.size() == 240, "Each threshold produces twelve independently prepared tears");
            long poison = burst.stream().filter(tear -> tear.getColorRl().equals(ModBulletColors.POISON.getId())).count();
            helper.assertTrue(poison > 0 && poison < burst.size(), "Poison is independently rolled rather than inherited");
            for (var tear : burst) {
                for (var split : List.of(ModSplitModules.HAEMOLACRIA, ModSplitModules.CRICKETS_BODY,
                        ModSplitModules.PARASITE, ModSplitModules.COMPOUND_FRACTURE)) {
                    helper.assertTrue(tear.getSplitSequence().contains(split.getId()), "Existing split modifier attaches to burst");
                }
                helper.assertTrue(tear.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.TINY_PLANET_BULLET.getId())), "Existing orbit attaches");
                helper.assertTrue(tear.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId())), "Gravity remains attached");
                var resolved = tear.toBuilder().build();
                resolved.freeze();
                helper.assertTrue(resolved.getTrajectorySpecs().stream().anyMatch(spec -> spec.id().equals(ModTrajectoryModules.TINY_PLANET_BULLET.getId())), "Orbit survives trajectory finalization");
                helper.assertTrue(resolved.getTrajectorySpecs().stream().noneMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId())), "Existing orbit precedence replaces gravity");
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void wholeAttackAndAllPreparedTearCancellationSkipCount(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ModPassiveAbilities.LEAD_PENCIL.get().handleObtain(player, null);
        Consumer<BeforePerformAttackEvent> cancelAttack = event -> {
            if (event.getEntity() == player) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BeforePerformAttackEvent.class, cancelAttack);
        try {
            AttackExecutor.perform(AttackRequest.generated(player, ModAttackTypes.BULLET.get(),
                    AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false));
            helper.assertTrue(!data(player).hasDouble(LeadPencilShotCounter.DATA_KEY), "Whole cancelled request never counts");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancelAttack);
        }
        Consumer<AttackContextPrepareEvent> cancelTear = event -> {
            if (event.getOwner() == player) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, AttackContextPrepareEvent.class, cancelTear);
        try {
            AttackExecutor.perform(AttackRequest.generated(player, ModAttackTypes.BULLET.get(),
                    AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false));
            helper.assertTrue(!data(player).hasDouble(LeadPencilShotCounter.DATA_KEY), "All prepared tears cancelled means nothing actually fired");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancelTear);
        }
        helper.succeed();
    }

    private static void publish(ServerPlayer player, AttackType type, AttackOrigin origin, int count) {
        var request = AttackRequest.generated(player, type, origin, AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false);
        List<AttackContext> base = new ArrayList<>();
        for (int i = 0; i < count; i++) base.add(type.createAttackContext(player, player));
        base.forEach(AttackContext::freeze);
        if (base.isEmpty()) MinecraftForge.EVENT_BUS.post(new AttackPlanEvent(request, new AttackPlan(request, base)));
        else MinecraftForge.EVENT_BUS.post(new AttackFiredEvent(request, base, base.get(0), 0));
    }

    private static ExtraData data(ServerPlayer player) {
        return player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).orElseThrow(IllegalStateException::new);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "lead-pencil-test"));
    }
}
