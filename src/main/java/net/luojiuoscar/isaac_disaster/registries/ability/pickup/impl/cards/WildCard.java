package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecord;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.ModPickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class WildCard extends PickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (hand == null) return;

        IForgeRegistry<IPillEffect> pillRegistry =
                RegistryManager.ACTIVE.getRegistry(ModPillEffect.PILL_EFFECT_KEY);
        IForgeRegistry<PickupAbility> pickupRegistry =
                RegistryManager.ACTIVE.getRegistry(ModPickupAbility.PICKUP_ABILITY_KEY);
        IForgeRegistry<ActiveAbility> activeRegistry =
                RegistryManager.ACTIVE.getRegistry(ModActiveAbility.ACTIVE_ABILITY_KEY);

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

                    if (maxSeq == seqEffect && pillRegistry != null) {

                        IPillEffect pillEffect = pillRegistry.getValue(effect.id());
                        if (pillEffect != null) pillEffect.redirectAndUse(player, effect.isHorse());

                    }
                    else if (maxSeq == seqCard && pickupRegistry != null) {

                        PickupAbility ability = pickupRegistry.getValue(card.id());
                        if (ability != null) ability.onUse(player, null, null);

                    }
                    else if (maxSeq == seqActive && activeRegistry != null) {

                        ActiveAbility ability = activeRegistry.getValue(active.id());
                        if (ability != null) ability.onUse(player, null);

                    }
                }
        );

    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.WILD_CARD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDesc() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.wild_card.lore.1"));

        return description;
    }
}

