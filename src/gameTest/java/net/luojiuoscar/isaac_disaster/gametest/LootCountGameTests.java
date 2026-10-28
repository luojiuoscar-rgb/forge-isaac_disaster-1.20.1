package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.GenerateLoot;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.GiveItemViaLoot;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.LootAtPosition;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.TransformEntityToLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LootCountGameTests {
    @GameTest(template = "trajectory_empty", batch = "loot_count")
    public static void itemViaLootUsesExplicitCountInsteadOfDoubleOrAmplifier(GameTestHelper helper) {
        var context = context(helper);
        context.set(ContextKeys.ITEM, Items.DIAMOND);
        context.set(ContextKeys.DOUBLE, List.of(20.0, 30.0));
        context.set(ContextKeys.AMPLIFIER, 99.0);
        context.set(ContextKeys.COUNT, 3);
        int before = count(helper, context, Items.DIAMOND);
        helper.assertTrue(new GiveItemViaLoot().applyEffect(context), "Explicit item generation succeeds");
        helper.assertTrue(count(helper, context, Items.DIAMOND) - before == 3, "LOOT_COUNT alone controls amount");
        context.set(ContextKeys.COUNT, 0);
        before = count(helper, context, Items.DIAMOND);
        new GiveItemViaLoot().applyEffect(context);
        helper.assertTrue(count(helper, context, Items.DIAMOND) == before, "Zero count produces no item");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loot_count")
    public static void tableGenerationUsesCountAndDefaultsToOneWithoutAmplifierLeak(GameTestHelper helper) {
        var context = context(helper);
        context.set(ContextKeys.RESOURCE_LOCATIONS, List.of(ModLootTables.RANDOM_KEYS));
        context.set(ContextKeys.AMPLIFIER, 99.0);
        int before = keys(helper, context);
        new GenerateLoot().applyEffect(context);
        helper.assertTrue(keys(helper, context) - before == 1, "Missing count defaults to one, independently of amplifier");
        context.set(ContextKeys.COUNT, 3);
        before = keys(helper, context);
        new GenerateLoot().applyEffect(context);
        helper.assertTrue(keys(helper, context) - before == 3, "Table is drawn three times");
        context.set(ContextKeys.COUNT, 0);
        before = keys(helper, context);
        new GenerateLoot().applyEffect(context);
        helper.assertTrue(keys(helper, context) == before, "Zero does not silently become one");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loot_count")
    public static void sourceFreeAndEntityConversionLootHonorExplicitCount(GameTestHelper helper) {
        var context = context(helper);
        context.set(ContextKeys.RESOURCE_LOCATIONS, List.of(ModLootTables.RANDOM_KEYS));
        context.set(ContextKeys.COUNT, 2);
        int before = keys(helper, context);
        new LootAtPosition().applyEffect(context);
        helper.assertTrue(keys(helper, context) - before == 2, "Source-free loot honors count");
        Zombie zombie = new Zombie(helper.getLevel());
        Vec3 pos = context.get(ContextKeys.TARGET_POSITION);
        zombie.setPos(pos);
        var transformed = context.copy(zombie);
        before = keys(helper, context);
        helper.assertTrue(new TransformEntityToLoot().applyEffect(transformed), "Entity transformation succeeds");
        helper.assertTrue(zombie.isRemoved(), "Transformed entity is removed");
        helper.assertTrue(keys(helper, context) - before == 2, "Transformation honors the same count parameter");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "loot_count")
    public static void cardPreparationsConvertTheirExistingAmountsToLootCount(GameTestHelper helper) {
        var context = context(helper);
        context.set(ContextKeys.AMPLIFIER, 2.0);
        int before = count(helper, context, ModItems.SOUL_HEART.get());
        ModExecutableEffects.THE_HIEROPHANT.get().apply(context);
        helper.assertTrue(context.get(ContextKeys.COUNT) == 3, "Card preparation explicitly supplies three items");
        helper.assertTrue(count(helper, context, ModItems.SOUL_HEART.get()) - before == 3, "Card keeps original amplified amount");
        before = count(helper, context, ModItems.RED_HEART.get());
        ModExecutableEffects.QUEEN_OF_HEART.get().apply(context);
        int amount = context.get(ContextKeys.COUNT);
        helper.assertTrue(amount >= 1 && amount <= 20, "Queen of Hearts keeps its original random range");
        helper.assertTrue(count(helper, context, ModItems.RED_HEART.get()) - before == amount,
                "Resolved random count is passed to the shared consumer");
        helper.succeed();
    }

    private static ExecutableEffectContext context(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "loot-count-test"));
        Vec3 pos = helper.absolutePos(new BlockPos(1, 1, 1)).getCenter();
        player.setPos(pos);
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.TARGET_POSITION, pos);
        return context;
    }

    private static int keys(GameTestHelper helper, ExecutableEffectContext context) {
        return count(helper, context, ModItems.KEY.get()) + count(helper, context, ModItems.GOLDEN_KEY.get());
    }

    private static int count(GameTestHelper helper, ExecutableEffectContext context, Item item) {
        Vec3 pos = context.get(ContextKeys.TARGET_POSITION);
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos).inflate(1.0)).stream()
                .filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }
}
