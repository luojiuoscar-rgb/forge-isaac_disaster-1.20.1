package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ThirdPassiveBatchGameTests {
    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "passive_batch3"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        return player;
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void bloodBagKeepsPermanentHealthButRemovesSpeed(GameTestHelper helper) {
        var player = player(helper);
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        float maxHealth = player.getMaxHealth();
        var ability = ModPassiveAbilities.BLOOD_BAG.get();
        ability.handleObtain(player, null);
        ability.handleFirstObtain(player, null);
        helper.assertTrue(Math.abs(player.getMaxHealth() - maxHealth - StatManager.MAX_HEALTH.getBonus()) < 0.01,
                "The existing blood bag grants one permanent health unit");
        ability.handleRemove(player, null);
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed) < 1e-6,
                "Removing blood bag restores speed");
        helper.assertTrue(player.getMaxHealth() > maxHealth, "Removing blood bag keeps first-obtain health");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void oldBandageHealthIsEmptyAndPermanent(GameTestHelper helper) {
        var player = player(helper);
        player.setHealth(3);
        float maxHealth = player.getMaxHealth();
        var ability = ModPassiveAbilities.OLD_BANDAGE.get();
        ability.handleObtain(player, null);
        ability.handleFirstObtain(player, null);
        helper.assertTrue(player.getHealth() == 3, "The new heart container is empty");
        ability.handleRemove(player, null);
        helper.assertTrue(player.getMaxHealth() > maxHealth, "The health bonus remains after removal");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void purseSlotChangeIsReversible(GameTestHelper helper) {
        var player = player(helper);
        var data = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
        int before = data.getExtraTrinketSlotCounts();
        ModPassiveAbilities.MOMS_PURSE.get().handleObtain(player, null);
        helper.assertTrue(data.getExtraTrinketSlotCounts() == before + 1, "Purse adds one slot");
        ModPassiveAbilities.MOMS_PURSE.get().handleRemove(player, null);
        helper.assertTrue(data.getExtraTrinketSlotCounts() == before, "Removing purse restores the count");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void scorpioPoisonsTwiceAtTheOwnersDamage(GameTestHelper helper) {
        var player = player(helper);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.2);
        var target = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(target));
        ModExecutableEffects.SCORPIO.get().apply(context);
        helper.assertTrue(target.hasEffect(ModEffects.DEADLY_POISON.get()), "Scorpio applies existing poison");
        double expected = 16;
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        for (int tick = 0; tick < 40; tick++) target.tick();
        helper.assertTrue(Math.abs(100 - target.getHealth() - expected) < 0.02,
                "Poison ticks twice using only its quantized potion level after the owner damage changes");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void shieldExtendsExistingInvincibility(GameTestHelper helper) {
        var player = player(helper);
        var context = new ExecutableEffectContext(player);
        ModExecutableEffects.CELTIC_CROSS.get().apply(context);
        helper.assertTrue(player.getEffect(ModEffects.INVINCIBLE.get()).getDuration() == 140, "Shield lasts seven seconds");
        ModExecutableEffects.CELTIC_CROSS.get().apply(context);
        helper.assertTrue(player.getEffect(ModEffects.INVINCIBLE.get()).getDuration() == 280, "Existing shield duration is preserved and extended");
        player.removeEffect(ModEffects.INVINCIBLE.get());
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void bandageDropsOneRedHeart(GameTestHelper helper) {
        var player = player(helper);
        AABB area = new AABB(player.position(), player.position()).inflate(2);
        int before = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .filter(e -> e.getItem().is(ModItems.RED_HEART.get())).mapToInt(e -> e.getItem().getCount()).sum();
        ModExecutableEffects.OLD_BANDAGE.get().apply(new ExecutableEffectContext(player));
        int after = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .filter(e -> e.getItem().is(ModItems.RED_HEART.get())).mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(after - before == 1, "The proc produces one ordinary red heart");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch3")
    public static void coneHeadZeroesHurtDamage(GameTestHelper helper) {
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "cone_head_damage"));
        player.connection = player(helper).connection;
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        // Use the real damage path after spawn protection expires, without a network login.
        for (int tick = 0; tick < 60; tick++) player.tick();
        float health = player.getHealth();
        helper.assertTrue(player.hurt(player.damageSources().generic(), 2), "The fixture accepts ordinary damage");
        helper.assertTrue(player.getHealth() < health && player.invulnerableTime > 0,
                "An ordinary hit damages the player and creates damage invincibility frames");
        player.setHealth(health);
        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new)
                .addItem(player, new ItemStack(ModPassiveItems.CONE_HEAD.get()));
        ModPassiveAbilities.CONE_HEAD.get().handleObtain(player, null);
        long seed = 0;
        while (RandomSource.create(seed).nextDouble() >= 0.2) seed++;
        player.getRandom().setSeed(seed);
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().generic(), 2);
        helper.assertTrue(player.getHealth() == health, "Cone Head sets the Hurt event damage to zero");
        helper.assertTrue(player.invulnerableTime > 0, "The Hurt stage retains vanilla damage invincibility frames");
        player.invulnerableTime = 0;
        seed = 0;
        while (RandomSource.create(seed).nextDouble() < 0.2) seed++;
        player.getRandom().setSeed(seed);
        helper.assertTrue(player.hurt(player.damageSources().generic(), 2), "A missed chance permits ordinary damage");
        helper.assertTrue(player.getHealth() < health && player.invulnerableTime > 0,
                "A missed chance retains normal damage invincibility frames");
        ModPassiveAbilities.CONE_HEAD.get().handleRemove(player, null);
        player.invulnerableTime = 0;
        player.getRandom().setSeed(0);
        helper.assertTrue(player.hurt(player.damageSources().generic(), 2), "Removing the module restores ordinary damage");
        player.discard();
        helper.succeed();
    }
}
