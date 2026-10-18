package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FamiliarDamageGameTests {
    private static final Map<GameTestHelper, List<Entity>> FIXTURES = new WeakHashMap<>();

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void equalSameTickHitsBothDealDamageAndKeepOwnerAttribution(GameTestHelper helper) {
        try {
            var owner = player(helper);
            var direct = fixture(helper, EntityType.ARMOR_STAND);
            var victim = fixture(helper, EntityType.COW);
            var source = source(helper, direct, owner);
            float initial = victim.getHealth();
            helper.assertTrue(victim.hurt(source, 2.0F), "First familiar hit is accepted");
            helper.assertTrue(victim.hurt(source, 2.0F), "Equal damage during the same tick is also accepted");
            helper.assertTrue(Math.abs(victim.getHealth() - (initial - 4.0F)) < 0.001F,
                    "Both hits apply their full damage through hurt cooldown");
            helper.assertTrue(victim.getLastDamageSource() == source && source.getDirectEntity() == direct
                            && source.getEntity() == owner && victim.getLastHurtByMob() == owner,
                    "The familiar remains the direct source and its owner receives attacker attribution");
            helper.succeed();
        } finally {
            cleanup(helper);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void familiarPreservesVelocityAndOrdinaryDamageStillKnocksBack(GameTestHelper helper) {
        try {
            var owner = player(helper);
            var victim = fixture(helper, EntityType.COW);
            owner.setPos(victim.getX() + 3.0D, victim.getY(), victim.getZ());
            var direct = fixture(helper, EntityType.ARMOR_STAND);
            var initial = new Vec3(0.08D, 0.01D, 0.03D);
            victim.setDeltaMovement(initial);
            helper.assertTrue(victim.hurt(source(helper, direct, owner), 1.0F), "Familiar hit is accepted");
            helper.assertTrue(victim.getDeltaMovement().equals(initial), "Familiar damage preserves existing velocity");
            victim.invulnerableTime = 0;
            helper.assertTrue(victim.hurt(helper.getLevel().damageSources().playerAttack(owner), 1.0F),
                    "Ordinary player damage is accepted after resetting only the fixture cooldown");
            helper.assertTrue(victim.getDeltaMovement().distanceToSqr(initial) > 0.001D,
                    "The normal hurt knockback path still changes velocity");
            helper.succeed();
        } finally {
            cleanup(helper);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void familiarRespectsArmorResistanceAndInvulnerability(GameTestHelper helper) {
        try {
            var owner = player(helper);
            var direct = fixture(helper, EntityType.ARMOR_STAND);
            var source = source(helper, direct, owner);
            Cow armored = fixture(helper, EntityType.COW);
            armored.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
            float initial = armored.getHealth();
            helper.assertTrue(armored.hurt(source, 4.0F), "Armored target accepts damage");
            helper.assertTrue(armored.getHealth() > initial - 4.0F && armored.getHealth() < initial,
                    "Armor continues to reduce familiar damage");
            Cow resistant = fixture(helper, EntityType.COW);
            resistant.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
            initial = resistant.getHealth();
            helper.assertTrue(resistant.hurt(source, 4.0F), "Resistant target accepts damage");
            helper.assertTrue(resistant.getHealth() > initial - 4.0F && resistant.getHealth() < initial,
                    "Resistance continues to reduce familiar damage");
            Cow invulnerable = fixture(helper, EntityType.COW);
            invulnerable.setInvulnerable(true);
            helper.assertTrue(!invulnerable.hurt(source, 4.0F) && invulnerable.getHealth() == invulnerable.getMaxHealth(),
                    "Entity invulnerability still rejects familiar damage");
            helper.succeed();
        } finally {
            cleanup(helper);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void facingShieldBlocksFamiliarDamage(GameTestHelper helper) {
        try {
        var owner = player(helper);
        var direct = fixture(helper, EntityType.ARMOR_STAND);
        var victim = fixture(helper, EntityType.ZOMBIE);
        victim.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        victim.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
        victim.setYRot(180.0F);
        victim.setYHeadRot(180.0F);
        victim.setXRot(0.0F);
        direct.setPos(victim.getX(), victim.getY(), victim.getZ() - 2.0D);
        owner.setPos(victim.getX(), victim.getY(), victim.getZ() - 3.0D);
        victim.startUsingItem(InteractionHand.MAIN_HAND);
        helper.runAfterDelay(6, () -> {
            try {
                helper.assertTrue(victim.isBlocking(), "The real shield use has passed its five tick warmup");
                victim.setYRot(180.0F);
                victim.setYHeadRot(180.0F);
                victim.setXRot(0.0F);
                var source = source(helper, direct, owner);
                helper.assertTrue(victim.isDamageSourceBlocked(source),
                        "Shield fixture faces the source: view=" + victim.getViewVector(1.0F)
                                + ", source=" + source.getSourcePosition() + ", victim=" + victim.position());
                float initial = victim.getHealth();
                helper.assertTrue(!victim.hurt(source, 4.0F), "Facing shield rejects familiar damage");
                helper.assertTrue(victim.getHealth() == initial, "Blocked familiar damage does not reduce health");
                helper.succeed();
            } finally {
                cleanup(helper);
            }
        });
        } catch (RuntimeException | Error failure) {
            cleanup(helper);
            throw failure;
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void flyTargetsHostilesAndRetaliatingMobsButNeverAnimals(GameTestHelper helper) {
        try {
            var level = helper.getLevel();
            var owner = player(helper);
            var zombie = fixture(helper, EntityType.ZOMBIE);
            var other = fixture(helper, EntityType.CREEPER);
            helper.assertTrue(TrackingTargetRules.eligibleFly(level, owner, zombie), "Hostiles are eligible without aggro");
            helper.assertTrue(TrackingTargetRules.priority(level, owner, zombie) == 1, "Idle hostile has priority one");
            zombie.setTarget(owner);
            helper.assertTrue(TrackingTargetRules.priority(level, owner, zombie) == 0, "Owner aggro has highest priority");
            var golem = fixture(helper, EntityType.IRON_GOLEM);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, golem), "An idle neutral mob is excluded");
            golem.setTarget(other);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, golem), "Aggro at somebody else is excluded");
            golem.setTarget(owner);
            helper.assertTrue(TrackingTargetRules.eligibleFly(level, owner, golem), "A nonanimal mob targeting owner is eligible");
            golem.setTarget(null);
            golem.setPersistentAngerTarget(owner.getUUID());
            golem.setRemainingPersistentAngerTime(200);
            helper.assertTrue(TrackingTargetRules.eligibleFly(level, owner, golem), "Persistent anger at owner is eligible");
            var wolf = fixture(helper, EntityType.WOLF);
            wolf.setTarget(owner);
            wolf.setPersistentAngerTarget(owner.getUUID());
            wolf.setRemainingPersistentAngerTime(200);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, wolf), "Angry animals are always excluded");
            helper.assertTrue(TrackingTargetRules.eligible(level, owner, wolf), "Ordinary tracking retains its animal policy");
            zombie.setInvulnerable(true);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, zombie), "Invulnerable hostiles are excluded");
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, owner), "Owner is excluded");
            helper.succeed();
        } finally {
            cleanup(helper);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_damage")
    public static void flyPlayerTargetsRespectPvpTeamsAndGameMode(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        var owner = pvpPlayer(helper);
        var candidate = pvpPlayer(helper);
        var scoreboard = server.getScoreboard();
        var team = scoreboard.addPlayerTeam("fly_" + UUID.randomUUID().toString().substring(0, 8));
        boolean previousPvp = server.isPvpAllowed();
        try {
            server.setPvpAllowed(true);
            helper.assertTrue(TrackingTargetRules.eligibleFly(level, owner, candidate), "PVP-eligible survival player is allowed");
            helper.assertTrue(TrackingTargetRules.priority(level, owner, candidate) == 3, "Players follow mob priorities");
            server.setPvpAllowed(false);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, candidate), "PVP-disabled server excludes players");
            server.setPvpAllowed(true);
            scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(candidate.getScoreboardName(), team);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, candidate), "Allied players are excluded");
            scoreboard.removePlayerFromTeam(candidate.getScoreboardName(), team);
            candidate.setGameMode(GameType.CREATIVE);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, candidate), "Creative players are excluded");
            candidate.setGameMode(GameType.SPECTATOR);
            helper.assertTrue(!TrackingTargetRules.eligibleFly(level, owner, candidate), "Spectators are excluded");
        } finally {
            server.setPvpAllowed(previousPvp);
            scoreboard.removePlayerTeam(team);
            cleanup(helper);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "fly_" + UUID.randomUUID().toString().substring(0, 8)));
        player.setGameMode(GameType.SURVIVAL);
        FIXTURES.computeIfAbsent(helper, ignored -> new ArrayList<>()).add(player);
        return player;
    }

    private static ServerPlayer pvpPlayer(GameTestHelper helper) {
        var player = new PvpPlayer(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "pvp_" + UUID.randomUUID().toString().substring(0, 8)));
        player.setGameMode(GameType.SURVIVAL);
        FIXTURES.computeIfAbsent(helper, ignored -> new ArrayList<>()).add(player);
        return player;
    }

    /** Retains FakePlayer's disconnected network stub while restoring vanilla PVP permissions. */
    private static final class PvpPlayer extends FakePlayer {
        private PvpPlayer(ServerLevel level, GameProfile profile) {
            super(level, profile);
        }

        @Override
        public boolean canHarmPlayer(Player other) {
            var team = getTeam();
            return team == null || !team.isAlliedTo(other.getTeam()) || team.isAllowFriendlyFire();
        }
    }

    private static <T extends Entity> T fixture(GameTestHelper helper, EntityType<T> type) {
        T entity = helper.spawn(type, new BlockPos(4, 3, 4));
        FIXTURES.computeIfAbsent(helper, ignored -> new ArrayList<>()).add(entity);
        entity.setNoGravity(true);
        if (entity instanceof Mob mob) mob.setNoAi(true);
        return entity;
    }

    private static void cleanup(GameTestHelper helper) {
        var entities = FIXTURES.remove(helper);
        if (entities != null) entities.forEach(Entity::discard);
    }

    private static DamageSource source(GameTestHelper helper, Entity direct, ServerPlayer owner) {
        return new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.FAMILIAR), direct, owner);
    }
}
