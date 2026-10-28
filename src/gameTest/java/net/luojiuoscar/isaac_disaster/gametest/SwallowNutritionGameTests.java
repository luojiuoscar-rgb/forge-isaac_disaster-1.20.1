package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.UUID;

@GameTestHolder("isaac_disaster_swallow_nutrition")
@PrefixGameTestTemplate(false)
public final class SwallowNutritionGameTests {
    @GameTest(template = "trajectory_empty", batch = "swallow_nutrition")
    public static void oneSwallowedTrinketRestoresBreadNutrition(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equip(player, 0);
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 1, "One equipped trinket is swallowed");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 10, "One trinket restores five hunger points");
        helper.assertTrue(player.getFoodData().getSaturationLevel() == 6, "One trinket restores six saturation points");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "swallow_nutrition")
    public static void eachSuccessfullySwallowedTrinketFeedsOnce(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equip(player, 0);
        equip(player, 1);
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 2, "Both equipped trinkets are swallowed");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 15, "Two trinkets restore two portions of bread");
        helper.assertTrue(player.getFoodData().getSaturationLevel() == 12, "Both portions restore saturation");
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 0, "Empty slots cannot be swallowed again");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 15 && player.getFoodData().getSaturationLevel() == 12,
                "An empty repeated call never grants additional nutrition");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "swallow_nutrition")
    public static void nutritionRespectsVanillaFoodAndSaturationLimits(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equip(player, 0);
        player.getFoodData().setFoodLevel(19);
        player.getFoodData().setSaturation(19);
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(player) == 1, "Swallowing remains allowed near full hunger");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && player.getFoodData().getSaturationLevel() == 20,
                "Vanilla food data clamps hunger and saturation to their normal limits");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "swallow_nutrition")
    public static void restoringSwallowedStorageDoesNotGrantNutrition(GameTestHelper helper) {
        ServerPlayer original = player(helper);
        equip(original, 0);
        helper.assertTrue(PlayerHelper.swallowAllTrinkets(original) == 1, "The saved record contains a swallowed trinket");
        CompoundTag saved = new CompoundTag();
        original.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().saveNBTData(saved);
        ServerPlayer restored = player(helper);
        restored.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow().loadNBTData(saved);
        CurioSlotManager.requestReconcile(restored);
        CurioSlotManager.tick(restored);
        helper.assertTrue(restored.getFoodData().getFoodLevel() == 5 && restored.getFoodData().getSaturationLevel() == 0,
                "Loading swallowed storage never counts as eating another trinket");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "swallow_food"));
        DefaultAttributeManager.apply(player);
        player.getAttribute(ModAttributes.TRINKET_SLOTS.get()).setBaseValue(2);
        CurioSlotManager.tick(player);
        player.getFoodData().setFoodLevel(5);
        player.getFoodData().setSaturation(0);
        return player;
    }

    private static void equip(ServerPlayer player, int index) {
        CuriosApi.getCuriosInventory(player).resolve().orElseThrow()
                .getStacksHandler(CurioSlotManager.TRINKET).orElseThrow().getStacks()
                .setStackInSlot(index, new ItemStack(ModTrinkets.LUCKY_TOE.get()));
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }
}
