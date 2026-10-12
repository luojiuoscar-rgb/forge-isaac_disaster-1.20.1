package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DefaultAttributeGameTests {
    @GameTest(template = "trajectory_empty", batch = "default_attributes")
    public static void freshPlayerStartsWithAllCanonicalDefaultsBeforeLogin(GameTestHelper helper) {
        assertDefaultAttributes(helper, player(helper));
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "default_attributes")
    public static void applyingDefaultConfigKeepsAllFreshPlayerDefaults(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        DefaultAttributeManager.apply(player);
        DefaultAttributeManager.apply(player);
        assertDefaultAttributes(helper, player);
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "default_attributes")
    public static void rangeIncrementUsesConfiguredBonusFromFreshDefault(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        assertValue(helper, player, ModAttributes.BULLET_RANGE.get(), 12.0);
        double originalBonus = Config.RANGE_BONUS.get();
        try {
            Config.RANGE_BONUS.set(3.0);
            StatManager.RANGE.apply(player, 1.0);
            assertValue(helper, player, ModAttributes.BULLET_RANGE.get(), 15.0);
            Config.RANGE_BONUS.set(5.0);
            StatManager.RANGE.apply(player, 1.0);
            assertValue(helper, player, ModAttributes.BULLET_RANGE.get(), 20.0);
        } finally {
            Config.RANGE_BONUS.set(originalBonus);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "default_attributes")
    public static void configuredDefaultsPreserveItemAndExternalModifiersOnReapplication(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        double originalRange = Config.DEFAULT_BULLET_RANGE.get();
        double originalReach = Config.DEFAULT_BLOCK_REACH.get();
        double originalBonus = Config.RANGE_BONUS.get();
        var range = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        UUID externalId = UUID.randomUUID();
        try {
            Config.DEFAULT_BULLET_RANGE.set(24.0);
            Config.DEFAULT_BLOCK_REACH.set(6.0);
            Config.RANGE_BONUS.set(3.0);
            StatManager.RANGE.apply(player, 1.0);
            range.addPermanentModifier(new AttributeModifier(externalId, "external test bonus", 2.0,
                    AttributeModifier.Operation.ADDITION));
            DefaultAttributeManager.apply(player);
            DefaultAttributeManager.apply(player);
            assertValue(helper, player, ModAttributes.BULLET_RANGE.get(), 29.0);
            assertValue(helper, player, ForgeMod.BLOCK_REACH.get(), 6.0);
            Config.DEFAULT_BULLET_RANGE.set(12.0);
            Config.DEFAULT_BLOCK_REACH.set(4.5);
            DefaultAttributeManager.apply(player);
            assertValue(helper, player, ModAttributes.BULLET_RANGE.get(), 17.0);
            assertValue(helper, player, ForgeMod.BLOCK_REACH.get(), 4.5);
            helper.assertTrue(range.getModifier(externalId) != null, "External modifiers remain installed");
        } finally {
            Config.DEFAULT_BULLET_RANGE.set(originalRange);
            Config.DEFAULT_BLOCK_REACH.set(originalReach);
            Config.RANGE_BONUS.set(originalBonus);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "default_stats"));
    }

    private static void assertDefaultAttributes(GameTestHelper helper, ServerPlayer player) {
        for (ExpectedAttribute expected : List.of(
                new ExpectedAttribute(Attributes.MAX_HEALTH, 20.0),
                new ExpectedAttribute(Attributes.MOVEMENT_SPEED, 0.1),
                new ExpectedAttribute(Attributes.ATTACK_DAMAGE, 1.0),
                new ExpectedAttribute(Attributes.LUCK, 0.0),
                new ExpectedAttribute(ModAttributes.SCALE.get(), 1.0),
                new ExpectedAttribute(ModAttributes.BULLET_RANGE.get(), 12.0),
                new ExpectedAttribute(ForgeMod.ENTITY_REACH.get(), 3.0),
                new ExpectedAttribute(ForgeMod.BLOCK_REACH.get(), 4.5),
                new ExpectedAttribute(ModAttributes.TEARS.get(), 0.0),
                new ExpectedAttribute(ModAttributes.TEARS_CORRECTION.get(), 0.0),
                new ExpectedAttribute(ModAttributes.BULLET_SPEED.get(), 1.0),
                new ExpectedAttribute(Attributes.ATTACK_SPEED, 4.0),
                new ExpectedAttribute(ModAttributes.BLOCK_BREAKING_SPEED.get(), 0.0),
                new ExpectedAttribute(Attributes.ATTACK_KNOCKBACK, 0.0),
                new ExpectedAttribute(ModAttributes.BULLET_SCALE.get(), 0.0),
                new ExpectedAttribute(ModAttributes.BULLET_COUNT.get(), 1.0),
                new ExpectedAttribute(ModAttributes.PILL_QUALITY.get(), 0.0),
                new ExpectedAttribute(ModAttributes.FLY_TIME.get(), 0.0))) {
            assertValue(helper, player, expected.attribute(), expected.value());
            double base = player.getAttribute(expected.attribute()).getBaseValue();
            helper.assertTrue(Math.abs(base - expected.value()) < 1.0e-6,
                    expected.attribute().getDescriptionId() + ": unexpected initial base " + base);
        }
    }

    private static void assertValue(GameTestHelper helper, ServerPlayer player, Attribute attribute, double expected) {
        double actual = player.getAttributeValue(attribute);
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6,
                attribute.getDescriptionId() + ": expected " + expected + ", got " + actual);
    }

    private record ExpectedAttribute(Attribute attribute, double value) {}
}
