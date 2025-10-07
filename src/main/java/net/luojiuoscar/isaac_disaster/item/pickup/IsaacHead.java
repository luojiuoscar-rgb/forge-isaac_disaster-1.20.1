package net.luojiuoscar.isaac_disaster.item.pickup;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IsaacHead extends Pickup{
    public IsaacHead(Properties pProperties, int itemId) {
        super(pProperties.stacksTo(1), itemId);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        return InteractionResultHolder.consume(stack);
    }
}
