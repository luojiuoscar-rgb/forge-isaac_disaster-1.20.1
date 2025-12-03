package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecord;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class EchoChamber extends PassiveAbility {
    public EchoChamber(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.echo_chamber.lore.1"),
                Component.translatable("item.isaac_disaster.echo_chamber.lore.2")
        );
    }

    public static void onTriggered(Player player){
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        IForgeRegistry<IPillEffect> pillRegistry =
                RegistryManager.ACTIVE.getRegistry(ModPillEffect.PILL_EFFECT_KEY);

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

                            if (pillRegistry == null) continue;
                            IPillEffect effect = pillRegistry.getValue(pe.id());

                            if (effect == null) continue;
                            effect.redirectAndUse(serverPlayer, pe.isHorse());

                            pe = pillIter.hasNext() ? pillIter.next() : null;

                        } else {

                            PickupManager.getInstance().getItemFromId(card.id()).onUseEffect(serverPlayer, null, null);
                            card = cardIter.hasNext() ? cardIter.next() : null;
                        }
                    }



                }
        );
    }

}
