package net.luojiuoscar.isaac_disaster.loot.modifier.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class MitreLootModifier extends LootModifier {

    public static final Codec<MitreLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, MitreLootModifier::new));

    public MitreLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
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
        if (player == null || !PlayerHelper.hasItem(ItemId.MITRE.getId(), player)) return objectArrayList;


        RandomSource rand = lootContext.getRandom();
        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>();

        int soulHeartCount = 0;
        for (ItemStack stack : objectArrayList) {
            if (stack.getItem() == ModItems.RED_HEART.get()){
                int originalCount = stack.getCount();
                int count = originalCount;

                for (int j = 0; j < originalCount; j++) {
                    if (rand.nextDouble() < 0.33) {
                        soulHeartCount++;
                        count--;
                    }
                }
                stack.setCount(count);
                if (count != 0){
                    newList.add(stack);
                }
                if (soulHeartCount > 0){
                    ItemStack soulHeart = new ItemStack(ModItems.SOUL_HEART.get());
                    soulHeart.setCount(soulHeartCount);
                    newList.add(soulHeart);
                    soulHeartCount = 0;
                }
            }else{
                newList.add(stack);
            }
        }


        return newList;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
