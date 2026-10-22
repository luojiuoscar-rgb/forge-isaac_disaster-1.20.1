package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.D8;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BrimstoneAttack;
import net.luojiuoscar.isaac_disaster.system.rockbottom.RockBottomState;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FireRateGameTests {
    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void tearsChangesSynchronizeBaseWithoutConsumingFireRateModifiers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var tears = player.getAttribute(ModAttributes.TEARS.get());
        var rate = player.getAttribute(ModAttributes.FIRE_RATE.get());
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.FIRE_RATE_BONUS, 1.0);
            StatManager.FIRE_RATE.apply(player, 1.0);
            helper.assertTrue(rate.getModifier(StatManager.FIRE_RATE.getUUID()).getOperation()
                    == AttributeModifier.Operation.ADDITION, "Fire-rate bonus uses ADDITION");
            tears.setBaseValue(1.0);
            near(helper, rate.getBaseValue(), PlayerHelper.tearsToFireRate(1.0), "Base updates immediately");
            near(helper, rate.getValue(), PlayerHelper.tearsToFireRate(1.0) + 1.0, "Addition survives Tears update");
            UUID id = UUID.randomUUID();
            tears.addPermanentModifier(new AttributeModifier(id, "tears test", -2.0,
                    AttributeModifier.Operation.ADDITION));
            near(helper, rate.getBaseValue(), 20.0 / 15.0, "Tears modifiers update base immediately");
            tears.removeModifier(id);
            near(helper, rate.getBaseValue(), PlayerHelper.tearsToFireRate(1.0), "Removal restores base");
            var saved = tears.save();
            tears.setBaseValue(-1.0);
            tears.load(saved);
            near(helper, rate.getValue(), PlayerHelper.tearsToFireRate(1.0) + 1.0, "Load restores derived base");
            tears.setBaseValue(100.0);
            near(helper, rate.getValue(), 6.0, "Additions exceed the Tears conversion cap");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void defaultAdditionAndHasteUseTheSameAttribute(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        near(helper, ModAttributes.FIRE_RATE.get().getDefaultValue(), 0.0, "Registered FireRate base is zero");
        near(helper, DefaultAttributes.getSupplier(EntityType.PLAYER).getBaseValue(ModAttributes.FIRE_RATE.get()),
                0.0, "Player supplier FireRate base is zero");
        near(helper, player.getAttributeValue(ModAttributes.FIRE_RATE.get()), 20.0 / 7.0,
                "Fresh player derives actual FireRate from Tears before login");
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.DEFAULT_TEARS, 1.0);
            settings.set(GameplayConfig.DEFAULT_FIRE_RATE_ADDITION, 2.0);
            DefaultAttributeManager.apply(player);
            DefaultAttributeManager.apply(player);
            near(helper, PlayerHelper.getFireRate(player), PlayerHelper.tearsToFireRate(1.0) + 2.0,
                    "Defaults preserve the Tears-derived base and do not stack");
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
            near(helper, PlayerHelper.getFireRate(player), PlayerHelper.tearsToFireRate(1.0) + 4.0,
                    "Haste II adds two shots per second");
            player.removeEffect(MobEffects.DIG_SPEED);
            near(helper, PlayerHelper.getFireRate(player), PlayerHelper.tearsToFireRate(1.0) + 2.0,
                    "Haste removal restores FireRate");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate", timeoutTicks = 200)
    public static void everyFiringItemCombinationRestoresInEitherAcquisitionOrder(GameTestHelper helper) {
        List<PassiveAbility> abilities = List.of(new Polyphemus(0, 1), new TheInnerEye(0, 1),
                new MutantSpider(0, 1), new Ipecac(0, 1), new PerfectVision(0, 1), new Haemolacria(0, 1));
        for (int mask = 0; mask < 64; mask++) {
            for (boolean reverse : List.of(false, true)) {
                ServerPlayer player = player(helper);
                for (int offset = 0; offset < abilities.size(); offset++) {
                    int index = reverse ? abilities.size() - 1 - offset : offset;
                    if ((mask & (1 << index)) != 0) abilities.get(index).handleObtain(player, null);
                }
                double delayFactor = (mask & 16) != 0 ? 1.0 : (mask & 8) != 0 ? 3.0 : (mask & 7) != 0 ? 2.0 : 1.0;
                if ((mask & 32) != 0) delayFactor *= 2.5;
                near(helper, PlayerHelper.getShotDelay(player), 7.0 * delayFactor,
                        "Combination " + mask + " reverse=" + reverse);
                for (int offset = 0; offset < abilities.size(); offset++) {
                    int index = reverse ? offset : abilities.size() - 1 - offset;
                    if ((mask & (1 << index)) != 0) abilities.get(index).handleRemove(player, null);
                    int remaining = mask & (reverse ? ~((1 << (index + 1)) - 1) : (1 << index) - 1);
                    double remainingFactor = (remaining & 16) != 0 ? 1.0 : (remaining & 8) != 0 ? 3.0
                            : (remaining & 7) != 0 ? 2.0 : 1.0;
                    if ((remaining & 32) != 0) remainingFactor *= 2.5;
                    near(helper, PlayerHelper.getShotDelay(player), 7.0 * remainingFactor, "Removal restores suppressed penalties");
                }
                near(helper, PlayerHelper.getShotDelay(player), 7.0, "All penalties removed");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void d8MultipliesFinalFireRateAndChargeWeaponsUseIt(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(new D8().applyEffect(new ExecutableEffectContext(player)), "D8 succeeds");
        var modifiers = player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).orElseThrow(IllegalStateException::new);
        var entry = modifiers.getMultiplierEntries().snapshot().get(
                new MultiplierEntry.Key(
                        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier/d8_fire_rate"),
                        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "d8"))).entry();
        helper.assertTrue(entry.attributeId().equals(ModAttributes.FIRE_RATE.getId()), "D8 modifies FireRate directly");
        near(helper, PlayerHelper.getFireRate(player), (20.0 / 7.0) * (1.0 + entry.amount()), "D8 multiplier");
        player.getAttribute(ModAttributes.TEARS.get()).setBaseValue(100.0);
        near(helper, PlayerHelper.getFireRate(player), 5.0 * (1.0 + entry.amount()), "Tears preserves D8 multiplier");
        int expectedCharge = (int) Math.ceil(PlayerHelper.getShotDelay(player) * 3.0);
        helper.assertTrue(new BrimstoneAttack(1.0).getTotalCharge(player) == expectedCharge,
                "Charge weapons use the same final FireRate");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void rockBottomProtectsFinalRateWithoutCombiningOldTearsPeaks(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new)
                .addItem(player, new ItemStack(ModPassiveItems.ROCK_BOTTOM.get()));
        var tears = player.getAttribute(ModAttributes.TEARS.get());
        var blood = new Haemolacria(0, 1);
        blood.handleObtain(player, null);
        tears.setBaseValue(100.0);
        near(helper, PlayerHelper.getFireRate(player), 2.0, "Record final rate with blood penalty");
        tears.setBaseValue(0.0);
        near(helper, PlayerHelper.getFireRate(player), 2.0, "Final peak survives Tears reduction");
        blood.handleRemove(player, null);
        near(helper, PlayerHelper.getFireRate(player), 20.0 / 7.0, "Removing blood cannot reuse the old Tears peak");
        helper.assertTrue(!RockBottomState.isTarget(ModAttributes.TEARS.get()), "Rock Bottom tracks final FireRate only");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void sharedFiringIdHonorsStackingAndPerfectVisionSuppressesTheGroup(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var eye = new TheInnerEye(0, 1);
        var spider = new MutantSpider(0, 1);
        var ipecac = new Ipecac(0, 1);
        var vision = new PerfectVision(0, 1);
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING, false);
            eye.handleObtain(player, null);
            eye.handleObtain(player, null);
            spider.handleObtain(player, null);
            ipecac.handleObtain(player, null);
            helper.assertTrue(TheInnerEye.Multipliers.FIRE_RATE.id().equals(MutantSpider.Multipliers.FIRE_RATE.id())
                    && TheInnerEye.Multipliers.FIRE_RATE.id().equals(Ipecac.Multipliers.FIRE_RATE.id()),
                    "Firing penalties share one ID");
            near(helper, PlayerHelper.getShotDelay(player), 21.0, "Default keeps only Ipecac penalty");
            settings.set(GameplayConfig.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING, true);
            StatManager.refreshMultipliers(player);
            near(helper, PlayerHelper.getShotDelay(player), 168.0, "All three sources and both eye copies stack");
            vision.handleObtain(player, null);
            near(helper, PlayerHelper.getShotDelay(player), 7.0, "20/20 cancels the whole penalty group");
            vision.handleRemove(player, null);
            near(helper, PlayerHelper.getShotDelay(player), 168.0, "Removing 20/20 restores held sources");
            settings.set(GameplayConfig.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING, false);
            ipecac.handleRemove(player, null);
            near(helper, PlayerHelper.getShotDelay(player), 14.0, "Removing Ipecac restores shared double delay");
            eye.handleRemove(player, null);
            eye.handleRemove(player, null);
            near(helper, PlayerHelper.getShotDelay(player), 14.0, "Spider survives removal of both eye copies");
            spider.handleRemove(player, null);
            near(helper, PlayerHelper.getShotDelay(player), 7.0, "Last source removal clears the modifier");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_rate")
    public static void mushroomAndCricketsHeadShareOneDamageModifierAndRestoreBySource(GameTestHelper helper) {
        try (var settings = GameTestDatapackSettings.override(helper)) {
            for (boolean reverse : List.of(false, true)) {
                ServerPlayer player = player(helper);
                var mushroom = new MagicMushroom(0, 1);
                var head = new CricketsHead(0, 1);
                settings.set(GameplayConfig.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING, false);
                if (reverse) {
                    head.handleObtain(player, null);
                    mushroom.handleObtain(player, null);
                } else {
                    mushroom.handleObtain(player, null);
                    head.handleObtain(player, null);
                }
                helper.assertTrue(MagicMushroom.Multipliers.DAMAGE.id().equals(CricketsHead.Multipliers.DAMAGE.id()),
                        "Both items share one damage ID");
                var damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
                near(helper, damage.getModifier(MagicMushroom.Multipliers.DAMAGE.uuid()).getAmount(), 0.5,
                        "Default grants one +50% modifier");
                settings.set(GameplayConfig.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING, true);
                StatManager.refreshMultipliers(player);
                near(helper, damage.getModifier(MagicMushroom.Multipliers.DAMAGE.uuid()).getAmount(), 1.0,
                        "Stacking grants +100% from both sources");
                head.handleRemove(player, null);
                near(helper, damage.getModifier(MagicMushroom.Multipliers.DAMAGE.uuid()).getAmount(), 0.5,
                        "Removing head preserves mushroom");
                mushroom.handleRemove(player, null);
                helper.assertTrue(damage.getModifier(MagicMushroom.Multipliers.DAMAGE.uuid()) == null,
                        "Removing last source clears shared damage modifier");
            }
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fire-rate-test"));
    }

    private static void near(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6,
                message + ": expected " + expected + ", got " + actual);
    }
}
