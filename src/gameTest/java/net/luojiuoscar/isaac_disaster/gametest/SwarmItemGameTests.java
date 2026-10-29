package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.ActiveItemUseEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.BeforeTriggerModuleActiveEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.TheMulligan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.AbstractLaserAttack;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Infestation;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.SpiderBaby;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

@GameTestHolder("isaac_disaster_swarm_items")
@PrefixGameTestTemplate(false)
public final class SwarmItemGameTests {
    @GameTest(template = "trajectory_empty")
    public static void underwearAddsConfiguredRangeAndOwnedFliesOnFirstAcquisition(GameTestHelper helper) {
        var item = ForgeRegistries.ITEMS.getValue(id("moms_underwear"));
        helper.assertTrue(item instanceof PassiveItem, "Mom's Underwear must be registered as a passive item");
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "swarm-items"));
        player.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(3, 210, 3));
        DefaultAttributeManager.apply(player);
        double before = player.getAttributeValue(ModAttributes.BULLET_RANGE.get());
        var ability = (PassiveAbility) ((PassiveItem) item).getAbility();
        try {
            ability.onObtain(player, new ItemStack(item));
            var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            int count = data.getCount(id("blue_fly"));
            helper.assertTrue(count >= 3 && count <= 6, "First acquisition creates three to six owned flies");
            helper.assertTrue(Math.abs(player.getAttributeValue(ModAttributes.BULLET_RANGE.get())
                    - before - StatManager.RANGE.getBonus()) < 0.0001, "Range uses the approved ratio one");
        } finally {
            ability.onRemove(player, null);
            FamiliarHelper.discardAllRuntimeFamiliars(player);
            player.discard();
        }
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("isaac_disaster", path);
    }

    @GameTest(template = "trajectory_empty")
    public static void hurtEventsAndDuplicateCopiesUseOneBatchPerEvent(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("infestation");
            f.grant("spider_baby");
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(7.5);
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            f.hurt(1, false, false);
            int flies = f.count("blue_fly"), spiders = f.count("blue_spider");
            helper.assertTrue(flies >= 1 && flies <= 2 && spiders >= 1 && spiders <= 2,
                    "Each successful hurt roll produces one or two familiars");
            f.reset();
            f.grant("infestation");
            f.grant("spider_baby");
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            f.hurt(1, false, false);
            helper.assertTrue(f.count("blue_fly") == flies && f.count("blue_spider") == spiders,
                    "Two stored copies do not change RNG draws, amounts, or number of executions");
            f.reset();
            f.hurt(1, false, true);
            helper.assertTrue(f.count("blue_fly") + f.count("blue_spider") == 0,
                    "Canceled hurt is not dispatched by Forge");
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            f.hurt(0, false, false);
            helper.assertTrue(f.count("blue_fly") >= 1 && f.count("blue_spider") >= 1,
                    "ON_HURT is consumed directly without a second amount or source filter");
            f.reset();
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            f.hurt(0, true, false);
            helper.assertTrue(f.count("blue_fly") >= 1 && f.count("blue_spider") >= 1,
                    "Zero SELF_DAMAGE is a valid simulated hurt");
            f.reset();
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            var ctx = new ExecutableEffectContext(f.owner);
            ctx.set(ContextKeys.AMPLIFIER, 3.0);
            ModExecutableEffects.DULL_RAZOR.get().apply(ctx);
            helper.assertTrue(f.count("blue_fly") >= 1 && f.count("blue_fly") <= 6
                    && f.count("blue_spider") >= 1 && f.count("blue_spider") <= 6,
                    "Dull Razor events each roll independently with batches of one or two");
            f.reset();
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(-5);
            f.owner.getRandom().setSeed(hurtSuccessSeed());
            f.hurt(1, false, false);
            helper.assertTrue(f.count("blue_fly") + f.count("blue_spider") == 0, "Negative luck can reduce both chances to zero");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void wigCountsOnePrimaryPlanAndUsesDesiredThresholds(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("moms_wig");
            f.grant("moms_wig");
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(9.5);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 8);
            helper.assertTrue(f.count("blue_spider") == 1, "Multishot primary plan rolls once, even with duplicate wigs");
            for (AttackOrigin origin : AttackOrigin.values()) {
                if (origin != AttackOrigin.PLAYER_PRIMARY) f.plan(origin, 1);
            }
            f.plan(AttackOrigin.PLAYER_PRIMARY, 0);
            helper.assertTrue(f.count("blue_spider") == 1, "Empty, split, derived and extra plans do not roll");
            f.reset();
            f.data.setCount(id("blue_spider"), 4);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 1);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 1);
            helper.assertTrue(f.count("blue_spider") == 5, "Desired count 4 becomes 5 then stops despite missing entities");
            f.grant("hive_mind");
            f.grant("hive_mind");
            f.data.setCount(id("blue_spider"), 9);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 1);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 1);
            helper.assertTrue(f.count("blue_spider") == 10, "Hive Mind raises the cap to ten only once");
            f.data.setCount(id("blue_spider"), 12);
            f.plan(AttackOrigin.PLAYER_PRIMARY, 1);
            helper.assertTrue(f.count("blue_spider") == 12, "Already over the threshold does not produce more spiders");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mulliganAttachmentIsIdempotentAndFiredSnapshotsSurviveRemoval(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("the_mulligan");
            f.grant("the_mulligan");
            AttackContext attack = f.attack(ModAttackTypes.BULLET.get());
            f.prepare(attack);
            f.prepare(attack);
            var child = attack.toBuilder().build();
            f.prepare(child);
            var copy = child.getTrigger().copy();
            helper.assertTrue(copy.remove(ModExecutableEffects.THE_MULLIGAN.get())
                    && !copy.remove(ModExecutableEffects.THE_MULLIGAN.get()), "Split inheritance and repeat preparation retain exactly one hit effect");
            f.items.clearPlayerPassiveItems(f.owner);
            var later = f.attack(ModAttackTypes.BULLET.get());
            f.prepare(later);
            helper.assertTrue(!later.getTrigger().contains(ModExecutableEffects.THE_MULLIGAN.get()),
                    "Removing the last copy stops new attachments");
            f.data.setCount(id("blue_fly"), 29);
            f.owner.getRandom().setSeed(successSeed());
            f.hit(child, 1, false);
            helper.assertTrue(f.count("blue_fly") == 30, "Previously fired child still generates at desired 29");
            f.owner.getRandom().setSeed(successSeed());
            f.hit(child, 1, false);
            helper.assertTrue(f.count("blue_fly") == 30, "Desired count thirty blocks further generation");
            f.reset();
            f.data.setCount(id("locust_of_death"), 31);
            f.owner.getRandom().setSeed(successSeed());
            f.hit(child, 1, false);
            helper.assertTrue(f.count("blue_fly") == 1, "Other locusts do not count toward the Mulligan threshold");
            f.reset();
            f.data.setCount(id("locust_of_death"), 64);
            f.owner.getRandom().setSeed(successSeed());
            f.hit(child, 1, false);
            helper.assertTrue(f.count("blue_fly") == 0, "Shared quota failure does not add a phantom demand");
            f.reset();
            f.hit(child, 0, false);
            f.hit(child, 1, true);
            helper.assertTrue(f.count("blue_fly") == 0, "Zero and canceled hits do not generate flies");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mulliganRealTearAndLaserHitsRespectDamageAcceptance(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("the_mulligan");
            Mob target = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            target.setInvulnerable(true);
            f.fireTear();
            helper.assertTrue(f.count("blue_fly") == 0, "Rejected real tear damage has no after-hit reward");
            target.setInvulnerable(false);
            f.fireTear();
            helper.assertTrue(f.count("blue_fly") == 1 && target.getHealth() < 100, "Accepted tear collision can proc");
            f.reset();
            target.invulnerableTime = 0;
            var laser = f.attack(ModAttackTypes.LASER.get());
            f.prepare(laser);
            f.owner.getRandom().setSeed(successSeed());
            ModAttackTypes.LASER.get().shoot(laser);
            helper.assertTrue(f.count("blue_fly") >= 1, "Real laser execution reaches the same hit payoff");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void mulliganAlsoConsumesRestrictedAttacksWithoutDuplicatingTearOrLaserRolls(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("the_mulligan");
            f.grant("the_mulligan");
            Mob target = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            f.owner.getRandom().setSeed(successSeed());
            target.hurt(f.owner.damageSources().playerAttack(f.owner), 2);
            helper.assertTrue(f.count("blue_fly") == 1, "An actual restricted melee attack can proc once with duplicate items");
            f.reset();
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(target,
                    ModDamageSources.familiar(helper.getLevel(), f.owner, f.owner), 2));
            helper.assertTrue(f.count("blue_fly") == 0, "Unrestricted familiar damage does not use the restricted entry");
            var canceled = new LivingAttackEvent(target, f.owner.damageSources().playerAttack(f.owner), 2);
            canceled.setCanceled(true);
            MinecraftForge.EVENT_BUS.post(canceled);
            helper.assertTrue(f.count("blue_fly") == 0, "Canceled restricted attacks are not dispatched");
            f.data.setCount(id("blue_fly"), 29);
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(target, f.owner.damageSources().playerAttack(f.owner), 2));
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(target, f.owner.damageSources().playerAttack(f.owner), 2));
            helper.assertTrue(f.count("blue_fly") == 30, "Restricted attacks share the same desired-count threshold");
            f.reset();
            var reference = RandomSource.create(successSeed());
            reference.nextDouble();
            long next = reference.nextLong();
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(target,
                    ModDamageSources.tear(helper.getLevel(), f.owner, new Vec3(1, 0, 0), 0.2D), 2));
            f.hit(prepared(f, ModAttackTypes.BULLET.get()), 2, false);
            helper.assertTrue(f.count("blue_fly") == 1 && f.owner.getRandom().nextLong() == next,
                    "The tear damage source does not add an extra restricted probability roll");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void spiderGenerationPayoffWorksAtAnExplicitPositionWithoutAnEvent(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ctx = new ExecutableEffectContext(f.owner);
            var position = f.owner.position().add(4, 1, 2);
            ctx.set(ContextKeys.TARGET_POSITION, position);
            ctx.set(ContextKeys.AMPLIFIER, 9.0);
            var effect = (IAbilityEffect) ModExecutableEffects.SPAWN_BLUE_SPIDER.get();
            helper.assertTrue(effect.applyEffect(ctx) && f.count("blue_spider") == 1,
                    "The reusable one-spider payoff needs no kill event, item or amplifier");
            var entry = f.data.getEntry(id("blue_spider")).orElseThrow();
            Entity spider = helper.getLevel().getEntity(entry.getEntityId(0));
            helper.assertTrue(spider != null && spider.position().equals(position), "The explicit context position is used");
        }
        helper.succeed();
    }

    private static AttackContext prepared(Fixture f, AttackType type) {
        var context = f.attack(type);
        f.prepare(context);
        return context;
    }

    @GameTest(template = "trajectory_empty")
    public static void infestationTwoUsesKillAttributionAndVictimPosition(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("infestation_2");
            f.grant("infestation_2");
            for (String type : List.of("blue_spider", "blue_fly", "locust_of_war", "mom_knife")) {
                f.reset();
                var direct = FamiliarHelper.spawnFamiliars(f.owner, id(type), f.owner.position()).get(0);
                Mob victim = f.mob(EntityType.HUSK, f.owner.position().add(4, 1, 2));
                victim.setHealth(1);
                int before = f.count("blue_spider");
                victim.hurt(ModDamageSources.familiar(helper.getLevel(), direct, f.owner), 1000);
                int expected = type.equals("blue_spider") ? before : before + 1;
                helper.assertTrue(f.count("blue_spider") == expected, "Kill source filtering for " + type);
                if (!type.equals("blue_spider")) {
                    var spawned = f.data.getEntry(id("blue_spider")).orElseThrow();
                    Entity spider = helper.getLevel().getEntity(spawned.getEntityId(0));
                    helper.assertTrue(spider instanceof AbstractIsaacFamiliarEntity familiar
                            && familiar.getOwner() == f.owner && spider.position().equals(victim.position()),
                            "Reward is immediately owned and placed at the victim");
                }
            }
            f.reset();
            Mob friendly = f.mob(EntityType.WOLF, f.owner.position().add(3, 0, 0));
            ((Wolf) friendly).tame(f.owner);
            friendly.setHealth(1);
            friendly.hurt(f.owner.damageSources().playerAttack(f.owner), 1000);
            helper.assertTrue(f.count("blue_spider") == 1, "Attributed friendly kills also count");
            f.reset();
            Mob discarded = f.mob(EntityType.HUSK, f.owner.position());
            discarded.discard();
            helper.assertTrue(f.count("blue_spider") == 0, "Direct removal does not publish a kill");
            f.data.setCount(id("blue_spider"), 64);
            Mob victim = f.mob(EntityType.HUSK, f.owner.position().add(2, 0, 0));
            victim.hurt(f.owner.damageSources().explosion(f.owner, f.owner), 1000);
            helper.assertTrue(f.count("blue_spider") == 64, "A full quota adds no unfulfilled requirement");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledDeathSkipsButLaterRevivalRetainsKillReward(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("infestation_2");
            Mob early = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            Consumer<LivingDeathEvent> cancel = event -> { if (event.getEntity() == early) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDeathEvent.class, cancel);
            try { early.hurt(f.owner.damageSources().playerAttack(f.owner), 1000); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            helper.assertTrue(f.count("blue_spider") == 0, "Cancellation before dispatch suppresses generation");
            Mob late = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 2));
            Consumer<LivingDeathEvent> revive = event -> {
                if (event.getEntity() == late) { event.setCanceled(true); late.setHealth(10); }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, revive);
            try { late.hurt(f.owner.damageSources().playerAttack(f.owner), 1000); }
            finally { MinecraftForge.EVENT_BUS.unregister(revive); }
            helper.assertTrue(late.isAlive() && f.count("blue_spider") == 1, "Later revival does not roll back a valid kill payoff");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void carBatteryDrawsIndependentBatchesAndPadKeepsFearDuration(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("car_battery");
            for (String itemId : List.of("guppys_head", "box_of_spiders")) {
                f.reset();
                int minimum = itemId.equals("guppys_head") ? 2 : 4;
                int width = itemId.equals("guppys_head") ? 3 : 5;
                long seed = 0;
                int expected;
                do {
                    RandomSource random = RandomSource.create(seed++);
                    expected = minimum * 2 + random.nextInt(width) + random.nextInt(width);
                } while (expected % 2 == 0);
                f.owner.getRandom().setSeed(seed - 1);
                f.use(itemId);
                helper.assertTrue(f.count(itemId.equals("guppys_head") ? "blue_fly" : "blue_spider") == expected,
                        "Car Battery uses independent draws, including odd total for " + itemId);
            }
            f.reset();
            Mob enemy = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            // FakePlayers are not in the server's player lookup, so resolve this tame owner explicitly.
            Wolf friendly = new Wolf(EntityType.WOLF, helper.getLevel()) {
                @Override public LivingEntity getOwner() { return f.owner; }
            };
            friendly.setPos(f.owner.position().add(2, 0, 2));
            friendly.setNoAi(true);
            friendly.setNoGravity(true);
            helper.assertTrue(helper.getLevel().addFreshEntity(friendly), "Owned wolf joins");
            f.mobs.add(friendly);
            friendly.tame(f.owner);
            Mob distant = f.mob(EntityType.HUSK, f.owner.position().add(StatManager.getNearbyRange() + 3, 0, 0));
            f.use("moms_pad");
            helper.assertTrue(f.count("blue_fly") == 2, "Car Battery pad generates two flies");
            helper.assertTrue(enemy.hasEffect(ModEffects.PANIC.get())
                    && enemy.getEffect(ModEffects.PANIC.get()).getDuration() == 80,
                    "Fear duration remains exactly four seconds");
            helper.assertTrue(!friendly.hasEffect(ModEffects.PANIC.get()) && !f.owner.hasEffect(ModEffects.PANIC.get())
                    && !distant.hasEffect(ModEffects.PANIC.get()), "Area effect excludes owner, friends and distant mobs: friend="
                    + friendly.hasEffect(ModEffects.PANIC.get()) + ", owner=" + f.owner.hasEffect(ModEffects.PANIC.get())
                    + ", distant=" + distant.hasEffect(ModEffects.PANIC.get()));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void activeItemsKeepChargeCancellationCooldownAndFirstUseSets(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            String[] names = {"moms_pad", "guppys_head", "box_of_spiders"};
            String[] sets = {"mom", "cat", "spider_baby"};
            int[] costs = {200, 100, 200};
            for (int i = 0; i < names.length; i++) {
                f.reset();
                ActiveItem item = (ActiveItem) ForgeRegistries.ITEMS.getValue(id(names[i]));
                ItemStack stack = new ItemStack(item);
                f.owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
                stack.setDamageValue(costs[i]);
                item.use(helper.getLevel(), f.owner, InteractionHand.MAIN_HAND);
                helper.assertTrue(f.count("blue_fly") + f.count("blue_spider") == 0
                        && !ActiveItem.hasBeenUsed(stack), "Insufficient charge has no effect or first-use membership");
                stack.setDamageValue(0);
                Consumer<ActiveItemUseEvent> cancel = event -> event.setCanceled(true);
                MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ActiveItemUseEvent.class, cancel);
                try { item.use(helper.getLevel(), f.owner, InteractionHand.MAIN_HAND); }
                finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
                helper.assertTrue(stack.getDamageValue() == 0 && !ActiveItem.hasBeenUsed(stack),
                        "Canceled use preserves charge and first-use state");
                f.data.setCount(id(names[i].equals("box_of_spiders") ? "blue_spider" : "blue_fly"), 64);
                item.use(helper.getLevel(), f.owner, InteractionHand.MAIN_HAND);
                helper.assertTrue(stack.getDamageValue() == costs[i] && stack.getMaxDamage() == costs[i]
                        && f.owner.getCooldowns().isOnCooldown(item), "Full quota still consumes normal charge and gives cooldown");
                helper.assertTrue(f.items.getSetCountFromId(id(sets[i])) == 1, "First use registers its set");
                ActiveItem.fullCharge(stack, false);
                f.owner.getCooldowns().removeCooldown(item);
                item.use(helper.getLevel(), f.owner, InteractionHand.MAIN_HAND);
                helper.assertTrue(f.items.getSetCountFromId(id(sets[i])) == 1, "Repeated use does not duplicate set membership");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void realCuriosReloadRetainsCopiesAndRemovalStopsOnlyTheLast(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
            tickCurios(original.owner);
            var slots = CuriosApi.getCuriosInventory(original.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            slots.setStackInSlot(0, new ItemStack(ModPassiveItems.INFESTATION.get()));
            slots.setStackInSlot(1, new ItemStack(ModPassiveItems.INFESTATION.get()));
            tickCurios(original.owner);
            helper.assertTrue(original.moduleCount("infestation") == 2, "Physical duplicate equip retains real copies");
            CompoundTag saved = original.owner.saveWithoutId(new CompoundTag());
            restored.owner.load(saved);
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner);
            tickCurios(restored.owner);
            tickCurios(restored.owner);
            helper.assertTrue(restored.moduleCount("infestation") == 2
                    && restored.items.getSetCountFromId(id("beelzebub")) == 2, "Full player load retains counts without replaying acquisition");
            var loaded = CuriosApi.getCuriosInventory(restored.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            loaded.setStackInSlot(0, ItemStack.EMPTY);
            tickCurios(restored.owner);
            restored.owner.getAttribute(Attributes.LUCK).setBaseValue(7.5);
            restored.owner.getRandom().setSeed(hurtSuccessSeed());
            restored.hurt(1, false, false);
            helper.assertTrue(restored.moduleCount("infestation") == 1 && restored.count("blue_fly") >= 1,
                    "One remaining copy still triggers");
            restored.reset();
            loaded.setStackInSlot(1, ItemStack.EMPTY);
            tickCurios(restored.owner);
            restored.hurt(1, false, false);
            helper.assertTrue(restored.moduleCount("infestation") == 0 && restored.count("blue_fly") == 0
                    && restored.items.getSetCountFromId(id("beelzebub")) == 0, "Last removal stops generation and reverses set contribution");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void firstRewardsDoNotRepeatWhenPhysicalItemsAreReequipped(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
            tickCurios(f.owner);
            var slots = CuriosApi.getCuriosInventory(f.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            ItemStack underwear = new ItemStack(ModPassiveItems.MOMS_UNDERWEAR.get());
            ItemStack wig = new ItemStack(ModPassiveItems.MOMS_WIG.get());
            f.owner.setHealth(1);
            slots.setStackInSlot(0, underwear);
            slots.setStackInSlot(1, wig);
            tickCurios(f.owner);
            helper.assertTrue(Math.abs(f.owner.getHealth() - Math.min(f.owner.getMaxHealth(), 1 + StatManager.MAX_HEALTH.getBonus())) < 0.001,
                    "First wig acquisition heals one configured health unit");
            int flies = f.count("blue_fly");
            helper.assertTrue(flies >= 3 && flies <= 6, "First underwear acquisition rewards flies");
            slots.setStackInSlot(0, ItemStack.EMPTY);
            slots.setStackInSlot(1, ItemStack.EMPTY);
            tickCurios(f.owner);
            f.owner.setHealth(1);
            slots.setStackInSlot(0, underwear);
            slots.setStackInSlot(1, wig);
            tickCurios(f.owner);
            helper.assertTrue(f.owner.getHealth() == 1 && f.count("blue_fly") == flies,
                    "Reequipping used physical items does not repeat reward or healing");
            helper.assertTrue(f.items.getSetCountFromId(id("mom")) == 2, "Reequip restores exactly two Mom memberships");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void allFiveModulesKeepRealCountsButRemoveSymmetrically(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (String item : List.of("infestation", "spider_baby", "the_mulligan", "moms_wig", "infestation_2")) {
                f.grant(item);
                f.grant(item);
                helper.assertTrue(f.moduleCount(item) == 2, "Two real contributions for " + item);
                f.items.removeFromIndex(f.owner, 0);
                helper.assertTrue(f.moduleCount(item) == 1, "Removing one leaves the other for " + item);
                f.items.removeFromIndex(f.owner, 0);
                helper.assertTrue(f.moduleCount(item) == 0, "Final removal clears " + item);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void itemIdentityQualityAndDescriptionsResolveRealTranslations(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            String[] names = {"moms_underwear", "moms_pad", "guppys_head", "infestation", "the_mulligan", "spider_baby", "moms_wig", "infestation_2", "box_of_spiders"};
            int[] levels = {1, 0, 2, 0, 3, 0, 2, 3, 1};
            for (int i = 0; i < names.length; i++) {
                var item = ForgeRegistries.ITEMS.getValue(id(names[i]));
                helper.assertTrue(item instanceof IsaacItem, "Registered Isaac item " + names[i]);
                var ability = ((IsaacItem) item).getAbility();
                helper.assertTrue(ability.getId() == ItemId.MOMS_UNDERWEAR.ordinal() + i && ability.getLevel() == levels[i],
                        "Ordered project ordinal and level for " + names[i]);
                for (var line : ability.getDesc(new ItemStack(item), f.owner)) {
                    String text = line.getString();
                    helper.assertTrue(!text.contains(".lore.") && !text.contains("%s") && !text.contains("\n")
                            && !text.contains("可叠加"), "Description resolves one semantic line: " + text);
                }
            }
            var wig = ((PassiveItem) ModPassiveItems.MOMS_WIG.get()).getAbility();
            String baseline = wig.getDesc(null, f.owner).get(1).getString();
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(9.5);
            helper.assertTrue(baseline.contains("5.00%") && wig.getDesc(null, f.owner).get(1).getString().contains("100.00%"),
                    "Real translated percentage follows actual luck formula");
            helper.assertTrue(((PassiveItem) ModPassiveItems.THE_MULLIGAN.get()).getAbility().getDesc(null, f.owner).get(0).getString().contains("16.67%"),
                    "Fixed 1/6 is formatted as a real percentage");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void synergyDescriptionsKeepTheBaseQuantityAndPutPartnersInTheirOwnLines(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var names = List.of("box_of_spiders", "guppys_head", "moms_pad");
            List<List<String>> baseline = new ArrayList<>();
            for (String name : names) {
                var item = (ActiveItem) ForgeRegistries.ITEMS.getValue(id(name));
                baseline.add(item.getAbility().getDesc(null, f.owner).stream().map(line -> line.getString()).toList());
            }
            f.grant("car_battery");
            for (int i = 0; i < names.size(); i++) {
                var item = (ActiveItem) ForgeRegistries.ITEMS.getValue(id(names.get(i)));
                helper.assertTrue(item.getAbility().getDesc(null, f.owner).stream().map(line -> line.getString()).toList().equals(baseline.get(i)),
                        "Car Battery does not rewrite base amounts for " + names.get(i));
                helper.assertTrue(item.getAbility().getSynergyDesc(null, f.owner).stream()
                                .anyMatch(line -> line.getString().contains("车载电池") && line.getString().contains("使用两次")),
                        "Car Battery is described as two uses for " + names.get(i));
            }
            var wig = ((PassiveItem) ModPassiveItems.MOMS_WIG.get()).getAbility();
            helper.assertTrue(wig.getDesc(null, f.owner).stream().anyMatch(line -> line.getString().equals("最多5只")),
                    "The Wig's ordinary description gives its base cap");
            f.grant("hive_mind");
            helper.assertTrue(wig.getDesc(null, f.owner).stream().anyMatch(line -> line.getString().equals("最多5只"))
                    && wig.getSynergyDesc(null, f.owner).stream().anyMatch(line -> line.getString().contains("虫群之心")
                    && line.getString().contains("最多10只")), "Hive Mind puts the ten-spider cap in its synergy line");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void hiveMindAndHurtItemDescriptionsUseBothSetsAndCurrentLuck(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant("hive_mind");
            var hive = ((PassiveItem) ModPassiveItems.HIVE_MIND.get()).getAbility();
            for (var lines : List.of(hive.getSynergyDesc(null, f.owner), hive.getExtraDesc(null, f.owner))) {
                helper.assertTrue(lines.stream().anyMatch(line -> line.getString().contains("苍蝇领主"))
                        && lines.stream().anyMatch(line -> line.getString().contains("蜘蛛宝宝")), "Both set descriptions are visible");
            }
            helper.assertTrue(f.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId()) == 1
                    && f.items.getSetCountFromId(ModSetAbilities.SPIDER_BABY.getId()) == 1, "Both existing set contributions remain intact");
            for (String name : List.of("infestation", "spider_baby")) {
                var item = (PassiveItem) ForgeRegistries.ITEMS.getValue(id(name));
                for (double[] pair : new double[][]{{0, 10}, {7.5, 25}, {-2, 6}, {-10, 0}}) {
                    f.owner.getAttribute(Attributes.LUCK).setBaseValue(pair[0]);
                    String percent = String.format(Locale.ROOT, "%.2f%%", pair[1]);
                    helper.assertTrue(item.getAbility().getDesc(null, f.owner).get(0).getString().contains(percent),
                            "Actual translated hurt probability reflects luck " + pair[0]);
                    helper.assertTrue(item.getAbility().getDesc(null, f.owner).get(1).getString().contains("1–2"),
                            "Hurt quantity uses the same one-to-two registered generator");
                }
            }
            helper.assertTrue(ModSetAbilities.CAT.get().getExtraDesc().stream()
                    .anyMatch(line -> line.getString().contains("66.67%")), "Cat description uses its fixed two-thirds formula");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void catSetPhysicalEquipReloadAndThresholdsKeepFlightAndModuleSymmetric(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
            double initial = original.owner.getAttributeValue(ModAttributes.FLY_TIME.get());
            tickCurios(original.owner);
            var slots = CuriosApi.getCuriosInventory(original.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            for (int i = 0; i < 3; i++) slots.setStackInSlot(i, new ItemStack(ModPassiveItems.GUPPYS_COLLAR.get()));
            tickCurios(original.owner);
            helper.assertTrue(original.moduleCount("cat") == 1
                    && Math.abs(original.owner.getAttributeValue(ModAttributes.FLY_TIME.get()) - initial - StatManager.FLY_TIME.getBonus()) < 1e-6,
                    "Three physical set members grant one configured flight unit and one module");
            slots.setStackInSlot(3, new ItemStack(ModPassiveItems.GUPPYS_COLLAR.get()));
            tickCurios(original.owner);
            slots.setStackInSlot(3, ItemStack.EMPTY);
            tickCurios(original.owner);
            helper.assertTrue(original.moduleCount("cat") == 1, "Crossing four-to-three does not duplicate or remove the effect");
            restored.owner.load(original.owner.saveWithoutId(new CompoundTag()));
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner);
            tickCurios(restored.owner);
            tickCurios(restored.owner);
            helper.assertTrue(restored.moduleCount("cat") == 1
                    && Math.abs(restored.owner.getAttributeValue(ModAttributes.FLY_TIME.get()) - initial - StatManager.FLY_TIME.getBonus()) < 1e-6,
                    "Full player reload retains one flight contribution and module without replaying acquisition");
            var loaded = CuriosApi.getCuriosInventory(restored.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            ItemStack third = loaded.getStackInSlot(2);
            loaded.setStackInSlot(2, ItemStack.EMPTY);
            tickCurios(restored.owner);
            helper.assertTrue(restored.moduleCount("cat") == 0
                    && Math.abs(restored.owner.getAttributeValue(ModAttributes.FLY_TIME.get()) - initial) < 1e-6,
                    "Dropping below three removes exactly the set flight and module");
            loaded.setStackInSlot(2, third);
            tickCurios(restored.owner);
            helper.assertTrue(restored.moduleCount("cat") == 1, "Reforming the set reactivates exactly once");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void catUsesFinalDamageAndIgnoresAbsorbedCanceledAndFriendlyDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.CAT.getId(), 3);
            Mob target = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            f.owner.getAttribute(Attributes.LUCK).setBaseValue(-100);
            f.owner.getRandom().setSeed(successSeed());
            target.hurt(f.owner.damageSources().playerAttack(f.owner), 2);
            helper.assertTrue(f.count("blue_fly") == 1 && target.getHealth() == 98, "Actual positive health damage can generate a fly despite negative luck");
            var fly = helper.getLevel().getEntity(f.data.getEntry(id("blue_fly")).orElseThrow().getEntityId(0));
            helper.assertTrue(fly != null && fly.position().equals(f.owner.position()), "Cat fly is admitted at the player's position");
            f.reset();
            target.invulnerableTime = 0;
            target.setAbsorptionAmount(10);
            f.owner.getRandom().setSeed(successSeed());
            target.hurt(f.owner.damageSources().playerAttack(f.owner), 2);
            helper.assertTrue(f.count("blue_fly") == 0 && target.getHealth() == 98, "Damage fully absorbed before LivingDamageEvent does not generate");
            target.setAbsorptionAmount(0);
            target.invulnerableTime = 0;
            Consumer<LivingDamageEvent> cancel = event -> { if (event.getEntity() == target) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDamageEvent.class, cancel);
            try { target.hurt(f.owner.damageSources().playerAttack(f.owner), 2); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            helper.assertTrue(f.count("blue_fly") == 0, "Canceled final damage does not generate");
            Wolf friendly = new Wolf(EntityType.WOLF, helper.getLevel()) {
                @Override public LivingEntity getOwner() { return f.owner; }
            };
            MinecraftForge.EVENT_BUS.post(new LivingDamageEvent(friendly, f.owner.damageSources().playerAttack(f.owner), 2));
            MinecraftForge.EVENT_BUS.post(new LivingDamageEvent(f.owner, f.owner.damageSources().playerAttack(f.owner), 2));
            helper.assertTrue(f.count("blue_fly") == 0, "Self and owned-friendly damage do not generate");
            StatManager.modifySetWithId(f.owner, ModSetAbilities.CAT.getId(), -1);
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingDamageEvent(target, f.owner.damageSources().playerAttack(f.owner), 2));
            helper.assertTrue(f.count("blue_fly") == 0, "Losing the set stops future damage procs");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void catExcludesAllDirectFamiliarsButAllowsTearsLasersAndExplosions(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.CAT.getId(), 3);
            Mob target = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            for (String type : List.of("blue_fly", "blue_spider", "locust_of_war", "locust_of_pestilence", "locust_of_famine",
                    "locust_of_death", "locust_of_conquest", "mom_knife")) {
                f.reset();
                var familiar = FamiliarHelper.spawnFamiliars(f.owner, id(type), f.owner.position()).get(0);
                int before = f.count("blue_fly");
                f.owner.getRandom().setSeed(successSeed());
                MinecraftForge.EVENT_BUS.post(new LivingDamageEvent(target, ModDamageSources.familiar(helper.getLevel(), familiar, f.owner), 2));
                helper.assertTrue(f.count("blue_fly") == before, "All familiar direct sources are excluded: " + type);
            }
            for (DamageSource source : List.of(ModDamageSources.tear(helper.getLevel(), f.owner, new Vec3(1, 0, 0), 0.2D),
                    ModDamageSources.laser(helper.getLevel(), f.owner), f.owner.damageSources().explosion(f.owner, f.owner))) {
                f.reset();
                target.invulnerableTime = 0;
                f.owner.getRandom().setSeed(successSeed());
                target.hurt(source, 2);
                helper.assertTrue(f.count("blue_fly") == 1, "An actual owned damage source can proc: " + source.getMsgId());
            }
            f.reset();
            f.data.setCount(id("blue_fly"), 64);
            f.owner.getRandom().setSeed(successSeed());
            MinecraftForge.EVENT_BUS.post(new LivingDamageEvent(target, f.owner.damageSources().playerAttack(f.owner), 2));
            helper.assertTrue(f.count("blue_fly") == 64, "A full shared quota does not accumulate unfulfilled demands");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void dealDamageDispatchProvidesTheAttackerVictimAmountAndHitPosition(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            StatManager.modifySetWithId(f.owner, ModSetAbilities.CAT.getId(), 3);
            Mob target = f.mob(EntityType.HUSK, f.owner.position().add(3, 0, 0));
            List<ExecutableEffectContext> observed = new ArrayList<>();
            Consumer<BeforeTriggerModuleActiveEvent> listener = event -> {
                var ctx = event.getContext();
                if (ctx.getEntity() == f.owner && ctx.get(ContextKeys.EVENT) instanceof LivingDamageEvent) observed.add(ctx.copy(null));
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, BeforeTriggerModuleActiveEvent.class, listener);
            try { target.hurt(f.owner.damageSources().playerAttack(f.owner), 2); }
            finally { MinecraftForge.EVENT_BUS.unregister(listener); }
            helper.assertTrue(observed.size() == 1, "A real hit dispatches the damage stage once to the attacker");
            var ctx = observed.get(0);
            helper.assertTrue(ctx.get(ContextKeys.SECONDARY_LIVING_ENTITIES).equals(List.of(target))
                    && ctx.get(ContextKeys.DOUBLE).equals(List.of(2.0))
                    && ctx.get(ContextKeys.TARGET_POSITION).equals(target.position())
                    && ctx.get(ContextKeys.ENTITY).equals(List.of(f.owner)), "Reusable damage context preserves victim, final amount, position and direct source");
        }
        helper.succeed();
    }

    private static long successSeed() {
        for (long seed = 0; ; seed += 1_000_003L) {
            if (RandomSource.create(seed).nextDouble() < TheMulligan.chance()) return seed;
        }
    }

    private static long hurtSuccessSeed() {
        for (long seed = 0; ; seed += 1_000_003L) {
            var random = RandomSource.create(seed);
            if (random.nextDouble() >= Infestation.chance(7.5)) continue;
            random.nextInt(2);
            if (random.nextDouble() < SpiderBaby.chance(7.5)) return seed;
        }
    }

    private static void tickCurios(ServerPlayer player) {
        CurioSlotManager.tick(player);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final ServerPlayer owner;
        final PlayerFamiliarData data;
        final PlayerIsaacItems items;
        final List<Entity> mobs = new ArrayList<>();
        Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "swarm-items"));
            Vec3 base = helper.absolutePos(BlockPos.ZERO).getCenter();
            owner.setPos(base.x + 3, 220, base.z + 3);
            DefaultAttributeManager.apply(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
        }
        void grant(String name) {
            PassiveItem item = (PassiveItem) ForgeRegistries.ITEMS.getValue(id(name));
            ItemStack stack = new ItemStack(item);
            ((PassiveAbility) item.getAbility()).onObtain(owner, stack);
            PassiveItem.setHasBeenUsed(stack, true);
            items.addItem(owner, stack);
        }
        int count(String name) { return data.getCount(id(name)); }
        int moduleCount(String name) {
            return owner.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getTriggerModules().snapshot().modules().stream()
                    .filter(entry -> entry.id().equals(id(name))).mapToInt(entry -> entry.stacks()).sum();
        }
        void reset() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            data.pruneEmptyEntries();
        }
        void hurt(float amount, boolean self, boolean canceled) {
            LivingHurtEvent event = new LivingHurtEvent(owner,
                    self ? PlayerHelper.getSelfDamageSource(owner) : owner.damageSources().generic(), amount);
            event.setCanceled(canceled);
            MinecraftForge.EVENT_BUS.post(event);
        }
        AttackContext attack(AttackType type) {
            return AttackContext.builder(owner, owner).attackType(type).position(owner.position().add(0, 0.5, 0))
                    .direction(new Vec3(1, 0, 0)).damage(2.0).speed(4).range(8).build();
        }
        void prepare(AttackContext context) {
            var request = AttackRequest.generated(owner, ModAttackTypes.BULLET.get(), AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.FULL, false);
            MinecraftForge.EVENT_BUS.post(new AttackContextPrepareEvent(request, new AttackPlan(request, List.of(context)), context, 0));
        }
        void plan(AttackOrigin origin, int count) {
            var request = AttackRequest.generated(owner, ModAttackTypes.BULLET.get(), origin, AttackPipelineMode.FULL, false);
            List<AttackContext> contexts = new ArrayList<>();
            for (int i = 0; i < count; i++) contexts.add(attack(ModAttackTypes.BULLET.get()));
            MinecraftForge.EVENT_BUS.post(new AttackPlanEvent(request, new AttackPlan(request, contexts)));
        }
        void hit(AttackContext context, double damage, boolean canceled) {
            var bullet = new AbstractLaserAttack.LaserProjectile(context);
            var event = new IsaacAttackAfterHitEvent(bullet, owner, context.getRootTypeId(), context.getTrigger(),
                    new EntityHitResult(owner), damage, 20);
            event.setCanceled(canceled);
            MinecraftForge.EVENT_BUS.post(event);
        }
        void fireTear() {
            BulletManager manager = new BulletManager();
            var context = attack(ModAttackTypes.BULLET.get());
            prepare(context);
            manager.spawn(BulletState.from(context).lifetime(20).collisionWidth(0.2).collisionHeight(0.2).build());
            owner.getRandom().setSeed(successSeed());
            try { for (int i = 0; i < 3; i++) manager.tick(helper.getLevel()); }
            finally { for (var bullet : List.copyOf(manager.activeStates())) manager.remove(bullet); }
        }
        <T extends Mob> T mob(EntityType<T> type, Vec3 pos) {
            T mob = type.create(helper.getLevel());
            mob.setPos(pos);
            mob.setNoAi(true);
            mob.setNoGravity(true);
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            mob.getAttribute(Attributes.ARMOR).setBaseValue(0);
            mob.setHealth(100);
            helper.assertTrue(helper.getLevel().addFreshEntity(mob), "Test mob admitted");
            mobs.add(mob);
            return mob;
        }
        void use(String name) {
            ActiveItem item = (ActiveItem) ForgeRegistries.ITEMS.getValue(id(name));
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            owner.getCooldowns().removeCooldown(item);
            item.use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
        }
        @Override public void close() {
            mobs.forEach(Entity::discard);
            reset();
            items.clearPlayerPassiveItems(owner);
            owner.discard();
        }
    }
}
