package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.KeeperCoinDrop;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsKey;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemContextRevisionGameTests {
    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void drinkingMilkCuresDeadlyPoison(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "milk_revision"));
        player.addEffect(new MobEffectInstance(ModEffects.DEADLY_POISON.get(), 80, 4));
        helper.assertTrue(player.hasEffect(ModEffects.DEADLY_POISON.get()), "The player has deadly poison before drinking milk");
        Items.MILK_BUCKET.finishUsingItem(new ItemStack(Items.MILK_BUCKET), helper.getLevel(), player);
        helper.assertTrue(!player.hasEffect(ModEffects.DEADLY_POISON.get()), "Milk clears deadly poison through its curative item list");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void coneHeadRequiresHurtEventAndOnlyZeroesItsAmount(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "cone_event_revision"));
        var effect = new net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.ConeHead();
        var context = new ExecutableEffectContext(player);
        helper.assertTrue(!effect.applyEffect(context), "A damage modifier requires a Hurt event");
        context.set(ContextKeys.EVENT, new Event());
        helper.assertTrue(!effect.applyEffect(context), "An unrelated event is invalid");
        var hurt = new LivingHurtEvent(player, player.damageSources().generic(), 4);
        context.set(ContextKeys.EVENT, hurt);
        helper.assertTrue(effect.applyEffect(context) && hurt.getAmount() == 0 && !hurt.isCanceled(),
                "Cone Head zeroes the Hurt event amount without canceling it");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void lootEffectsRequireLootGenerationEvents(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "loot_revision"));
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.CHEST);
        var parameters = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absolutePos(pos).getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.EMPTY);
        var lootContext = new LootContext.Builder(parameters)
                .withQueriedLootTableId(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pedestal_test"))
                .create(null);
        var output = new ObjectArrayList<ItemStack>();
        output.add(new ItemStack(Items.APPLE));
        var context = new ExecutableEffectContext(player);
        var effect = new MomsKey();
        helper.assertTrue(!effect.applyEffect(context), "Loot mutation requires a loot generation event");
        context.set(ContextKeys.EVENT, new LivingHurtEvent(player, player.damageSources().generic(), 2));
        helper.assertTrue(!effect.applyEffect(context), "Hurt events cannot modify generated loot");
        context.set(ContextKeys.EVENT, new GeneralLootModifyEvent(player, lootContext, output));
        LootGenerationContext.run(LootGenerationMode.DERIVED_DROP, () ->
                helper.assertTrue(effect.applyEffect(context) && output.size() == 1, "Derived loot is safely skipped"));
        helper.assertTrue(effect.applyEffect(context) && output.size() > 1, "The loot event output receives duplicated loot");
        helper.assertTrue(output.stream().anyMatch(stack -> stack.is(Items.APPLE)), "The original loot is preserved");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void fartUsesTheSameDeadlyPoisonPotionPipeline(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fart_revision"));
        player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getCenter());
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
        var target = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 1, 1));
        target.setNoAi(true);
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.TARGET_POSITION, player.position());
        context.set(ContextKeys.DOUBLE, List.of(20.0, 0.0));
        ModExecutableEffects.FART.get().apply(context);
        var potion = target.getEffect(ModEffects.DEADLY_POISON.get());
        helper.assertTrue(potion != null && potion.getAmplifier() == 4,
                "Fart applies deadly poison using the source damage ratio through POTIONS");
        helper.assertTrue(!target.hasEffect(MobEffects.POISON), "Isaac fart does not use vanilla poison");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void commonColdStoresItsDamageInThePotionLevel(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "cold_revision"));
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10);
        var target = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(target));
        ModExecutableEffects.THE_COMMON_COLD.get().apply(context);
        var potion = target.getEffect(ModEffects.DEADLY_POISON.get());
        helper.assertTrue(potion != null && potion.getAmplifier() == 4 && potion.getDuration() == 70,
                "Thirty percent of ten damage is encoded as deadly poison amplifier four");
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        for (int tick = 0; tick < 70; tick++) target.tick();
        helper.assertTrue(Math.abs(target.getHealth() - 91) < 0.01, "Three poison ticks retain the applied potion level");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void deadlyPoisonSurvivesPotionSerializationAndKillsUndead(GameTestHelper helper) {
        var potion = new MobEffectInstance(ModEffects.DEADLY_POISON.get(), 40, 4);
        var loaded = MobEffectInstance.load(potion.save(new CompoundTag()));
        helper.assertTrue(loaded != null && loaded.getAmplifier() == 4, "Normal potion NBT carries all required strength");
        var target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        target.setNoAi(true);
        helper.assertTrue(!target.addEffect(new MobEffectInstance(MobEffects.POISON, 40)), "Vanilla poison retains its undead immunity");
        helper.assertTrue(target.addEffect(loaded), "Deadly poison can affect undead");
        target.setHealth(2);
        loaded.getEffect().applyEffectTick(target, loaded.getAmplifier());
        helper.assertTrue(target.getHealth() <= 0, "Deadly poison deals three damage and is lethal to undead");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void weakerPoisonApplicationsDoNotOverwriteStrongerLevels(GameTestHelper helper) {
        var target = helper.spawn(EntityType.COW, new BlockPos(1, 1, 1));
        target.setNoAi(true); target.setHealth(10);
        target.addEffect(new MobEffectInstance(ModEffects.DEADLY_POISON.get(), 20, 4));
        target.addEffect(new MobEffectInstance(ModEffects.DEADLY_POISON.get(), 100, 0));
        helper.assertTrue(target.getEffect(ModEffects.DEADLY_POISON.get()).getAmplifier() == 4,
                "Normal potion merging retains the stronger active level");
        ModEffects.DEADLY_POISON.get().applyEffectTick(target, target.getEffect(ModEffects.DEADLY_POISON.get()).getAmplifier());
        helper.assertTrue(target.getHealth() == 7, "The weaker application does not overwrite damage through another capability");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "item_context_revision")
    public static void keeperCoinsNeedOnlyAPositionContext(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "keeper_revision"));
        var context = new ExecutableEffectContext(player);
        var effect = new KeeperCoinDrop();
        helper.assertTrue(!effect.applyEffect(context), "A missing position is invalid");
        var position = helper.absolutePos(new BlockPos(1, 1, 1)).getCenter();
        var area = new AABB(position, position).inflate(0.25);
        String previousCoinId = Config.COIN_TIER_1_ID.get();
        try {
            Config.COIN_TIER_1_ID.set("minecraft:gold_nugget");
            int before = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                    .filter(item -> item.getItem().is(Items.GOLD_NUGGET)).mapToInt(item -> item.getItem().getCount()).sum();
            int penniesBefore = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                    .filter(item -> item.getItem().is(ModItems.PENNY.get())).mapToInt(item -> item.getItem().getCount()).sum();
            context.set(ContextKeys.TARGET_POSITION, position);
            helper.assertTrue(effect.applyEffect(context), "No event or bullet object is needed");
            int after = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                    .filter(item -> item.getItem().is(Items.GOLD_NUGGET)).mapToInt(item -> item.getItem().getCount()).sum();
            int penniesAfter = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                    .filter(item -> item.getItem().is(ModItems.PENNY.get())).mapToInt(item -> item.getItem().getCount()).sum();
            helper.assertTrue(after - before == 1, "One configured coin appears at the supplied position");
            helper.assertTrue(penniesAfter == penniesBefore, "The configured coin replaces the hardcoded penny");
        } finally {
            Config.COIN_TIER_1_ID.set(previousCoinId);
        }
        helper.succeed();
    }
}
