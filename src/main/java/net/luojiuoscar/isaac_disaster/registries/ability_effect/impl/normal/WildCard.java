package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecord;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.ModPickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class WildCard implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        InteractionHand hand = context.get(ContextKeys.HAND);
        if (hand == null) return false;
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        IForgeRegistry<IExecutableEffect> pillRegistry =
                RegistryManager.ACTIVE.getRegistry(ModExecutableEffects.EXECUTABLE_EFFECT);
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

                        PillEffect pillEffect = (PillEffect) pillRegistry.getValue(effect.id());
                        if (pillEffect != null) pillEffect.apply(context);

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
        return true;
    }
}
