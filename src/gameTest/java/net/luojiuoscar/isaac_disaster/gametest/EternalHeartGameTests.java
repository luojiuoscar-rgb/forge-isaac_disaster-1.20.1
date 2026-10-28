package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.EternalHeartPunish;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
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
    public static void whiteHeartUsesPotionEffect(GameTestHelper helper) {
        ServerPlayer player = player(helper);

        ModExecutableEffects.ETERNAL_HEART.get().apply(new ExecutableEffectContext(player));

        MobEffectInstance effect = player.getEffect(ModEffects.ETERNAL_HEART.get());
        helper.assertTrue(effect != null, "The player receives the eternal-heart potion effect");
        helper.assertTrue(effect.getDuration() == 3600, "The eternal-heart duration is 3600 ticks");
        helper.assertTrue(effect.getAmplifier() == 0, "The eternal-heart amplifier is zero");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void whiteHeartBlocksDamageWithoutAbsorption(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(ModEffects.ETERNAL_HEART.get(), 3600, 0));
        LivingHurtEvent hurt = new LivingHurtEvent(player, player.damageSources().generic(), 4.0f);
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, hurt);

        new EternalHeartPunish().applyEffect(context);

        helper.assertTrue(hurt.getAmount() == 0.0f, "White heart blocks qualifying damage");
        helper.assertTrue(player.getEffect(ModEffects.ETERNAL_HEART.get()) == null, "Blocked damage consumes the white heart");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void whiteHeartWaitsWhileAbsorptionIsPresent(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(ModEffects.ETERNAL_HEART.get(), 3600, 0));
        player.setAbsorptionAmount(1.0f);
        LivingHurtEvent hurt = new LivingHurtEvent(player, player.damageSources().generic(), 4.0f);
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, hurt);

        new EternalHeartPunish().applyEffect(context);

        helper.assertTrue(hurt.getAmount() == 4.0f, "Absorption keeps the white heart from triggering");
        helper.assertTrue(player.getEffect(ModEffects.ETERNAL_HEART.get()) != null, "Absorption does not consume the white heart");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "eternal_heart")
    public static void expiredWhiteHeartConvertsToMaxHealth(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        double maxHealth = player.getMaxHealth();
        player.addEffect(new MobEffectInstance(ModEffects.ETERNAL_HEART.get(), 11, 0));

        // FakePlayer.tick() is empty; doTick() advances the inherited living-entity effects.
        player.doTick();
        MobEffectInstance pending = player.getEffect(ModEffects.ETERNAL_HEART.get());
        helper.assertTrue(pending != null && pending.getDuration() == 10,
                "White heart remains until its final scheduled effect tick");
        helper.assertTrue(player.getMaxHealth() == maxHealth,
                "White heart does not convert before its final scheduled effect tick");

        player.doTick();

        helper.assertTrue(player.getEffect(ModEffects.ETERNAL_HEART.get()) == null, "The expired white heart is removed");
        helper.assertTrue(Math.abs(player.getMaxHealth() - maxHealth - StatManager.MAX_HEALTH.getBonus()) < 0.01,
                "The expired white heart grants exactly one permanent health unit");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "eternal_heart_test"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        return player;
    }
}
