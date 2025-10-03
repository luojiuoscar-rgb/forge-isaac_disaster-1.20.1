package net.luojiuoscar.isaac_disaster.item.pickup;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Bomb extends Pickup {
    public Bomb(Properties pProperties, int ItemId) {
        super(pProperties, ItemId);
    }

    /**
     * 右键使用方法
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 获取玩家手中的物品栈
        ItemStack stack = player.getItemInHand(hand);

        // 返回成功结果
        return InteractionResultHolder.success(stack);
    }
}
