package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.gametest.GameplayTestSettings;
import net.luojiuoscar.isaac_disaster.gametest.FamiliarModifiersGameTests;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.AbstractSpiderFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Exercises registered factories, real hurt handling, poison pulses and ownership accounting. */
@GameTestHolder("isaac_disaster_locust")
@PrefixGameTestTemplate(false)
public final class LocustGameTests {
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();
    private static final double DAMAGE = 3.5;

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void warUsesVanillaExplosionCooldownAndConsumesOnce(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_war")) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            List<Float> contacts = new ArrayList<>();
            List<Float> blasts = new ArrayList<>();
            fixture.listen(event -> {
                helper.assertTrue(event.getSource().getEntity() == fixture.owner
                                && event.getSource().getDirectEntity() == fixture.familiar,
                        "Contact and blast preserve the War locust and its owner");
                helper.assertTrue(fixture.data.getCount(fixture.type) == 0,
                        "Ownership is consumed before any hurt callback");
                if (event.getSource().is(ModDamageType.FAMILIAR)) contacts.add(event.getAmount());
                else {
                    helper.assertTrue(event.getSource().is(DamageTypes.PLAYER_EXPLOSION)
                                    && event.getSource().is(DamageTypeTags.IS_EXPLOSION)
                                    && !event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN),
                            "War uses the ordinary attributed vanilla explosion type and cooldown rules");
                    blasts.add(event.getAmount());
                }
                ((AbstractFlyFamiliarEntity) fixture.familiar).tickFamiliar();
            });
            fixture.contact();
            helper.assertTrue(contacts.equals(List.of(10F)) && blasts.size() == 1,
                    "One current 2D contact and one explosion survive reentrant ticks");
            double blastDamage = expectedWarDamage(20 * StatManager.DAMAGE.getBonus());
            helper.assertTrue(Math.abs(blasts.get(0) - blastDamage) < 0.001,
                    "Open-air blast follows radius-eight falloff from the contact center");
            fixture.assertDamage(Math.max(10, blastDamage));
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void warExplosionUsesActualDamageBonusInsteadOfOwnerAttack(GameTestHelper helper) {
        withDamageBonus(helper, 2.25, () -> {
            try (Fixture fixture = new Fixture(helper, "locust_of_war")) {
                fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(9);
                fixture.contact();
                fixture.assertDamage(Math.max(18, expectedWarDamage(45)));
                fixture.assertConsumed();
            }
        });
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void warExplosionBelowContactDamageRespectsVanillaCooldown(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_war")) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            AtomicInteger blasts = new AtomicInteger();
            fixture.listen(event -> {
                if (event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) blasts.incrementAndGet();
            });
            fixture.contact();
            helper.assertTrue(blasts.get() == 1, "Explosion is attempted after the stronger contact hit");
            fixture.assertDamage(40);
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void warExplodesAfterCanceledContact(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_war")) {
            AtomicInteger blasts = new AtomicInteger();
            fixture.listen(event -> {
                if (event.getSource().is(ModDamageType.FAMILIAR)) event.setCanceled(true);
                else if (event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) blasts.incrementAndGet();
            });
            fixture.contact();
            helper.assertTrue(blasts.get() == 1, "Canceled contact still triggers one War explosion");
            fixture.assertDamage(expectedWarDamage(20 * StatManager.DAMAGE.getBonus()));
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void warExplodesAtContactSnapshotAfterLethalHitAndHurtsFriends(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_war", true)) {
            fixture.owner.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            fixture.owner.setHealth(100);
            try {
                var spawnProtection = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
                spawnProtection.setAccessible(true);
                spawnProtection.setInt(fixture.owner, 0);
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
            Vec3 center = fixture.target.getBoundingBox().getCenter();
            Zombie bystander = fixture.spawnTarget(fixture.target.position().add(6, 0, 0));
            Zombie outside = fixture.spawnTarget(fixture.target.position().add(8.5, 0, 0));
            Wolf friend = EntityType.WOLF.create(helper.getLevel());
            friend.setTame(true);
            friend.setOwnerUUID(fixture.owner.getUUID());
            friend.setNoAi(true);
            friend.setNoGravity(true);
            friend.setPos(fixture.target.position().add(1, 0, 0));
            helper.assertTrue(helper.getLevel().addFreshEntity(friend), "Owned wolf is present");
            // Verify allied entities as well as the owner's own explosion damage.
            var scoreboard = helper.getLevel().getScoreboard();
            var team = scoreboard.addPlayerTeam("war-" + NEXT_FIXTURE.get());
            team.setAllowFriendlyFire(true);
            scoreboard.addPlayerToTeam(fixture.owner.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(friend.getScoreboardName(), team);
            float friendHealth = friend.getHealth();
            float ownerHealth = fixture.owner.getHealth();
            Vec3 ownerMotion = fixture.owner.getDeltaMovement();
            Vec3 friendMotion = friend.getDeltaMovement();
            List<Vec3> centers = new ArrayList<>();
            Consumer<ExplosionEvent.Start> listener = event -> {
                if (event.getExplosion().getDamageSource().getDirectEntity() == fixture.familiar) {
                    centers.add(event.getExplosion().getPosition());
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Start.class, listener);
            try {
                fixture.target.setHealth(1);
                fixture.listen(event -> {
                    if (event.getSource().is(ModDamageType.FAMILIAR))
                        fixture.target.setPos(fixture.target.position().add(20, 0, 0));
                });
                fixture.contact();
                helper.assertTrue(!fixture.target.isAlive(), "Ordinary contact kills the target");
                helper.assertTrue(centers.size() == 1 && centers.get(0).distanceToSqr(center) < 1e-8,
                        "Explosion uses the captured collision point even when hurt moves and kills the target");
                helper.assertTrue(bystander.getHealth() < 100 && outside.getHealth() == 100,
                        "Power four damages an exposed hostile six blocks away, with actual radius eight");
                helper.assertTrue(friend.getHealth() < friendHealth && !friend.getDeltaMovement().equals(friendMotion),
                        "An owned allied wolf receives explosion damage and knockback");
                helper.assertTrue(fixture.owner.getHealth() < ownerHealth,
                        "The War locust owner receives explosion damage: " + ownerHealth + " -> " + fixture.owner.getHealth());
                helper.assertTrue(!fixture.owner.getDeltaMovement().equals(ownerMotion),
                        "The War locust owner receives explosion knockback too");
                fixture.assertConsumed();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
                scoreboard.removePlayerTeam(team);
                bystander.discard();
                outside.discard();
                friend.discard();
            }
        }
        helper.succeed();
    }

    // A husk is 1.95 blocks high. At its contact center, distance/radius is .975/8.
    // Full exposure gives (impact^2 + impact)/2 = .8246142578125; the vanilla +1 is retained.
    private static double expectedWarDamage(double centerDamage) {
        return 0.8246142578125 * (centerDamage - 1) + 1;
    }

    private static void withDamageBonus(GameTestHelper helper, double bonus, Runnable action) {
        GameplayTestSettings.with(helper, json -> json.addProperty("damage_bonus", bonus), action);
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void blueFlyDealsTwiceCurrentDamage(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "blue_fly")) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            fixture.contact();
            fixture.assertDamage(10);
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void blueSpiderDealsTwoAndAHalfTimesCurrentDamage(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "blue_spider")) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            fixture.contact();
            fixture.assertDamage(12.5);
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void pestilenceAppliesTwoPoisonPulsesFromContactDamageSnapshot(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_pestilence")) {
            fixture.contact();
            fixture.assertDamage(2 * DAMAGE);
            var poison = fixture.target.getEffect(ModEffects.DEADLY_POISON.get());
            helper.assertTrue(poison != null && poison.getAmplifier() == 5 && poison.getDuration() == 50,
                    "D=3.5 converts to custom poison amplifier 5 for the current 50-tick duration");
            // Changing the owner's attack after contact must not change the existing poison.
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            List<Integer> pulses = new ArrayList<>();
            float previous = fixture.target.getHealth();
            for (int tick = 1; tick <= 60; tick++) {
                fixture.target.tick();
                if (fixture.target.getHealth() < previous) pulses.add(tick);
                previous = fixture.target.getHealth();
            }
            helper.assertTrue(pulses.equals(List.of(11, 31)),
                    "The current 50-tick poison delivers two pulses past contact hurt cooldown: " + pulses);
            fixture.assertDamage(4 * DAMAGE);
            helper.assertTrue(!fixture.target.hasEffect(ModEffects.DEADLY_POISON.get()), "Poison expires");
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void famineDealsTwiceDamageAndSlows(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_famine")) {
            fixture.contact();
            fixture.assertDamage(2 * DAMAGE);
            var slow = fixture.target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            helper.assertTrue(slow != null && slow.getAmplifier() == 1 && slow.getDuration() == 40,
                    "Famine applies Slowness II for two seconds");
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void deathDealsOneFourDamageHitAndConsumesOnlyOnce(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_death")) {
            fixture.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            List<Float> hits = new ArrayList<>();
            fixture.listen(event -> {
                helper.assertTrue(event.getSource().is(ModDamageType.FAMILIAR)
                                && event.getSource().getEntity() == fixture.owner
                                && event.getSource().getDirectEntity() == fixture.familiar,
                        "Death's single hit retains familiar source and owner attribution");
                helper.assertTrue(fixture.data.getCount(fixture.type) == 0,
                        "Ownership is consumed before each hurt callback");
                hits.add(event.getAmount());
                ((AbstractFlyFamiliarEntity) fixture.familiar).tickFamiliar();
            });
            fixture.contact();
            helper.assertTrue(hits.equals(List.of(20F)), "Death delivers one current 4D attempt: " + hits);
            fixture.assertDamage(20);
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void conquestHasOrdinaryPerFlyDamage(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_conquest")) {
            fixture.contact();
            fixture.assertDamage(2 * DAMAGE);
            helper.assertTrue(fixture.target.getActiveEffects().isEmpty(), "White locust has no extra status effect");
            fixture.assertConsumed();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void rejectedContactsDoNotApplyPoisonSlowOrExtraDamage(GameTestHelper helper) {
        for (String name : List.of("locust_of_pestilence", "locust_of_famine", "locust_of_death")) {
            try (Fixture fixture = new Fixture(helper, name)) {
                AtomicInteger attempts = new AtomicInteger();
                fixture.listen(event -> {
                    attempts.incrementAndGet();
                    event.setCanceled(true);
                });
                fixture.contact();
                fixture.assertDamage(0);
                helper.assertTrue(attempts.get() == 1 && fixture.target.getActiveEffects().isEmpty(),
                        "Canceled contact has no follow-up effect: " + name);
                fixture.assertConsumed();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void variantsShareTheFlyLimitAndRemainIndependentlyAddressable(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "blue_fly")) {
            fixture.data.setCount(fixture.type, 64);
            for (String name : List.of("locust_of_pestilence", "locust_of_famine", "locust_of_death", "locust_of_conquest", "locust_of_war")) {
                ResourceLocation type = ResourceLocation.fromNamespaceAndPath("isaac_disaster", name);
                int result = helper.getLevel().getServer().getCommands().performPrefixedCommand(
                        fixture.owner.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
                        "isd familiar @s add " + type + " 1");
                helper.assertTrue(result == 1 && fixture.data.getEffectiveCount(type) == 1,
                        "Command accepts an independent locust descriptor: " + name);
            }
            fixture.data.addCount(fixture.type, 1);
            helper.assertTrue(fixture.data.getEffectiveCount(fixture.type) == 59 && fixture.data.getCount(fixture.type) == 59,
                    "Five higher-priority locusts replace five ordinary flies within the shared 64 slots");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void equalPriorityLocustsDoNotPreemptExistingLocusts(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper, "locust_of_pestilence")) {
            fixture.data.setCount(fixture.type, 64);
            for (String name : List.of("locust_of_famine", "locust_of_death", "locust_of_conquest", "locust_of_war", "blue_fly")) {
                ResourceLocation incoming = ResourceLocation.fromNamespaceAndPath("isaac_disaster", name);
                fixture.data.addCount(incoming, 1);
                helper.assertTrue(fixture.data.getCount(incoming) == 0 && fixture.data.getEffectiveCount(incoming) == 0,
                        "Equal or lower priority cannot replace an admitted locust or retain overflow: " + name);
            }
            helper.assertTrue(fixture.data.getCount(fixture.type) == 64 && fixture.data.getEffectiveCount(fixture.type) == 64,
                    "Previously admitted locusts keep all 64 slots");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void hiveMindDoublesEachSwarmContactWithoutBffsOrDuplicateOverlap(GameTestHelper helper) {
        for (String name : List.of("blue_fly", "blue_spider", "locust_of_famine", "locust_of_death", "locust_of_conquest")) {
            try (Fixture f = new Fixture(helper, name)) {
                f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
                grant(f.owner, ModPassiveItems.BFFS.get());
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                f.contact();
                f.assertDamage(name.equals("blue_spider") ? 25 : name.equals("locust_of_death") ? 40 : 20);
                f.assertConsumed();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void bffsAloneLeavesSwarmContactAndPoisonUnchanged(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, "locust_of_pestilence")) {
            grant(f.owner, ModPassiveItems.BFFS.get());
            f.contact();
            f.assertDamage(2 * DAMAGE);
            helper.assertTrue(f.target.getEffect(ModEffects.DEADLY_POISON.get()).getAmplifier()
                    == DeadlyPoisonEffect.amplifierForDamage(DAMAGE), "BFFS cannot boost swarm poison indirectly");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void boostedPoisonUsesTheContactSnapshotExactlyOnce(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper, "locust_of_pestilence")) {
            grant(f.owner, ModPassiveItems.HIVE_MIND.get());
            f.contact();
            f.assertDamage(4 * DAMAGE);
            var poison = f.target.getEffect(ModEffects.DEADLY_POISON.get());
            helper.assertTrue(poison != null && poison.getDuration() == 50
                    && poison.getAmplifier() == DeadlyPoisonEffect.amplifierForDamage(2 * DAMAGE),
                    "Hive Mind doubles the poison snapshot without doubling the multiplier twice");
            f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().removeFromIndex(f.owner, 0);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            for (int tick = 0; tick < 60; tick++) f.target.tick();
            f.assertDamage(8 * DAMAGE);
            helper.assertTrue(!f.target.hasEffect(ModEffects.DEADLY_POISON.get()), "The unchanged duration still expires");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void boostedWarAddsTenConfiguredUnitsAndKeepsVanillaCooldown(GameTestHelper helper) {
        withDamageBonus(helper, 1.7, () -> {
            try (Fixture f = new Fixture(helper, "locust_of_war")) {
                grant(f.owner, ModPassiveItems.BFFS.get());
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                List<Float> blasts = new ArrayList<>();
                f.listen(event -> {
                    if (event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) blasts.add(event.getAmount());
                    else helper.assertTrue(Math.abs(event.getAmount() - 4 * DAMAGE) < 0.001, "Contact doubles once");
                });
                f.contact();
                double expected = expectedWarDamage(30 * StatManager.DAMAGE.getBonus());
                helper.assertTrue(blasts.size() == 1 && Math.abs(blasts.get(0) - expected) < 0.001,
                        "One ordinary explosion uses 30 configured damage units: " + blasts);
                f.assertDamage(expected);
                f.assertConsumed();
            }
        });
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void enabledDuplicateEntriesGiveSixDamageUnitsAndFortyExplosionUnits(GameTestHelper helper) {
        GameplayTestSettings.with(helper, json -> json.addProperty("allow_same_familiar_modifier_entry_stacking", true), () -> {
            try (Fixture f = new Fixture(helper, "blue_fly")) {
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                f.familiar.tick();
                helper.assertTrue(f.familiar.getVisualScale() == 2 && f.familiar.getEnhancement() == 2,
                        "Two active copies add their visual and enhancement increments");
                f.contact();
                f.assertDamage(6 * DAMAGE);
                f.assertConsumed();
            }
            try (Fixture f = new Fixture(helper, "locust_of_war")) {
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                List<Float> blasts = new ArrayList<>();
                f.listen(event -> {
                    if (event.getSource().is(DamageTypes.PLAYER_EXPLOSION)) blasts.add(event.getAmount());
                });
                f.contact();
                double expected = expectedWarDamage(40 * StatManager.DAMAGE.getBonus());
                helper.assertTrue(blasts.size() == 1 && Math.abs(blasts.get(0) - expected) < 0.001,
                        "War independently interprets two enhancement points as ten extra units each");
                f.assertDamage(Math.max(6 * DAMAGE, expected));
                f.assertConsumed();
            }
        });
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "locust_contact")
    public static void anotherRegisteredSourceAddsToHiveMindWithStackingDisabled(GameTestHelper helper) {
        GameplayTestSettings.with(helper, json -> json.addProperty("allow_same_familiar_modifier_entry_stacking", false), () -> {
            try (Fixture f = new Fixture(helper, "blue_fly")) {
                grant(f.owner, ModPassiveItems.HIVE_MIND.get());
                StatManager.addFamiliarModifier(f.owner, FamiliarModifiersGameTests.EXTRA_ENHANCEMENT, 1);
                helper.assertTrue(f.familiar.getEnhancement() == 1.5, "Different source IDs add even with duplicate stacking disabled");
                f.contact();
                f.assertDamage(5 * DAMAGE);
            }
        });
        helper.succeed();
    }

    private static void grant(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        ((PassiveAbility) ((PassiveItem) item).getAbility()).onObtain(player, stack);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().addItem(player, stack);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final ResourceLocation type;
        private final AbstractIsaacFamiliarEntity familiar;
        private final Zombie target;
        private final boolean registeredOwner;
        private final boolean originalPvp;
        private Consumer<LivingAttackEvent> listener;

        private Fixture(GameTestHelper helper, String name) {
            this(helper, name, false);
        }

        private Fixture(GameTestHelper helper, String name, boolean registeredOwner) {
            this.helper = helper;
            this.registeredOwner = registeredOwner;
            var level = helper.getLevel();
            originalPvp = level.getServer().isPvpAllowed();
            GameProfile profile = new GameProfile(UUID.randomUUID(), "locust-test");
            if (registeredOwner) {
                level.getServer().setPvpAllowed(true);
                owner = new ServerPlayer(level.getServer(), level, profile);
                new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), owner);
            } else {
                owner = FakePlayerFactory.get(level, profile);
            }
            BlockPos anchor = helper.absolutePos(BlockPos.ZERO);
            double y = Math.min(220 + NEXT_FIXTURE.getAndIncrement() * 12, level.getMaxBuildHeight() - 16);
            owner.setPos(anchor.getX() + 4, y, anchor.getZ() + 4);
            owner.setGameMode(GameType.SURVIVAL);
            owner.setInvulnerable(false);
            owner.getAbilities().invulnerable = false;
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(DAMAGE);
            if (registeredOwner) level.addNewPlayer(owner);
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            type = ResourceLocation.fromNamespaceAndPath("isaac_disaster", name);
            helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(type), "Registered familiar descriptor: " + type);
            data.setCount(type, 1);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Scheduler creates " + type);
            familiar = (AbstractIsaacFamiliarEntity) level.getEntity(data.getEntry(type).orElseThrow().getEntityId(0));
            helper.assertTrue(familiar != null && familiar.getOwner() == owner && familiar.isValidFamiliar(),
                    "Factory admits an owned, linked familiar");
            target = spawnTarget(owner.position().add(2, 0, 0));
        }

        private Zombie spawnTarget(Vec3 position) {
            Zombie target = EntityType.HUSK.create(helper.getLevel());
            target.setNoAi(true);
            target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setHealth(100);
            target.setPos(position);
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "Hostile target is present");
            return target;
        }

        private void listen(Consumer<LivingAttackEvent> callback) {
            listener = event -> { if (event.getEntity() == target) callback.accept(event); };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, listener);
        }

        private void contact() {
            try {
                Class<?> base;
                if (familiar instanceof AbstractFlyFamiliarEntity) {
                    FlyOwnerFrame frame = FlyOwnerFrame.get(owner);
                    frame.refresh(owner, helper.getLevel().getGameTime());
                    var field = AbstractFlyFamiliarEntity.class.getDeclaredField("ownerFrame");
                    field.setAccessible(true);
                    field.set(familiar, frame);
                    base = AbstractFlyFamiliarEntity.class;
                } else {
                    base = AbstractSpiderFamiliarEntity.class;
                }
                familiar.setPos(target.getBoundingBox().getCenter().add(0, -familiar.getBbHeight() * 0.5, 0));
                var contact = base.getDeclaredMethod("tryContact", ServerPlayer.class, Vec3.class, Vec3.class);
                contact.setAccessible(true);
                helper.assertTrue((boolean) contact.invoke(familiar, owner, familiar.position(), familiar.position()),
                        "A real contact is detected for " + type + " at " + familiar.position());
            } catch (InvocationTargetException failure) {
                if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
                if (failure.getCause() instanceof Error error) throw error;
                throw new AssertionError(failure.getCause());
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
        }

        private void assertDamage(double expected) {
            helper.assertTrue(Math.abs(target.getHealth() - (100 - expected)) < 0.001,
                    type + " expected " + expected + " damage, health=" + target.getHealth());
        }

        private void assertConsumed() {
            helper.assertTrue(familiar.isRemoved() && data.getCount(type) == 0 && !data.containsEntity(type, familiar.getUUID()),
                    "Contact consumes exactly one requirement and unlinks the entity");
        }

        @Override
        public void close() {
            if (listener != null) MinecraftForge.EVENT_BUS.unregister(listener);
            owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS)
                    .ifPresent(items -> items.clearPlayerPassiveItems(owner));
            target.discard();
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            data.pruneEmptyEntries();
            if (registeredOwner) {
                helper.getLevel().removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
                helper.getLevel().getServer().setPvpAllowed(originalPvp);
            } else owner.discard();
        }
    }
}
