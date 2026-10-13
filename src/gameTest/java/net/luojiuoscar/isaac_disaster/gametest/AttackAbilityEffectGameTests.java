package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.minecraftforge.eventbus.api.Event;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AttackAbilityEffectGameTests {
    @GameTest(template = "trajectory_empty")
    public static void invalidInputsFailAndOrdinarySkipsSucceed(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        List<IAbilityEffect> eventEffects = List.of(new BrimstonePlusMonstrosLung(),
                new FiringModifierAttackPlan(),
                new TheWizAttackPlan(), new LeadPencilShotCounter());
        var invalidEntity = new ExecutableEffectContext(new Zombie(helper.getLevel()));
        for (var effect : eventEffects) {
            helper.assertTrue(!effect.applyEffect(invalidEntity), "Wrong entity is an invalid execution input");
            helper.assertTrue(!effect.applyEffect(new ExecutableEffectContext(player)), "Missing required event fails");
        }
        helper.assertTrue(!new LeadPencilBurst().applyEffect(invalidEntity), "Burst requires a server player");

        var context = new ExecutableEffectContext(player);
        var extraPlan = plan(player, ModAttackTypes.BULLET.get(), AttackOrigin.ABILITY_EXTRA);
        context.set(ContextKeys.EVENT, extraPlan);
        helper.assertTrue(new BrimstonePlusMonstrosLung().applyEffect(context), "Ineligible beam plan is safely skipped");
        helper.assertTrue(new FiringModifierAttackPlan().applyEffect(context), "Derived plan is safely skipped");
        helper.assertTrue(new TheWizAttackPlan().applyEffect(context), "Missing Wiz status is safely skipped");
        helper.assertTrue(new LeadPencilShotCounter().applyEffect(context), "Derived plan is not counted");
        helper.assertTrue(extraPlan.getExtraContexts().isEmpty(), "Skipped beam effect appends nothing");
        var data = player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).orElseThrow(IllegalStateException::new);
        helper.assertTrue(!data.hasDouble(LeadPencilShotCounter.DATA_KEY), "Skipped counter does not create progress");

        context.set(ContextKeys.EVENT, new Event());
        for (var effect : List.of(new BrimstonePlusMonstrosLung(), new FiringModifierAttackPlan(),
                new TheWizAttackPlan(), new LeadPencilShotCounter())) {
            helper.assertTrue(!effect.applyEffect(context), "Wrong event type fails");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void validEventEffectsKeepAttackBehavior(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var context = new ExecutableEffectContext(player);
        var primary = plan(player, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY);
        context.set(ContextKeys.EVENT, primary);
        helper.assertTrue(new FiringModifierAttackPlan().applyEffect(context), "Valid multishot execution succeeds");
        helper.assertTrue(primary.getBaseContexts().size() == 1, "No multishot items preserves one projectile");
        helper.assertTrue(new LeadPencilShotCounter().applyEffect(context), "Counting below threshold succeeds");
        var data = player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).orElseThrow(IllegalStateException::new);
        helper.assertTrue(data.getDouble(LeadPencilShotCounter.DATA_KEY) == 1, "One primary round advances progress once");

        player.addEffect(new MobEffectInstance(ModEffects.THE_WIZ.get(), 200, 1));
        helper.assertTrue(new TheWizAttackPlan().applyEffect(context), "Valid Wiz execution succeeds");
        helper.assertTrue(primary.getBaseContexts().size() == 2, "Amplified Wiz retains dual spread");

        var brimstone = plan(player, ModAttackTypes.BRIMSTONE.get(), AttackOrigin.PLAYER_PRIMARY);
        context.set(ContextKeys.EVENT, brimstone);
        helper.assertTrue(new BrimstonePlusMonstrosLung().applyEffect(context), "Valid beam combination succeeds");
        int count = brimstone.getExtraContexts().size();
        helper.assertTrue(count >= 3 && count <= 5, "Combination retains three to five extra beams");
        helper.succeed();
    }

    private static AttackPlanEvent plan(ServerPlayer player, AttackType type, AttackOrigin origin) {
        var request = AttackRequest.generated(player, type, origin, AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false);
        return new AttackPlanEvent(request, new AttackPlan(request, List.of(type.createAttackContext(player, player))));
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ability-effect-test"));
    }
}
