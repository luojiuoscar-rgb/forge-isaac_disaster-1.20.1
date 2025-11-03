package net.luojiuoscar.isaac_disaster.item.pickup.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface IUsablePickup {
    @NotNull
    InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand);
}
