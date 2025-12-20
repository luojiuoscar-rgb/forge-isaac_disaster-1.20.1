package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class PickupAbility {
    public void onUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand){
        onUseEffect(player, stack, hand);

        if (!player.isCreative() && stack != null){
            shrinkAfterUse(stack);
        }
    }

    public void shrinkAfterUse(@NotNull ItemStack stack){
        stack.shrink(1);
    }

    public abstract void onUseEffect(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);
    public abstract void makeSound(ServerPlayer player);
    public List<Component> getDesc(){
        return List.of();
    }
}
