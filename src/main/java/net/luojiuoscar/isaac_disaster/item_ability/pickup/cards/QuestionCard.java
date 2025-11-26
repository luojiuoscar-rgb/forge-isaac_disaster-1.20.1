package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuestionCard implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.QUESTION_CARD.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (player.level().isClientSide) return;

        ItemStack target = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (hand == InteractionHand.MAIN_HAND){
            target = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        if (!(target.getItem() instanceof ActiveItem activeItem)) return;
        if (!(activeItem.getAbility() instanceof ActiveAbility activeAbility)) return;

        if (PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), player)){
            activeAbility.onTriggerStronger(player, null);
        }else{
            activeAbility.onTrigger(player, null);
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.QUESTION_CARD.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.question_card.lore.1"));

        return description;
    }
}
