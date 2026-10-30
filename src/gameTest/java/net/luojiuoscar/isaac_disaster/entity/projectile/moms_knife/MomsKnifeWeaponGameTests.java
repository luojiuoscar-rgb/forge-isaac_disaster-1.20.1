package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.event.RuntimeEntityEvents;
import net.luojiuoscar.isaac_disaster.event.MomsKnifeWeaponEvents;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.item.IAttackLauncher;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.weapon.MomsKnifeWeapon;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackCandidate;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackSelection;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeWeaponAttack;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class MomsKnifeWeaponGameTests {
    private static final AtomicInteger NEXT_WEAPON_FIXTURE = new AtomicInteger();

    private static PlayerMomsKnifeGameTests.Fixture fixture(GameTestHelper helper) {
        return new PlayerMomsKnifeGameTests.Fixture(helper, NEXT_WEAPON_FIXTURE.getAndIncrement());
    }
    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 10)
    public static void weaponIsAnUnbreakableUnenchantableSwordWithMultiplicativeDamage(GameTestHelper helper) {
        var weapon = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("isaac_disaster:moms_knife_weapon"));
        helper.assertTrue(weapon instanceof SwordItem, "Mom's Knife must be registered as a sword weapon");
        ItemStack stack = new ItemStack(weapon);
        helper.assertTrue(stack.getMaxStackSize() == 1 && !stack.isDamageableItem(), "The weapon is nonstackable and never breaks");
        helper.assertTrue(!stack.isEnchantable(), "The weapon cannot be enchanted");
        helper.assertTrue(stack.getMaxDamage() == 0 && !weapon.isBookEnchantable(stack, new ItemStack(Items.ENCHANTED_BOOK)),
                "There is no durability or enchanted-book entry point");
        var damage = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        helper.assertTrue(damage.size() == 1 && damage.iterator().next().getAmount() == 5
                        && damage.iterator().next().getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL,
                "The weapon multiplies held attack damage by six without a flat damage bonus");
        helper.assertTrue(stack.getAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty(), "Only the main hand grants modifiers");
        helper.assertTrue(stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).stream()
                .anyMatch(modifier -> modifier.getAmount() == -2.4F), "The weapon uses the ordinary sword speed modifier");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 10)
    public static void obtainingAndReequippingThePassiveNeverGivesAWeapon(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var ability = ModPassiveAbilities.MOMS_KNIFE.get();
            var source = new ItemStack(ModPassiveItems.MOMS_KNIFE.get());
            ability.onObtain(f.owner, source);
            PassiveItem.setHasBeenUsed(source, true);
            helper.assertTrue(f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 0,
                    "First obtain does not grant a weapon");
            ability.onRemove(f.owner, source);
            ability.onObtain(f.owner, source);
            helper.assertTrue(f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 0,
                    "Reequipping does not grant a weapon");
            ability.onObtain(f.owner, new ItemStack(ModPassiveItems.MOMS_KNIFE.get()));
            helper.assertTrue(f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 0 && weaponDrops(f).isEmpty(),
                    "Additional unused copies grant neither inventory weapons nor dropped weapons");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 10)
    public static void obtainingThePassiveWithAFullInventoryDoesNotDropAWeapon(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            for (int slot = 0; slot < f.owner.getInventory().items.size(); slot++) {
                f.owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            }
            ModPassiveAbilities.MOMS_KNIFE.get().handleFirstObtain(f.owner, new ItemStack(ModPassiveItems.MOMS_KNIFE.get()));
            var drops = f.level.getEntitiesOfClass(ItemEntity.class, f.owner.getBoundingBox().inflate(3),
                    item -> item.getItem().is(ModItems.MOMS_KNIFE_WEAPON.get()));
            helper.assertTrue(drops.isEmpty() && f.owner.getInventory().countItem(Items.COBBLESTONE) == 36 * 64,
                    "Obtaining the passive with a full inventory creates no weapon drop and leaves existing items intact");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void targetingAChestAllowsWeaponChargeWithoutOpeningTheChest(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            BlockPos pos = f.owner.blockPosition().offset(0, 0, 2);
            var previous = f.level.getBlockState(pos);
            f.level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
            try {
                var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
                var blocked = f.owner.gameMode.useItemOn(f.owner, f.level, f.owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
                helper.assertTrue(blocked == InteractionResult.PASS && f.owner.containerMenu == f.owner.inventoryMenu,
                        "Blocking chest interaction must allow vanilla's item-use fallback instead of returning FAIL");
                var use = f.owner.gameMode.useItem(f.owner, f.level, f.owner.getMainHandItem(), InteractionHand.MAIN_HAND);
                f.ability.tickAttacks(f.owner);
                var attack = (MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get();
                helper.assertTrue(use.consumesAction() && f.owner.isUsingItem() && f.ability.isHoldingRightClick()
                                && f.ability.getChargeAmount(attack.getChargeBarId()) > 0,
                        "A weapon aimed at a chest starts native use and charge through the fallback");
                f.owner.stopUsingItem();
                f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
                helper.assertTrue(f.owner.gameMode.useItemOn(f.owner, f.level, f.owner.getMainHandItem(),
                                InteractionHand.MAIN_HAND, hit) == InteractionResult.FAIL,
                        "Isaac Head keeps its existing packet-driven block interaction behavior");
                f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
                var ordinary = new PlayerInteractEvent.RightClickBlock(f.owner, InteractionHand.MAIN_HAND, pos, hit);
                MinecraftForge.EVENT_BUS.post(ordinary);
                helper.assertTrue(!ordinary.isCanceled(), "Ordinary held items retain their normal block interactions");
                helper.succeed();
            } finally {
                f.owner.stopUsingItem();
                f.level.setBlockAndUpdate(pos, previous);
            }
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void weaponSelectionAndInputDoNotOverwriteThePassiveSelection(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            var passive = f.ability.getAttackSelection();
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            var active = IAttackLauncher.selection(f.owner);
            helper.assertTrue(active.mainAttack() == ModAttackTypes.MOMS_KNIFE_WEAPON.get() && active.additionalAttacks().isEmpty(),
                    "The held weapon provides only its own fixed attack");
            f.ability.handleAttackInput(f.owner, true);
            f.ability.tickAttacks(f.owner);
            helper.assertTrue(f.ability.getChargeAmount(((MomsKnifeWeaponAttack) active.mainAttack()).getChargeBarId()) > 0,
                    "The shared input path charges a weapon");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            f.ability.tickAttacks(f.owner);
            helper.assertTrue(!f.ability.isHoldingRightClick() && f.ability.getVisibleChargeBarIds().isEmpty(),
                    "Changing source clears the old press and charge bar");
            helper.assertTrue(f.ability.getAttackSelection() == passive && IAttackLauncher.selection(f.owner).mainAttack() == passive.mainAttack(),
                    "Returning to the head restores the untouched passive selection");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 60)
    public static void anUnownedWeaponThrowsOnePlainKnifeAndReturnsAfterSwitchingItems(GameTestHelper helper) {
        var f = fixture(helper);
        equipIndependent(f);
        f.ability.addAttackType(ModAttackTypes.BRIMSTONE.getId(), 1, f.owner);
        f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), 1, f.owner);
        f.ability.setHoming(1);
        f.ability.setControllable(1);
        f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(16);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(32);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        StatManager.addTriggerModule(f.owner, ModTriggerModules.BOOK_MOMS_KNIFE.getId(), 1);
        StatManager.addTriggerModule(f.owner, ModTriggerModules.MONSTROS_LUNG_MOMS_KNIFE.getId(), 1);
        StatManager.addTriggerModule(f.owner, ModTriggerModules.TINY_PLANET.getId(), 1);
        StatManager.addTriggerModule(f.owner, ModTriggerModules.LOKIS_HORNS.getId(), 1);
        StatManager.addTriggerModule(f.owner, ModTriggerModules.LEAD_PENCIL.getId(), 1);
        AtomicInteger events = new AtomicInteger();
        Consumer<AttackFiredEvent> fired = event -> { if (event.getOwner() == f.owner) events.incrementAndGet(); };
        Consumer<AttackPlanEvent> plan = event -> { if (event.getPlayer() == f.owner) events.incrementAndGet(); };
        Consumer<AttackContextPrepareEvent> prepare = event -> { if (event.getPlayer() == f.owner) events.incrementAndGet(); };
        MinecraftForge.EVENT_BUS.addListener(fired);
        MinecraftForge.EVENT_BUS.addListener(plan);
        MinecraftForge.EVENT_BUS.addListener(prepare);
        try { fireFull(f); }
        finally {
            MinecraftForge.EVENT_BUS.unregister(fired);
            MinecraftForge.EVENT_BUS.unregister(plan);
            MinecraftForge.EVENT_BUS.unregister(prepare);
        }
        var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
        helper.assertTrue(knife != null && knife.discardsOnReturn() && !knife.isHoming() && !knife.isControllable(),
                "An independent knife ignores tracking and control without requiring the passive");
        helper.assertTrue(events.get() == 0 && knife.getTrajectorySpecs().isEmpty() && HeldMomsKnifeGroup.knives(f.owner).isEmpty(),
                "No planning, preparation, fired effects, trajectories or held slots are used");
        helper.assertTrue(f.knives().size() == 1 && knife.shouldBeSaved() && f.owner.getMainHandItem().isEmpty(),
                "One knife carries the actual weapon and can save its recoverable item snapshot");
        f.owner.getCooldowns().removeCooldown(ModItems.MOMS_KNIFE_WEAPON.get());
        helper.assertTrue(MomsKnifeWeapon.isUnavailable(f.owner), "The entity slot also prevents bypassing cooldown");
        f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        long start = f.level.getGameTime();
        helper.onEachTick(() -> { if (!f.closed && !knife.isRemoved() && knife.getTrajectoryAge() < f.level.getGameTime() - start) knife.tick(); });
        helper.runAfterDelay(2, () -> helper.assertTrue(!knife.isRemoved(), "Changing the held item does not cancel the flight"));
        helper.runAfterDelay(36, () -> {
            try {
                helper.assertTrue(knife.isRemoved() && MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null,
                        "The knife returns and releases its independent slot");
                helper.assertTrue(!f.owner.getCooldowns().isOnCooldown(ModItems.MOMS_KNIFE_WEAPON.get()), "Actual cleanup releases cooldown");
                helper.assertTrue(f.knives().isEmpty(), "No brimstone, lung, bookworm or Loki extras were created");
                helper.assertTrue(f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 1
                        && f.owner.getMainHandItem().is(Items.STICK), "Return restores exactly one weapon without replacing the newly held item");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void meleeUsesTheAttributeWhileSweepAndExplicitDamageKeepVanillaAmounts(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            readyMelee(f);
            var primary = f.cow(f.owner.position().add(0, 0.45, 1));
            var swept = f.cow(f.owner.position().add(0.75, 0.45, 1));
            f.owner.setOnGround(true);
            f.owner.attack(primary);
            helper.assertTrue(Math.abs(primary.getHealth() - 994) < 0.001, "Main melee hit is six times one damage; health="
                    + primary.getHealth() + ", panel=" + f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
            helper.assertTrue(Math.abs(swept.getHealth() - 999) < 0.001, "Unenchanted sweep retains vanilla's fixed one damage");
            var collision = f.cow(f.owner.position().add(10, 0.45, 0));
            collision.hurt(f.owner.damageSources().playerAttack(f.owner), 1);
            helper.assertTrue(collision.getHealth() == 999, "A direct player-typed collision outside Player.attack stays one damage");
            var cancelled = f.cow(f.owner.position().add(15, 0.45, 0));
            Consumer<LivingHurtEvent> cancel = event -> { if (event.getEntity() == cancelled) event.setCanceled(true); };
            MinecraftForge.EVENT_BUS.addListener(cancel);
            try { readyMelee(f); f.owner.setOnGround(false); f.owner.attack(cancelled); }
            finally { MinecraftForge.EVENT_BUS.unregister(cancel); }
            helper.assertTrue(cancelled.getHealth() == 1000, "Normal hurt cancellation is respected");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void heldDamageMultiplierFollowsEquipmentAndLeavesOnThrow(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            f.owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3);
            helper.assertTrue(f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) == 18,
                    "The weapon scales the actual panel instead of replacing its base value");
            updateEquipment(f.owner);
            helper.assertTrue(f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) == 18, "Equipment refresh never stacks the multiplier");
            fireFull(f);
            updateEquipment(f.owner);
            var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
            helper.assertTrue(knife != null && f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) == 3 && knife.getDamage() == 6,
                    "Transferring the held weapon removes its modifier; the flying knife uses twice the current panel");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void invalidItemTransfersKeepTheOriginalWeaponAndDoNotThrow(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            ItemStack rejected = new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get());
            helper.assertTrue(!f.main.carryItem(rejected, 0) && !f.main.hasCarriedItem() && rejected.getCount() == 1,
                    "A held passive knife rejects a payload without consuming it or throwing");
            equipIndependent(f);
            ItemStack original = f.owner.getMainHandItem();
            original.setHoverName(Component.literal("original payload"));
            fireFull(f);
            var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
            helper.assertTrue(!knife.carryItem(rejected, 0) && !knife.carryItem(ItemStack.EMPTY, 0),
                    "Duplicate and empty transfers report failure");
            CompoundTag saved = new CompoundTag();
            knife.saveWithoutId(saved);
            helper.assertTrue(knife.hasCarriedItem() && ItemStack.of(saved.getCompound("CarriedItem")).getHoverName()
                            .getString().equals("original payload") && rejected.getCount() == 1,
                    "Rejected duplicate and empty transfers preserve both the carried weapon and the caller's stack");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void failedPayloadTransferDoesNotRemoveTheHeldWeapon(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            ItemStack original = f.owner.getMainHandItem();
            Consumer<EntityJoinLevelEvent> occupy = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner) {
                    knife.carryItem(new ItemStack(Items.STICK), -1);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(occupy);
            try { fireFull(f); }
            finally { MinecraftForge.EVENT_BUS.unregister(occupy); }
            helper.assertTrue(f.owner.getMainHandItem() == original && original.getCount() == 1
                            && MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null && !MomsKnifeWeapon.isUnavailable(f.owner),
                    "A payload rejected after spawning leaves the actual weapon in hand and releases the failed flight");
            f.level.getEntitiesOfClass(ItemEntity.class, f.owner.getBoundingBox().inflate(3),
                    item -> item.getItem().is(Items.STICK)).forEach(ItemEntity::discard);
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void criticalCooldownArmorAndThrowLockUseTheNormalCombatRules(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            readyMelee(f);
            var critical = f.cow(f.owner.position().add(0, 0.45, 1));
            f.owner.setOnGround(false);
            f.owner.fallDistance = 1;
            f.owner.attack(critical);
            helper.assertTrue(Math.abs(critical.getHealth() - 991) < 0.001, "A full critical hit remains 1.5 times the sixfold melee; health="
                    + critical.getHealth() + ", panel=" + f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
            f.owner.fallDistance = 0;
            var weak = f.cow(f.owner.position().add(10, 0.45, 0));
            f.owner.attack(weak);
            helper.assertTrue(1000 - weak.getHealth() < 6, "Spamming left click still has the ordinary attack cooldown penalty");
            readyMelee(f);
            var armored = f.cow(f.owner.position().add(20, 0.45, 0));
            var reference = f.cow(f.owner.position().add(30, 0.45, 0));
            armored.getAttribute(Attributes.ARMOR).setBaseValue(20);
            reference.getAttribute(Attributes.ARMOR).setBaseValue(20);
            reference.hurt(f.owner.damageSources().playerAttack(f.owner), 6);
            f.owner.setOnGround(false);
            f.owner.attack(armored);
            helper.assertTrue(Math.abs(armored.getHealth() - reference.getHealth()) < 0.001, "Armor reduces the scaled damage normally");
            fireFull(f);
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            var blocked = f.cow(f.owner.position().add(40, 0.45, 0));
            f.owner.attack(blocked);
            helper.assertTrue(blocked.getHealth() == 1000, "An in-flight weapon cannot make a melee hit");
            RuntimeEntityEvents.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.owner));
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null && !MomsKnifeWeapon.isUnavailable(f.owner),
                    "Logout cleanup removes the knife and its cooldown");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void aSubtickTapStillDealsOnlyPanelTimesTwoAndReleasesItsCooldown(GameTestHelper helper) {
        var f = fixture(helper);
        equipIndependent(f);
        var fireRate = f.owner.getAttribute(ModAttributes.FIRE_RATE.get());
        fireRate.addTransientModifier(new AttributeModifier(UUID.randomUUID(), "weapon-test-fire-rate",
                20 - fireRate.getValue(), AttributeModifier.Operation.ADDITION));
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(1);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        var attack = (MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get();
        helper.assertTrue(attack.getTotalCharge(f.owner) == 4, "Full charge takes four ordinary shot intervals");
        f.ability.handleAttackInput(f.owner, true);
        f.ability.handleAttackInput(f.owner, false);
        updateEquipment(f.owner);
        var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
        helper.assertTrue(knife != null && Math.abs(knife.getFlightDuration() - 0.5) < 1e-6,
                "A press and release within one tick uses one charge tick and an eighth-block range");
        var target = f.cow(knife.position().add(0, 0, 0.3));
        long start = f.level.getGameTime();
        helper.onEachTick(() -> {
            if (!f.closed && !knife.isRemoved() && f.level.getGameTime() > start) knife.tick();
        });
        helper.runAfterDelay(3, () -> {
            try {
                helper.assertTrue(target.getHealth() == 998, "The out-and-back sweep damages once at two times panel, without the melee multiplier");
                helper.assertTrue(knife.isRemoved() && !MomsKnifeWeapon.isUnavailable(f.owner), "Subtick return clears the entity and cooldown");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void deathDimensionAndEarlyRemovalReleaseTheIndependentSlot(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            fireFull(f);
            RuntimeEntityEvents.onDeath(new LivingDeathEvent(f.owner, f.owner.damageSources().generic()));
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null && !MomsKnifeWeapon.isUnavailable(f.owner),
                    "Death cleanup clears the flight slot and cooldown");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            fireFull(f);
            RuntimeEntityEvents.onChangedDimension(new PlayerEvent.PlayerChangedDimensionEvent(f.owner, f.level.dimension(), Level.NETHER));
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null && !MomsKnifeWeapon.isUnavailable(f.owner),
                    "Dimension cleanup clears the source level's flight and cooldown");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            fireFull(f);
            MomsKnifeWeaponAttack.getFlyingKnife(f.owner).discard();
            helper.assertTrue(!MomsKnifeWeapon.isUnavailable(f.owner), "An early entity removal immediately releases cooldown");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void headDispatchesPrimaryAndAdditionalOnceWhileWeaponUsesOnlyItsOwnSelection(GameTestHelper helper) throws Exception {
        try (var f = fixture(helper)) {
            var primary = new RecordingAttack("launcher_primary");
            var extra = new RecordingAttack("launcher_extra");
            var candidate = new AttackCandidate(primary.getId(), primary, primary.getId(), 1, 0, 0);
            var selectionField = PlayerAbility.class.getDeclaredField("attackSelection");
            selectionField.setAccessible(true);
            selectionField.set(f.ability, new AttackSelection(candidate, candidate, List.of(extra)));
            f.ability.tickAttacks(f.owner);
            f.ability.handleAttackInput(f.owner, true);
            f.ability.handleAttackInput(f.owner, false);
            for (var attack : List.of(primary, extra)) {
                helper.assertTrue(attack.ticks == 1 && attack.presses == 1 && attack.releases == 1 && attack.syncs == 3,
                        "The head forwards each update and input once to both selected attacks");
            }
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            f.ability.tickAttacks(f.owner);
            f.ability.handleAttackInput(f.owner, true);
            helper.assertTrue(primary.ticks == 1 && extra.ticks == 1 && extra.presses == 1,
                    "The weapon neither ticks nor presses the head's primary or additional attacks");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void changingOwnedAttacksDoesNotConsumeAFixedLaunchersCharge(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
            f.ability.handleAttackInput(f.owner, true);
            f.ability.tickAttacks(f.owner);
            var weapon = (MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get();
            int before = f.ability.getChargeAmount(weapon.getChargeBarId());
            helper.assertTrue(before > 0, "The weapon is charging before the owned attack changes");
            f.ability.addAttackType(ModAttackTypes.C_SECTION.getId(), 1, f.owner);
            helper.assertTrue(f.ability.getAttackSelection().mainAttack() != ModAttackTypes.MOMS_KNIFE.get(),
                    "The passive combination actually selects a different primary");
            helper.assertTrue(f.ability.getChargeAmount(weapon.getChargeBarId()) == before
                            && IAttackLauncher.selection(f.owner).mainAttack() == weapon,
                    "Changing the head's selection neither resets the weapon's shared bar nor changes its fixed attack");
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ISAAC_HEAD.get()));
            helper.assertTrue(IAttackLauncher.selection(f.owner) == f.ability.getAttackSelection(),
                    "The head receives the updated passive selection when equipped again");
            helper.succeed();
        }
    }

    private static final class RecordingAttack extends AttackType implements IChargeableAttack {
        private final ResourceLocation id;
        int ticks, presses, releases, syncs;
        RecordingAttack(String name) { super(0); id = ResourceLocation.fromNamespaceAndPath("test", name); }
        @Override public ResourceLocation getId() { return id; }
        @Override public ResourceLocation getChargeBarId() { return id; }
        @Override public int getTotalCharge(Player player) { return 10; }
        @Override public void onTick(ServerPlayer player) { ticks++; }
        @Override public void onPressed(ServerPlayer player) { presses++; }
        @Override public void onReleased(ServerPlayer player) { releases++; }
        @Override public void syncCharge(ServerPlayer player) { syncs++; }
        @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(); }
        @Override public void performAttack(List<AttackContext> contexts, Consumer<AttackContext> fired) {}
        @Override public void makeSound(LivingEntity entity) {}
        @Override public void shoot(AttackContext context) {}
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void chargingUsesTheVanillaSpearUseState(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            var stack = f.owner.getMainHandItem();
            stack.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(f.owner.isUsingItem() && f.owner.getUseItem() == stack,
                    "Charging enters vanilla item use so the normal movement slowdown applies");
            helper.assertTrue(stack.getUseAnimation() == UseAnim.SPEAR && stack.getUseDuration() > 100,
                    "The weapon reuses the native spear charge animation");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void throwingTransfersTheActualWeaponOutOfTheHand(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            fireFull(f);
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) != null, "A knife was launched");
            helper.assertTrue(f.owner.getMainHandItem().isEmpty() && f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 0,
                    "The real weapon belongs to the projectile until it returns");
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void nativeReleaseReturnsTheSameNamedWeaponToItsOriginalEmptySlot(GameTestHelper helper) {
        var f = fixture(helper);
        equipIndependent(f);
        ItemStack original = f.owner.getMainHandItem();
        original.setHoverName(Component.literal("Named knife"));
        original.getOrCreateTag().putString("test_payload", "preserved");
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(1);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        original.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
        var attack = (MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        f.owner.releaseUsingItem();
        var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
        helper.assertTrue(knife != null && !f.owner.isUsingItem() && f.owner.getMainHandItem().isEmpty(),
                "Native release launches once and finishes the spear pose");
        long start = f.level.getGameTime();
        helper.onEachTick(() -> { if (!f.closed && !knife.isRemoved() && f.level.getGameTime() > start) knife.tick(); });
        helper.runAfterDelay(4, () -> {
            try {
                helper.assertTrue(knife.isRemoved() && f.owner.getMainHandItem() == original,
                        "An empty original slot receives the actual weapon on return");
                helper.assertTrue("preserved".equals(original.getTag().getString("test_payload"))
                        && f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 1,
                        "Return preserves item data without duplicating the weapon");
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void logoutDropsTheCarriedWeaponExactlyOnce(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            f.owner.getMainHandItem().setHoverName(Component.literal("Logout knife"));
            fireFull(f);
            RuntimeEntityEvents.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.owner));
            RuntimeEntityEvents.onLogout(new PlayerEvent.PlayerLoggedOutEvent(f.owner));
            var drops = weaponDrops(f);
            helper.assertTrue(f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 0 && drops.size() == 1,
                    "Logout leaves one world item and no inventory copy, even when cleanup repeats");
            helper.assertTrue(drops.get(0).getItem().getHoverName().getString().equals("Logout knife"), "Drop keeps the original data");
            drops.forEach(ItemEntity::discard);
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void savingDoesNotInterruptButLoadingTheSnapshotCreatesOneItemDrop(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            f.owner.getMainHandItem().getOrCreateTag().putString("test_payload", "saved");
            fireFull(f);
            var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
            var saved = new CompoundTag();
            helper.assertTrue(knife.save(saved), "The carried item is saved with the projectile's location");
            helper.assertTrue(!knife.isRemoved() && knife.hasCarriedItem() && weaponDrops(f).isEmpty(),
                    "An ordinary save does not interrupt a live flight or emit a drop");
            Vec3 savedPosition = knife.position();
            knife.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            helper.assertTrue(weaponDrops(f).isEmpty(), "Chunk unloading retains the saved payload rather than spawning another copy");
            var restored = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), f.level);
            restored.load(saved);
            restored.tick();
            restored.tick();
            var drops = weaponDrops(f);
            helper.assertTrue(restored.isRemoved() && drops.size() == 1, "A restored snapshot becomes exactly one drop without replaying the flight");
            helper.assertTrue(drops.get(0).position().distanceTo(savedPosition) < 0.01
                    && "saved".equals(drops.get(0).getItem().getTag().getString("test_payload")), "Recovery retains location and item data");
            drops.forEach(ItemEntity::discard);
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon_shutdown", timeoutTicks = 15)
    public static void serverStoppingConvertsTheWeaponBeforeWorldsAreSaved(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            fireFull(f);
            MomsKnifeWeaponEvents.onServerStopping(new ServerStoppingEvent(f.level.getServer()));
            var drops = weaponDrops(f);
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null && drops.size() == 1,
                    "The pre-save shutdown hook settles the carried item into the world");
            helper.assertTrue(!f.owner.getCooldowns().isOnCooldown(ModItems.MOMS_KNIFE_WEAPON.get()), "Shutdown also clears cooldown");
            drops.forEach(ItemEntity::discard);
            helper.succeed();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void rejectedProjectileSpawnKeepsTheWeaponInHand(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            var original = f.owner.getMainHandItem();
            Consumer<EntityJoinLevelEvent> reject = event -> {
                if (event.getEntity() instanceof PlayerMomsKnifeEntity knife && knife.getOwner() == f.owner) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(reject);
            try { fireFull(f); }
            finally { MinecraftForge.EVENT_BUS.unregister(reject); }
            helper.assertTrue(f.owner.getMainHandItem() == original && MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null
                    && !MomsKnifeWeapon.isUnavailable(f.owner), "A cancelled spawn neither consumes the item nor starts cooldown");
            helper.succeed();
        }
    }

    private static List<ItemEntity> weaponDrops(PlayerMomsKnifeGameTests.Fixture f) {
        return f.level.getEntitiesOfClass(ItemEntity.class, f.owner.getBoundingBox().inflate(10),
                item -> item.getItem().is(ModItems.MOMS_KNIFE_WEAPON.get()));
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void returningToAFullInventoryDropsOnceWithoutOverwritingItems(GameTestHelper helper) {
        var f = fixture(helper);
        equipIndependent(f);
        f.owner.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(1);
        f.owner.getAttribute(ModAttributes.BULLET_SPEED.get()).setBaseValue(1);
        fireFull(f);
        var knife = MomsKnifeWeaponAttack.getFlyingKnife(f.owner);
        for (int slot = 0; slot < f.owner.getInventory().items.size(); slot++) {
            f.owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        long start = f.level.getGameTime();
        helper.onEachTick(() -> { if (!f.closed && !knife.isRemoved() && f.level.getGameTime() > start) knife.tick(); });
        helper.runAfterDelay(4, () -> {
            try {
                var drops = weaponDrops(f);
                helper.assertTrue(knife.isRemoved() && drops.size() == 1 && !MomsKnifeWeapon.isUnavailable(f.owner),
                        "A full-inventory return becomes one drop and unlocks the weapon type");
                helper.assertTrue(f.owner.getInventory().countItem(Items.COBBLESTONE) == 36 * 64,
                        "The original slot's replacement and all other inventory contents remain intact");
                drops.forEach(ItemEntity::discard);
                helper.succeed();
            } finally { f.close(); }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "moms_knife_weapon", timeoutTicks = 15)
    public static void switchingCopiesCancelsChargeAndAStaleReleaseCannotThrow(GameTestHelper helper) {
        try (var f = fixture(helper)) {
            equipIndependent(f);
            var original = f.owner.getMainHandItem();
            original.getItem().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            f.ability.tickAttacks(f.owner);
            f.owner.getInventory().selected = 1;
            var replacement = new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get());
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            replacement.getItem().releaseUsing(replacement, f.level, f.owner, replacement.getUseDuration() - 1);
            helper.assertTrue(MomsKnifeWeaponAttack.getFlyingKnife(f.owner) == null
                            && f.owner.getInventory().countItem(ModItems.MOMS_KNIFE_WEAPON.get()) == 2,
                    "A changed source invalidates the old press even when the new item has the same type");
            f.owner.stopUsingItem();
            helper.assertTrue(!f.ability.isHoldingRightClick() && f.ability.getVisibleChargeBarIds().isEmpty(),
                    "Cancelling vanilla use also clears the launcher state and its charge bar");
            helper.succeed();
        }
    }

    private static void equipIndependent(PlayerMomsKnifeGameTests.Fixture f) {
        f.main.discard();
        f.ability.addAttackType(ModAttackTypes.MOMS_KNIFE.getId(), -1, f.owner);
        f.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MOMS_KNIFE_WEAPON.get()));
        updateEquipment(f.owner);
        f.ability.refreshLauncher(f.owner);
    }

    private static void fireFull(PlayerMomsKnifeGameTests.Fixture f) {
        var attack = (MomsKnifeWeaponAttack) ModAttackTypes.MOMS_KNIFE_WEAPON.get();
        f.ability.setChargeAmount(attack.getChargeBarId(), attack.getTotalCharge(f.owner));
        attack.onReleased(f.owner);
    }

    private static void readyMelee(PlayerMomsKnifeGameTests.Fixture f) {
        updateEquipment(f.owner);
        f.owner.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4);
        f.owner.fallDistance = 0;
        f.owner.setSprinting(false);
        // FakePlayer.tick is empty, so place the vanilla cooldown at its fully recovered starting state.
        ObfuscationReflectionHelper.setPrivateValue(LivingEntity.class, f.owner, 30, "f_20922_");
    }

    private static void updateEquipment(ServerPlayer player) {
        // FakePlayer skips LivingEntity.tick, which normally applies equipment attributes.
        try { ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_21315_").invoke(player); }
        catch (ReflectiveOperationException exception) { throw new AssertionError("Equipment update fixture failed", exception); }
    }
}
