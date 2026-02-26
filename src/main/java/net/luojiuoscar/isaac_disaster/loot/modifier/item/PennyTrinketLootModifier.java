package net.luojiuoscar.isaac_disaster.loot.modifier.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PennyTrinketLootModifier extends LootModifier {

    public static final Codec<PennyTrinketLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, PennyTrinketLootModifier::new));

    public PennyTrinketLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (objectArrayList.isEmpty()) return objectArrayList;

        ServerPlayer player = null;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        }
        if (player == null) return objectArrayList;

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

        return newList;
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

        int count = PlayerHelper.getTrinketCount(TrinketId.BLOODY_PENNY.getId(), player);

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1 - (25 * (4 - count)) * 0.01;
        chance = Mth.clamp(chance, 0.0, 1.0);

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

        int count = PlayerHelper.getTrinketCount(TrinketId.BURNT_PENNY.getId(), player);

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1 - (25 * (4 - count)) * 0.01;
        chance = Mth.clamp(chance, 0.0, 1.0);

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

        int count = PlayerHelper.getTrinketCount(TrinketId.FLAT_PENNY.getId(), player);

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1 - (25 * (4 - count)) * 0.01;
        chance = Mth.clamp(chance, 0.0, 1.0);

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

        int count = PlayerHelper.getTrinketCount(TrinketId.BLESSED_PENNY.getId(), player);

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1.0 / 8.0;

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

        int count = PlayerHelper.getTrinketCount(TrinketId.CHARGED_PENNY.getId(), player);

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1.0 / 8.0;

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

        int count = PlayerHelper.getTrinketCount(TrinketId.COUNTERFEIT_PENNY.getId(), player);
        Item tier1Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_1_ID.get());
        if (tier1Coin == null) return ItemStack.EMPTY;

        if (count == 0 || value <= 0) {
            return ItemStack.EMPTY;
        }

        double chance = 1.0 / 3.0;

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
