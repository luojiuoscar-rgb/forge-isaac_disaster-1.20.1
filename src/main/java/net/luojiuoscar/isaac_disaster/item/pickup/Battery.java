package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Battery extends Pickup implements ICommonPickup, IUsablePickup {
    public Battery(Properties pProperties, int itemId) {
        super(pProperties, itemId);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return InteractionResultHolder.success(stack);
    }
}
