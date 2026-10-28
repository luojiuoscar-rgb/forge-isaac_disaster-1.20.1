package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BloodRights;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DullRazor;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.IvBag;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Kamikaze;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ModReviveModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_self_damage")
@PrefixGameTestTemplate(false)
public final class SelfDamageGameTests {
    private static final ResourceKey<DamageType> SELF_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("isaac_disaster", "self_damage"));

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void selfDamageIsSeparateFromGenericKill(GameTestHelper helper) {
        var victim = cow(helper);
        try {
            var source = PlayerHelper.getSelfDamageSource(victim);
            helper.assertTrue(source.is(SELF_DAMAGE), "Self damage uses its own registered type");
            helper.assertTrue(PlayerHelper.isSelfDamage(source), "New type is recognized as self damage");
            helper.assertTrue(!PlayerHelper.isSelfDamage(victim.damageSources().genericKill()),
                    "Generic kill is no longer classified as self damage");
            helper.assertTrue(!PlayerHelper.isSelfDamage(victim.damageSources().generic()),
                    "Ordinary damage is not self damage");
            helper.assertTrue(source.getEntity() == null && source.getDirectEntity() == null,
                    "Self damage does not attribute an attack to the victim");
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void selfDamageBypassesCooldownButRespectsInvulnerability(GameTestHelper helper) {
        var victim = cow(helper);
        try {
            victim.getAttribute(Attributes.ARMOR).setBaseValue(20);
            victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
            victim.setInvulnerable(true);
            var source = PlayerHelper.getSelfDamageSource(victim);
            helper.assertTrue(!victim.hurt(source, 4), "Entity invulnerability protects against self damage");
            close(helper, victim.getHealth(), 20, "Rejected self damage leaves health intact");
            victim.setInvulnerable(false);
            helper.assertTrue(victim.hurt(source, 4), "Unprotected target accepts self damage");
            close(helper, victim.getHealth(), 16, "Armor and resistance do not reduce self damage");
            helper.assertTrue(victim.invulnerableTime > 10, "First hit establishes a hurt cooldown");
            helper.assertTrue(victim.hurt(source, 4), "Equal self damage bypasses the existing hurt cooldown");
            close(helper, victim.getHealth(), 12, "Both same-tick self hits apply in full");
            helper.assertTrue(!victim.hurt(victim.damageSources().generic(), 4),
                    "Ordinary damage still respects the hurt cooldown");
            helper.assertTrue(source.is(DamageTypeTags.BYPASSES_COOLDOWN)
                            && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                            && !source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                            && !source.is(DamageTypeTags.BYPASSES_EFFECTS),
                    "Self damage has only the intended bypass rules");
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void creativePlayersIgnoreSelfDamageButGenericKillStillWorks(GameTestHelper helper) {
        var victim = player(helper);
        try {
            victim.setGameMode(GameType.CREATIVE);
            helper.assertTrue(!victim.hurt(PlayerHelper.getSelfDamageSource(victim), 2),
                    "Creative players reject self damage");
            close(helper, victim.getHealth(), 100, "Self damage preserves creative health");
            new BloodRights().applyEffect(new ExecutableEffectContext(victim));
            new Kamikaze().applyEffect(new ExecutableEffectContext(victim));
            close(helper, victim.getHealth(), 100, "Blood rights and kamikaze cannot self-damage creative players");
            helper.assertTrue(victim.hurt(victim.damageSources().genericKill(), 2),
                    "Generic kill retains its creative bypass");
            close(helper, victim.getHealth(), 98, "Generic kill still damages creative players");
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void creativeIvBagPreservesHealthAndAbsorption(GameTestHelper helper) {
        var victim = player(helper);
        try {
            victim.setGameMode(GameType.CREATIVE);
            var context = new ExecutableEffectContext(victim);
            context.set(ContextKeys.COUNT, 0);
            float damage = (float) (StatManager.MAX_HEALTH.getBonus() * 0.5);
            for (float health : new float[]{100, damage * 0.5f}) {
                victim.invulnerableTime = 20;
                victim.setHealth(health);
                victim.setAbsorptionAmount(20);
                new IvBag().applyEffect(context);
                close(helper, victim.getHealth(), health, "Creative IV bag does not deduct red hearts");
                close(helper, victim.getAbsorptionAmount(), 20, "Creative IV bag does not deduct absorption");
                helper.assertTrue(victim.invulnerableTime == 20, "Creative IV bag leaves the hurt cooldown untouched");
                helper.assertTrue(victim.getLastDamageSource() == null, "Creative IV bag causes no hurt");
            }
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void vanillaTotemProtectsAgainstSelfDamage(GameTestHelper helper) {
        var victim = cow(helper);
        try {
            victim.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            victim.hurt(PlayerHelper.getSelfDamageSource(victim), 1000);
            helper.assertTrue(!victim.isDeadOrDying(), "Vanilla totem prevents self damage death");
            helper.assertTrue(victim.getOffhandItem().isEmpty(),
                    "Vanilla totem is consumed when protecting against self damage");
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void selfDamageKeepsCommonTriggersAndSkipsNegativeTriggers(GameTestHelper helper) {
        var victim = player(helper);
        try {
            var modules = victim.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow();
            modules.getTriggerModules().add(ModTriggerModules.PLAYER_PERMANENT_MODULE.getId(), 1);
            modules.getTriggerModules().add(ModTriggerModules.BLIND_RAGE.getId(), 1);
            victim.addEffect(new MobEffectInstance(ModEffects.HOLY_SHIELD.get(), 200, 0));
            var selfEvent = new LivingHurtEvent(victim, PlayerHelper.getSelfDamageSource(victim), 1);
            TriggerModuleEvents.onLivingHurt(selfEvent);
            helper.assertTrue(!selfEvent.isCanceled() && selfEvent.getAmount() == 1,
                    "Self damage skips the negative holy-shield trigger");
            helper.assertTrue(victim.invulnerableTime == 25, "Common blind-rage trigger still runs");
            for (var source : List.of(victim.damageSources().generic(), victim.damageSources().genericKill())) {
                victim.invulnerableTime = 0;
                var event = new LivingHurtEvent(victim, source, 1);
                TriggerModuleEvents.onLivingHurt(event);
                helper.assertTrue(event.isCanceled(), "Non-self damage reaches the holy-shield trigger");
                helper.assertTrue(victim.invulnerableTime == 25, "Non-self damage also runs common triggers");
            }
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void ivBagUsesSelfDamageForBothHealthBranches(GameTestHelper helper) {
        var victim = player(helper);
        try {
            var context = new ExecutableEffectContext(victim);
            context.set(ContextKeys.COUNT, 0);
            float damage = (float) (StatManager.MAX_HEALTH.getBonus() * 0.5);
            victim.invulnerableTime = 20;
            new IvBag().applyEffect(context);
            close(helper, victim.getHealth(), 100 - damage - 0.01f, "Red hearts are deducted during hurt cooldown");
            assertSelfSource(helper, victim.getLastDamageSource());
            helper.assertTrue(victim.invulnerableTime == 20, "IV bag does not manually shorten hurt cooldown");
            new IvBag().applyEffect(context);
            close(helper, victim.getHealth(), 100 - damage * 2 - 0.02f, "Repeated red-heart payments both apply");
            victim.setHealth(damage * 0.5f);
            victim.setAbsorptionAmount(20);
            new IvBag().applyEffect(context);
            close(helper, victim.getHealth(), damage * 0.5f, "Low-health branch uses absorption");
            close(helper, victim.getAbsorptionAmount(), 20 - damage, "Low-health branch charges full damage");
            new IvBag().applyEffect(context);
            close(helper, victim.getAbsorptionAmount(), 20 - damage * 2, "Repeated absorption payments both apply");
            assertSelfSource(helper, victim.getLastDamageSource());
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void bloodRightsAndKamikazeUseSelfDamage(GameTestHelper helper) {
        var victim = player(helper);
        try {
            float damage = (float) StatManager.MAX_HEALTH.getBonus();
            new BloodRights().applyEffect(new ExecutableEffectContext(victim));
            close(helper, victim.getHealth(), 100 - damage, "Blood rights keeps its self-damage amount");
            assertSelfSource(helper, victim.getLastDamageSource());
            new Kamikaze().applyEffect(new ExecutableEffectContext(victim));
            close(helper, victim.getHealth(), 100 - damage * 2, "Kamikaze keeps its self-damage amount");
            assertSelfSource(helper, victim.getLastDamageSource());
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void dullRazorPostsZeroSelfDamageWithoutLosingHealth(GameTestHelper helper) {
        var victim = player(helper);
        var probe = new HurtProbe(victim);
        MinecraftForge.EVENT_BUS.register(probe);
        try {
            new DullRazor().applyEffect(new ExecutableEffectContext(victim));
            helper.assertTrue(probe.events.size() == 2, "Default razor posts two hurt events");
            for (var event : probe.events) {
                helper.assertTrue(event.getAmount() == 0, "Razor's hurt event has zero damage");
                assertSelfSource(helper, event.getSource());
            }
            close(helper, victim.getHealth(), 100, "Razor does not deduct health");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(probe);
            victim.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void selfDamageCanReviveButGenericKillCannot(GameTestHelper helper) {
        var revived = player(helper);
        var killed = player(helper);
        try {
            var revive = revived.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getReviveSequence();
            var killRevive = killed.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getReviveSequence();
            revive.addConsumer(ModReviveModules.SIMPLE_REVIVE.getId(), 1);
            killRevive.addConsumer(ModReviveModules.SIMPLE_REVIVE.getId(), 1);
            helper.assertTrue(revive.getHudPreview(1).size() == 1 && killRevive.getHudPreview(1).size() == 1,
                    "Both fixtures have a registered revive charge");
            revived.hurt(PlayerHelper.getSelfDamageSource(revived), 1000);
            helper.assertTrue(!revived.isDeadOrDying(), "Self damage death is canceled by the mod revive");
            helper.assertTrue(revive.getHudPreview(1).isEmpty(), "Self damage consumes the revive charge");
            killed.hurt(killed.damageSources().genericKill(), 1000);
            helper.assertTrue(killed.isDeadOrDying(), "Generic kill still bypasses the mod revive");
            helper.assertTrue(killRevive.getHudPreview(1).size() == 1, "Generic kill leaves the charge untouched");
            helper.succeed();
        } finally {
            revived.discard();
            killed.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "self_damage")
    public static void spawnProtectionBlocksSelfDamageButNotGenericKill(GameTestHelper helper) {
        var victim = new DamagePlayer(helper.getLevel());
        try {
            victim.setGameMode(GameType.SURVIVAL);
            helper.assertTrue(!victim.hurt(PlayerHelper.getSelfDamageSource(victim), 2),
                    "Spawn protection rejects self damage");
            helper.assertTrue(victim.hurt(victim.damageSources().genericKill(), 2),
                    "Generic kill retains its spawn-protection bypass");
            helper.succeed();
        } finally {
            victim.discard();
        }
    }

    private static Cow cow(GameTestHelper helper) {
        var victim = helper.spawn(EntityType.COW, new BlockPos(1, 2, 1));
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        victim.setHealth(20);
        victim.setNoAi(true);
        victim.setNoGravity(true);
        return victim;
    }

    private static DamagePlayer player(GameTestHelper helper) {
        var victim = new DamagePlayer(helper.getLevel());
        victim.setGameMode(GameType.SURVIVAL);
        victim.setPos(helper.absolutePos(new BlockPos(1, 40, 1)).getCenter());
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        victim.setHealth(100);
        // Other tests exercise damage after the server's initial 60-tick spawn protection.
        ObfuscationReflectionHelper.setPrivateValue(ServerPlayer.class, victim, 0, "f_8921_");
        victim.invulnerableTime = 0;
        return victim;
    }

    private static void assertSelfSource(GameTestHelper helper, DamageSource source) {
        helper.assertTrue(source != null && source.is(SELF_DAMAGE), "Effect uses the dedicated self-damage type");
    }

    private static void close(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.001f, message + ": " + actual + " vs " + expected);
    }

    /** Restores damage and the real Forge death hook while retaining FakePlayer's network stub. */
    private static final class DamagePlayer extends FakePlayer {
        private DamagePlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "self_damage"));
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return isRemoved() || isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                    && !source.isCreativePlayer();
        }

        @Override
        public void die(DamageSource source) {
            ForgeHooks.onLivingDeath(this, source);
        }
    }

    public static final class HurtProbe {
        private final DamagePlayer victim;
        private final List<LivingHurtEvent> events = new ArrayList<>();

        private HurtProbe(DamagePlayer victim) {
            this.victim = victim;
        }

        @SubscribeEvent(receiveCanceled = true)
        public void onHurt(LivingHurtEvent event) {
            if (event.getEntity() == victim) events.add(event);
        }
    }
}
