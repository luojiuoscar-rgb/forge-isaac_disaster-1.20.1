package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class SackHead implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        if (!(event.getEntity() instanceof ServerPlayer player)
                || PlayerHelper.getItemCount(ItemId.SACK_HEAD.getId(), player) == 0) return true;


        ResourceLocation tableId = lootContext.getQueriedLootTableId();

        if (tableId.equals(ModLootTables.BLACK_SACK) || tableId.equals(ModLootTables.GRAB_BAG)){
            return true;
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

        event.setObjectArrayList(newList);

        return true;
    }
}
