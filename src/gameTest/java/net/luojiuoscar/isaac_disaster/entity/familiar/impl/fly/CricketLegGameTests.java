package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.CricketLeg;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_cricket_leg")
@PrefixGameTestTemplate(false)
public final class CricketLegGameTests {
    @GameTest(template = "trajectory_empty")
    public static void cricketLegIsARegisteredTrinket(GameTestHelper helper) {
        var item = ForgeRegistries.ITEMS.getValue(id("cricket_leg"));
        helper.assertTrue(item instanceof Trinket,
                "Cricket Leg must be registered as a trinket");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void warLocustInheritsBomberBoyAtAdmission(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.BOMBER_BOY, ModTriggerModules.BOMBER_BOY);
            LocustOfWarEntity war = (LocustOfWarEntity) f.spawn("locust_of_war");
            Vec3 center = f.owner.position().add(2, 0, 0);
            war.setPos(center);
            war.onContactHit(f.owner, f.owner, center, 0, false);
            var children = helper.getLevel().getEntitiesOfClass(IsaacBomb.class, new AABB(center, center).inflate(10));
            helper.assertTrue(children.size() == 4,
                    "War locust must execute inherited Bomber Boy and create four child bombs; found " + children.size());
            helper.assertTrue(children.stream().allMatch(b -> !b.isOriginal() && b.getOwner() == f.owner),
                    "Child bombs preserve ownership and cannot recursively create another cross");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredPoolContainsOnlyTheFiveLocusts(GameTestHelper helper) {
        var pool = FamiliarHelper.getRegisteredFamiliarTypeIds()
                .filter(type -> CricketLeg.isEligibleType(FamiliarHelper.getFamiliarType(type))).toList();
        helper.assertTrue(pool.size() == 5 && pool.stream().allMatch(type -> type.getPath().startsWith("locust_of_")),
                "Registered class markers select five locusts and exclude ordinary flies, spiders and knives");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void batchAdditionUsesProvidedRandomAndSharedQuota(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            for (int seed = 0; seed < 40; seed++) {
                f.data.clearRequirements();
                int expected = RandomSource.create(seed).nextInt(4) + 1;
                helper.assertTrue(ModFamiliarEntities.LOCUST_OF_CONQUEST.get().addEntry(f.owner,
                        ModFamiliarEntities.LOCUST_OF_CONQUEST.getId(), RandomSource.create(seed)), "Conquest batch is added");
                helper.assertTrue(f.total() == expected && f.data.getEntries().stream().allMatch(e -> e.hasNoEntities()),
                        "Conquest draws 1–4 from the caller's RNG and queues requirements without creating entities");
            }
            f.data.clearRequirements();
            f.data.addCount(ModFamiliarEntities.BLUE_FLY.getId(), 64);
            helper.assertTrue(ModFamiliarEntities.LOCUST_OF_WAR.get().addEntry(f.owner,
                    ModFamiliarEntities.LOCUST_OF_WAR.getId(), f.owner.getRandom()), "Default batch is added");
            helper.assertTrue(f.total() == 64 && f.data.getCount(ModFamiliarEntities.BLUE_FLY.getId()) == 63
                            && f.data.getCount(ModFamiliarEntities.LOCUST_OF_WAR.getId()) == 1,
                    "One default locust replaces one blue fly and preserves the shared limit");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void allAttributedKillSourcesIncludingFriendlyVictimsProc(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            StatManager.addTriggerModule(f.owner, ModTriggerModules.CRICKET_LEG.getId(), 6);
            var war = f.spawn("locust_of_war");
            Wolf friend = f.victim(EntityType.WOLF);
            friend.setTame(true);
            friend.setOwnerUUID(f.owner.getUUID());
            var sources = List.of(f.owner.damageSources().playerAttack(f.owner),
                    ModDamageSources.familiar(helper.getLevel(), war, f.owner),
                    helper.getLevel().damageSources().explosion(war, f.owner));
            for (var source : sources) {
                int before = f.total();
                long seed = 0;
                while (RandomSource.create(seed).nextDouble() >= 1.0 / 3) seed += 1_000_003L;
                f.owner.getRandom().setSeed(seed);
                MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(friend, source));
                helper.assertTrue(f.total() > before && f.total() - before <= 4,
                        "Every attributed kill event produces one batch, including an owned wolf: " + source.getMsgId());
            }
            int before = f.total();
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(friend, helper.getLevel().damageSources().generic()));
            helper.assertTrue(f.total() == before, "Unattributed death does not grant this player locusts");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledDeathAndMissingContextDoNotGenerateLocusts(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            CricketLeg effect = new CricketLeg();
            var context = new ExecutableEffectContext(f.owner);
            helper.assertTrue(!effect.applyEffect(context), "A kill effect requires the real death event");
            var death = new LivingDeathEvent(f.victim(EntityType.HUSK), f.owner.damageSources().playerAttack(f.owner));
            death.setCanceled(true);
            context.set(ContextKeys.EVENT, death);
            context.set(ContextKeys.AMPLIFIER, 6.0);
            helper.assertTrue(effect.applyEffect(context) && f.total() == 0, "Canceled death safely completes with no reward");
            death.setCanceled(false);
            context.set(ContextKeys.AMPLIFIER, 0.0);
            helper.assertTrue(effect.applyEffect(context) && f.total() == 0, "Zero strength safely completes with no reward");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void killChanceUsesEntityRandomAndSelectsExactlyOneBatch(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.EVENT,
                    new LivingDeathEvent(f.victim(EntityType.HUSK), f.owner.damageSources().playerAttack(f.owner)));
            context.set(ContextKeys.AMPLIFIER, 1.0);
            var effect = new CricketLeg();
            int successes = 0;
            for (int seed = 0; seed < 150; seed++) {
                f.data.clearRequirements();
                long randomSeed = seed * 1_000_003L;
                f.owner.getRandom().setSeed(randomSeed);
                boolean expected = RandomSource.create(randomSeed).nextDouble() < 1.0 / 6;
                helper.assertTrue(effect.applyEffect(context), "A missed roll is a successful no-op");
                helper.assertTrue((f.total() > 0) == expected, "Proc roll uses the entity's RNG at seed " + seed);
                if (expected) {
                    successes++;
                    helper.assertTrue(f.data.getEntries().stream().filter(e -> e.getCount() > 0).count() == 1,
                            "Each successful roll chooses one type rather than one roll per copy");
                }
            }
            helper.assertTrue(successes > 0 && successes < 150, "Both probability branches were exercised");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void extraCopiesCannotRaiseChancePastTwoContributions(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.EVENT,
                    new LivingDeathEvent(f.victim(EntityType.HUSK), f.owner.damageSources().playerAttack(f.owner)));
            var effect = new CricketLeg();
            for (int amplifier : new int[]{2, 3, 6, Integer.MAX_VALUE}) {
                context.set(ContextKeys.AMPLIFIER, (double) amplifier);
                for (int seed = 0; seed < 40; seed++) {
                    f.data.clearRequirements();
                    long randomSeed = seed * 1_000_003L;
                    f.owner.getRandom().setSeed(randomSeed);
                    boolean expected = RandomSource.create(randomSeed).nextDouble() < 1.0 / 3;
                    helper.assertTrue(effect.applyEffect(context) && (f.total() > 0) == expected,
                            "Two or more contributions all use the same one-third roll: " + amplifier);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalEquipSwallowAndRemovalKeepAggregateStrength(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var slots = f.slots();
            ItemStack ordinary = new ItemStack(ModTrinkets.CRICKET_LEG.get());
            slots.setStackInSlot(0, ordinary);
            f.tickCurios();
            helper.assertTrue(f.amplifier() == 1 && f.total() == 0, "Equip adds one contribution without a first reward");
            CurioSlotManager.requestReconcile(f.owner);
            f.tickCurios();
            helper.assertTrue(f.amplifier() == 1, "Repeated reconciliation does not duplicate the module");
            helper.assertTrue(CurioSlotManager.swallowTrinkets(f.owner) == 1, "Physical ordinary copy is swallowed");
            f.tickCurios();
            helper.assertTrue(f.amplifier() == 1 && slots.getStackInSlot(0).isEmpty(), "Swallow preserves the existing contribution");
            ItemStack golden = new ItemStack(ModTrinkets.CRICKET_LEG.get());
            Trinket.setEnchanted(golden, true);
            slots.setStackInSlot(0, golden);
            f.tickCurios();
            helper.assertTrue(f.amplifier() == 3, "One swallowed ordinary and one equipped gold contribute three");
            helper.assertTrue(CricketLeg.getChance(f.amplifier()) == 1.0 / 3,
                    "Actual contributions are retained for removal while effective chance caps at two");
            slots.setStackInSlot(0, ItemStack.EMPTY);
            f.tickCurios();
            helper.assertTrue(f.amplifier() == 1, "Unequipping gold removes exactly two contributions");
            f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow()
                    .removeAt(f.owner, 0);
            helper.assertTrue(f.amplifier() == 0 && f.total() == 0, "Removing the swallowed copy removes the final contribution");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void goldenSwallowedStateAndModuleStrengthSurviveNbtReload(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            ItemStack golden = new ItemStack(ModTrinkets.CRICKET_LEG.get());
            Trinket.setEnchanted(golden, true);
            f.slots().setStackInSlot(0, golden);
            f.tickCurios();
            CurioSlotManager.swallowTrinkets(f.owner);
            f.tickCurios();
            CompoundTag items = new CompoundTag();
            var sourceItems = f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            sourceItems.saveNBTData(items);
            var restoredItems = restored.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
            restoredItems.loadNBTData(items);
            CompoundTag effects = new CompoundTag();
            f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow().saveNBTData(effects);
            restored.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow().loadNBTData(effects);
            var loaded = restoredItems.getSwallowedTrinkets().get(0);
            helper.assertTrue(Trinket.isEnchanted(loaded) && Trinket.isConsumed(loaded) && restored.amplifier() == 2,
                    "Existing persistence preserves enhancement, consumed marker and aggregate strength");
            restoredItems.removeAt(restored.owner, 0);
            helper.assertTrue(restored.amplifier() == 0, "Loaded swallowed copy can remove its full contribution");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void warLocustInheritsScatterBomb(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.SCATTER_BOMB, ModTriggerModules.SCATTER_BOMB);
            f.blast((LocustOfWarEntity) f.spawn("locust_of_war"));
            var children = helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds());
            helper.assertTrue(children.size() == 4 && children.stream().allMatch(b -> b.getPower() == 1 && !b.isOriginal()),
                    "Scatter Bomb creates four small children without recursive splitting");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void warLocustInheritsHotBomb(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.HOT_BOMB, ModTriggerModules.HOT_BOMB);
            f.blast((LocustOfWarEntity) f.spawn("locust_of_war"));
            var children = helper.getLevel().getEntitiesOfClass(TimedFireball.class, f.bounds());
            helper.assertTrue(children.size() == 5 && children.stream().allMatch(b -> b.getOwner() == f.owner),
                    "Hot Bomb creates the ordinary power-four batch of five owned fireballs");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void warLocustInheritsSadBomb(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.SAD_BOMB, ModTriggerModules.SAD_BOMB);
            f.blast((LocustOfWarEntity) f.spawn("locust_of_war"));
            var tears = BulletRuntime.INSTANCE.manager(helper.getLevel()).activeStates().stream()
                    .filter(bullet -> bullet.getOwner() == f.owner).toList();
            helper.assertTrue(tears.size() == 8, "Sad Bomb executes the existing ordinary-bomb attack ring; found " + tears.size());
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void warLocustInheritsButtBomb(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.BUTT_BOMBS, ModTriggerModules.BUTT_BOMBS);
            var target = f.victim(EntityType.HUSK);
            f.blast((LocustOfWarEntity) f.spawn("locust_of_war"));
            helper.assertTrue(target.hasEffect(ModEffects.PANIC.get()),
                    "Butt Bomb applies its existing fear effect to a nearby hostile");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void consumedWarLocustExecutesCombinedEffectsOnce(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.BOMBER_BOY, ModTriggerModules.BOMBER_BOY);
            f.addPassive(ModPassiveItems.SCATTER_BOMB, ModTriggerModules.SCATTER_BOMB);
            f.addPassive(ModPassiveItems.HOT_BOMB, ModTriggerModules.HOT_BOMB);
            f.addPassive(ModPassiveItems.SAD_BOMB, ModTriggerModules.SAD_BOMB);
            f.addPassive(ModPassiveItems.BUTT_BOMBS, ModTriggerModules.BUTT_BOMBS);
            var war = (LocustOfWarEntity) f.spawn("locust_of_war");
            var target = f.victim(EntityType.HUSK);
            AtomicInteger executions = new AtomicInteger();
            war.getCachedEffect().add(context -> executions.incrementAndGet());
            f.contact(war, target);
            helper.assertTrue(war.isRemoved() && f.total() == 0 && executions.get() == 1,
                    "Real contact consumes and removes its source before executing cached effects exactly once");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).size() == 8,
                    "Combined Bomber Boy and Scatter Bomb produce four children each");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(TimedFireball.class, f.bounds()).size() == 5,
                    "Combined Hot Bomb executes after source removal");
            helper.assertTrue(BulletRuntime.INSTANCE.manager(helper.getLevel()).activeStates().stream()
                            .filter(bullet -> bullet.getOwner() == f.owner).count() == 8 && target.hasEffect(ModEffects.PANIC.get()),
                    "Sad Bomb and Butt Bomb also execute after source removal");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void admissionSnapshotDoesNotRetroactivelyAttachNewItems(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            LocustOfWarEntity oldWar = (LocustOfWarEntity) f.spawn("locust_of_war");
            f.addPassive(ModPassiveItems.BOMBER_BOY, ModTriggerModules.BOMBER_BOY);
            f.blast(oldWar);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).isEmpty(),
                    "A locust admitted before the item has no retroactive attachment");
            LocustOfWarEntity newWar = (LocustOfWarEntity) f.spawn("locust_of_war");
            MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(newWar, helper.getLevel()));
            AtomicInteger executions = new AtomicInteger();
            newWar.getCachedEffect().add(context -> executions.incrementAndGet());
            f.blast(newWar);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).size() == 4 && executions.get() == 1,
                    "New locust captures the item once and executes each cached effect once despite a repeated join event");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void canceledWarExplosionDoesNotRunCachedEffects(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.BOMBER_BOY, ModTriggerModules.BOMBER_BOY);
            var war = (LocustOfWarEntity) f.spawn("locust_of_war");
            Consumer<ExplosionEvent.Start> cancel = event -> {
                if (event.getExplosion().getDirectSourceEntity() == war) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Start.class, cancel);
            try {
                f.blast(war);
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).isEmpty(),
                        "Forge Start cancellation prevents both explosion and attached effects");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void ordinaryBombKeepsCacheAndGigaStillDiscardsIt(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.addPassive(ModPassiveItems.BOMBER_BOY, ModTriggerModules.BOMBER_BOY);
            IsaacBomb bomb = EntityHelper.spawnBomb(f.owner.position(), f.owner, helper.getLevel(), Vec3.ZERO, BombData.NORMAL, 80);
            helper.assertTrue(bomb.getCachedEffect().contains(ModExecutableEffects.BOMBER_BOY.get()),
                    "Ordinary bombs still capture item attachments");
            LevelHelper.explodeCustom(helper.getLevel(), bomb.position(), helper.getLevel().damageSources().explosion(bomb, f.owner),
                    ExplosionParameters.standard(4, 60));
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).size() == 5,
                    "Ordinary bomb and its four Bomber Boy children remain present");
            GigaBomb giga = new GigaBomb(helper.getLevel(), f.owner.getX(), f.owner.getY(), f.owner.getZ(), f.owner, 10, 2.5f);
            helper.getLevel().addFreshEntity(giga);
            helper.assertTrue(!giga.getCachedEffect().contains(ModExecutableEffects.BOMBER_BOY.get()), "Giga discards attachments");
            int before = helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).size();
            LevelHelper.explodeCustom(helper.getLevel(), giga.position(), helper.getLevel().damageSources().explosion(giga, f.owner),
                    ExplosionParameters.standard(0, 0));
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(IsaacBomb.class, f.bounds()).size() == before,
                    "Giga explosion event does not execute ordinary item attachments");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void loreUsesMinecraftPercentFormatting(GameTestHelper helper) {
        var ability = ((Trinket) ModTrinkets.CRICKET_LEG.get()).getAbility();
        var ordinary = ability.getDesc(null, null);
        ItemStack gold = new ItemStack(ModTrinkets.CRICKET_LEG.get());
        Trinket.setEnchanted(gold, true);
        helper.assertTrue(ordinary.size() == 3 && ordinary.get(0).getString().contains("16.67%"),
                "Minecraft translation parser formats the ordinary probability and separate quantity line");
        Component goldenLine = ability.getDesc(gold, null).get(0);
        helper.assertTrue(goldenLine.getString().contains("33.33%") && hasDynamicNumber(goldenLine, "33.33"),
                "Golden chance replaces the ordinary percentage inline as a yellow dynamic component");
        helper.assertTrue(ability.getSynergyDesc(gold, null).isEmpty(), "No duplicate golden explanation line is appended");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void callusShowsItsCurrentReductionInline(GameTestHelper helper) {
        var ability = ((Trinket) ModTrinkets.CALLUS.get()).getAbility();
        var ordinary = ability.getDesc(null, null).get(0);
        ItemStack gold = new ItemStack(ModTrinkets.CALLUS.get());
        Trinket.setEnchanted(gold, true);
        var enhanced = ability.getDesc(gold, null).get(0);
        helper.assertTrue(ordinary.getString().contains("1") && hasDynamicNumber(ordinary, "1"),
                "Ordinary Callus displays its one-point reduction as an integer component");
        helper.assertTrue(enhanced.getString().contains("2") && hasDynamicNumber(enhanced, "2")
                        && ability.getSynergyDesc(gold, null).isEmpty(),
                "Golden Callus replaces the number with two instead of repeating an unchanged description");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void locustTrinketsShowCurrentCountsWithoutColoringConfiguredTime(GameTestHelper helper) {
        for (var item : List.of(ModTrinkets.LOCUST_OF_WAR, ModTrinkets.LOCUST_OF_PESTILENCE,
                ModTrinkets.LOCUST_OF_FAMINE, ModTrinkets.LOCUST_OF_DEATH, ModTrinkets.LOCUST_OF_CONQUEST)) {
            var ability = ((Trinket) item.get()).getAbility();
            ItemStack gold = new ItemStack(item.get());
            Trinket.setEnchanted(gold, true);
            var ordinary = ability.getDesc(null, null).get(0);
            var enhanced = ability.getDesc(gold, null).get(0);
            helper.assertTrue(hasDynamicNumber(ordinary, "1") && hasDynamicNumber(enhanced, "2"),
                    "All five trinkets replace the ordinary generation count inline");
            helper.assertTrue(!hasDynamicNumber(enhanced, "40") && ability.getSynergyDesc(gold, null).isEmpty(),
                    "Configured time remains plain text and the separate golden line is removed");
            if (item == ModTrinkets.LOCUST_OF_CONQUEST) {
                helper.assertTrue(hasDynamicNumber(ordinary, "4") && hasDynamicNumber(enhanced, "6"),
                        "Conquest preserves its one-to-four and two-to-six periodic ranges");
            }
        }
        helper.succeed();
    }

    private static boolean hasDynamicNumber(Component component, String expected) {
        return component.visit((style, text) -> expected.equals(text) && style.getColor() != null
                        && style.getColor().getValue() == ColorManager.SYNERGY
                        ? Optional.of(true) : Optional.<Boolean>empty(), Style.EMPTY).orElse(false);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("isaac_disaster", path);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final List<LivingEntity> victims = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "cricket-test"));
            BlockPos anchor = helper.absolutePos(BlockPos.ZERO);
            owner.setPos(anchor.getX() + 4, 220, anchor.getZ() + 4);
            DefaultAttributeManager.apply(owner);
            owner.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(1);
            CurioSlotManager.tick(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        private void addPassive(RegistryObject<Item> item, RegistryObject<TriggerModule> module) {
            owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().addItem(owner, new ItemStack(item.get()));
            StatManager.addTriggerModule(owner, module.getId(), 1);
        }

        private AABB bounds() {
            return owner.getBoundingBox().inflate(20);
        }

        private void blast(LocustOfWarEntity war) {
            Vec3 center = owner.position().add(2, 0, 0);
            war.setPos(center);
            war.onContactHit(owner, owner, center, 0, false);
        }

        private void contact(LocustOfWarEntity war, LivingEntity target) {
            FlyOwnerFrame frame = FlyOwnerFrame.get(owner);
            frame.refresh(owner, helper.getLevel().getGameTime());
            war.setPos(target.getBoundingBox().getCenter().add(0, -war.getBbHeight() * 0.5, 0));
            try {
                var field = AbstractFlyFamiliarEntity.class.getDeclaredField("ownerFrame");
                field.setAccessible(true);
                field.set(war, frame);
                var method = AbstractFlyFamiliarEntity.class.getDeclaredMethod("tryContact", ServerPlayer.class, Vec3.class, Vec3.class);
                method.setAccessible(true);
                helper.assertTrue((boolean) method.invoke(war, owner, war.position(), war.position()), "Real contact is detected");
            } catch (InvocationTargetException failure) {
                if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
                if (failure.getCause() instanceof Error error) throw error;
                throw new AssertionError(failure.getCause());
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
        }

        private <T extends LivingEntity> T victim(EntityType<T> type) {
            T victim = type.create(helper.getLevel());
            victim.setNoGravity(true);
            if (victim instanceof Mob mob) mob.setNoAi(true);
            victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            victim.setHealth(100);
            victim.setPos(owner.position().add(3, 0, 0));
            helper.getLevel().addFreshEntity(victim);
            victims.add(victim);
            return victim;
        }

        private int total() {
            return data.getEntries().stream().mapToInt(entry -> entry.getCount()).sum();
        }

        private int amplifier() {
            var module = owner.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getTriggerModules().get(ModTriggerModules.CRICKET_LEG.getId());
            return module == null ? 0 : module.stacks();
        }

        private IDynamicStackHandler slots() {
            return CuriosApi.getCuriosInventory(owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.TRINKET).orElseThrow().getStacks();
        }

        private void tickCurios() {
            new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(owner));
            CurioSlotManager.tick(owner);
        }

        private AbstractIsaacFamiliarEntity spawn(String name) {
            ResourceLocation type = id(name);
            data.addCount(type, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Familiar is admitted: " + name);
            var entry = data.getEntry(type).orElseThrow();
            return (AbstractIsaacFamiliarEntity) helper.getLevel().getEntity(entry.getEntityId(entry.getEntityCount() - 1));
        }

        @Override
        public void close() {
            for (var entity : helper.getLevel().getEntitiesOfClass(IsaacBomb.class, owner.getBoundingBox().inflate(20))) {
                if (entity.getOwner() == owner) entity.discard();
            }
            for (var entity : helper.getLevel().getEntities((Entity) null, bounds(), e -> e instanceof Projectile)) {
                if (((Projectile) entity).getOwner() == owner) entity.discard();
            }
            for (var victim : victims) victim.discard();
            var bullets = BulletRuntime.INSTANCE.manager(helper.getLevel());
            for (var bullet : bullets.activeStates()) {
                if (bullet.getOwner() == owner) bullets.remove(bullet);
            }
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            owner.discard();
        }
    }
}
