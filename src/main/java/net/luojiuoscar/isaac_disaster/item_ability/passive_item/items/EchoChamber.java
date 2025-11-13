package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecord;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class EchoChamber implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.ECHO_CHAMBER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.echo_chamber.lore.1"),
                Component.translatable("item.isaac_disaster.echo_chamber.lore.2")
        );
    }

    public static void onTriggered(Player player){
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pes = playerItemUseRecord.getPillEffectRecords();
                    var cards = playerItemUseRecord.getCardRecords();

                    Iterator<PlayerItemUseRecord.PillEffectData> pillIter = pes.iterator();
                    Iterator<PlayerItemUseRecord.CardData> cardIter = cards.iterator();

                    PlayerItemUseRecord.PillEffectData pe = pillIter.hasNext() ? pillIter.next() : null;
                    PlayerItemUseRecord.CardData card = cardIter.hasNext() ? cardIter.next() : null;

                    for (int i = 0; i < 3; i++) {
                        if (pe == null && card == null) break; // 药丸卡牌均不存在

                        if (card == null || (pe != null && pe.sequence() >= card.sequence())) {

                            if (pe.isHorse()){
                                PillEffectManager.getInstance().getEffectFromEffectId(pe.id()).onUseH(player, false);
                            }else{
                                PillEffectManager.getInstance().getEffectFromEffectId(pe.id()).onUse(player, false);
                            }
                            pe = pillIter.hasNext() ? pillIter.next() : null;

                        } else {

                            PickupManager.getInstance().getItemFromId(card.id()).onUseEffect(player, null, null);
                            card = cardIter.hasNext() ? cardIter.next() : null;
                        }
                    }



                }
        );
    }

}
