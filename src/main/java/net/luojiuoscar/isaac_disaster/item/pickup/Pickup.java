package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.event.custom.misc.PickupUseEvent;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class Pickup extends Item {
    protected final RegistryObject<PickupAbility> ability;

    public Pickup(Properties pProperties, RegistryObject<PickupAbility> ability) {
        super(pProperties);
        this.ability = ability;
    }

    public PickupAbility getAbility(){
        return ability.get();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof Pickup item && item instanceof IUsablePickup))
            return InteractionResultHolder.fail(stack);

        // 触发事件
        PickupUseEvent event = new PickupUseEvent(player, stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return InteractionResultHolder.pass(stack);

        if (player instanceof ServerPlayer serverPlayer){
            getAbility().onUse(serverPlayer, stack, hand);
            getAbility().makeSound(serverPlayer);
        }


        return InteractionResultHolder.success(stack);
    }
}
