package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.event.custom.misc.PickupUseEvent;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

public class Pickup extends Item {
    private final int itemId;

    public Pickup(Properties pProperties, int itemId) {
        super(pProperties);
        this.itemId = itemId;
    }

    public int getPickupId() {
        return itemId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof Pickup item && item instanceof IUsablePickup)) return InteractionResultHolder.fail(stack);

        // 触发事件
        PickupUseEvent event = new PickupUseEvent(player, stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return InteractionResultHolder.pass(stack);

        if (!level.isClientSide)
            PickupManager.getInstance().getItemFromId(item.getPickupId()).onUse(player, stack, hand);

        return InteractionResultHolder.success(stack);
    }
}
