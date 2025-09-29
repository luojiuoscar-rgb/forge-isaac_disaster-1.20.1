package net.luojiuoscar.isaac_disaster.item.custom;

import net.luojiuoscar.isaac_disaster.isaac.passive_item.PassiveItem;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemC2SPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


public class NormalPassiveItem extends IsaacItems {

    public NormalPassiveItem(Properties properties, int itemLevel, int itemId ) {
        this(properties, itemLevel, itemId, false);
    }

    public NormalPassiveItem(Properties properties, int itemLevel, int itemId, boolean useOriginalColor) {
        super(properties, itemLevel, itemId, useOriginalColor);
    }

    /**
     * 右键使用方法
     */
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 获取玩家手中的物品栈
        ItemStack stack = player.getItemInHand(hand);

        // 发包请求服务端回应
        ModMessages.sendToServer(new ObtainPassiveItemC2SPacket(this.getItemId()));

        // 返回成功结果
        return InteractionResultHolder.pass(stack);
    }
}
