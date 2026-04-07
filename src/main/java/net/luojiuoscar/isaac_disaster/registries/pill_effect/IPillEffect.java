package net.luojiuoscar.isaac_disaster.registries.pill_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;


public interface IPillEffect {

    IPillEffect redirect(ServerPlayer player);

    default AbilityEffectContext getCtx(ServerPlayer player, boolean isHorse){
        AbilityEffectContext context = new AbilityEffectContext(player);
        context.set(ContextKeys.TARGET_POSITION, player.position());
        context.set(ContextKeys.BOOLEAN, List.of(isHorse));
        return context;
    }

    default void redirectAndUse(ServerPlayer player, boolean isHorse){
        IPillEffect effect = redirect(player);

        if (isHorse) {
            effect.onUseEffectH(player);
        } else {
            effect.onUseEffect(player);
        }
    }

    default void redirectAndMakeSound(ServerPlayer player, boolean isHorse){
        IPillEffect effect = redirect(player);

        if (isHorse){
            effect.makeSoundH(player);
        }else{
            effect.makeSound(player);
        }
    }

    void onUseEffect(ServerPlayer player);

    void onUseEffectH(ServerPlayer player);

    void makeSound(ServerPlayer player);

    void makeSoundH(ServerPlayer player);

    String getDescriptionId(int pillQuality);







}
