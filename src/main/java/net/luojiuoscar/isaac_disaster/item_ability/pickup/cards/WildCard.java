package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecord;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WildCard implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.WILD_CARD.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        if (hand == null) return;

        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pillEffectRecords = playerItemUseRecord.getPillEffectRecords();
                    var cardRecords = playerItemUseRecord.getCardRecords();
                    var activeRecords = playerItemUseRecord.getActiveRecords();

                    PlayerItemUseRecord.PillEffectData effect = pillEffectRecords.isEmpty() ? null :
                            pillEffectRecords.get(pillEffectRecords.size() - 1);
                    PlayerItemUseRecord.CardData card = cardRecords.isEmpty() ? null :
                            cardRecords.get(cardRecords.size() - 1);
                    PlayerItemUseRecord.ActiveData active = activeRecords.isEmpty() ? null :
                            activeRecords.get(activeRecords.size() - 1);




                    // 筛选最大
                    long seqEffect = effect != null ? effect.sequence() : -1;
                    long seqCard = card != null ? card.sequence() : -1;
                    long seqActive = active != null ? active.sequence() : -1;

                    long maxSeq = Math.max(seqEffect, Math.max(seqCard, seqActive));

                    if (maxSeq == seqEffect) {
                        PillEffectManager.getInstance().getEffectFromEffectId(effect.id()).onUse(player, false);
                    }
                    else if (maxSeq == seqCard) {
                        PickupManager.getInstance().getItemFromId(card.id()).onUseEffect(player, null, null);
                    }
                    else if (maxSeq == seqActive) {
                        ActiveItemManager.getInstance().getItemFromId(active.id()).onUse(player, null);
                    }
                }
        );

    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.WILD_CARD.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.wild_card.lore.1"));

        return description;
    }
}
