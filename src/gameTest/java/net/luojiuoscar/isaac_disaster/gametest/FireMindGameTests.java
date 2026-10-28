package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.BurningEffect;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.FireMindAttachment;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.FireMindImpact;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_fire_mind")
@PrefixGameTestTemplate(false)
public final class FireMindGameTests {
    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void burningKeepsVanillaFireDamageAndEndsWhenTheFlameEnds(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            burn(target, 4);
            health(helper, target, 200);
            target.tick();
            health(helper, target, 199);
            tick(target, 79);
            helper.assertTrue(target.getHealth() < 199, "Vanilla fire and the custom pulse both remain eligible to damage");
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()) && !target.isOnFire(), "The effect ends once natural fire expires");
            helper.assertTrue(!target.getPersistentData().contains("isaac_disaster:burning_ticks"), "Natural expiry clears the clock");
            Cow contact = f.cow(f.center.add(1, 4, 0));
            burn(contact, 0);
            contact.hurt(f.level.damageSources().inFire(), 1);
            health(helper, contact, 199);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void continuousRefreshCannotStarveTheFortyTickDamageClock(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            burn(target, 4);
            List<Integer> pulses = new ArrayList<>();
            int[] currentTick = {0};
            Consumer<LivingAttackEvent> record = event -> {
                if (event.getEntity() == target && event.getAmount() == 3) pulses.add(currentTick[0]);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LivingAttackEvent.class, record);
            try {
                for (int tick = 1; tick <= 120; tick++) {
                    currentTick[0] = tick;
                    if (tick % 3 == 0) burn(target, 4);
                    target.tick();
                }
            } finally {
                MinecraftForge.EVENT_BUS.unregister(record);
            }
            helper.assertTrue(pulses.equals(List.of(40, 80, 120)), "Refreshes preserve the independent pulse schedule, including rejected hurt attempts");
            helper.assertTrue(target.hasEffect(ModEffects.BURNING.get()), "Refreshes extend duration without delaying damage");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void upgradedAndWeakerHitsRefreshWithoutResettingTheClock(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            burn(target, 0);
            tick(target, 20);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
            helper.assertTrue(f.impact(f.attack(ModAttackTypes.BULLET.get()), target), "An upgrade is valid");
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1);
            helper.assertTrue(f.impact(f.attack(ModAttackTypes.BULLET.get()), target), "A weaker refresh is valid");
            helper.assertTrue(target.getEffect(ModEffects.BURNING.get()).getAmplifier() == 6, "A weaker hit retains the stronger level");
            helper.assertTrue(target.getEffect(ModEffects.BURNING.get()).getDuration() == 80, "Refresh restores all 80 ticks");
            tick(target, 20);
            helper.assertTrue(target.getPersistentData().getInt("isaac_disaster:burning_ticks") == 0,
                    "An upgrade does not restart the first pulse");
            helper.assertTrue(target.getHealth() < 199, "The stronger pulse remains able to deal damage");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void milkLeavesBurningAndRemovalPreservesOtherFire(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            burn(target, 0);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
            target.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
            helper.assertTrue(target.hasEffect(ModEffects.BURNING.get()) && !target.hasEffect(MobEffects.POISON), "Milk cures poison but not burning");
            target.removeAllEffects();
            helper.assertTrue(target.isOnFire() && !target.getPersistentData().contains("isaac_disaster:burning_ticks"), "Effect removal clears its clock without clearing ordinary fire");
            burn(target, 0);
            target.extinguishFire();
            target.tick();
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()), "Explicit extinguishing removes the effect on the next effect tick");
            burn(target, 0);
            target.setRemainingFireTicks(-1);
            target.tick();
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()), "Negative vanilla fire timers also extinguish");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void enteringWaterExtinguishesAndWetTargetsCannotBeReignited(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            burn(target, 4);
            f.level.setBlockAndUpdate(target.blockPosition(), Blocks.WATER.defaultBlockState());
            target.tick();
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()) && !target.isOnFire(), "Water clears burning in the first wet tick");
            burn(target, 4);
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()), "A wet target stays extinguished");
            health(helper, target, 200);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void powderSnowUsesTheSameVanillaExtinguishPath(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            burn(target, 4);
            f.level.setBlockAndUpdate(target.blockPosition(), Blocks.POWDER_SNOW.defaultBlockState());
            target.move(MoverType.SELF, new Vec3(0, -.01, 0));
            target.tick();
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()), "Powder snow extinguishes through the fire-timer hook");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void fireResistanceBlocksDamageAndFireProtectionCanShortenBurning(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
            boots.enchant(Enchantments.FIRE_PROTECTION, 4);
            target.setItemSlot(EquipmentSlot.FEET, boots);
            target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240));
            burn(target, 10);
            int fireTicks = target.getRemainingFireTicks();
            helper.assertTrue(fireTicks > 0 && fireTicks < 40, "Fire protection may end burning before the first custom pulse");
            for (int tick = 1; tick < fireTicks; tick++) {
                target.tick();
                helper.assertTrue(target.isOnFire(), "The initial shortened flame burns naturally");
                health(helper, target, 200);
            }
            target.tick();
            helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()), "The effect does not extend a shortened flame");
            health(helper, target, 200);
            helper.assertTrue(target.hasEffect(MobEffects.FIRE_RESISTANCE), "Burning expiry preserves other resistance effects");
            Blaze immune = f.add(new Blaze(EntityType.BLAZE, f.level), f.center.add(1, 4, 0));
            burn(immune, 10);
            helper.assertTrue(!immune.hasEffect(ModEffects.BURNING.get()), "Intrinsic fire immunity rejects burning");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void meleeRestrictedHitAddsBurningWithoutGroundFireOrExtraImmediateDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.FIRE_MIND.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            helper.assertTrue(target.hurt(f.level.damageSources().playerAttack(f.owner), 2), "Original melee damage lands");
            helper.assertTrue(target.hasEffect(ModEffects.BURNING.get()), "Restricted melee applies burning");
            helper.assertTrue(f.level.isEmptyBlock(target.blockPosition()), "Restricted hits do not place fire");
            health(helper, target, 198);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void tearsUseHitTimePanelDamageAndDoNotDoubleProcRestrictedHits(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.FIRE_MIND.get().handleObtain(f.owner, null);
            Cow target = f.cow(f.center);
            AttackContext saved = f.attack(ModAttackTypes.BULLET.get());
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.3);
            f.bullets.spawn(f.bullet(saved, f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
            f.bullets.tick(f.level);
            helper.assertTrue(target.getEffect(ModEffects.BURNING.get()).getAmplifier() == 15, "Burning snapshots the current panel, not the saved tear damage");
            helper.assertTrue(f.level.getBlockState(target.blockPosition()).is(Blocks.FIRE), "A successful tear impact places legal ground fire");
            health(helper, target, 198);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void piercingTearsApplyBurningToEachSuccessfulTarget(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow first = f.cow(f.center.add(-.75, 0, 0));
            Cow second = f.cow(f.center.add(.75, 0, 0));
            f.bullets.spawn(f.bullet(f.attack(ModAttackTypes.BULLET.get()), f.center.add(-2, .8, 0), new Vec3(4.5, 0, 0), true));
            f.bullets.tick(f.level);
            helper.assertTrue(first.hasEffect(ModEffects.BURNING.get()) && second.hasEffect(ModEffects.BURNING.get()), "Both piercing collisions apply burning");
            health(helper, first, 198);
            health(helper, second, 198);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void lasersBurnTargetsAndPlaceGroundFireWithoutChangingColor(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            AttackContext laser = f.attack(ModAttackTypes.TECHNOLOGY2.get()).toBuilder()
                    .position(f.center.add(-2, .8, 0)).mainAxis(new Vec3(1, 0, 0)).build();
            ModAttackTypes.TECHNOLOGY2.get().performAttack(List.of(laser));
            helper.assertTrue(target.hasEffect(ModEffects.BURNING.get()), "Laser after-hit attaches burning");
            helper.assertTrue(f.level.getBlockState(target.blockPosition()).is(Blocks.FIRE), "Lasers also place legal fire");
            helper.assertTrue(laser.getColorRl().equals(ModBulletColors.BASE.getId()), "Laser color stays unchanged");
            health(helper, target, 199.8F);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void failedDamageAndNaturalExpiryDoNotBurnOrPlaceFire(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center);
            Consumer<LivingAttackEvent> cancel = event -> {
                if (event.getEntity() == target) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, cancel);
            try {
                f.bullets.spawn(f.bullet(f.attack(ModAttackTypes.BULLET.get()), f.center.add(-1.5, .8, 0), new Vec3(2, 0, 0), false));
                f.bullets.tick(f.level);
                helper.assertTrue(!target.hasEffect(ModEffects.BURNING.get()) && f.level.isEmptyBlock(target.blockPosition()), "Rejected tear damage has no payoff");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
            f.bullets.clear();
            f.bullets.spawn(BulletState.from(f.attack(ModAttackTypes.BULLET.get()))
                    .position(f.center.add(0, 4, 0)).velocity(Vec3.ZERO).lifetime(1).range(8).build());
            f.bullets.tick(f.level);
            helper.assertTrue(f.level.isEmptyBlock(BlockPos.containing(f.center.add(0, 4, 0))), "Natural expiry places no fire");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void blockImpactsUseTheActualFaceAndRespectPlacementCancellation(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos floor = BlockPos.containing(f.center).below();
            helper.assertTrue(f.blockImpact(floor, Direction.UP), "Valid ground collision completes");
            helper.assertTrue(f.level.getBlockState(floor.above()).is(Blocks.FIRE), "Fire appears on the hit face");
            f.level.setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
            Consumer<BlockEvent.EntityPlaceEvent> cancel = event -> {
                if (event.getEntity() == f.owner) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, cancel);
            try {
                helper.assertTrue(f.blockImpact(floor, Direction.UP), "Blocked placement is a safe no-op");
                helper.assertTrue(f.level.isEmptyBlock(floor.above()), "Canceled placement restores the original air block");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
            BlockPos wall = floor.above().east();
            f.level.setBlockAndUpdate(wall, Blocks.OAK_LOG.defaultBlockState());
            helper.assertTrue(f.blockImpact(wall, Direction.WEST), "A burnable wall supports face fire");
            helper.assertTrue(f.level.getBlockState(wall.west()).is(Blocks.FIRE), "Wall fire uses the side face");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void canceledFirePlacementCannotLeavePortalSideEffects(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos base = BlockPos.containing(f.center).below().west();
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 5; y++) {
                    f.level.setBlockAndUpdate(base.offset(x, y, 0),
                            (x == 0 || x == 3 || y == 0 || y == 4 ? Blocks.OBSIDIAN : Blocks.AIR).defaultBlockState());
                }
            }
            Consumer<BlockEvent.EntityPlaceEvent> cancel = event -> {
                if (event.getEntity() == f.owner) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, BlockEvent.EntityPlaceEvent.class, cancel);
            try {
                f.blockImpact(base.east(), Direction.UP);
                for (int x = 1; x <= 2; x++) {
                    for (int y = 1; y <= 3; y++) {
                        helper.assertTrue(f.level.isEmptyBlock(base.offset(x, y, 0)), "Canceled fire must not leave any portal blocks");
                    }
                }
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void invalidFirePositionsAreSafeAndNeverForceLoadChunks(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            BlockPos floor = BlockPos.containing(f.center).below();
            BlockPos feet = floor.above();
            f.level.setBlockAndUpdate(feet, Blocks.STONE.defaultBlockState());
            helper.assertTrue(f.blockImpact(floor, Direction.UP), "Occupied placement completes safely");
            helper.assertTrue(f.level.getBlockState(feet).is(Blocks.STONE), "Solid blocks are not replaced");
            f.level.setBlockAndUpdate(feet, Blocks.WATER.defaultBlockState());
            f.blockImpact(floor, Direction.UP);
            helper.assertTrue(f.level.getBlockState(feet).is(Blocks.WATER), "Water is not replaced");
            BlockPos unsupported = floor.above(5);
            f.blockImpact(unsupported.below(), Direction.UP);
            helper.assertTrue(f.level.isEmptyBlock(unsupported), "Unsupported air stays empty");
            BlockPos unloaded = new BlockPos(20_000_000, 100, 20_000_000);
            helper.assertTrue(!f.level.hasChunkAt(unloaded), "The distant chunk starts unloaded");
            f.blockImpact(unloaded, Direction.UP);
            helper.assertTrue(!f.level.hasChunkAt(unloaded), "The placement attempt does not load it");
            Cow airborne = f.cow(f.center.add(0, 4, 0));
            helper.assertTrue(f.impact(f.attack(ModAttackTypes.BULLET.get()), airborne)
                    && airborne.hasEffect(ModEffects.BURNING.get()), "No legal fire placement does not prevent burning");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void copiesAndRemovalPreserveFiredAttachmentsAndOtherFireImmunityOwners(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var ability = ModPassiveAbilities.FIRE_MIND.get();
            ability.handleObtain(f.owner, null);
            ability.handleObtain(f.owner, null);
            ModPassiveAbilities.HOT_BOMB.get().handleObtain(f.owner, null);
            var modules = f.owner.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new);
            helper.assertTrue(modules.getTriggerModules().get(ModTriggerModules.FIRE_MIND.getId()).stacks() == 2, "Copies count ownership in one module");
            AttackContext saved = f.attack(ModAttackTypes.BULLET.get());
            ability.handleRemove(f.owner, null);
            helper.assertTrue(modules.getTriggerModules().contains(ModTriggerModules.FIRE_MIND.getId()), "One copy retains the attachment module");
            ability.handleRemove(f.owner, null);
            helper.assertTrue(!modules.getTriggerModules().contains(ModTriggerModules.FIRE_MIND.getId()), "Removing the last copy removes the module");
            helper.assertTrue(modules.getPeriodicModuleQueue().getQueue().stream()
                    .anyMatch(entry -> entry.id.equals(ModPeriodicModules.FIRE_RESISTANCE.getId()) && entry.stacks == 1), "Hot Bomb still owns fire resistance");
            modules.getPeriodicModuleQueue().tickAll(f.owner);
            helper.assertTrue(f.owner.hasEffect(MobEffects.FIRE_RESISTANCE), "The retained module grants resistance");
            Cow target = f.cow(f.center.add(0, 4, 0));
            helper.assertTrue(f.impact(saved, target) && target.hasEffect(ModEffects.BURNING.get()), "Previously fired attachments survive item removal");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void saveReloadPreservesPulseProgressAndDeathCleansUp(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            Cow target = f.cow(f.center.add(0, 4, 0));
            burn(target, 4);
            tick(target, 25);
            float savedHealth = target.getHealth();
            CompoundTag saved = new CompoundTag();
            target.saveWithoutId(saved);
            target.discard();
            Cow loaded = new Cow(EntityType.COW, f.level);
            loaded.load(saved);
            helper.assertTrue(f.level.addFreshEntity(loaded), "The saved entity is restored into the world");
            f.targets.add(loaded);
            tick(loaded, 14);
            health(helper, loaded, savedHealth);
            loaded.tick();
            helper.assertTrue(loaded.getHealth() < savedHealth
                    && loaded.getPersistentData().getInt("isaac_disaster:burning_ticks") == 0,
                    "The restored pulse clock reaches 40 without a reset");
            loaded.hurt(f.level.damageSources().genericKill(), 500);
            helper.assertTrue(!loaded.hasEffect(ModEffects.BURNING.get()), "Death clears the burning effect");
            helper.assertTrue(!loaded.getPersistentData().contains("isaac_disaster:burning_ticks"), "Death clears the pulse clock");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind")
    public static void invalidAndCanceledContextsReturnFalse(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var context = new ExecutableEffectContext(f.owner);
            helper.assertTrue(!new FireMindImpact().applyEffect(context), "Missing collision context is rejected");
            Cow target = f.cow(f.center);
            LivingAttackEvent canceled = new LivingAttackEvent(target, f.level.damageSources().playerAttack(f.owner), 2);
            canceled.setCanceled(true);
            context.set(ContextKeys.EVENT, canceled);
            helper.assertTrue(!new FireMindImpact().applyEffect(context) && !target.hasEffect(ModEffects.BURNING.get()), "Canceled restricted contexts have no payoff");
        }
        helper.succeed();
    }

    private static void burn(Mob target, int amplifier) {
        target.addEffect(new MobEffectInstance(ModEffects.BURNING.get(), BurningEffect.DURATION, amplifier));
    }

    private static void tick(Mob target, int ticks) {
        for (int i = 0; i < ticks; i++) target.tick();
    }

    private static void health(GameTestHelper helper, Mob target, float expected) {
        helper.assertTrue(Math.abs(target.getHealth() - expected) < .001,
                "Expected health " + expected + ", got " + target.getHealth());
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerLevel level;
        private final ServerPlayer owner;
        private final Vec3 center;
        private final BulletManager bullets = new BulletManager();
        private final List<Mob> targets = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            center = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos floor = BlockPos.containing(center).below().offset(x, 0, z);
                    level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
                    for (int y = 1; y <= 7; y++) level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
                }
            }
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "fire_" + id.toString().substring(0, 8)));
            owner.setPos(center.add(6, 4, 0));
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
            level.addNewPlayer(owner);
        }

        private <T extends Mob> T add(T target, Vec3 position) {
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            target.setHealth(200);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(position);
            level.addFreshEntity(target);
            targets.add(target);
            return target;
        }

        private Cow cow(Vec3 position) {
            return add(new Cow(EntityType.COW, level), position);
        }

        private AttackContext attack(AttackType type) {
            var attack = AttackContext.builder(owner, owner).attackType(type).damage(2.0).bulletScaleModifier(0.0)
                    .range(8).speed(2).position(center).mainAxis(new Vec3(1, 0, 0)).color(ModBulletColors.BASE.getId()).build();
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.ATTACK_CONTEXT, attack);
            new FireMindAttachment().apply(context);
            return attack;
        }

        private BulletState bullet(AttackContext attack, Vec3 position, Vec3 velocity, boolean piercing) {
            return BulletState.from(attack).position(position).velocity(velocity).baseSpeed(velocity.length())
                    .lifetime(8).collisionWidth(.2).collisionHeight(.2).piercing(piercing).build();
        }

        private boolean impact(AttackContext attack, Mob target) {
            var bullet = bullet(attack, target.position(), Vec3.ZERO, false);
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.EVENT, new IsaacAttackAfterHitEvent(bullet, owner, bullet.getTypeId(),
                    bullet.getTriggers(), new EntityHitResult(target), 2, target.getHealth()));
            return new FireMindImpact().applyEffect(context);
        }

        private boolean blockImpact(BlockPos block, Direction face) {
            var bullet = bullet(attack(ModAttackTypes.BULLET.get()), block.getCenter(), Vec3.ZERO, false);
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.EVENT, new IsaacAttackHitBlockEvent(bullet, owner, bullet.getTypeId(),
                    bullet.getTriggers(), new BlockHitResult(block.getCenter(), face, block, false)));
            return new FireMindImpact().applyEffect(context);
        }

        @Override
        public void close() {
            bullets.clear();
            for (Mob target : targets) target.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
