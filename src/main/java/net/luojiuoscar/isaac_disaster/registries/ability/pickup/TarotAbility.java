package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public abstract class TarotAbility extends PickupAbility{

    protected TarotAbility(CompositeTrigger triggers) {
        super(triggers);
    }

    @Override
    public void onUse(ServerPlayer player, ItemStack stack, InteractionHand hand){

        AbilityEffectContext ctx = getCtx(player, stack, hand);
        ctx.set(ContextKeys.AMPLIFIER, PlayerHelper.hasItem(ItemId.TAROT_CLOTH.getId(), player) // tarot cloth
                ? getStrongerAmplifier() : getNormalAmplifier());

        fire(ctx);

        if (!player.isCreative() && stack != null){
            shrinkAfterUse(stack);
        }
    }

    protected double getNormalAmplifier(){
        return 1;
    }

    protected double getStrongerAmplifier(){
        return 2;
    }
}
