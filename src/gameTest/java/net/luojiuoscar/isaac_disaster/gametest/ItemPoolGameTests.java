package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemPoolGameTests {
    @GameTest(template = "trajectory_empty")
    public static void defaultPoolUsesBothActualTagsAndRemovalRules(GameTestHelper helper) {
        boolean shared = Config.PLAYERS_SHARE_ITEM_POOLS.get();
        Config.PLAYERS_SHARE_ITEM_POOLS.set(false);
        try {
            ResourceLocation table = ResourceLocation.parse("isaac_disaster:pools/item/default");
            for (Item survivor : List.of(ModActiveItems.YUM_HEART.get(), ModPassiveItems.DESSERT.get())) {
                ServerPlayer player = player(helper);
                keepOnly(player, table, survivor);
                for (int i = 0; i < 8; i++) {
                    List<ItemStack> result = roll(helper, player, table);
                    helper.assertTrue(result.size() == 1 && result.get(0).is(survivor),
                            "Default pool must select the surviving item from its actual tag");
                }
            }
        } finally {
            Config.PLAYERS_SHARE_ITEM_POOLS.set(shared);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void tagPoolPreservesWeightsConditionsAndFunctions(GameTestHelper helper) {
        boolean shared = Config.PLAYERS_SHARE_ITEM_POOLS.get();
        Config.PLAYERS_SHARE_ITEM_POOLS.set(false);
        try {
            ServerPlayer player = player(helper);
            ResourceLocation table = ResourceLocation.parse("isaac_disaster:pools/item/tag_regression");
            keepOnly(player, table, ModPassiveItems.DESSERT.get(), ModActiveItems.YUM_HEART.get());
            for (int i = 0; i < 8; i++) {
                List<ItemStack> result = roll(helper, player, table);
                helper.assertTrue(result.size() == 1 && result.get(0).is(ModPassiveItems.DESSERT.get())
                        && result.get(0).hasTag() && result.get(0).getTag().getBoolean("pool_regression"),
                        "Entry functions survive rebuilding; zero-weight and false-condition items are excluded: " + result);
            }
        } finally {
            Config.PLAYERS_SHARE_ITEM_POOLS.set(shared);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void exhaustedDefaultPoolFallsBackAndAcceptsAdditions(GameTestHelper helper) {
        boolean shared = Config.PLAYERS_SHARE_ITEM_POOLS.get();
        Config.PLAYERS_SHARE_ITEM_POOLS.set(false);
        try {
            ServerPlayer player = player(helper);
            ResourceLocation table = ResourceLocation.parse("isaac_disaster:pools/item/default");
            keepOnly(player, table);
            helper.assertTrue(roll(helper, player, table).get(0).is(ModPassiveItems.BREAKFAST.get()),
                    "Exhausted pool retains Breakfast fallback");
            PoolHelper.addToPool(player, table, BuiltInRegistries.ITEM.getKey(ModActiveItems.YUM_HEART.get()));
            helper.assertTrue(roll(helper, player, table).get(0).is(ModActiveItems.YUM_HEART.get()),
                    "Explicit additions are available even after removal");
        } finally {
            Config.PLAYERS_SHARE_ITEM_POOLS.set(shared);
        }
        helper.succeed();
    }

    private static void keepOnly(ServerPlayer player, ResourceLocation table, Item... survivors) {
        for (var tag : List.of(TagManager.ACTIVE_ITEMS, TagManager.PASSIVE_ITEMS)) {
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> {
                if (!java.util.Arrays.asList(survivors).contains(holder.value())) PoolHelper.removeFromPool(player, table,
                        BuiltInRegistries.ITEM.getKey(holder.value()));
            });
        }
    }

    private static List<ItemStack> roll(GameTestHelper helper, ServerPlayer player, ResourceLocation table) {
        return LootHelper.generateLoot(helper.getLevel(), table, new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(net.minecraft.world.phys.Vec3.ZERO))
                .withParameter(LootContextParams.THIS_ENTITY, player), LootContextParamSets.CHEST);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "pool-test"));
    }
}
