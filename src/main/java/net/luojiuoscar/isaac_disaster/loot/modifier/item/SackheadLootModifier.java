package net.luojiuoscar.isaac_disaster.loot.modifier.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class SackheadLootModifier extends LootModifier {

    public static final Codec<SackheadLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .apply(inst, SackheadLootModifier::new));

    public SackheadLootModifier(LootItemCondition[] conditionsIn) {
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
        if (player == null || PlayerHelper.getItemCount(ItemId.SACK_HEAD.getId(), player) == 0) return objectArrayList;


        ResourceLocation tableId = lootContext.getQueriedLootTableId();

        if (tableId == LootTableManager.BLACK_SACK || tableId == LootTableManager.GRAB_BAG){
            return objectArrayList;
        }

        RandomSource rand = lootContext.getRandom();
        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>();

        int grabBagCount = 0;
        for (ItemStack stack : objectArrayList) {
            if (stack.getItem() instanceof ICommonPickup || LevelHelper.isCoin(stack.getItem())){
                int originalCount = stack.getCount();
                int count = originalCount;

                for (int j = 0; j < originalCount; j++) {
                    if ((stack.getItem() instanceof ICommonPickup && rand.nextDouble() < 0.2) ||
                            LevelHelper.isCoin(stack.getItem()) && rand.nextDouble() < 0.1) {

                        grabBagCount++;
                        count--;
                    }
                }
                stack.setCount(count);
                if (count != 0){
                    newList.add(stack);
                }
                if (grabBagCount > 0){
                    ItemStack grabBag = new ItemStack(ModItems.GRAB_BAG.get());
                    grabBag.setCount(grabBagCount);
                    newList.add(grabBag);
                    grabBagCount = 0;
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
