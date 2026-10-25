package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PennyTrinket implements IAbilityEffect {
    public static double getResourceChance() {
        return 0.25;
    }

    public static double getHeartOrBatteryChance() {
        return 1.0 / 8;
    }

    public static double getCounterfeitChance() {
        return 1.0 / 3;
    }

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.LOOT;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        if (!(event.getEntity() instanceof ServerPlayer player)) return false;

        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>();
        newList.addAll(objectArrayList);

        // 获取生成的钱币总价值
        int value = PlayerHelper.countMoney(objectArrayList);
        int max = PlayerHelper.countMoneyItemCount(objectArrayList);

        // 生成的特殊资源的数量不会超过硬币个数
        newList.addAll(List.of(
                bloodyPenny(player, value, max),
                burntPenny(player, value, max),
                flatPenny(player, value, max),
                blessedPenny(player, value, max),
                chargePenny(player, value, max),
                counterfeitPenny(player, value, max)
        ));

        event.setObjectArrayList(newList);
        return true;
    }


    private static int sampleBinomial(int n, double p, RandomSource random) {
        if (n <= 0 || p <= 0.0) return 0;
        if (p >= 1.0) return n;

        int x = 0;
        double log_q = Math.log(1.0 - p);
        double sum = 0.0;

        while (true) {
            sum += Math.log(random.nextDouble());
            if (sum < n * log_q) break;
            x++;
        }

        return Math.min(x, n);
    }

    private ItemStack bloodyPenny(ServerPlayer player, int value, int max) {

        if (!PlayerHelper.hasTrinket(ModTrinkets.BLOODY_PENNY.getId(), player) || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getResourceChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.HALF_RED_HEART.get());
        stack.setCount(result);

        return stack;
    }

    private ItemStack burntPenny(ServerPlayer player, int value, int max) {

        if (!PlayerHelper.hasTrinket(ModTrinkets.BURNT_PENNY.getId(), player) || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getResourceChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.BOMB.get());
        stack.setCount(result);

        return stack;
    }

    private ItemStack flatPenny(ServerPlayer player, int value, int max) {

        if (!PlayerHelper.hasTrinket(ModTrinkets.FLAT_PENNY.getId(), player) || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getResourceChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.KEY.get());
        stack.setCount(result);

        return stack;
    }

    private ItemStack blessedPenny(ServerPlayer player, int value, int max) {

        if (!PlayerHelper.hasTrinket(ModTrinkets.BLESSED_PENNY.getId(), player) || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getHeartOrBatteryChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.HALF_SOUL_HEART.get());
        stack.setCount(result);

        return stack;
    }

    private ItemStack chargePenny(ServerPlayer player, int value, int max) {

        if (!PlayerHelper.hasTrinket(ModTrinkets.CHARGED_PENNY.getId(), player) || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getHeartOrBatteryChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.SMALL_BATTERY.get());
        stack.setCount(result);

        return stack;
    }

    private ItemStack counterfeitPenny(ServerPlayer player, int value, int max) {

        int count = PlayerHelper.getTrinketCount(ModTrinkets.COUNTERFEIT_PENNY.getId(), player);
        Item tier1Coin = PlayerHelper.getItemFromConfig(GameplayConfig.COIN_TIER_1_ID.get());
        if (tier1Coin == null) return ItemStack.EMPTY;

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = getCounterfeitChance();

        int result = sampleBinomial(value, chance, player.getRandom());
        result = Mth.clamp(result, 0, Math.min(max, 64));

        if (result == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(tier1Coin);
        stack.setCount(result);

        return stack;
    }
}
