package net.luojiuoscar.isaac_disaster.item_ability.active_item;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDisposableActiveItem extends IActiveItem{
    @Override
    default void onUse(Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof ActiveItem)) return;

        // 如果不是创造模式则消耗道具
        if (!player.isCreative()) stack.shrink(1);

        // 判断车载电池
        if (PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), (ServerPlayer) player)){
            onTriggeredEffectStronger(player);
        }else {
            onTriggeredEffect(player);
        }

        // 触发音效（由于主动道具为范围音效，因此需要在服务端触发）
        onTriggerSound(player);
    }
}
