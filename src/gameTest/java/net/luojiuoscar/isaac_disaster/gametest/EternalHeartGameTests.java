package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.EternalHeartPunish;
import net.luojiuoscar.isaac_disaster.system.heart.EternalHeartState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EternalHeartGameTests {
    private EternalHeartGameTests() {
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void whiteHeartUsesInternalState(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        double maxHealth = player.getMaxHealth();

        ModExecutableEffects.ETERNAL_HEART.get().apply(new ExecutableEffectContext(player));

        helper.assertTrue(EternalHeartState.has(player), "The player has a pending white heart");

        for (int tick = 0; tick < EternalHeartState.DURATION_TICKS; tick++) {
            EternalHeartState.tick(player);
        }
        helper.assertTrue(!EternalHeartState.has(player), "The pending white heart is consumed at the deadline");
        helper.assertTrue(player.getMaxHealth() > maxHealth, "The deadline grants a permanent health unit");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void whiteHeartBlocksDamageWithoutAbsorption(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        EternalHeartState.grant(player);
        LivingHurtEvent hurt = new LivingHurtEvent(player, player.damageSources().generic(), 4.0f);
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, hurt);

        new EternalHeartPunish().applyEffect(context);

        helper.assertTrue(hurt.getAmount() == 0.0f, "White heart blocks qualifying damage");
        helper.assertTrue(!EternalHeartState.has(player), "Blocked damage consumes the white heart");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void whiteHeartWaitsWhileAbsorptionIsPresent(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        EternalHeartState.grant(player);
        player.setAbsorptionAmount(1.0f);
        LivingHurtEvent hurt = new LivingHurtEvent(player, player.damageSources().generic(), 4.0f);
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, hurt);

        new EternalHeartPunish().applyEffect(context);

        helper.assertTrue(hurt.getAmount() == 4.0f, "Absorption keeps the white heart from triggering");
        helper.assertTrue(EternalHeartState.has(player), "Absorption does not consume the white heart");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void secondWhiteHeartConvertsImmediately(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        double maxHealth = player.getMaxHealth();
        ExecutableEffectContext context = new ExecutableEffectContext(player);

        ModExecutableEffects.ETERNAL_HEART.get().apply(context);
        ModExecutableEffects.ETERNAL_HEART.get().apply(context);

        helper.assertTrue(!EternalHeartState.has(player), "Two white hearts do not leave a pending state");
        helper.assertTrue(player.getMaxHealth() > maxHealth, "Two white hearts convert to one health unit");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "eternal_heart_test"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        return player;
    }
}
