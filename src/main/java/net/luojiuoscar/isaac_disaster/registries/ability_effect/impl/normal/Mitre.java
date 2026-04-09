package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class Mitre implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        ServerPlayer player = null;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        }
        if (player == null || !PlayerHelper.hasItem(ItemId.MITRE.getId(), player)) return true;

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


        event.setObjectArrayList(newList);

        return true;
    }
}
