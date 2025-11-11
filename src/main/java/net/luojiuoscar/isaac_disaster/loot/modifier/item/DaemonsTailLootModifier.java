package net.luojiuoscar.isaac_disaster.loot.modifier.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.pickup.Heart;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class DaemonsTailLootModifier extends LootModifier {

    public static final Codec<DaemonsTailLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, DaemonsTailLootModifier::new));

    public DaemonsTailLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> objectArrayList, LootContext lootContext) {
        if (objectArrayList.isEmpty()) return objectArrayList;

        ServerPlayer player;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        } else {
            player = null;
        }
        if (player == null || !PlayerHelper.hasTrinket(TrinketId.DAEMONS_TAIL.getId(), player)) return objectArrayList;

        // based on type
        double threshold = PlayerHelper.getValueFromTrinket(0.7, 0.35, TrinketId.DAEMONS_TAIL.getId(), player);

        RandomSource rand = lootContext.getRandom();
        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>();

        int blackHeartCount = 0;
        for (ItemStack stack : objectArrayList) {
            if (stack.getItem() instanceof Heart){
                int originalCount = stack.getCount();

                for (int j = 0; j < originalCount; j++) {
                    if (rand.nextDouble() < threshold) {
                        blackHeartCount++;
                    }
                }

                if (blackHeartCount > 0){
                    ItemStack blackHeart = new ItemStack(ModItems.BLACK_HEART.get());
                    blackHeart.setCount(blackHeartCount);
                    newList.add(blackHeart);
                    blackHeartCount = 0;
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
