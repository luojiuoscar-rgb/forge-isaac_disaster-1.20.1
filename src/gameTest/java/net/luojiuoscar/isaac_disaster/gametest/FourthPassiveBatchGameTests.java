package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.item.pickup.Heart;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.JumperCablesKillCounter;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items.SoulHeart;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FourthPassiveBatchGameTests {
    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "passive_batch4"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        DefaultAttributeManager.apply(player);
        CurioSlotManager.tick(player);
        return player;
    }

    private static ItemStack equip(ServerPlayer player, int index, Item item) {
        var stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        CuriosApi.getCuriosInventory(player).resolve().orElseThrow()
                .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks().setStackInSlot(index, stack);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
        return stack;
    }

    private static void tickPeriodicModules(ServerPlayer player) {
        player.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow().getPeriodicModuleQueue().tickAll(player);
    }

    private static void close(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-4, message + ": " + actual + " vs " + expected);
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void allTenPhysicallyEquipAndRemoveWithoutResidualStats(GameTestHelper helper) {
        var player = player(helper);
        var items = List.of(ModPassiveItems.NUMBER_ONE.get(), ModPassiveItems.HUMBLEING_BUNDLE.get(),
                ModPassiveItems.BUTT_BOMBS.get(), ModPassiveItems.INFAMY.get(), ModPassiveItems.DARK_PRINCES_CROWN.get(),
                ModPassiveItems.APPLE.get(), ModPassiveItems.DEAD_TOOTH.get(), ModPassiveItems.ADRENALINE.get(),
                ModPassiveItems.JUMPER_CABLES.get(), ModPassiveItems.SOUL_LOCKET.get());
        for (var item : items) {
            var before = new EnumMap<StatManager, Double>(StatManager.class);
            for (var stat : StatManager.values()) before.put(stat, player.getAttributeValue(stat.getAttribute()));
            ItemStack equipped = equip(player, 0, item);
            tickPeriodicModules(player);
            CurioSlotManager.requestReconcile(player);
            CurioSlotManager.tick(player);
            equip(player, 0, Items.AIR);
            for (var stat : StatManager.values()) close(helper, player.getAttributeValue(stat.getAttribute()), before.get(stat), item + " removes " + stat);
            helper.assertTrue(player.getCapability(EffectModulesProvider.EFFECT_MODULES).resolve().orElseThrow()
                    .getPeriodicModuleQueue().getQueue().isEmpty(), "No periodic modules remain after " + item);
            helper.assertTrue(!equipped.isEmpty(), "Physical equipment is retained by the caller");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void allTenSaveAndReloadWithoutReapplyingEquipment(GameTestHelper helper) {
        for (var item : List.of(ModPassiveItems.NUMBER_ONE.get(), ModPassiveItems.HUMBLEING_BUNDLE.get(),
                ModPassiveItems.BUTT_BOMBS.get(), ModPassiveItems.INFAMY.get(), ModPassiveItems.DARK_PRINCES_CROWN.get(),
                ModPassiveItems.APPLE.get(), ModPassiveItems.DEAD_TOOTH.get(), ModPassiveItems.ADRENALINE.get(),
                ModPassiveItems.JUMPER_CABLES.get(), ModPassiveItems.SOUL_LOCKET.get())) {
            var original = player(helper);
            equip(original, 0, item); tickPeriodicModules(original);
            PlayerHelper.refreshFireRateBaseValue(original);
            var expected = new EnumMap<StatManager, Double>(StatManager.class);
            for (var stat : StatManager.values()) expected.put(stat, original.getAttributeValue(stat.getAttribute()));
            int rewards = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(original.position(), original.position()).inflate(2)).stream().mapToInt(s -> s.getItem().getCount()).sum();
            CompoundTag saved = original.saveWithoutId(new CompoundTag());
            var restored = player(helper); restored.load(saved); DefaultAttributeManager.apply(restored);
            CurioSlotManager.requestReconcile(restored);
            for (int tick = 0; tick < 3; tick++) {
                CurioSlotManager.tick(restored);
                new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(restored));
            }
            tickPeriodicModules(restored); PlayerHelper.refreshFireRateBaseValue(restored);
            for (var stat : StatManager.values()) close(helper, restored.getAttributeValue(stat.getAttribute()), expected.get(stat), item + " persists " + stat);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(original.position(), original.position()).inflate(2)).stream().mapToInt(s -> s.getItem().getCount()).sum() == rewards,
                    "Reload does not repeat first-obtain drops for " + item);
            equip(restored, 0, Items.AIR); equip(original, 0, Items.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void numberOneAppliesOrdinaryRangeToTearsAndLasers(GameTestHelper helper) {
        var player = player(helper);
        player.getAttribute(ModAttributes.BULLET_RANGE.get()).setBaseValue(40);
        double tears = player.getAttributeValue(ModAttributes.TEARS.get());
        equip(player, 0, ModPassiveItems.NUMBER_ONE.get());
        equip(player, 1, ModPassiveItems.NUMBER_ONE.get());
        close(helper, player.getAttributeValue(ModAttributes.TEARS.get()), tears + StatManager.TEARS.getBonus() * 2 * 2, "Both additive tear bonuses apply");
        close(helper, ModAttackTypes.BULLET.get().getRange(player), (40 - 2 * StatManager.RANGE.getBonus()) * 0.8, "Range additions stack while the multiplier applies once");
        for (var laser : List.of(ModAttackTypes.LASER.get(), ModAttackTypes.BRIMSTONE.get(), ModAttackTypes.TECHNOLOGY2.get())) {
            close(helper, laser.getRange(player), (40 - 2 * StatManager.RANGE.getBonus()) * 0.8, "Lasers use the same range attribute as tears");
        }
        var attack = ModAttackTypes.BULLET.get().createAttackContext(player, player);
        double y = attack.getPos().y;
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.ATTACK_CONTEXT, attack);
        TriggerModuleEvents.dispatch(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        close(helper, attack.getPos().y, y, "Number One only changes attributes and does not move the launch point");
        equip(player, 2, ModPassiveItems.CRICKETS_BODY.get());
        close(helper, ModAttackTypes.LASER.get().getRange(player), (40 - 2 * StatManager.RANGE.getBonus()) * 0.8 * 0.8, "Range multipliers compose normally for lasers");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void bundlePairsOnlyEligibleLootAndUsesConfiguredCoin(GameTestHelper helper) {
        var player = player(helper);
        equip(player, 0, ModPassiveItems.HUMBLEING_BUNDLE.get());
        try (var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.COIN_TIER_1_ID, "minecraft:gold_nugget");
            var entries = new ObjectArrayList<ItemStack>();
            var configuredCoin = new ItemStack(Items.GOLD_NUGGET, 256);
            configuredCoin.getOrCreateTag().putBoolean("bundle_test", true);
            entries.add(configuredCoin);
            entries.add(new ItemStack(ModItems.BOMB.get(), 256));
            entries.add(new ItemStack(ModItems.KEY.get(), 256));
            entries.add(new ItemStack(ModItems.RED_HEART.get(), 256));
            entries.add(new ItemStack(ModItems.DOUBLE_BOMB.get(), 3));
            entries.add(new ItemStack(ModItems.SOUL_HEART.get(), 3));
            var params = new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY);
            var event = new GeneralLootModifyEvent(player, new LootContext.Builder(params).create(null), entries);
            TriggerModuleEvents.onLoot(event);
            int coins = entries.stream().filter(s -> s.is(Items.GOLD_NUGGET)).mapToInt(ItemStack::getCount).sum();
            helper.assertTrue(coins > 320 && coins < 448, "Configured ordinary coins pair with roughly 50% probability");
            helper.assertTrue(entries.stream().filter(s -> s.is(Items.GOLD_NUGGET)).allMatch(s -> s.getCount() <= s.getMaxStackSize() && s.getTag() != null && s.getTag().getBoolean("bundle_test")), "Coin conversion keeps NBT and legal stack sizes");
            helper.assertTrue(entries.stream().filter(s -> s.is(ModItems.SOUL_HEART.get())).mapToInt(ItemStack::getCount).sum() == 3, "Soul hearts are excluded");
            helper.assertTrue(entries.stream().anyMatch(s -> s.is(ModItems.KEY_RING.get())), "Keys become key rings");
            helper.assertTrue(entries.stream().anyMatch(s -> s.is(ModItems.DOUBLE_RED_HEART.get())), "Red hearts become double red hearts");
            equip(player, 1, ModPassiveItems.BOGO_BOMBS.get());
            var bombs = new ObjectArrayList<ItemStack>(); bombs.add(new ItemStack(ModItems.BOMB.get(), 32));
            TriggerModuleEvents.onLoot(new GeneralLootModifyEvent(player, event.getLootContext(), bombs));
            helper.assertTrue(bombs.size() == 1 && bombs.get(0).is(ModItems.DOUBLE_BOMB.get()) && bombs.get(0).getCount() == 32, "Bogo pairing is not doubled a second time");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void sharedLootTransformsKeepMappingsAndLegalStackSizes(GameTestHelper helper) {
        var player = player(helper);
        var params = new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY);
        var loot = new LootContext.Builder(params).create(null);
        var entries = new ObjectArrayList<ItemStack>();
        entries.add(new ItemStack(ModItems.BOMB.get(), 256));
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.EVENT, new GeneralLootModifyEvent(player, loot, entries));
        ModExecutableEffects.BOGO_BOMBS.get().apply(context);
        helper.assertTrue(entries.stream().allMatch(s -> s.is(ModItems.DOUBLE_BOMB.get()) && s.getCount() <= s.getMaxStackSize()), "Large converted counts split into legal stacks");
        helper.assertTrue(entries.stream().mapToInt(ItemStack::getCount).sum() == 256, "BOGO conversion preserves the original pickup quantity");
        entries.clear();
        entries.add(new ItemStack(ModItems.SOUL_HEART.get(), 12)); entries.add(new ItemStack(ModItems.HALF_SOUL_HEART.get(), 3));
        entries.add(new ItemStack(ModItems.RED_HEART.get(), 4));
        context.set(ContextKeys.AMPLIFIER, 10.0);
        ModExecutableEffects.BLACK_LIPSTICK.get().apply(context);
        helper.assertTrue(entries.stream().filter(s -> s.is(ModItems.BLACK_HEART.get())).mapToInt(ItemStack::getCount).sum() == 15, "The last replacement covers a shorter mapping list");
        helper.assertTrue(entries.stream().filter(s -> s.is(ModItems.RED_HEART.get())).mapToInt(ItemStack::getCount).sum() == 4, "Non-selected loot stays unchanged");
        entries.clear();
        entries.add(new ItemStack(ModItems.RED_HEART.get(), 2)); entries.add(new ItemStack(ModItems.SOUL_HEART.get(), 3)); entries.add(new ItemStack(ModItems.BOMB.get(), 4));
        context.set(ContextKeys.AMPLIFIER, 30.0);
        ModExecutableEffects.BIBLE_TRACT.get().apply(context);
        helper.assertTrue(entries.stream().filter(s -> s.is(ModItems.ETERNAL_HEART.get())).mapToInt(ItemStack::getCount).sum() == 5, "Heart instance matching remains higher priority than explicit type lists");
        helper.assertTrue(entries.stream().filter(s -> s.is(ModItems.BOMB.get())).mapToInt(ItemStack::getCount).sum() == 4, "Instance conversion excludes non-heart items");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void buttBombActuallyAttachesAndDamagesNearbyEnemies(GameTestHelper helper) {
        var player = player(helper);
        var area = new AABB(player.position(), player.position()).inflate(2);
        int before = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .filter(s -> s.getItem().is(ModItems.BOMB.get())).mapToInt(s -> s.getItem().getCount()).sum();
        equip(player, 0, ModPassiveItems.BUTT_BOMBS.get());
        int rewards = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .filter(s -> s.getItem().is(ModItems.BOMB.get())).mapToInt(s -> s.getItem().getCount()).sum();
        helper.assertTrue(rewards - before == 5, "First equip grants five bombs through the existing drop flow");
        CurioSlotManager.requestReconcile(player); CurioSlotManager.tick(player);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .filter(s -> s.getItem().is(ModItems.BOMB.get())).mapToInt(s -> s.getItem().getCount()).sum() == rewards, "Reconciliation does not repeat the first reward");
        var cow = helper.spawn(EntityType.COW, new BlockPos(2, 1, 1)); cow.setNoAi(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        for (int power : new int[]{1, 4, 5, 7}) {
            cow.setHealth(200); cow.invulnerableTime = 0; cow.removeEffect(ModEffects.PANIC.get());
            var bomb = new IsaacBomb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), player, BombData.NORMAL);
            bomb.setPower(power);
            helper.getLevel().addFreshEntity(bomb);
            helper.assertTrue(bomb.getCachedEffect().contains(ModExecutableEffects.BUTT_BOMB.get()), "Bomb spawn attaches the fart effect at power " + power);
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.ENTITY, List.of(bomb)); context.set(ContextKeys.TARGET_POSITION, bomb.position());
            bomb.getCachedEffect().apply(context);
            close(helper, 200 - cow.getHealth(), StatManager.DAMAGE.getBonus() * (power > 4 ? 20 : 10), "Fart damage uses the requested power boundary");
            helper.assertTrue(cow.getEffect(ModEffects.PANIC.get()).getDuration() == (power > 4 ? 160 : 80), "Fart fear scales with bomb size");
            bomb.discard();
        }
        cow.discard(); helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void infamyBlocksAllRangedTypesButNotMelee(GameTestHelper helper) {
        var player = player(helper);
        equip(player, 0, ModPassiveItems.INFAMY.get());
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 1)); zombie.setNoAi(true);
        var arrow = new Arrow(helper.getLevel(), zombie);
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        for (var source : List.of(player.damageSources().arrow(arrow, zombie),
                new DamageSource(registry.getHolderOrThrow(ModDamageType.TEAR), zombie, zombie),
                new DamageSource(registry.getHolderOrThrow(ModDamageType.LASER), zombie, zombie))) {
            int blocked = 0;
            for (int i = 0; i < 100; i++) {
                var hit = new LivingHurtEvent(player, source, 3);
                TriggerModuleEvents.onLivingHurt(hit);
                if (hit.getAmount() == 0) blocked++;
            }
            helper.assertTrue(blocked > 25 && blocked < 75, "Infamy blocks approximately half of arrows, tears and lasers: " + source.type().msgId());
        }
        for (var source : List.of(player.damageSources().mobAttack(zombie), player.damageSources().explosion(null, zombie))) {
            var hit = new LivingHurtEvent(player, source, 3); TriggerModuleEvents.onLivingHurt(hit);
            close(helper, hit.getAmount(), 3, "Melee and explosions are excluded");
        }
        zombie.discard(); arrow.discard(); helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void conditionalStatsTrackHealthAndCleanUp(GameTestHelper helper) {
        var player = player(helper);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        player.setHealth((float) StatManager.MAX_HEALTH.getBonus());
        double rate = player.getAttributeValue(ModAttributes.FIRE_RATE.get());
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        equip(player, 0, ModPassiveItems.DARK_PRINCES_CROWN.get());
        equip(player, 1, ModPassiveItems.ADRENALINE.get());
        tickPeriodicModules(player);
        close(helper, player.getAttributeValue(ModAttributes.FIRE_RATE.get()), rate + 2 * StatManager.FIRE_RATE.getBonus(), "Crown grants fire rate rather than Tears");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) > damage, "Missing complete hearts grant adrenaline damage");
        player.setHealth(player.getMaxHealth()); tickPeriodicModules(player);
        close(helper, player.getAttributeValue(ModAttributes.FIRE_RATE.get()), rate, "Crown bonus disappears above one red heart");
        close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE), damage, "Healing removes adrenaline damage");
        double unit = StatManager.MAX_HEALTH.getBonus();
        double oneUnitDamage = Math.pow(2, 1.6) * 0.1 * StatManager.DAMAGE.getBonus();
        for (double missingUnits : new double[]{0.01, 0.1, 0.5, 0.99, 1, 1.01, 1.5, 2}) {
            player.setHealth((float) (player.getMaxHealth() - unit * missingUnits));
            player.setAbsorptionAmount(10);
            tickPeriodicModules(player);
            close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage, oneUnitDamage * missingUnits,
                    "Adrenaline scales linearly even below one missing heart and across its boundary");
        }
        var bonusLine = ModPassiveAbilities.ADRENALINE.get().getDesc(null, player).get(2);
        var arguments = ((TranslatableContents) bonusLine.getContents()).getArgs();
        helper.assertTrue(arguments.length == 1 && arguments[0] instanceof Component number
                        && TextColor.fromRgb(ColorManager.SYNERGY).equals(number.getStyle().getColor()),
                "Adrenaline presents its current bonus through the yellow dynamic component");
        equip(player, 2, ModPassiveItems.ADRENALINE.get());
        tickPeriodicModules(player);
        close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage, oneUnitDamage * 2,
                "A second Adrenaline does not increase its damage contribution");
        equip(player, 1, Items.AIR);
        tickPeriodicModules(player);
        close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE) - damage, oneUnitDamage * 2,
                "Removing one copy preserves the one-copy bonus");
        equip(player, 2, Items.AIR);
        close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE), damage,
                "Removing the last Adrenaline immediately removes its bonus");
        player.setHealth((float) StatManager.MAX_HEALTH.getBonus()); tickPeriodicModules(player);
        equip(player, 0, Items.AIR); equip(player, 1, Items.AIR);
        close(helper, player.getAttributeValue(ModAttributes.FIRE_RATE.get()), rate, "Unequip removes conditional fire rate");
        close(helper, player.getAttributeValue(Attributes.ATTACK_DAMAGE), damage, "Unequip removes conditional damage");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void appleRazorsCarryFourfoldDamageIntoDerivedTears(GameTestHelper helper) {
        var player = player(helper); equip(player, 0, ModPassiveItems.APPLE.get());
        player.getAttribute(Attributes.LUCK).setBaseValue(14);
        var attack = ModAttackTypes.BULLET.get().createAttackContext(player, player);
        var context = new ExecutableEffectContext(player); context.set(ContextKeys.ATTACK_CONTEXT, attack);
        var originalVisuals = attack.getVisualIds();
        TriggerModuleEvents.dispatch(context, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        helper.assertTrue(attack.getVisualIds().equals(originalVisuals), "Apple keeps the ordinary tear appearance even when its damage effect triggers");
        attack.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, () -> new IExecutableEffect() {
            @Override
            public double getPriority() { return 0; }

            @Override
            public void apply(ExecutableEffectContext hitContext) {
                var hit = (IsaacAttackBeforeHitEntityEvent) hitContext.get(ContextKeys.EVENT);
                hit.setDamage(hit.getDamage() + 3);
            }
        }));
        var target = helper.spawn(EntityType.COW, new BlockPos(2, 1, 1)); target.setNoAi(true);
        for (var shot : List.of(attack, attack.toBuilder().damage(1.0).build())) {
            var hit = new IsaacAttackBeforeHitEntityEvent(null, player, ModAttackTypes.BULLET.getId(), shot.copyTrigger(), new EntityHitResult(target), shot.getDamage());
            var hitContext = new ExecutableEffectContext(player); hitContext.set(ContextKeys.EVENT, hit);
            shot.copyTrigger().fire(hitContext, ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE);
            close(helper, hit.getDamage(), (shot.getDamage() + 3) * 4, "Apple quadruples the complete damage after a later-inserted additive trigger, including derived tears");
        }
        equip(player, 1, ModPassiveItems.TERRA.get());
        var stone = ModAttackTypes.BULLET.get().createAttackContext(player, player);
        var stoneContext = new ExecutableEffectContext(player); stoneContext.set(ContextKeys.ATTACK_CONTEXT, stone);
        TriggerModuleEvents.dispatch(stoneContext, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        helper.assertTrue(stone.getVisualIds().contains(ModBulletVisuals.TERRA_ROCK.getId()),
                "Terra attaches its rock art alongside Apple without losing damage triggers");
        helper.assertTrue(stone.getTrigger().contains(ModExecutableEffects.BREAK_BLOCK_AND_DROP.get()),
                "Terra retains its block-breaking trigger");
        helper.assertTrue(stone.toBuilder().damage(1.0).build().getVisualIds().contains(ModBulletVisuals.TERRA_ROCK.getId()),
                "Derived tears inherit Terra's rock visual");
        equip(player, 1, Items.AIR);
        var ordinary = ModAttackTypes.BULLET.get().createAttackContext(player, player);
        var ordinaryContext = new ExecutableEffectContext(player); ordinaryContext.set(ContextKeys.ATTACK_CONTEXT, ordinary);
        TriggerModuleEvents.dispatch(ordinaryContext, ModTriggerTypes.ATTACK_CONTEXT_PREPARE);
        helper.assertTrue(!ordinary.getVisualIds().contains(ModBulletVisuals.TERRA_ROCK.getId()), "Removing Terra restores normal tear visuals");
        target.discard(); helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void deadToothRunsWhileIdleAndSnapshotsPoisonDamage(GameTestHelper helper) {
        var player = player(helper); equip(player, 0, ModPassiveItems.DEAD_TOOTH.get());
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8);
        var target = helper.spawn(EntityType.COW, new BlockPos(2, 1, 1)); target.setNoAi(true);
        target.setPos(player.position().add(2.4, 0, 0));
        var outside = helper.spawn(EntityType.COW, new BlockPos(3, 1, 1)); outside.setNoAi(true);
        outside.setPos(player.position().add(2.6, 0, 0));
        var friendly = helper.spawn(EntityType.COW, new BlockPos(1, 1, 2)); friendly.setNoAi(true);
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("tooth" + UUID.randomUUID().toString().substring(0, 8));
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(friendly.getScoreboardName(), team);
        try {
            player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), 100));
            for (int i = 0; i < 12; i++) tickPeriodicModules(player);
            var poison = target.getEffect(ModEffects.DEADLY_POISON.get());
            helper.assertTrue(poison != null && poison.getAmplifier() == 14 && poison.getDuration() == 120, "At 2.4 blocks the aura encodes full panel damage and caps linger");
            helper.assertTrue(!outside.hasEffect(ModEffects.DEADLY_POISON.get()), "Targets beyond 2.5 blocks are excluded");
            helper.assertTrue(!friendly.hasEffect(ModEffects.DEADLY_POISON.get()), "Allied targets inside range are excluded");
            target.removeEffect(ModEffects.DEADLY_POISON.get());
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
            for (int i = 0; i < 4; i++) tickPeriodicModules(player);
            helper.assertTrue(target.getEffect(ModEffects.DEADLY_POISON.get()).getAmplifier() == 18, "Idle aura without IsaacHead also runs while firing is disabled and snapshots current damage");
            target.removeEffect(ModEffects.DEADLY_POISON.get());
            equip(player, 0, Items.AIR);
            for (int i = 0; i < 4; i++) tickPeriodicModules(player);
            helper.assertTrue(!target.hasEffect(ModEffects.DEADLY_POISON.get()), "Unequip removes the unconditional aura");
        } finally {
            scoreboard.removePlayerTeam(team);
            target.discard(); outside.discard(); friendly.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void jumperCablesPersistsProgressAndCountsAllPlayerSourcedDeaths(GameTestHelper helper) {
        var player = player(helper); equip(player, 0, ModPassiveItems.JUMPER_CABLES.get());
        var active = new ItemStack(ModActiveItems.BOOK_OF_SHADOW.get()); active.setDamageValue(300); player.getInventory().setItem(3, active);
        var victim = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 1)); victim.setNoAi(true);
        for (int i = 0; i < 14; i++) TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(victim, player.damageSources().playerAttack(player)));
        helper.assertTrue(active.getDamageValue() == 300, "Fourteen kills grant no charge");
        var data = player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).resolve().orElseThrow();
        var direct = new ExecutableEffectContext(player);
        ModExecutableEffects.JUMPER_CABLES.get().apply(direct);
        helper.assertTrue(active.getDamageValue() == 200, "Charging can be invoked directly without a death event");
        close(helper, data.getDouble(JumperCablesKillCounter.DATA_KEY), 14,
                "Direct charging does not consume kill progress");
        active.setDamageValue(300);
        direct.set(ContextKeys.EVENT, new LivingHurtEvent(player, player.damageSources().generic(), 1));
        ModExecutableEffects.JUMPER_CABLES.get().apply(direct);
        helper.assertTrue(active.getDamageValue() == 200, "Charging also works from another event context");
        close(helper, data.getDouble(JumperCablesKillCounter.DATA_KEY), 14,
                "Charging from a hurt context does not modify kill progress");
        active.setDamageValue(300);
        var saved = new CompoundTag(); data.saveNBTData(saved); data.loadNBTData(saved);
        victim.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), 40));
        TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(victim, player.damageSources().playerAttack(player)));
        helper.assertTrue(active.getDamageValue() == 200, "The frozen fifteenth player-sourced death charges one unit after progress reload");
        TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(player, player.damageSources().playerAttack(player)));
        var stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(3, 1, 1));
        TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(stand, player.damageSources().playerAttack(player)));
        close(helper, data.getDouble(JumperCablesKillCounter.DATA_KEY), 2, "Player and non-Mob deaths count when credited to the owner");
        var canceled = new LivingDeathEvent(victim, player.damageSources().playerAttack(player)); canceled.setCanceled(true);
        TriggerModuleEvents.onKilledEntity(canceled);
        TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(victim, player.damageSources().mobAttack(victim)));
        close(helper, data.getDouble(JumperCablesKillCounter.DATA_KEY), 2, "Canceled deaths and other attackers do not count");
        stand.discard();
        equip(player, 0, Items.AIR);
        helper.assertTrue(data.getDouble(JumperCablesKillCounter.DATA_KEY) == null, "Losing the last copy clears progress");
        active.setDamageValue(100);
        ModExecutableEffects.JUMPER_CABLES.get().apply(new ExecutableEffectContext(player));
        helper.assertTrue(active.getDamageValue() == 0, "The reusable payoff does not require ownership of Jumper Cables");
        helper.assertTrue(data.getDouble(JumperCablesKillCounter.DATA_KEY) == null,
                "Direct invocation does not create a counter");
        equip(player, 0, ModPassiveItems.JUMPER_CABLES.get());
        player.getInventory().clearContent();
        player.getInventory().selected = 5;
        var ordered = new ArrayList<ItemStack>();
        for (int slot : new int[]{5, 40, 0, 2, 9, 17}) {
            var stack = new ItemStack(ModActiveItems.BOOK_OF_SHADOW.get());
            stack.setDamageValue(100);
            player.getInventory().setItem(slot, stack);
            ordered.add(stack);
        }
        for (int charged = 0; charged < ordered.size(); charged++) {
            for (int i = 0; i < 15; i++) TriggerModuleEvents.onKilledEntity(new LivingDeathEvent(victim, player.damageSources().playerAttack(player)));
            for (int index = 0; index < ordered.size(); index++) {
                helper.assertTrue(ordered.get(index).getDamageValue() == (index <= charged ? 0 : 100),
                        "Each fifteenth kill charges one unfinished item: main hand, offhand, hotbar by index, then inventory by index");
            }
        }
        equip(player, 0, Items.AIR);
        victim.discard(); helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void incrementalChargingSelectsOneUnfinishedActiveAndPreservesMegaOvercharge(GameTestHelper helper) {
        var player = player(helper);
        for (var effect : List.of(ModExecutableEffects.HABIT, ModExecutableEffects.VOLT45,
                ModExecutableEffects.BATTERY, ModExecutableEffects.SMALL_BATTERY, ModExecutableEffects.MEGA_BATTERY)) {
            player.getInventory().clearContent();
            player.getInventory().selected = 4;
            var sword = new ItemStack(Items.DIAMOND_SWORD); sword.setDamageValue(100);
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
            for (int slot : new int[]{40, 0}) {
                var full = new ItemStack(ModActiveItems.BOOK_OF_SHADOW.get());
                ActiveItem.fullCharge(full, true);
                player.getInventory().setItem(slot, full);
            }
            var selected = new ItemStack(ModActiveItems.BOOK_OF_SHADOW.get()); selected.setDamageValue(500);
            var later = new ItemStack(ModActiveItems.BOOK_OF_SHADOW.get()); later.setDamageValue(500);
            player.getInventory().setItem(1, selected); player.getInventory().setItem(2, later);
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.HAND, InteractionHand.MAIN_HAND);
            context.set(ContextKeys.DOUBLE, List.of(6.8));
            effect.get().apply(context);
            int amount = effect == ModExecutableEffects.HABIT ? 100
                    : effect == ModExecutableEffects.VOLT45 ? Math.max(6 * (GameplayConfig.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get() / 2), 1)
                    : effect == ModExecutableEffects.SMALL_BATTERY ? 400
                    : effect == ModExecutableEffects.BATTERY ? 1200 : 2400;
            helper.assertTrue(effect == ModExecutableEffects.MEGA_BATTERY || selected.getDamageValue() == Math.max(0, 500 - amount),
                    effect.getId() + " selects the first unfinished active after full hands/slots");
            helper.assertTrue(later.getDamageValue() == 500 && !ActiveItem.getOverCharged(later),
                    effect.getId() + " charges one item without spillover");
            helper.assertTrue(sword.getDamageValue() == 100, "Battery charging never repairs another held item");
            helper.assertTrue(ActiveItem.getOverCharged(selected) == (effect == ModExecutableEffects.MEGA_BATTERY),
                    "Only the mega battery forces overcharge without The Battery");
        }
        helper.succeed();
    }

    private static double locketUnits(ServerPlayer player) {
        double total = 0;
        for (var stat : List.of(StatManager.MOVEMENT_SPEED, StatManager.TEARS, StatManager.DAMAGE, StatManager.RANGE, StatManager.BULLET_SPEED, StatManager.LUCK)) {
            var modifier = player.getAttribute(stat.getAttribute()).getModifier(stat.getUUID());
            double ratio = stat == StatManager.TEARS ? 0.15 : stat == StatManager.RANGE ? 0.35 : 0.2;
            if (modifier != null) total += modifier.getAmount() / stat.getBonus() / ratio;
        }
        return total;
    }

    @GameTest(template = "trajectory_empty", batch = "passive_batch4")
    public static void soulLocketRewardsActualHeartUseAndKeepsPermanentStats(GameTestHelper helper) {
        var player = player(helper); equip(player, 0, ModPassiveItems.SOUL_LOCKET.get());
        for (var item : List.of(ModItems.HALF_SOUL_HEART.get(), ModItems.SOUL_HEART.get(), ModItems.BLACK_HEART.get())) {
            helper.assertTrue(((Heart) item).getAbility() instanceof SoulHeart, "Soul-heart variants share the soul-heart use lifecycle");
            ((Heart) item).finishUsingItem(new ItemStack(item), helper.getLevel(), player);
        }
        close(helper, locketUnits(player), 5, "One half soul, one soul and one black heart grant five rolls");
        ModExecutableEffects.ABSORPTION.get().apply(new ExecutableEffectContext(player));
        close(helper, locketUnits(player), 5, "Direct absorption grants do not trigger rewards");
        equip(player, 0, Items.AIR);
        close(helper, locketUnits(player), 5, "Unequip preserves earned stats");
        ((Heart) ModItems.SOUL_HEART.get()).finishUsingItem(new ItemStack(ModItems.SOUL_HEART.get()), helper.getLevel(), player);
        close(helper, locketUnits(player), 5, "Future heart use without the item grants no rewards");
        helper.succeed();
    }
}
