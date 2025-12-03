package net.luojiuoscar.isaac_disaster.registries.pill_effect;

import net.minecraft.server.level.ServerPlayer;


public interface IPillEffect {

    IPillEffect redirect(ServerPlayer player);

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
