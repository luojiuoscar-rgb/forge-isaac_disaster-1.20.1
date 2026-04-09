package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.pickup.Heart;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class DaemonsTail implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return false;

        ServerPlayer player;
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer thisPlayer) {
            player = thisPlayer;
        } else if (lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer killerPlayer) {
            player = killerPlayer;
        } else {
            player = null;
        }
        if (player == null || !PlayerHelper.hasTrinket(TrinketId.DAEMONS_TAIL.getId(), player)) return false;

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

        event.setObjectArrayList(newList);

        return true;
    }
}
