package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.item.IIgnoreRecord;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Card extends Pickup implements IUsablePickup {
    public Card(Properties pProperties, int pickupId) {
        super(pProperties.rarity(Rarity.UNCOMMON), pickupId);
    }

    @Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        List<Component> description = PickupManager.getInstance().getItemFromId(getPickupId()).getDescription();
        tooltipComponents.addAll(description);
    }


    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand){
        super.use(level, player, hand);

        if (!(player.getItemInHand(hand).getItem() instanceof IIgnoreRecord)){
            player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                    playerItemUseRecord -> playerItemUseRecord.addCardRecord(getPickupId()));
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
